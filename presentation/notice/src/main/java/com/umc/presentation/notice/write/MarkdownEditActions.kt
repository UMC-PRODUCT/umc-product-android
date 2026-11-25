package com.umc.presentation.notice.write

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.Color
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
 * 툴바가 가공한 값에서 IME 조합(composing) 영역을 비운다.
 *
 * `TextFieldValue.copy`는 composition을 그대로 유지한다. 조합 중이던 구간이 남아 있으면
 * 키보드가 그 구간을 이어 써서, 서식을 끝낸 뒤 입력한 글자가 마커 안쪽에 들어가거나
 * 새로 삽입한 마커와 조합 구간이 겹쳐 마커 쌍이 깨질 수 있다
 */
internal fun TextFieldValue.endComposition(): TextFieldValue =
    if (composition == null) this else copy(composition = null)

/**
 * 마크다운 툴바 버튼이 수행하는 TextFieldValue 변환 모음.
 * iOS 파서는 마커 안쪽 공백(`** text **`)을 인식하지 못하므로
 * 선택 영역 양끝 공백은 마커 바깥에 남긴다.
 */
object MarkdownEditActions {

    private val editorParser = MarkdownVisualTransformation(Color.Unspecified, Color.Unspecified)

    private val headingPrefixes = listOf("### ", "## ", "# ")
    private val numberListPrefix = Regex("""^(\d+)\. """)
    private val closingTriggerChars = setOf('`', ')', '*', '~', '_')
    private val completedInlineAtEnd = listOf(
        Regex("""(?<!\\)`(?:\\.|[^`\n])+`$"""),
        Regex("""(?<!\\)\[(?:\\.|[^\]\n])+]\((?:\\.|[^)\n])+\)$"""),
        Regex("""(?<!\\)\*\*\*(?=\S).+?(?<=\S)\*\*\*$"""),
        Regex("""(?<!\\)\*\*_(?=\S).+?(?<=\S)_\*\*$"""),
        Regex("""(?<!\\)(?<!\*)\*\*(?=\S).+?(?<=\S)\*\*$"""),
        Regex("""(?<!\\)~~(?=\S).+?(?<=\S)~~$"""),
        Regex("""(?<!\\)(?<!\*)\*(?=\S).+?(?<=\S)\*$"""),
        Regex("""(?<!\\)(?<!_)_(?=\S).+?(?<=\S)_$"""),
    )

    /** 저장·수정 전송 및 수정 로드 시 개행(`\r\n`/`\r` → `\n`)과 Zero-Width Space(`\u200B`)를 정리한다 */
    fun sanitizeForSubmit(raw: String): String =
        raw.replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("\u200B", "")

    /**
     * 에디터 입력 변화에 대한 통합 후처리기.
     *
     * 1. 붙여넣기 정규화 (`\r\n`/`\u200B`/`• ` → `- `/레거시 공백 마커)
     * 2. 숨겨진 이스케이프(`\` + 15자) 백스페이스 동시 삭제 보정
     * 3. 개행(Enter) 시 목록 자동 이어쓰기·번호 재정렬·빈 목록 탈출 및 열린 인라인 마커 닫기
     * 4. 타이핑 자동 변환 (줄 시작 `* ` → `- `, 닫는 인라인 마커 완성 시 서식 미상속)
     */
    fun processContentChange(previous: TextFieldValue, current: TextFieldValue): TextFieldValue {
        if (previous.text == current.text) return current

        normalizePaste(previous, current)?.let { return it }
        deleteHiddenEscapeOnBackspace(previous, current)?.let { return it }
        handleNewline(previous, current)?.let { return it }
        autoFormatOnTyping(previous, current)?.let { return it }

        return current
    }

    /** 붙여넣기(2글자 이상 일괄 삽입) 시 개행·ZWS·불릿 기호·레거시 공백 마커를 정규화한다 */
    private fun normalizePaste(previous: TextFieldValue, current: TextFieldValue): TextFieldValue? {
        if (!current.selection.collapsed) return null
        val replaceStart = previous.selection.min
        val replaceEnd = previous.selection.max
        val prefix = previous.text.substring(0, replaceStart)
        val suffix = previous.text.substring(replaceEnd)
        val insertedEnd = current.text.length - suffix.length
        if (insertedEnd < replaceStart) return null
        if (!current.text.startsWith(prefix) || !current.text.endsWith(suffix)) return null

        val inserted = current.text.substring(replaceStart, insertedEnd)
        if (inserted.length < 2) return null

        val atLineStart = replaceStart == 0 || previous.text[replaceStart - 1] == '\n'
        val normalized = normalizePastedText(inserted, startsAtLineHead = atLineStart)
        if (normalized == inserted) return null

        val newText = prefix + normalized + suffix
        return current.copy(
            text = newText,
            selection = TextRange(replaceStart + normalized.length),
        ).endComposition()
    }

