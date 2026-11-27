package com.umc.presentation.notice.write

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** 밑줄·취소선·형광펜 선택 편집. 원문 조상 서식을 유지하며 선택한 부분의 래핑만 바꾼다. */
internal fun editSelectedMarkdownStyle(
    value: TextFieldValue,
    kind: InlineTokenKind,
    open: String,
    close: String,
    argument: String? = null,
): TextFieldValue {
    if (value.selection.collapsed) return value
    val raw = value.text
    var start = value.selection.min
    var end = value.selection.max
    while (start < end && raw[start].isWhitespace()) start++
    while (end > start && raw[end - 1].isWhitespace()) end--
    if (start == end) return value
    // 표시 선택은 숨긴 백슬래시 뒤에서 시작한다. escape와 대상 문자를 함께 옮겨야 리터럴이 유지된다.
    if (start > 0 && MarkdownParser.isHiddenEscapeAt(raw, start - 1)) start--
    // 선택 끝이 다음 문자의 숨긴 escape 뒤로 매핑돼도 그 escape는 다음 문자에 남긴다.
    if (end > start && MarkdownParser.isHiddenEscapeAt(raw, end - 1)) end--
    val lineStart = raw.lastIndexOf('\n', start - 1) + 1
    val lineEnd = raw.indexOf('\n', end).let { if (it < 0) raw.length else it }
    val runs = mutableListOf<InlineEditRun>()

    fun leaf(from: Int, until: Int, wrappers: List<InlineEditWrapper>, editable: Boolean = true) {
        val cuts = listOf(from, start.coerceIn(from, until), end.coerceIn(from, until), until).distinct().sorted()
        cuts.zipWithNext().forEach { (a, b) ->
            if (a < b) runs += InlineEditRun(raw.substring(a, b), wrappers, a >= start && b <= end, editable)
        }
    }

    fun collect(from: Int, until: Int, tokens: List<InlineToken>, wrappers: List<InlineEditWrapper>) {
        var cursor = from
        for (token in tokens) {
            leaf(cursor, token.start, wrappers)
            if (token.kind == InlineTokenKind.CODE || token.kind.isEmptyPair) {
                leaf(token.start, token.end, wrappers, editable = false)
            } else {
                val wrapper = InlineEditWrapper(
                    token.kind, raw.substring(token.start, token.innerStart),
                    raw.substring(token.innerEnd, token.end), token.argument, token.start,
                )
                collect(token.innerStart, token.innerEnd, token.children, wrappers + wrapper)
            }
            cursor = token.end
        }
        leaf(cursor, until, wrappers)
    }

    var at = lineStart
    while (at <= lineEnd) {
        val until = raw.indexOf('\n', at).let { if (it < 0 || it > lineEnd) lineEnd else it }
        val prefix = inlineEditBlockPrefix(raw.substring(at, until))
        leaf(at, at + prefix.length, emptyList(), editable = false)
        collect(at + prefix.length, until, MarkdownParser.parseInline(raw, at + prefix.length, until), emptyList())
        if (until == lineEnd) break
        leaf(until, until + 1, emptyList(), editable = false)
        at = until + 1
    }
    val eligible = runs.filter { it.selected && it.editable && it.text.isNotBlank() }
    if (eligible.isEmpty()) return value
    val desired = InlineEditWrapper(kind, open, close, argument, -1)
    val remove = eligible.all { run -> run.wrappers.any { it.matches(desired) } }
    val changed = runs.map { run ->
        if (!run.selected || !run.editable || run.text.isBlank()) run else {
            val wrappers = run.wrappers.filterNot { it.kind == kind }.toMutableList()
            if (!remove) {
                val index = wrappers.indexOfFirst { it.kind.wrapOrder > kind.wrapOrder }
                wrappers.add(if (index < 0) wrappers.size else index, desired)
            }
            run.copy(wrappers = wrappers)
        }
    }
    val output = StringBuilder(raw.substring(0, lineStart))
    var active = emptyList<InlineEditWrapper>()
    var selectedStart: Int? = null
    var selectedEnd: Int? = null
    fun marker(text: String) {
        val before = output.length
        appendMarkdownBoundary(output, text)
        if (selectedEnd == before) selectedEnd = output.length - text.length
    }
    fun append(text: String, wrappers: List<InlineEditWrapper>, selected: Boolean) {
        if (text.isEmpty()) return
        var common = 0
        while (common < minOf(active.size, wrappers.size) && active[common] == wrappers[common]) common++
        active.drop(common).asReversed().forEach { marker(it.close) }
        wrappers.drop(common).forEach { marker(it.open) }
        if (selected && selectedStart == null) selectedStart = output.length
        output.append(text)
        if (selected) selectedEnd = output.length
        active = wrappers
    }
    fun sharedPrefix(a: List<InlineEditWrapper>, b: List<InlineEditWrapper>): List<InlineEditWrapper> =
        a.zip(b).takeWhile { (left, right) -> left == right }.map { it.first }

    for ((index, run) in changed.withIndex()) {
        if (!run.editable) {
            append(run.text, run.wrappers, run.selected)
        } else {
            val next = changed.drop(index + 1).firstOrNull { !it.editable || it.text.isNotBlank() }
                ?.wrappers.orEmpty()
            val leading = run.text.takeWhile { it.isWhitespace() }.length
            val trailing = run.text.takeLastWhile { it.isWhitespace() }.length.coerceAtMost(run.text.length - leading)
            val links = run.wrappers.filter { it.kind == InlineTokenKind.LINK }
            // 계속 이어지는 조상 서식의 공백은 유지하고 새로 열리거나 닫히는 경계만 분리한다.
            val leadingWrappers = (sharedPrefix(active, run.wrappers).let {
                if (run.text.isBlank()) sharedPrefix(it, next) else it
            } + links).distinct()
            val trailingWrappers = (sharedPrefix(run.wrappers, next) + links).distinct()
            append(run.text.take(leading), leadingWrappers, run.selected)
            append(run.text.substring(leading, run.text.length - trailing), run.wrappers, run.selected)
            append(run.text.takeLast(trailing), trailingWrappers, run.selected)
        }
    }
    active.asReversed().forEach { marker(it.close) }
    output.append(raw, lineEnd, raw.length)
    val newStart = selectedStart ?: return value
    val newEnd = selectedEnd ?: newStart
    return value.copy(
        text = output.toString(),
        selection = if (value.selection.start > value.selection.end) TextRange(newEnd, newStart) else TextRange(newStart, newEnd),
    )
}

