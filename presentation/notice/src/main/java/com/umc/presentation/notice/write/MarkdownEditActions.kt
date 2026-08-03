package com.umc.presentation.notice.write

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.umc.component.theme.AppStrings

/**
 * 마크다운 텍스트 크기. prefix는 iOS 직렬화 스펙과 동일 (# 28 / ## 22 / ### 17 / 본문)
 */
enum class MarkdownHeading(val prefix: String, val label: String) {
    TITLE1("# ", AppStrings.NOTICE_WRITE_TEXT_TITLE1),
    TITLE2("## ", AppStrings.NOTICE_WRITE_TEXT_TITLE2),
    TITLE3("### ", AppStrings.NOTICE_WRITE_TEXT_TITLE3),
    BODY("", AppStrings.NOTICE_WRITE_TEXT_BODY),
}

/**
 * 마크다운 툴바 버튼이 수행하는 TextFieldValue 변환 모음.
 * iOS 파서는 마커 안쪽 공백(`** text **`)을 인식하지 못하므로
 * 선택 영역 양끝 공백은 마커 바깥에 남긴다.
 */
object MarkdownEditActions {

    private val headingPrefixes = listOf("### ", "## ", "# ")

    /** 커서(선택 시작)가 위치한 줄의 제목 prefix를 교체. BODY는 prefix 제거 */
    fun applyHeading(value: TextFieldValue, heading: MarkdownHeading): TextFieldValue {
        val text = value.text
        val lineStart = text.lastIndexOf('\n', value.selection.min - 1) + 1
        val lineEnd = text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
        val line = text.substring(lineStart, lineEnd)

        val existing = headingPrefixes.firstOrNull { line.startsWith(it) } ?: ""
        if (existing == heading.prefix) return value

        val newLine = heading.prefix + line.removePrefix(existing)
        val delta = heading.prefix.length - existing.length
        val newText = text.replaceRange(lineStart, lineEnd, newLine)
        return value.copy(
            text = newText,
            selection = TextRange(
                (value.selection.min + delta).coerceIn(lineStart, lineStart + newLine.length),
                (value.selection.max + delta).coerceIn(lineStart, newText.length),
            ),
        )
    }

    /** 굵게(`**`) 토글. 기울임과 조합되면 `***...***` 형태가 됨 */
    fun toggleBold(value: TextFieldValue): TextFieldValue = toggleAsterisk(value, count = 2)

    /** 기울임(`*`) 토글 */
    fun toggleItalic(value: TextFieldValue): TextFieldValue = toggleAsterisk(value, count = 1)

    /** 밑줄(`<u></u>`) 토글 */
    fun toggleUnderline(value: TextFieldValue): TextFieldValue =
        toggleWrap(value, open = "<u>", close = "</u>")

    /** 취소선(`~~`) 토글 */
    fun toggleStrikethrough(value: TextFieldValue): TextFieldValue =
        toggleWrap(value, open = "~~", close = "~~")

    /** 글머리 기호(`- `) 토글 */
    fun toggleBullet(value: TextFieldValue): TextFieldValue =
        toggleLinePrefix(value, prefix = "- ")

    /** 인용구(`> `) 토글 */
    fun toggleQuote(value: TextFieldValue): TextFieldValue =
        toggleLinePrefix(value, prefix = "> ")

    /**
     * 선택 영역에 걸친 모든 줄의 [prefix]를 토글한다.
     * 한 줄이라도 prefix가 없으면 전체에 추가하고, 모두 있으면 전체에서 제거한다.
     * 제목 prefix와는 공존할 수 없으므로 제목이 있으면 먼저 걷어낸다
     */
    private fun toggleLinePrefix(value: TextFieldValue, prefix: String): TextFieldValue {
        val text = value.text
        val blockStart = text.lastIndexOf('\n', value.selection.min - 1) + 1
        val blockEnd = text.indexOf('\n', value.selection.max)
            .let { if (it == -1) text.length else it }

        val lines = text.substring(blockStart, blockEnd).split('\n')
        val bodies = lines.map { line ->
            line.removePrefix(headingPrefixes.firstOrNull { line.startsWith(it) } ?: "")
        }
        val isActive = bodies.all { it.startsWith(prefix) }

        val newLines = bodies.map { body ->
            if (isActive) body.removePrefix(prefix) else prefix + body
        }
        val newText = text.replaceRange(blockStart, blockEnd, newLines.joinToString("\n"))

        return value.copy(
            text = newText,
            selection = TextRange(
                mapOffset(lines, newLines, blockStart, value.selection.min),
                mapOffset(lines, newLines, blockStart, value.selection.max),
            ),
        )
    }