    private fun normalizePastedText(inserted: String, startsAtLineHead: Boolean): String {
        val cleaned = sanitizeForSubmit(inserted)
        val lines = cleaned.split('\n').mapIndexed { index, line ->
            val canConvertBullet = index > 0 || startsAtLineHead
            if (canConvertBullet && line.startsWith("• ")) "- " + line.removePrefix("• ") else line
        }
        return normalizeLegacyWhitespaceMarkers(lines.joinToString("\n"))
    }

    /**
     * 편집기에서 `\`가 숨겨진 이스케이프 시퀀스(`\*`, `\#`, `\.` 등)의 대상 문자를
     * 백스페이스로 지울 때 앞의 숨겨진 `\`도 함께 제거해 고아 `\` 잔존을 막는다.
     */
    private fun deleteHiddenEscapeOnBackspace(
        previous: TextFieldValue,
        current: TextFieldValue,
    ): TextFieldValue? {
        if (!previous.selection.collapsed || !current.selection.collapsed) return null
        if (current.text.length != previous.text.length - 1) return null

        val deletedAt = current.selection.min
        if (deletedAt != previous.selection.min - 1 || deletedAt <= 0) return null
        if (deletedAt >= previous.text.length) return null

        if (previous.text.removeRange(deletedAt, deletedAt + 1) != current.text) return null
        if (previous.text[deletedAt - 1] != '\\' || !isMarkdownEscaped(previous.text, deletedAt)) return null
        // 링크 URL에는 보이는 '\'가 있고, 코드는 15자 외의 문자도 해제하므로 현재 줄의 표시 매핑을 기준으로 한다.
        val lineStart = previous.text.lastIndexOf('\n', deletedAt - 1) + 1
        val lineEnd = previous.text.indexOf('\n', deletedAt).let { if (it == -1) previous.text.length else it }
        val localDeletedAt = deletedAt - lineStart
        val mapping = editorParser.parse(previous.text.substring(lineStart, lineEnd)).transformed.offsetMapping
        if (mapping.originalToTransformed(localDeletedAt - 1) != mapping.originalToTransformed(localDeletedAt)) return null

        val newText = current.text.removeRange(deletedAt - 1, deletedAt)
        return current.copy(
            text = newText,
            selection = TextRange(deletedAt - 1),
        ).endComposition()
    }

    /**
     * 단일 개행(Enter) 입력 처리.
     * - 빈 목록 줄(`- `, `– `, `1. `)에서 Enter → 목록 접두어 제거 및 탈출
     * - 내용 있는 목록 줄에서 Enter → 열린 인라인 마커 닫기 + 다음 항목 자동 생성(번호 목록은 +1 및 후속 재번호)
     * - 일반 줄에서 Enter → 열린 인라인 마커 닫기
     */
    private fun handleNewline(previous: TextFieldValue, current: TextFieldValue): TextFieldValue? {
        val cursor = current.selection.min
        if (singleInsertedChar(previous, current) != '\n') return null

        val newlineAt = cursor - 1
        val lineStart = current.text.lastIndexOf('\n', newlineAt - 1) + 1
        val prevLine = current.text.substring(lineStart, newlineAt)
        val listMatch = matchListPrefix(prevLine)
            ?: return closeMarkersOnNewline(previous, current)

        val remainderOnPrevLine = prevLine.substring(listMatch.prefixLength)
        val afterNewline = current.text.substring(cursor)
        val nextLineBreak = afterNewline.indexOf('\n').let { if (it == -1) afterNewline.length else it }
        val splitTail = afterNewline.substring(0, nextLineBreak)

        // 빈 목록 줄에서 Enter → 목록 접두어와 방금 입력한 개행을 제거해 목록에서 탈출한다
        if (remainderOnPrevLine.isBlank() && splitTail.isEmpty()) {
            val newText = current.text.substring(0, lineStart) + afterNewline
            return current.copy(
                text = newText,
                selection = TextRange(lineStart),
            ).endComposition()
        }

        val nextPrefix = listMatch.nextPrefix()
            ?: return closeMarkersOnNewline(previous, current)
        val split = splitInlineAt(previous.text, newlineAt)
        val updatedTail = if (listMatch is ListPrefixMatch.Numbered && listMatch.number <= Long.MAX_VALUE - 2L) {
            renumberFollowingLines(split.second, startNumber = listMatch.number + 2L)
        } else {
            split.second
        }

        val beforeNewline = split.first
        val newText = beforeNewline + "\n" + nextPrefix + updatedTail
        val newCursor = beforeNewline.length + 1 + nextPrefix.length
        return current.copy(
            text = newText,
            selection = TextRange(newCursor),
        ).endComposition()
    }

