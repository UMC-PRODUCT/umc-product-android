package com.umc.presentation.notice.write

/** 마커 앞 연속 백슬래시가 홀수 개일 때만 해당 마커가 이스케이프되어 있다. */
internal fun isMarkdownEscaped(text: String, at: Int): Boolean {
    var i = at - 1
    while (i >= 0 && text[i] == '\\') i--
    return (at - 1 - i) % 2 == 1
}

internal fun Regex.findUnescaped(text: String, from: Int = 0): MatchResult? {
    var cursor = from
    while (cursor <= text.length) {
        val match = find(text, cursor) ?: return null
        if (!isMarkdownEscaped(text, match.range.first)) return match
        cursor = match.range.first + 1
    }
    return null
}

// 백슬래시는 반드시 다음 문자와 함께 소비해야 닫는 마커로 되돌아가는 오파싱을 막는다.
internal val MARKDOWN_CODE_REGEX = Regex("""`((?:\\.|[^\\`\n])+)`""")
internal val MARKDOWN_LINK_REGEX = Regex("""\[((?:\\.|[^\\\]\n])+)\]\(((?:\\.|[^\\)\n])+)\)""")

internal fun markdownCodeRanges(text: String): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    var cursor = 0
    while (cursor < text.length) {
        val match = MARKDOWN_CODE_REGEX.findUnescaped(text, cursor) ?: break
        ranges += match.range
        cursor = match.range.last + 1
    }
    return ranges
}

internal fun unescapeMarkdownText(text: String): String = buildString(text.length) {
    var i = 0
    while (i < text.length) {
        if (text[i] == '\\' && i + 1 < text.length && text[i + 1] in MARKDOWN_ESCAPABLE_CHARS) i++
        append(text[i++])
    }
}
