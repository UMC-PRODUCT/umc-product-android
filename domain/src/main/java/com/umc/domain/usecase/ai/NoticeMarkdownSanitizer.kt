package com.umc.domain.usecase.ai

/**
 * 온디바이스 모델의 출력을 앱이 렌더링할 수 있는 마크다운으로 되돌리는 결정론적 후처리기.
 *
 * 모델에게 형식을 지키라고 프롬프트로 부탁하는 것만으로는 출력이 보장되지 않는다. 특히 작은
 * 온디바이스 모델은 코드펜스로 감싸기, "다음은 요약입니다" 같은 머리말 붙이기, 표·수평선처럼
 * 이 앱이 지원하지 않는 문법 사용을 자주 한다. 앱의 마크다운 파서는 지원하지 않는 마커를
 * 숨기지 못하므로, 그대로 두면 사용자 화면에 `|---|` 같은 기호가 노출된다.
 *
 * 따라서 모델에는 "문장을 다듬는" 일만 맡기고, 형식 보정은 규칙으로 확정한다.
 * 추론 결과를 신뢰 구간 밖으로 내보내지 않는 것이 목적이다.
 */
object NoticeMarkdownSanitizer {

    private val CODE_FENCE = Regex("^\\s*```[a-zA-Z]*\\s*\\n|\\n\\s*```\\s*$")
    private val LEADING_PREAMBLE = Regex(
        "^\\s*(?:다음은|아래는)[^\\n]{0,40}(?:입니다|이에요|예요)[.:]?\\s*\\n+",
    )
    private val HORIZONTAL_RULE = Regex("^[ \\t]*(?:-{3,}|\\*{3,}|_{3,})[ \\t]*\\r?\\n?", RegexOption.MULTILINE)
    private val TABLE_SEPARATOR = Regex("^[ \\t]*\\|[ \\t:|-]*-{2,}[ \\t:|-]*\\|[ \\t]*\\r?\\n?", RegexOption.MULTILINE)
    private val TABLE_ROW = Regex("^[ \\t]*\\|(.+)\\|[ \\t]*$", RegexOption.MULTILINE)
    private val CHECKBOX = Regex("""^([ \t]*)-[ \t]+\[[ xX]](?:[ \t]+|(?=\n|$))""", RegexOption.MULTILINE)
    private val MARKER_INNER_SPACE = Regex("(\\*{1,3}|~~)[ \\t]+([^*~\\s][^\\n]*?)[ \\t]+(\\1)")
    private val BLOCK_MARKER_NO_SPACE = Regex("^(#{1,3}|>)([^\\s#>])", RegexOption.MULTILINE)
    private val PAREN_NUMBER_LIST = Regex("^(\\d+)\\)[ \\t]+", RegexOption.MULTILINE)
    private val INLINE_CODE = Regex("""`(?:\\.|[^\\`\n])+`""")
    private val COLORLESS_HIGHLIGHT = Regex("<mark>")
    private const val DEFAULT_HIGHLIGHT = "<mark color=\"0.686,0.322,0.871,0.350\">"

    /** 코드·이스케이프를 제외한 보정 대상 문법 수. 보정 전후 비교에 사용한다. */
    fun countViolations(text: String): Int {
        val normalized = normalizeNewlines(text)
        return CODE_FENCE.findAll(normalized).count() + LEADING_PREAMBLE.findAll(normalized).count() +
            listOf(HORIZONTAL_RULE, TABLE_SEPARATOR, TABLE_ROW, CHECKBOX, PAREN_NUMBER_LIST,
                MARKER_INNER_SPACE, BLOCK_MARKER_NO_SPACE, COLORLESS_HIGHLIGHT).sumOf { pattern ->
                var count = 0
                forEachPlainMatch(normalized, pattern) { match ->
                    if (pattern != TABLE_ROW || !TABLE_SEPARATOR.matches(match.value)) count++
                }
                count
            }
    }

