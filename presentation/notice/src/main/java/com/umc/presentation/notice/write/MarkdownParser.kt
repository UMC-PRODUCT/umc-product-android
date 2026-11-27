package com.umc.presentation.notice.write

/** 화면 표시와 편집이 공유하는 문법 분석. 스타일과 커서 상태를 생성하지 않는다. */
internal object MarkdownParser {
    fun parseInline(
        raw: String,
        start: Int = 0,
        end: Int = raw.length,
        includeEmptyPairs: Boolean = true,
        detectPlainUrls: Boolean = false,
        insideLink: Boolean = false,
    ): List<InlineToken> {
        val region = raw.substring(start, end)
        val failed = HashSet<Long>()
        val tokens = mutableListOf<InlineToken>()
        var cursor = 0
        while (cursor < region.length) {
            val local = findEarliestToken(region, cursor, detectPlainUrls && !insideLink, includeEmptyPairs, failed) ?: break
            val token = local.copy(start = start + local.start, end = start + local.end)
            val children = if (token.kind == InlineTokenKind.CODE || token.kind.isEmptyPair) {
                emptyList()
            } else {
                parseInline(
                    raw, token.innerStart, token.innerEnd, includeEmptyPairs, detectPlainUrls,
                    insideLink || token.kind == InlineTokenKind.LINK,
                )
            }
            tokens += token.copy(children = children)
            cursor = local.end
        }
        return tokens
    }

    /** 전체 토큰을 원문 순서로 조회한다. 코드와 URL 내부에는 자식 토큰을 만들지 않는다. */
    fun descendants(tokens: List<InlineToken>): Sequence<InlineToken> = sequence {
        for (token in tokens) {
            yield(token)
            yieldAll(descendants(token.children))
        }
    }

    /** 읽기 모드와 같은 문법으로 마커를 제거하되 스타일·링크·표시 좌표는 생성하지 않는다. */
    fun plainText(raw: String): String {
        val source = preprocessForReadOnly(raw)
        return source.split('\n').joinToString("\n") { line ->
            val block = MarkdownBlock.at(line)
            val body = line.substring(block?.prefix?.length ?: 0)
            (block?.replacement ?: "") + plainRegion(
                body, 0, body.length,
                parseInline(body, includeEmptyPairs = false, detectPlainUrls = true),
            )
        }
    }

    private fun plainRegion(raw: String, start: Int, end: Int, tokens: List<InlineToken>): String = buildString {
        var cursor = start
        for (token in tokens) {
            append(unescapeMarkdownText(raw.substring(cursor, token.start)))
            if (token.kind == InlineTokenKind.CODE) {
                append(unescapeMarkdownText(raw.substring(token.innerStart, token.innerEnd), inCode = true))
            } else {
                append(plainRegion(raw, token.innerStart, token.innerEnd, token.children))
            }
            cursor = token.end
        }
        append(unescapeMarkdownText(raw.substring(cursor, end)))
    }

    /** 편집 모드에서 숨겨지는 escape인지 판별한다. 링크 URL의 escape는 화면에 남는다. */
    fun isHiddenEscapeAt(raw: String, at: Int): Boolean {
        if (raw.getOrNull(at) != '\\' || at + 1 >= raw.length || isMarkdownEscaped(raw, at)) return false
        val lineStart = raw.lastIndexOf('\n', at - 1) + 1
        val lineEnd = raw.indexOf('\n', at).let { if (it < 0) raw.length else it }
        val enclosing = descendants(parseInline(raw, lineStart, lineEnd)).filter { at in it.start until it.end }.toList()
        if (enclosing.any { it.kind == InlineTokenKind.CODE && at in it.innerStart until it.innerEnd - 1 }) return true
        if (enclosing.any { it.kind == InlineTokenKind.LINK && at >= it.innerEnd }) return false
        return raw[at + 1] in MARKDOWN_ESCAPABLE_CHARS
    }

    private fun findEarliestToken(
        region: String,
        from: Int,
        detectPlainUrls: Boolean = false,
        includeEmptyPairs: Boolean = true,
        failedTokens: MutableSet<Long> = HashSet(),
    ): InlineToken? {
        var best: InlineToken? = null
        var bestPriority = Int.MAX_VALUE
        // 이미 소비한 링크·코드의 URL은 제외하고, 일반 텍스트 구간에서 한 번만 검색한다.
        val plainUrls = if (detectPlainUrls) PLAIN_URL_REGEX.findAll(region, from).map { it.range }.toList() else emptyList()

        InlineTokenKind.entries.forEachIndexed { priority, kind ->
            if (!includeEmptyPairs && kind.isEmptyPair) return@forEachIndexed
            var token = kind.findToken(region, from, failedTokens) ?: return@forEachIndexed
            while (isInvalidMixedWhitespace(token, region) ||
                plainUrls.any { token.start > it.first && token.start <= it.last }
            ) {
                token = kind.findToken(region, token.start + 1, failedTokens) ?: return@forEachIndexed
            }
            val current = best
            if (current == null || token.start < current.start ||
                (token.start == current.start && priority < bestPriority)
            ) {
                best = token
                bestPriority = priority
            }
        }
        return best
    }