    /** 번호 목록 중간에 새 항목이 삽입된 뒤, 바로 이어지는 후속 번호 목록 줄들을 연속 재번호한다 */
    private fun renumberFollowingLines(tail: String, startNumber: Long): String {
        val firstNewline = tail.indexOf('\n')
        if (firstNewline == -1) return tail

        val firstLineRemainder = tail.substring(0, firstNewline)
        val followingLines = tail.substring(firstNewline + 1).split('\n')
        var nextNumber = startNumber
        var renumbering = true
        val updated = followingLines.map { line ->
            if (!renumbering) return@map line
            val match = numberListPrefix.find(line)
            if (match != null) {
                val replaced = "${nextNumber}. " + line.substring(match.value.length)
                if (nextNumber == Long.MAX_VALUE) {
                    renumbering = false
                } else {
                    nextNumber++
                }
                replaced
            } else {
                renumbering = false
                line
            }
        }
        return firstLineRemainder + "\n" + updated.joinToString("\n")
    }

    /**
     * 단일 문자 타이핑 자동 변환:
     * - 줄 시작 `* ` 입력 시 `- `로 변환 (화면에는 즉시 `• `로 표시)
     * - 닫는 인라인 마커 완성 시 composition을 종료해 다음 입력에 서식이 번지지 않게 함
     */
    private fun autoFormatOnTyping(previous: TextFieldValue, current: TextFieldValue): TextFieldValue? {
        val typedChar = singleInsertedChar(previous, current) ?: return null
        val cursor = current.selection.min
        if (cursor <= 0 || cursor > current.text.length) return null

        val lineStart = current.text.lastIndexOf('\n', cursor - 1) + 1
        val lineBeforeCursor = current.text.substring(lineStart, cursor)

        // 줄 시작에서 `* `를 치면 글머리 목록 `- `로 자동 변환한다
        if (typedChar == ' ' && lineBeforeCursor == "* ") {
            val newText = current.text.replaceRange(lineStart, cursor, "- ")
            return current.copy(
                text = newText,
                selection = TextRange(cursor),
            ).endComposition()
        }

        // 닫는 마커가 완성된 직후에는 IME 조합을 끊어 다음 입력에 서식이 상속되지 않게 한다
        if (typedChar in closingTriggerChars &&
            completedInlineAtEnd.any { it.containsMatchIn(lineBeforeCursor) }
        ) {
            return current.endComposition()
        }

        return null
    }

    private sealed class ListPrefixMatch(val prefixLength: Int) {
        abstract fun nextPrefix(): String?

        data object Bullet : ListPrefixMatch(2) {
            override fun nextPrefix(): String = "- "
        }

        data object Dash : ListPrefixMatch(2) {
            override fun nextPrefix(): String = "– "
        }

        data class Numbered(val number: Long, val rawLength: Int) : ListPrefixMatch(rawLength) {
            override fun nextPrefix(): String? =
                if (number < Long.MAX_VALUE) "${number + 1L}. " else null
        }
    }

    private fun matchListPrefix(line: String): ListPrefixMatch? {
        if (line.startsWith("- ")) return ListPrefixMatch.Bullet
        if (line.startsWith("– ")) return ListPrefixMatch.Dash
        val match = numberListPrefix.find(line) ?: return null
        val number = match.groupValues[1].toLongOrNull() ?: return null
        return ListPrefixMatch.Numbered(number = number, rawLength = match.value.length)
    }

    /** 제목·인용구·목록 등 기존 줄 단위 블록 prefix를 찾아 반환한다 */
    private fun findBlockPrefix(line: String): String {
        headingPrefixes.firstOrNull { line.startsWith(it) }?.let { return it }
        if (line.startsWith("> ")) return "> "
        if (line.startsWith("- ")) return "- "
        if (line.startsWith("– ")) return "– "
        numberListPrefix.find(line)?.let { return it.value }
        return ""
    }

