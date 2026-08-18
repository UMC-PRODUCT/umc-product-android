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

    /**
     * 개행이 입력되면 그 줄에서 열려 있던 인라인 마커를 개행 앞에서 모두 닫는다.
     *
     * 파서가 줄 단위로만 인라인 토큰을 찾으므로 토큰이 개행을 넘기면 마커가 화면에
     * 그대로 드러난다. 또한 사용자 기대상 `<mark>가나다` 뒤에서 엔터를 치면
     * 형광펜은 "가나다"까지만 적용되어야 한다
     */
    fun closeMarkersOnNewline(previous: TextFieldValue, current: TextFieldValue): TextFieldValue {
        val cursor = current.selection.min
        val isSingleNewlineInput = current.text.length == previous.text.length + 1 &&
                current.selection.collapsed &&
                cursor in 1..current.text.length &&
                current.text[cursor - 1] == '\n'
        if (!isSingleNewlineInput) return current

        val closers = MarkdownScanner.closersAt(current.text, cursor - 1)
        if (closers.isEmpty()) return current

        val newlineAt = cursor - 1
        // 마커 안에서 엔터를 치면 짝이 되던 닫는 마커가 다음 줄로 밀려 고아가 된다.
        // (`<mark>가나|</mark>` -> `<mark>가나</mark>\n</mark>`)
        // 줄 끝에서 스타일을 닫고, 뒤에 남는 같은 마커는 걷어내 다음 줄은 일반 텍스트로 시작한다
        val orphanEnd = if (current.text.startsWith(closers, cursor)) cursor + closers.length else cursor

        return current.copy(
            text = current.text.substring(0, newlineAt) + closers + "\n" +
                    current.text.substring(orphanEnd),
            selection = TextRange(newlineAt + closers.length + 1),
        )
    }

    /** 색상 인자가 없는 `<mark>`도 렌더러가 받아주므로 동일하게 인식한다 */
    private val MARK_OPEN = Regex("""<mark(?:\s+color="[^"]*")?>""")
    private const val MARK_CLOSE = "</mark>"

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
    fun toggleBold(value: TextFieldValue): TextFieldValue =
        toggleAsterisk(value, count = 2, style = MarkdownStyle.BOLD)

    /**
     * 기울임 토글.
     *
     * 이미 있는 `*...*`도 그대로 해제하지만, 새로 넣을 때는 `_`를 쓴다.
     * 굵게 안에 별표 기울임을 넣으면 `**` + `*`가 `***`(굵게+기울임)와 구분되지 않아
     * 기울임을 끌 때 굵게까지 함께 사라진다. `_`는 별표와 섞이지 않는다
     */
    fun toggleItalic(value: TextFieldValue): TextFieldValue =
        toggleAsterisk(value, count = 1, style = MarkdownStyle.ITALIC, insertMarker = "_")

    /** 밑줄(`<u></u>`) 토글 */
    fun toggleUnderline(value: TextFieldValue): TextFieldValue =
        toggleWrap(value, open = "<u>", close = "</u>", style = MarkdownStyle.UNDERLINE)

    /** 취소선(`~~`) 토글 */
    fun toggleStrikethrough(value: TextFieldValue): TextFieldValue =
        toggleWrap(value, open = "~~", close = "~~", style = MarkdownStyle.STRIKETHROUGH)

    /**
     * 형광펜(`<mark color="...">`) 토글.
     * 같은 색이 이미 걸려 있으면 해제하고, 다른 색이면 색만 교체한다
     */
    fun toggleHighlight(value: TextFieldValue, color: MarkdownHighlightColor): TextFieldValue {
        val open = """<mark color="${color.markColorCode}">"""
        // 형광펜 마커는 `<mark color="...">`로 길어서, 빈 쌍을 넣어두면 줄을 넘기며
        // 화면에 그대로 드러난다. 그래서 선택 영역이 있을 때만 적용한다
        val (start, end) = value.trimmedSelection() ?: return value
        val text = value.text
        val selected = text.substring(start, end)

        // 선택 영역이 마커째 선택된 경우
        val innerOpen = MARK_OPEN.matchAt(selected, 0)
        if (innerOpen != null && selected.endsWith(MARK_CLOSE) &&
            selected.length > innerOpen.value.length + MARK_CLOSE.length
        ) {
            val body = selected.substring(innerOpen.value.length, selected.length - MARK_CLOSE.length)
            val replacement = if (innerOpen.value == open) body else open + body + MARK_CLOSE
            return value.copy(
                text = text.replaceRange(start, end, replacement),
                selection = TextRange(start, start + replacement.length),
            )
        }

        // 선택 영역 바로 바깥에 마커가 있는 경우
        val outerOpen = MARK_OPEN.findAll(text.substring(0, start))
            .lastOrNull()
            ?.takeIf { it.range.last == start - 1 && text.startsWith(MARK_CLOSE, end) }
        if (outerOpen != null) {
            return if (outerOpen.value == open) {
                val newText = text.replaceRange(end, end + MARK_CLOSE.length, "")
                    .replaceRange(outerOpen.range.first, start, "")
                value.copy(
                    text = newText,
                    selection = TextRange(outerOpen.range.first, end - outerOpen.value.length),
                )
            } else {
                val delta = open.length - outerOpen.value.length
                value.copy(
                    text = text.replaceRange(outerOpen.range.first, start, open),
                    selection = TextRange(start + delta, end + delta),
                )
            }
        }

        return wrapRange(value, start, end, open, MARK_CLOSE)
    }

    /** 커서 위치(선택 영역이 있으면 그 자리)에 [text]를 넣고 커서를 끝으로 옮긴다 */
    fun insertText(value: TextFieldValue, text: String): TextFieldValue {
        val start = value.selection.min
        val newText = value.text.replaceRange(start, value.selection.max, text)
        return value.copy(text = newText, selection = TextRange(start + text.length))
    }

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
    private fun toggleAsterisk(
        value: TextFieldValue,
        count: Int,
        style: MarkdownStyle,
        /** 선택 없이 새로 넣을 때 쓸 마커. 기본은 별표와 동일 */
        insertMarker: String = "*".repeat(count),
    ): TextFieldValue {
        val marker = "*".repeat(count)
        val (start, end) = value.trimmedSelection()
            ?: return deactivateOrInsert(value, insertMarker, insertMarker, style)

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
    private fun toggleWrap(
        value: TextFieldValue,
        open: String,
        close: String,
        style: MarkdownStyle,
    ): TextFieldValue {
        val (start, end) = value.trimmedSelection()
            ?: return deactivateOrInsert(value, open, close, style)
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

    /**
     * 선택 없이 툴바를 눌렀을 때의 동작. 누를 때마다 그 스타일 하나만 켜지고 꺼져야 한다.
     *
     * 켜져 있는 상태에서 다시 누르면:
     * - 아직 아무것도 입력하지 않은 빈 마커 쌍이면 마커째 지운다.
     *   커서만 밖으로 옮기면 `****` 같은 별표 뭉치가 남고, 거기서 한 번 더 누르면
     *   `***`(굵게+기울임)로 읽혀 누르지도 않은 기울임까지 켜진다
     * - 이미 입력한 내용이 있으면 닫는 마커 뒤로 커서를 옮겨 스타일을 끝낸다
     */
    private fun deactivateOrInsert(
        value: TextFieldValue,
        open: String,
        close: String,
        style: MarkdownStyle,
    ): TextFieldValue {
        val marker = MarkdownScanner.activeMarkerAt(value, style)
            ?: return insertEmptyMarker(value, open, close)

        val text = value.text
        val at = value.selection.min
        if (!text.startsWith(marker.close, at)) return insertEmptyMarker(value, open, close)

        val isEmptyPair = at >= marker.open.length && text.startsWith(marker.open, at - marker.open.length)

        return if (isEmptyPair) {
            val newText = text
                .removeRange(at, at + marker.close.length)
                .removeRange(at - marker.open.length, at)
            value.copy(text = newText, selection = TextRange(at - marker.open.length))
        } else {
            value.copy(selection = TextRange(at + marker.close.length))
        }
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