    /**
     * prefix 변화를 반영해 [offset]을 새 텍스트 기준으로 옮긴다.
     * prefix 안쪽에 있던 커서가 앞줄로 넘어가지 않도록 각 줄 범위로 가둔다
     */
    private fun mapOffset(
        lines: List<String>,
        newLines: List<String>,
        blockStart: Int,
        offset: Int,
    ): Int {
        var lineStart = blockStart
        var newLineStart = blockStart
        lines.forEachIndexed { index, line ->
            val lineEnd = lineStart + line.length
            if (offset <= lineEnd) {
                val delta = newLines[index].length - line.length
                return (newLineStart + (offset - lineStart) + delta)
                    .coerceIn(newLineStart, newLineStart + newLines[index].length)
            }
            lineStart = lineEnd + 1
            newLineStart += newLines[index].length + 1
        }
        return newLineStart
    }

    /**
     * 별표 마커 토글. 굵게/기울임은 같은 문자를 공유하므로 양끝 별표 개수로 상태를 판단:
     * 1개=기울임, 2개=굵게, 3개=굵게+기울임
     */
    private fun toggleAsterisk(value: TextFieldValue, count: Int): TextFieldValue {
        val marker = "*".repeat(count)
        val (start, end) = value.trimmedSelection() ?: return insertEmptyMarker(value, marker, marker)

        val selected = value.text.substring(start, end)
        val edgeMarks = minOf(
            selected.takeWhile { it == '*' }.length,
            selected.takeLastWhile { it == '*' }.length,
            3,
        ).coerceAtMost((selected.length - 1) / 2)
        val isActive = if (count == 2) edgeMarks >= 2 else edgeMarks == 1 || edgeMarks == 3

        return if (isActive) {
            val inner = selected.substring(count, selected.length - count)
            val newText = value.text.replaceRange(start, end, inner)
            value.copy(text = newText, selection = TextRange(start, start + inner.length))
        } else {
            wrapRange(value, start, end, marker, marker)
        }
    }

    /** 열림/닫힘 마커가 다른 스타일(밑줄, 취소선) 토글 */
    private fun toggleWrap(value: TextFieldValue, open: String, close: String): TextFieldValue {
        val (start, end) = value.trimmedSelection() ?: return insertEmptyMarker(value, open, close)
        val text = value.text
        val selected = text.substring(start, end)

        return when {
            // 선택 영역이 마커째 선택된 경우 해제
            selected.length > open.length + close.length &&
                    selected.startsWith(open) && selected.endsWith(close) -> {
                val inner = selected.substring(open.length, selected.length - close.length)
                val newText = text.replaceRange(start, end, inner)
                value.copy(text = newText, selection = TextRange(start, start + inner.length))
            }

            // 선택 영역 바로 바깥에 마커가 있는 경우 해제
            start >= open.length && end + close.length <= text.length &&
                    text.startsWith(open, start - open.length) && text.startsWith(close, end) -> {
                val newText = text.replaceRange(end, end + close.length, "")
                    .replaceRange(start - open.length, start, "")
                value.copy(
                    text = newText,
                    selection = TextRange(start - open.length, end - open.length),
                )
            }

            else -> wrapRange(value, start, end, open, close)
        }
    }

    /** 선택 영역에서 양끝 공백을 제외한 범위. 유효한 선택이 없으면 null */
    private fun TextFieldValue.trimmedSelection(): Pair<Int, Int>? {
        if (selection.collapsed) return null
        var start = selection.min
        var end = selection.max
        while (start < end && text[start].isWhitespace()) start++
        while (end > start && text[end - 1].isWhitespace()) end--
        return if (start == end) null else start to end
    }

    /** 선택이 없을 때 커서 위치에 빈 마커 쌍을 삽입하고 커서를 그 사이로 이동 */
    private fun insertEmptyMarker(value: TextFieldValue, open: String, close: String): TextFieldValue {
        val at = value.selection.min
        val newText = value.text.replaceRange(at, at, open + close)
        return value.copy(text = newText, selection = TextRange(at + open.length))
    }

    private fun wrapRange(
        value: TextFieldValue,
        start: Int,
        end: Int,
        open: String,
        close: String,
    ): TextFieldValue {
        val newText = value.text.replaceRange(end, end, close).replaceRange(start, start, open)
        return value.copy(
            text = newText,
            selection = TextRange(start, end + open.length + close.length),
        )
    }
}
