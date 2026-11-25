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
    private val CHECKBOX = Regex("^(\\s*)-\\s+\\[[ xX]]\\s+", RegexOption.MULTILINE)
    private val MARKER_INNER_SPACE = Regex("(\\*{1,3}|~~)[ \\t]+([^*~\\s][^\\n]*?)[ \\t]+(\\1)")
    private val BLOCK_MARKER_NO_SPACE = Regex("^(#{1,3}|>)([^\\s#>])", RegexOption.MULTILINE)
    private val PAREN_NUMBER_LIST = Regex("^(\\d+)\\)[ \\t]+", RegexOption.MULTILINE)
    private val CODE_OR_HIGHLIGHT = Regex("`(?:\\\\.|[^\\\\`\\n])+`|<mark>")
    private const val DEFAULT_HIGHLIGHT = "<mark color=\"0.686,0.322,0.871,0.350\">"

    /** 지원하지 않는 문법이 몇 군데 남아 있는지 센다. 보정 전후 비교용. */
    fun countViolations(text: String): Int =
        CODE_FENCE.findAll(text).count() +
            LEADING_PREAMBLE.findAll(text).count() +
            HORIZONTAL_RULE.findAll(text).count() +
            TABLE_SEPARATOR.findAll(text).count() +
            TABLE_ROW.findAll(text).count() +
            CHECKBOX.findAll(text).count() +
            PAREN_NUMBER_LIST.findAll(text).count() +
            MARKER_INNER_SPACE.findAll(text).count() +
            BLOCK_MARKER_NO_SPACE.findAll(text).count() +
            countColorlessHighlights(text)

    /** 모델 출력을 앱이 지원하는 문법만 남도록 보정한다. */
    fun sanitize(raw: String): String {
        var text = raw.replace("\r\n", "\n").replace("\r", "\n")
        text = CODE_FENCE.replace(text, "")
        text = LEADING_PREAMBLE.replace(text, "")
        // 표는 렌더링 불가 — 구분선은 버리고 셀은 불릿 한 줄로 편다.
        text = TABLE_SEPARATOR.replace(text, "")
        text = TABLE_ROW.replace(text) { m ->
            val cells = m.groupValues[1].split('|').map { it.trim() }.filter { it.isNotEmpty() }
            if (cells.isEmpty()) "" else "- " + cells.joinToString(" · ")
        }
        text = HORIZONTAL_RULE.replace(text, "")
        text = CHECKBOX.replace(text) { m -> m.groupValues[1] + "- " }
        text = PAREN_NUMBER_LIST.replace(text) { m -> m.groupValues[1] + ". " }
        // "** 텍스트 **" 처럼 마커 안쪽에 공백이 있으면 파서가 토큰으로 인정하지 않는다.
        text = MARKER_INNER_SPACE.replace(text) { m ->
            m.groupValues[1] + m.groupValues[2].trim() + m.groupValues[3]
        }
        // "#제목" 처럼 블록 마커 뒤 공백이 빠지면 제목으로 인식되지 않는다.
        text = BLOCK_MARKER_NO_SPACE.replace(text) { m -> m.groupValues[1] + " " + m.groupValues[2] }
        return normalizeHighlights(text)
    }

    /** 코드 스팬과 이스케이프를 제외하고 색상이 없는 `<mark>` 개수를 센다. */
    private fun countColorlessHighlights(text: String): Int {
        var count = 0
        forEachColorlessHighlight(text) { count++ }
        return count
    }

    /** 새 AI 출력의 색상 없는 형광펜은 공식 기본 보라로 보정한다. 코드와 기존 RGBA는 유지한다. */
    private fun normalizeHighlights(text: String): String = buildString(text.length) {
        var cursor = 0
        forEachColorlessHighlight(text) { range ->
            append(text, cursor, range.first)
            append(DEFAULT_HIGHLIGHT)
            cursor = range.last + 1
        }
        append(text, cursor, text.length)
    }

    private inline fun forEachColorlessHighlight(text: String, onMatch: (IntRange) -> Unit) {
        var cursor = 0
        while (cursor < text.length) {
            val match = CODE_OR_HIGHLIGHT.find(text, cursor) ?: break
            var slashStart = match.range.first - 1
            while (slashStart >= 0 && text[slashStart] == '\\') slashStart--
            if ((match.range.first - 1 - slashStart) % 2 == 1) {
                cursor = match.range.first + 1
                continue
            }
            if (match.value == "<mark>") onMatch(match.range)
            cursor = match.range.last + 1
        }
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