    private fun isInvalidMixedWhitespace(token: InlineToken, region: String): Boolean {
        if (token.kind != InlineTokenKind.BOLD) return false
        val inner = region.substring(token.start + token.openLength, token.end - token.closeLength)
        if (inner.length < 2 || !inner.startsWith('_') || !inner.endsWith('_')) return false
        val body = inner.substring(1, inner.length - 1)
        // 공백 때문에 무효인 **_ ... _**를 일반 굵게로 재해석하지 않는다.
        return body.isEmpty() || body.first().isWhitespace() || body.last().isWhitespace()
    }

}

/** 줄 단위 문법. 대시·번호 목록은 저장된 접두어를 그대로 표시한다. */
internal enum class MarkdownBlock(val prefix: String, val replacement: String? = null) {
    TITLE3("### "), TITLE2("## "), TITLE1("# "), QUOTE("> "), BULLET("- ", "• ");

    companion object {
        fun at(line: String): MarkdownBlock? = entries.firstOrNull { line.startsWith(it.prefix) }
    }
}

internal enum class InlineTokenKind(
    val regex: Regex,
    /** 본문에 해당하는 정규식 그룹 번호 (형광펜은 색상이 1번이라 2번이 본문) */
    val innerGroup: Int = 1,
    val isEmptyPair: Boolean = false,
    val closingMarker: String? = null,
) {
    EMPTY_UNDERLINE(Regex("""<u></u>"""), isEmptyPair = true),
    // 형광펜 안 글자를 모두 지운 경우. 마커가 길어 그대로 드러나면 가장 눈에 띈다
    EMPTY_HIGHLIGHT(Regex("""<mark(?:\s+color="[^"]*")?></mark>"""), isEmptyPair = true),
    // 밑줄 기울임 빈 쌍 (`_` + `_`)
    EMPTY_ITALIC_UNDERSCORE(Regex("""__"""), isEmptyPair = true),
    EMPTY_STRIKETHROUGH(Regex("""~~~~"""), isEmptyPair = true),
    // 굵게+기울임 빈 쌍(`***` + `***`). 굵게 빈 쌍보다 먼저 잡아야 뒤 두 개가 남지 않는다
    EMPTY_BOLD_ITALIC(Regex("""\*{6}"""), isEmptyPair = true),
    EMPTY_BOLD(Regex("""\*{4}"""), isEmptyPair = true),
    CODE(MARKDOWN_CODE_REGEX),
    LINK(MARKDOWN_LINK_REGEX),
    UNDERLINE(Regex("""<u>((?:\\.|[^\\\n])+?)</u>""")),
    STRIKETHROUGH(Regex("""~~"""), closingMarker = "~~"),
    HIGHLIGHT(Regex("""<mark(?:\s+color="([^"]*)")?>((?:\\.|[^\\\n])+?)</mark>"""), innerGroup = 2),
    BOLD_ITALIC_MIXED(Regex("""\*\*_"""), closingMarker = "_**"),
    BOLD_ITALIC_TRIPLE(Regex("""\*\*\*"""), closingMarker = "***"),
    BOLD(Regex("""\*\*"""), closingMarker = "**"),
    ITALIC_ASTERISK(Regex("""\*"""), closingMarker = "*"),
    ITALIC_UNDERSCORE(Regex("""_"""), closingMarker = "_");

    fun findToken(text: String, from: Int, failed: MutableSet<Long> = HashSet()): InlineToken? {
        var cursor = from
        while (cursor < text.length) {
            val match = regex.findUnescaped(text, cursor) ?: return null
            val token = tokenAt(text, match.range.first, failed)
            if (token != null) return token
            cursor = match.range.first + 1
        }
        return null
    }

    private fun tokenAt(text: String, at: Int, failed: MutableSet<Long>): InlineToken? {
        // 닫히지 않은 다중 마커 줄에서의 지수 백트래킹을 막기 위해 실패한 (종류, 위치)를 캐시한다.
        val cacheKey = (ordinal.toLong() shl 32) or (at.toLong() and 0xFFFF_FFFFL)
        if (cacheKey in failed) return null
        fun fail(): InlineToken? {
            failed += cacheKey
            return null
        }

        if (isMarkdownEscaped(text, at)) return fail()
        val match = regex.matchAt(text, at) ?: return fail()
        val close = closingMarker ?: return toToken(match) ?: fail()
        val open = match.value
        val innerStart = at + open.length
        if (innerStart >= text.length || text[innerStart].isWhitespace()) return fail()
        if (open.startsWith('*')) {
            if (at > 0 && text[at - 1] == '*') return fail()
            if (text.getOrNull(innerStart) == '*') {
                // `***가* 나**`처럼 굵게 시작 직후 별표 기울임이 바로 열릴 때만 허용한다.
                val nestedItalicStart = this == BOLD &&
                    text.getOrNull(innerStart + 1)?.let { !it.isWhitespace() && it != '*' } == true
                if (!nestedItalicStart) return fail()
            }
        }
        if (this == ITALIC_UNDERSCORE && (text.getOrNull(at - 1) == '_' || text.getOrNull(innerStart) == '_')) return fail()

        var cursor = innerStart
        while (cursor < text.length) {
            if (text[cursor] == '\n') return fail()
            if (text[cursor] == '\\' && cursor + 1 < text.length) {
                cursor += 2
                continue
            }
            val isOwnClose = when {
                this == ITALIC_ASTERISK && text[cursor] == '*' -> {
                    var runEnd = cursor
                    while (runEnd < text.length && text[runEnd] == '*') runEnd++
                    val runLength = runEnd - cursor
                    // 단독 `*` 또는 `**가 *나***`처럼 단어 끝에 붙은 `***`의 첫 `*`로 닫는다.
                    runLength == 1 || (runLength == 3 && !text[cursor - 1].isWhitespace())
                }
                close.endsWith('*') -> {
                    // `**가***`, `**_가_***`, `***가****`처럼 닫는 별표 뒤에 여분 `*`가 이어지면 닫지 않는다.
                    text.startsWith(close, cursor) && text.getOrNull(cursor + close.length) != '*'
                }
                this == ITALIC_UNDERSCORE -> text.startsWith(close, cursor) && text.getOrNull(cursor + 1) != '_'
                else -> text.startsWith(close, cursor)
            }
            if (isOwnClose) {
                if (!text[cursor - 1].isWhitespace()) {
                    return InlineToken(at, cursor + close.length, open.length, close.length, this)
                }
                // 안쪽 공백이 붙은 닫는 마커를 넘어 다음 토큰까지 삼키지 않는다.
                val nested = tokenAt(text, cursor, failed) ?: return fail()
                cursor = nested.end
                continue
            }
            // 코드·링크·다른 인라인 서식 내부의 마커는 현재 토큰을 닫지 않는다.
            var nested: InlineToken? = null
            for (kind in entries) {
                if (kind.isEmptyPair || kind == this) continue
                val candidate = kind.tokenAt(text, cursor, failed)
                if (candidate != null) {
                    nested = candidate
                    break
                }
            }
            if (nested != null) {
                cursor = nested.end
            } else if (text[cursor] == '*' || text[cursor] == '~') {
                val marker = text[cursor]
                do { cursor++ } while (cursor < text.length && text[cursor] == marker)
            } else {
                cursor++
            }
        }
        return fail()
    }

    /** 매치 결과를 마커 길이가 계산된 토큰으로 변환 */
    fun toToken(match: MatchResult): InlineToken? {
        val start = match.range.first
        val end = match.range.last + 1
        return when (this) {
            EMPTY_UNDERLINE -> InlineToken(start, end, openLength = 3, closeLength = 4, kind = this)
            // 여는 태그 길이는 색상 유무에 따라 달라지므로 전체에서 `</mark>`를 뺀 만큼
            EMPTY_HIGHLIGHT -> InlineToken(
                start,
                end,
                openLength = (end - start) - 7,
                closeLength = 7,
                kind = this,
            )

            EMPTY_ITALIC_UNDERSCORE, EMPTY_STRIKETHROUGH, EMPTY_BOLD, EMPTY_BOLD_ITALIC -> {
                val half = (end - start) / 2
                InlineToken(start, end, openLength = half, closeLength = half, kind = this)
            }

            LINK -> {
                val innerRange = match.groups[1]?.range ?: return null
                InlineToken(
                    start = start,
                    end = end,
                    openLength = innerRange.first - start,
                    closeLength = match.range.last - innerRange.last,
                    kind = this,
                    argument = match.groups[2]?.value,
                )
            }

            else -> {
                val innerRange = match.groups[innerGroup]?.range ?: return null
                InlineToken(
                    start = start,
                    end = end,
                    openLength = innerRange.first - start,
                    closeLength = match.range.last - innerRange.last,
                    kind = this,
                    argument = if (this == HIGHLIGHT) match.groups[1]?.value else null,
                )
            }
        }
    }

}

/** 모든 범위는 원문 UTF-16 좌표이며 끝 좌표는 제외한다. 자식은 본문 안의 토큰이다. */
internal data class InlineToken(
    val start: Int,
    val end: Int,
    val openLength: Int,
    val closeLength: Int,
    val kind: InlineTokenKind,
    val argument: String? = null,
    val children: List<InlineToken> = emptyList(),
) {
    val innerStart: Int get() = start + openLength
    val innerEnd: Int get() = end - closeLength
}