private data class InlineEditRun(
    val text: String,
    val wrappers: List<InlineEditWrapper>,
    val selected: Boolean,
    val editable: Boolean,
)

private data class InlineEditWrapper(
    val kind: InlineTokenKind,
    val open: String,
    val close: String,
    val argument: String?,
    val identity: Int,
) {
    fun matches(other: InlineEditWrapper): Boolean = kind == other.kind &&
        (kind != InlineTokenKind.HIGHLIGHT || argument?.split(',')?.map { it.trim().toFloatOrNull() } ==
            other.argument?.split(',')?.map { it.trim().toFloatOrNull() })
}

private val InlineTokenKind.wrapOrder: Int
    get() = when (this) {
        InlineTokenKind.LINK -> 0
        InlineTokenKind.HIGHLIGHT -> 1
        InlineTokenKind.STRIKETHROUGH -> 2
        InlineTokenKind.UNDERLINE -> 3
        else -> 4
    }

private fun inlineEditBlockPrefix(line: String): String = MarkdownBlock.at(line)?.prefix
    ?: when {
        line.startsWith("– ") -> "– "
        else -> INLINE_EDIT_NUMBER_PREFIX.find(line)?.value.orEmpty()
    }

private val INLINE_EDIT_NUMBER_PREFIX = Regex("""^\d+\. """)