    /**
     * 개행이 입력되면 그 줄에서 열려 있던 인라인 마커를 개행 앞에서 모두 닫는다.
     *
     * 파서가 줄 단위로만 인라인 토큰을 찾으므로 토큰이 개행을 넘기면 마커가 화면에
     * 그대로 드러난다. 또한 사용자 기대상 `<mark>가나다` 뒤에서 엔터를 치면
     * 형광펜은 "가나다"까지만 적용되어야 한다
     */
    fun closeMarkersOnNewline(previous: TextFieldValue, current: TextFieldValue): TextFieldValue {
        if (singleInsertedChar(previous, current) != '\n') return current
        if (MarkdownScanner.closersAt(previous.text, previous.selection.min).isEmpty()) return current
        val split = splitInlineAt(previous.text, previous.selection.min)
        return current.copy(
            text = split.first + "\n" + split.second,
            selection = TextRange(split.first.length + 1),
        ).endComposition()
    }

    private fun singleInsertedChar(previous: TextFieldValue, current: TextFieldValue): Char? {
        if (!previous.selection.collapsed || !current.selection.collapsed) return null
        val at = previous.selection.min
        if (current.text.length != previous.text.length + 1 || current.selection.min != at + 1) return null
        if (current.text.removeRange(at, at + 1) != previous.text) return null
        return current.text[at]
    }

    private fun splitInlineAt(text: String, at: Int): Pair<String, String> {
        val markers = MarkdownScanner.openMarkersAt(text, at)
        if (markers.isEmpty()) return text.substring(0, at) to text.substring(at)
        val originalClosers = markers.asReversed().joinToString("") { it.close }
        val retained = markers.toMutableList()
        val ranges = editorParser.closingMarkerRangesAt(text, at)
        val rangesToRemove = ranges.sortedBy { it.first }.toMutableList()
        val lineEnd = text.indexOf('\n', at).let { if (it == -1) text.length else it }
        val firstCloseStart = rangesToRemove.firstOrNull()?.first ?: lineEnd
        val hasTailBody = text.substring(at, firstCloseStart).isNotBlank()
        val movedOpens = StringBuilder()
        var before = text.substring(0, at)
        // 여는 마커 직후에 본문이 이어지면 서식을 다음 줄로 옮겨 보존하고, 내용 없는 빈 서식만 걷어낸다.
        markers.asReversed().forEach { marker ->
            val body = before.substring(marker.start + marker.open.length)
            if (body.isBlank() && (!marker.isCode || body.isEmpty())) {
                before = before.removeRange(marker.start, marker.start + marker.open.length)
                retained.remove(marker)
                if (body.isEmpty() && hasTailBody) {
                    val closeIndex = rangesToRemove.indexOfFirst {
                        text.substring(it.first, it.last + 1) == marker.close
                    }
                    if (closeIndex != -1) {
                        rangesToRemove.removeAt(closeIndex)
                        movedOpens.insert(0, marker.open)
                    }
                }
            }
        }
        val closers = retained.asReversed().joinToString("") { it.close }
        var tail = text.substring(at)
        rangesToRemove.sortedByDescending { it.first }.forEach { range ->
            tail = tail.removeRange(range.first - at, range.last + 1 - at)
        }
        if (ranges.isEmpty() && tail.startsWith(originalClosers)) {
            tail = tail.removePrefix(originalClosers)
        }
        tail = movedOpens.toString() + tail
        // 코드 내부 공백은 원문이다. 다른 서식은 끝 공백을 닫는 마커 밖으로 옮긴다.
        val trimmed = if (closers.startsWith('`')) before else before.trimEnd(' ', '\t')
        return (trimmed + closers + before.substring(trimmed.length)) to tail
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

        val existing = findBlockPrefix(line)
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
        val isActive = lines.all { it.startsWith(prefix) }

        val newLines = lines.map { line ->
            if (isActive) {
                line.removePrefix(prefix)
            } else {
                val existing = findBlockPrefix(line)
                prefix + line.removePrefix(existing)
            }
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
        if (!value.selection.collapsed && value.text.substring(value.selection.min, value.selection.max).isBlank()) return value
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
        if (!value.selection.collapsed && value.text.substring(value.selection.min, value.selection.max).isBlank()) return value
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