    /** 모델 출력에서 코드 원문과 빈 줄을 보존하며 지원 문법을 보정한다. */
    fun sanitize(raw: String): String {
        var text = normalizeNewlines(raw)
        text = CODE_FENCE.replace(text, "")
        text = LEADING_PREAMBLE.replace(text, "")
        text = replaceOutsideCode(text, TABLE_SEPARATOR) { "" }
        text = replaceOutsideCode(text, TABLE_ROW) { m ->
            val cells = tableCells(m.groupValues[1])
            if (cells.isEmpty()) "" else "- " + cells.joinToString(" · ")
        }
        text = replaceOutsideCode(text, HORIZONTAL_RULE) { "" }
        text = replaceOutsideCode(text, CHECKBOX) { it.groupValues[1] + "- " }
        text = replaceOutsideCode(text, PAREN_NUMBER_LIST) { it.groupValues[1] + ". " }
        text = replaceOutsideCode(text, MARKER_INNER_SPACE) {
            it.groupValues[1] + it.groupValues[2].trim() + it.groupValues[3]
        }
        text = replaceOutsideCode(text, BLOCK_MARKER_NO_SPACE) { it.groupValues[1] + " " + it.groupValues[2] }
        return replaceOutsideCode(text, COLORLESS_HIGHLIGHT) { DEFAULT_HIGHLIGHT }
    }

    private fun replaceOutsideCode(text: String, pattern: Regex, replacement: (MatchResult) -> String): String =
        buildString(text.length) {
            var cursor = 0
            forEachPlainMatch(text, pattern) { match ->
                append(text, cursor, match.range.first)
                append(replacement(match))
                cursor = match.range.last + 1
            }
            append(text, cursor, text.length)
        }

    private fun normalizeNewlines(text: String): String = text.replace("\r\n", "\n").replace("\r", "\n")

    private fun tableCells(body: String): List<String> {
        val codes = codeRanges(body)
        val cells = mutableListOf<String>()
        var start = 0
        var codeIndex = 0
        for (index in body.indices) {
            while (codeIndex < codes.size && codes[codeIndex].last < index) codeIndex++
            val insideCode = codeIndex < codes.size && index in codes[codeIndex]
            if (body[index] == '|' && !insideCode && !isEscaped(body, index)) {
                cells += body.substring(start, index).trim()
                start = index + 1
            }
        }
        cells += body.substring(start).trim()
        return cells.filter { it.isNotEmpty() }
    }

    private fun codeRanges(text: String): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        var cursor = 0
        while (cursor < text.length) {
            val code = INLINE_CODE.find(text, cursor) ?: break
            if (isEscaped(text, code.range.first)) cursor = code.range.first + 1 else {
                ranges += code.range
                cursor = code.range.last + 1
            }
        }
        return ranges
    }

    private fun forEachPlainMatch(text: String, pattern: Regex, onMatch: (MatchResult) -> Unit) {
        val codes = codeRanges(text)
        var codeIndex = 0
        pattern.findAll(text).forEach { match ->
            while (codeIndex < codes.size && codes[codeIndex].last < match.range.first) codeIndex++
            var index = codeIndex
            var changesCode = false
            while (index < codes.size && codes[index].first <= match.range.last) {
                val code = codes[index++]
                // 표의 외곽 파이프와 서식 바깥 공백만 바꾸며 완전히 포함된 코드 본문은 유지한다.
                val enclosesCode = (pattern == TABLE_ROW || pattern == MARKER_INNER_SPACE) &&
                    code.first > match.range.first && code.last < match.range.last
                if (!enclosesCode) changesCode = true
            }
            if (!changesCode && !isEscaped(text, match.range.first)) onMatch(match)
        }
    }

    private fun isEscaped(text: String, at: Int): Boolean {
        var start = at - 1
        while (start >= 0 && text[start] == '\\') start--
        return (at - 1 - start) % 2 == 1
    }
}

/**
 * 성공 응답의 본문만 결정론적으로 보정한다. 실패 응답은 그대로 흘려보낸다.
 */
fun com.umc.domain.model.base.ApiState<String>.sanitizeMarkdown(): com.umc.domain.model.base.ApiState<String> =
    when (this) {
        is com.umc.domain.model.base.ApiState.Success ->
            com.umc.domain.model.base.ApiState.Success(NoticeMarkdownSanitizer.sanitize(data))
        else -> this
    }
