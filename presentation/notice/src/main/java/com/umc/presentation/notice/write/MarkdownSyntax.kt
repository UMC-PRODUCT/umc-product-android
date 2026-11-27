package com.umc.presentation.notice.write

import java.util.Locale

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

internal fun unescapeMarkdownText(text: String, inCode: Boolean = false): String = buildString(text.length) {
    var i = 0
    while (i < text.length) {
        if (text[i] == '\\' && i + 1 < text.length && (inCode || text[i + 1] in MARKDOWN_ESCAPABLE_CHARS)) i++
        append(text[i++])
    }
}

/** iOS 스펙 기준 일반 텍스트에서 백슬래시 이스케이프 해제 대상 15자 */
internal val MARKDOWN_ESCAPABLE_CHARS: Set<Char> = setOf(
    '\\', '*', '_', '[', ']', '(', ')', '~', '`', '<', '>', '#', '-', '.', '–',
)

/** 읽기 전용 렌더링 전 개행·Zero-Width Space·레거시 공백 마커를 정규화한다 */
internal fun preprocessForReadOnly(raw: String): String =
    normalizeLegacyWhitespaceMarkers(
        raw.replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("\u200B", ""),
    )

/**
 * 과거 저장된 `**일시: **`, `** **`, `~~취소 ~~`, `~~ ~~`처럼
 * 마커 안쪽에 공백이 붙은 레거시 표현을 파싱 전에 마커 바깥으로 분리한다.
 * 단독 `*`/`_`는 `2 * 3`, `user_name` 훼손을 막기 위해 정규화하지 않는다.
 */
internal fun normalizeLegacyWhitespaceMarkers(text: String): String {
    val result = StringBuilder(text.length)
    var cursor = 0
    markdownCodeRanges(text).forEach { code ->
        result.append(normalizeLegacyPlainSegment(text.substring(cursor, code.first)))
        result.append(text, code.first, code.last + 1)
        cursor = code.last + 1
    }
    result.append(normalizeLegacyPlainSegment(text.substring(cursor)))
    return result.toString()
}

private fun normalizeLegacyPlainSegment(text: String): String {
    var result = text
    listOf(LEGACY_BOLD_WHITESPACE to "**", LEGACY_STRIKE_WHITESPACE to "~~").forEach { (regex, marker) ->
        val normalized = StringBuilder(result.length)
        var cursor = 0
        while (cursor < result.length) {
            val match = regex.findUnescaped(result, cursor) ?: break
            normalized.append(result, cursor, match.range.first)
            normalized.append(relocateEdgeSpaces(match.groupValues[1], marker))
            cursor = match.range.last + 1
        }
        normalized.append(result, cursor, result.length)
        result = normalized.toString()
    }
    return result
}

private fun relocateEdgeSpaces(inner: String, marker: String): String {
    val leadingCount = inner.takeWhile { it == ' ' || it == '\t' }.length
    val trailingCount = inner.takeLastWhile { it == ' ' || it == '\t' }.length
    if (leadingCount == 0 && trailingCount == 0) return "$marker$inner$marker"

    val trimmed = inner.trim(' ', '\t')
    if (trimmed.isEmpty()) return inner

    val leading = inner.substring(0, leadingCount)
    val trailing = inner.substring(inner.length - trailingCount)
    return "$leading$marker$trimmed$marker$trailing"
}

/** 링크 URL 내부의 `\)`, `\\` 이스케이프를 해제한다 */
internal fun unescapeLinkUrl(rawUrl: String): String {
    if ('\\' !in rawUrl) return rawUrl
    val sb = StringBuilder(rawUrl.length)
    var i = 0
    while (i < rawUrl.length) {
        if (rawUrl[i] == '\\' && i + 1 < rawUrl.length && (rawUrl[i + 1] == ')' || rawUrl[i + 1] == '\\')) {
            sb.append(rawUrl[i + 1])
            i += 2
        } else {
            sb.append(rawUrl[i])
            i++
        }
    }
    return sb.toString()
}

/** 허용 스킴(`https`, `http`, `mailto`, `tel`, 대소문자 무관)만 링크로 인정한다 */
internal fun isAllowedLinkScheme(url: String): Boolean {
    val trimmed = url.trim()
    val colonIndex = trimmed.indexOf(':')
    if (colonIndex <= 0) return false
    val scheme = trimmed.substring(0, colonIndex).lowercase(Locale.ROOT)
    return scheme in ALLOWED_LINK_SCHEMES
}

/** Plain URL 끝에 붙은 문장부호(`.`, `,`, `;`, `:`, `!`, `?` 및 짝 없는 `)`)를 제거한다 */
internal fun trimTrailingUrlPunctuation(url: String): String {
    var end = url.length
    while (end > 0) {
        val last = url[end - 1]
        when {
            last in TRAILING_URL_PUNCTUATION -> end--
            last == ')' -> {
                val slice = url.substring(0, end)
                val openCount = slice.count { it == '(' }
                val closeCount = slice.count { it == ')' }
                if (closeCount > openCount) end-- else break
            }

            else -> break
        }
    }
    return url.substring(0, end)
}

private val ALLOWED_LINK_SCHEMES = setOf("https", "http", "mailto", "tel")
private val TRAILING_URL_PUNCTUATION = setOf('.', ',', ';', ':', '!', '?')
internal val PLAIN_URL_REGEX = Regex("""https?://[^\s<>]+""", RegexOption.IGNORE_CASE)
private val LEGACY_BOLD_WHITESPACE = Regex("""(?<!\*)\*\*(?!\*)((?:\\.|(?!\*\*)[^\\\n])+)\*\*(?!\*)""")
private val LEGACY_STRIKE_WHITESPACE = Regex("""~~((?:\\.|(?!~~)[^\\\n])+)~~""")


internal val MARKDOWN_HIGHLIGHT_OPEN = Regex("""<mark(?:\s+color="[^"]*")?>""")
