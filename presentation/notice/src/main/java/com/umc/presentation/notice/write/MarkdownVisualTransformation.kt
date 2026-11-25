package com.umc.presentation.notice.write

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * 마크다운 원문에서 마커 문자를 숨기고 스타일만 입혀 보여주는 VisualTransformation.
 * 화면에는 마커가 보이지 않지만 실제 TextFieldValue(서버 전송값)에는 그대로 남는다.
 * 문법은 iOS(umc-product-iOS)의 공지 본문 마크다운 스펙과 일치시킨다.
 *
 * - 편집 모드([isReadOnly] = false): 커서가 숨겨진 마커 경계에 놓이면 토큰 안쪽으로 매핑하고,
 *   링크(`[라벨](url)`)는 URL 편집이 가능하도록 마커를 숨기지 않고 흐리게 표시한다.
 * - 읽기 전용 모드([isReadOnly] = true): 개행·레거시 공백 마커를 전처리한 뒤,
 *   링크 마커를 숨기고 허용 스킴(`https`/`http`/`mailto`/`tel`) 클릭과 Plain URL 자동 링크를 지원한다.
 */
class MarkdownVisualTransformation(
    private val markerColor: Color,
    private val linkColor: Color,
    private val isReadOnly: Boolean = false,
    private val onOpenUrl: ((String) -> Unit)? = null,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText = parse(text.text).transformed

    /** 커서가 나누는 기존 토큰의 닫는 마커 좌표. 개행 뒤 고아 마커 제거에 사용한다. */
    internal fun closingMarkerRangesAt(raw: String, at: Int): List<IntRange> {
        val lineStart = raw.lastIndexOf('\n', at - 1) + 1
        val lineEnd = raw.indexOf('\n', at).let { if (it == -1) raw.length else it }
        val ranges = mutableListOf<IntRange>()
        fun collect(start: Int, end: Int) {
            val region = raw.substring(start, end)
            val failedTokens = HashSet<Long>()
            var cursor = 0
            while (cursor < region.length) {
                val token = findEarliestToken(region, cursor, failedTokens = failedTokens) ?: break
                val innerStart = start + token.start + token.openLength
                val innerEnd = start + token.end - token.closeLength
                if (at in innerStart..innerEnd) {
                    if (token.kind != InlineTokenKind.LINK) {
                        ranges += innerEnd until (start + token.end)
                    }
                    if (token.kind != InlineTokenKind.CODE) collect(innerStart, innerEnd)
                }
                cursor = token.end
            }
        }
        collect(lineStart, lineEnd)
        return ranges
    }

    /** 표시 문자열과 함께 인용 블록 좌표까지 계산 (인용구 세로선을 그리려면 좌표가 필요) */
    fun parse(raw: String): MarkdownRendered {
        val source = if (isReadOnly) preprocessForReadOnly(raw) else raw
        val hidden = mutableListOf<HiddenRange>()
        val replaced = mutableListOf<ReplacedRange>()
        val spans = mutableListOf<SpanRange>()
        val links = mutableListOf<LinkRange>()
        val quoteLines = mutableListOf<IntRange>()

        collectBlocks(source, hidden, replaced, spans, links, quoteLines)
        if (!isReadOnly) {
            hideOrphanMarkers(source, hidden)
        }
        val normalizedHidden = normalizeHiddenRanges(hidden)

        val mapping = MarkdownOffsetMapping(originalLength = source.length, hidden = normalizedHidden)
        val transformed = TransformedText(
            buildTransformed(source, normalizedHidden, replaced, spans, links, mapping),
            mapping,
        )

        return MarkdownRendered(
            transformed = transformed,
            quoteBlocks = mergeQuoteBlocks(quoteLines, source).map { block ->
                mapping.originalToTransformed(block.first)..mapping.originalToTransformed(block.last)
            },
        )
    }

    /** 줄 단위 블록 → 인라인 순서로 숨김/치환/스타일/링크 범위를 수집 */
    private fun collectBlocks(
        raw: String,
        hidden: MutableList<HiddenRange>,
        replaced: MutableList<ReplacedRange>,
        spans: MutableList<SpanRange>,
        links: MutableList<LinkRange>,
        quoteLines: MutableList<IntRange>,
    ) {
        var lineStart = 0
        raw.split('\n').forEach { lineWithCr ->
            val rawLineEnd = lineStart + lineWithCr.length
            // 편집기에서 \r\n이 들어온 경우 \r만 숨겨 OffsetMapping을 유지한다
            val hasTrailingCr = lineWithCr.endsWith('\r')
            val line = if (hasTrailingCr) lineWithCr.dropLast(1) else lineWithCr
            val lineEnd = lineStart + line.length
            if (hasTrailingCr) {
                hidden += HiddenRange(lineEnd, rawLineEnd, mapAfter = false)
            }

            val block = BlockSpec.entries.firstOrNull { line.startsWith(it.prefix) }
            var contentStart = lineStart

            if (block != null) {
                contentStart = lineStart + block.prefix.length

                if (block.replacement != null) {
                    // 길이가 같은 문자로 치환하면 OffsetMapping을 건드릴 필요가 없다
                    replaced += ReplacedRange(lineStart, contentStart, block.replacement)
                } else {
                    hidden += HiddenRange(lineStart, contentStart, mapAfter = true)
                }

                block.fontSize?.let { fontSize ->
                    spans += SpanRange(
                        SpanStyle(fontSize = fontSize, fontWeight = block.fontWeight),
                        contentStart,
                        lineEnd,
                        isInline = false,
                    )
                }

                if (block == BlockSpec.QUOTE) quoteLines += contentStart..lineEnd
            }

            collectInline(
                raw = raw,
                regionStart = contentStart,
                regionEnd = lineEnd,
                inheritedDecorations = emptyList(),
                insideLink = false,
                hidden = hidden,
                spans = spans,
                links = links,
            )
            lineStart = rawLineEnd + 1
        }
    }

    /**
     * 토큰으로 짝이 맞지 않아 남은 태그형 마커를 숨긴다.
     *
     * 글자를 지우다 한쪽 마커만 남으면(`<mark ...>가나` 처럼) 토큰이 성립하지 않아
     * 마커가 화면에 그대로 드러난다. 이스케이프된 `\<u\>` 등은 건드리지 않는다.
     */
    private fun hideOrphanMarkers(raw: String, hidden: MutableList<HiddenRange>) {
        val codeRanges = markdownCodeRanges(raw)
        ORPHAN_MARKER.findAll(raw).forEach { match ->
            val start = match.range.first
            if (isMarkdownEscaped(raw, start) || codeRanges.any { start in it }) return@forEach
            val end = match.range.last + 1
            val alreadyHidden = hidden.any { it.start <= start && end <= it.end }
            if (!alreadyHidden) {
                // 여는 태그는 커서를 뒤(안쪽)로, 닫는 태그는 앞으로 보내야 이어서 입력하기 자연스럽다
                hidden += HiddenRange(start, end, mapAfter = !match.value.startsWith("</"))
            }
        }
    }

    /** 연속된 인용 줄을 하나의 블록으로 병합 (iOS도 연속 인용을 한 덩어리로 그린다) */
    private fun mergeQuoteBlocks(lines: List<IntRange>, raw: String): List<IntRange> {
        if (lines.isEmpty()) return emptyList()

        val merged = mutableListOf<IntRange>()
        var current = lines.first()

        lines.drop(1).forEach { line ->
            // 빈 줄이나 다른 본문이 끼면 간격이 짧더라도 서로 다른 인용 블록이다.
            val gap = raw.substring(current.last, line.first)
            val isAdjacent = gap == "\n> " || gap == "\r\n> "
            current = if (isAdjacent) current.first..line.last else {
                merged += current
                line
            }
        }
        merged += current
        return merged
    }

    /** [regionStart, regionEnd) 구간의 인라인 토큰 수집. 내부 토큰은 재귀 처리 */
    private fun collectInline(
        raw: String,
        regionStart: Int,
        regionEnd: Int,
        inheritedDecorations: List<TextDecoration>,
        insideLink: Boolean,
        hidden: MutableList<HiddenRange>,
        spans: MutableList<SpanRange>,
        links: MutableList<LinkRange>,
    ) {
        if (regionStart >= regionEnd) return
        val region = raw.substring(regionStart, regionEnd)
        val failedTokens = HashSet<Long>()
        var searchFrom = 0

        while (searchFrom < region.length) {
            val token = findEarliestToken(
                region = region,
                from = searchFrom,
                detectPlainUrls = isReadOnly && !insideLink,
                failedTokens = failedTokens,
            )
            if (token == null) {
                collectPlainText(
                    raw = raw,
                    start = regionStart + searchFrom,
                    end = regionEnd,
                    insideLink = insideLink,
                    hidden = hidden,
                    spans = spans,
                    links = links,
                )
                break
            }

            if (token.start > searchFrom) {
                collectPlainText(
                    raw = raw,
                    start = regionStart + searchFrom,
                    end = regionStart + token.start,
                    insideLink = insideLink,
                    hidden = hidden,
                    spans = spans,
                    links = links,
                )
            }

            val openStart = regionStart + token.start
            val innerStart = openStart + token.openLength
            val innerEnd = regionStart + token.end - token.closeLength
            val closeEnd = regionStart + token.end

            when (token.kind) {
                InlineTokenKind.LINK -> {
                    val unescapedUrl = unescapeLinkUrl(token.argument.orEmpty()).trim()
                    if (isReadOnly) {
                        hidden += HiddenRange(openStart, innerStart, mapAfter = true)
                        hidden += HiddenRange(innerEnd, closeEnd, mapAfter = false)
                        val isAllowed = isAllowedLinkScheme(unescapedUrl)
                        val decorations = if (isAllowed) {
                            inheritedDecorations + token.kind.decorations
                        } else {
                            inheritedDecorations
                        }
                        if (isAllowed) {
                            links += LinkRange(unescapedUrl, innerStart, innerEnd)
                            token.kind.spanStyle(linkColor, decorations)?.let { style ->
                                spans += SpanRange(style, innerStart, innerEnd, preserveOnCode = true)
                            }
                        }
                        collectInline(
                            raw = raw,
                            regionStart = innerStart,
                            regionEnd = innerEnd,
                            inheritedDecorations = decorations,
                            insideLink = true,
                            hidden = hidden,
                            spans = spans,
                            links = links,
                        )
                    } else {
                        // 편집 모드에서는 URL 확인·편집이 가능하도록 마커를 숨기지 않고 흐리게 표시한다
                        spans += SpanRange(SpanStyle(color = markerColor), openStart, innerStart)
                        spans += SpanRange(SpanStyle(color = markerColor), innerEnd, closeEnd)
                        val decorations = inheritedDecorations + token.kind.decorations
                        token.kind.spanStyle(linkColor, decorations)?.let { style ->
                            spans += SpanRange(style, innerStart, innerEnd, preserveOnCode = true)
                        }
                        collectInline(
                            raw = raw,
                            regionStart = innerStart,
                            regionEnd = innerEnd,
                            inheritedDecorations = decorations,
                            insideLink = true,
                            hidden = hidden,
                            spans = spans,
                            links = links,
                        )
                    }
                }

                InlineTokenKind.CODE -> {
                    hidden += HiddenRange(openStart, innerStart, mapAfter = true)
                    hidden += HiddenRange(innerEnd, closeEnd, mapAfter = false)
                    // 바깥 인라인 서식은 코드 내부에 상속하지 않는다. 단락 서식과 링크는 유지한다.
                    val outerSpans = spans.toList()
                    spans.clear()
                    outerSpans.forEach { span ->
                        if (!span.isInline || span.end <= innerStart || span.start >= innerEnd) {
                            spans += span
                        } else {
                            if (span.start < innerStart) spans += span.copy(end = innerStart)
                            if (span.end > innerEnd) spans += span.copy(start = innerEnd)
                            if (span.preserveOnCode) {
                                spans += span.copy(
                                    style = span.style.copy(textDecoration = TextDecoration.Underline),
                                    start = maxOf(span.start, innerStart),
                                    end = minOf(span.end, innerEnd),
                                )
                            }
                        }
                    }
                    token.kind.spanStyle(linkColor, emptyList())?.let { style ->
                        spans += SpanRange(style, innerStart, innerEnd)
                    }
                    // 코드 스팬 내부는 마크다운 문법을 해석하지 않고 \x → x 해제만 적용한다
                    collectCodeUnescape(raw, innerStart, innerEnd, hidden)
                }

                else -> {
                    hidden += HiddenRange(openStart, innerStart, mapAfter = true)
                    hidden += HiddenRange(innerEnd, closeEnd, mapAfter = false)
                    val decorations = inheritedDecorations + token.kind.decorations
                    token.kind.spanStyle(linkColor, decorations, token.argument)?.let { style ->
                        spans += SpanRange(style, innerStart, innerEnd)
                    }
                    collectInline(
                        raw = raw,
                        regionStart = innerStart,
                        regionEnd = innerEnd,
                        inheritedDecorations = decorations,
                        insideLink = insideLink,
                        hidden = hidden,
                        spans = spans,
                        links = links,
                    )
                }
            }

            searchFrom = token.end
        }
    }

    /**
     * 인라인 토큰 사이의 일반 텍스트 구간을 처리한다.
     * - 읽기 전용 모드(링크 바깥)에서는 Plain URL(`https://...`, `http://...`)을 자동 링크화한다.
     * - 일반 텍스트의 15자 앞 백슬래시(`\`)만 선택적으로 숨겨 iOS 이스케이프와 호환한다.
     */
    private fun collectPlainText(
        raw: String,
        start: Int,
        end: Int,
        insideLink: Boolean,
        hidden: MutableList<HiddenRange>,
        spans: MutableList<SpanRange>,
        links: MutableList<LinkRange>,
    ) {
        if (start >= end) return

        if (!isReadOnly || insideLink) {
            collectPlainUnescape(raw, start, end, hidden)
            return
        }

        val segment = raw.substring(start, end)
        var cursor = 0
        PLAIN_URL_REGEX.findAll(segment).forEach { match ->
            val rawUrlStart = match.range.first
            val trimmedUrl = trimTrailingUrlPunctuation(unescapeMarkdownText(match.value))
            if (trimmedUrl.isEmpty()) return@forEach
            // 문장부호를 제외한 표시 길이를 원문 좌표로 환산해 '\.'의 '\'만 링크에 남지 않게 한다.
            var rawLength = 0
            repeat(trimmedUrl.length) {
                rawLength += if (match.value[rawLength] == '\\' && rawLength + 1 < match.value.length &&
                    match.value[rawLength + 1] in MARKDOWN_ESCAPABLE_CHARS) 2 else 1
            }
            val rawUrlEnd = rawUrlStart + rawLength

            if (rawUrlStart > cursor) {
                collectPlainUnescape(raw, start + cursor, start + rawUrlStart, hidden)
            }
            val absStart = start + rawUrlStart
            val absEnd = start + rawUrlEnd
            collectPlainUnescape(raw, absStart, absEnd, hidden)
            links += LinkRange(trimmedUrl, absStart, absEnd)
            spans += SpanRange(
                SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                absStart,
                absEnd,
            )
            cursor = rawUrlEnd
        }

        if (cursor < segment.length) {
            collectPlainUnescape(raw, start + cursor, end, hidden)
        }
    }

    /** 일반 텍스트 구간에서 iOS 기준 15자 앞의 `\`만 숨긴다 (`C:\Users` 등은 보존) */
    private fun collectPlainUnescape(
        raw: String,
        start: Int,
        end: Int,
        hidden: MutableList<HiddenRange>,
    ) {
        var i = start
        while (i < end - 1) {
            if (raw[i] == '\\' && raw[i + 1] in MARKDOWN_ESCAPABLE_CHARS) {
                hidden += HiddenRange(i, i + 1, mapAfter = true)
                i += 2
            } else {
                i++
            }
        }
    }

    /** 코드 스팬 내부에서는 집합 구분 없이 `\x` → `x`로 해제한다 */
    private fun collectCodeUnescape(
        raw: String,
        start: Int,
        end: Int,
        hidden: MutableList<HiddenRange>,
    ) {
        var i = start
        while (i < end - 1) {
            if (raw[i] == '\\') {
                hidden += HiddenRange(i, i + 1, mapAfter = true)
                i += 2
            } else {
                i++
            }
        }
    }

    private fun findEarliestToken(
        region: String,
        from: Int,
        detectPlainUrls: Boolean = false,
        failedTokens: MutableSet<Long> = HashSet(),
    ): InlineToken? {
        var best: InlineToken? = null
        var bestPriority = Int.MAX_VALUE
        // 이미 소비한 링크·코드의 URL은 제외하고, 일반 텍스트 구간에서 한 번만 검색한다.
        val plainUrls = if (detectPlainUrls) PLAIN_URL_REGEX.findAll(region, from).map { it.range }.toList() else emptyList()

        InlineTokenKind.entries.forEachIndexed { priority, kind ->
            if (isReadOnly && kind.isEmptyPair) return@forEachIndexed
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

    /** 숨김 범위를 시작 좌표 순으로 정렬하고 혹시 겹치는 구간이 있으면 정리한다 */
    private fun normalizeHiddenRanges(hidden: List<HiddenRange>): List<HiddenRange> {
        if (hidden.isEmpty()) return emptyList()
        val sorted = hidden.sortedWith(compareBy<HiddenRange> { it.start }.thenByDescending { it.end })
        val result = mutableListOf<HiddenRange>()
        var lastEnd = 0
        sorted.forEach { range ->
            if (range.start >= lastEnd && range.start < range.end) {
                result += range
                lastEnd = range.end
            }
        }
        return result
    }

    /** 숨김 범위를 제외한 문자열을 만들고, 치환을 덮어쓴 뒤 스타일과 링크를 변환 좌표로 적용 */
    private fun buildTransformed(
        raw: String,
        hidden: List<HiddenRange>,
        replaced: List<ReplacedRange>,
        spans: List<SpanRange>,
        links: List<LinkRange>,
        mapping: MarkdownOffsetMapping,
    ): AnnotatedString {
        val visible = StringBuilder(raw.length)
        var pos = 0
        hidden.forEach { range ->
            visible.append(raw, pos, range.start)
            pos = range.end
        }
        visible.append(raw, pos, raw.length)

        // 치환은 길이를 보존하므로 숨김 제거본 위에 그대로 덮어쓸 수 있다
        replaced.forEach { range ->
            val at = mapping.originalToTransformed(range.start)
            visible.replace(at, at + range.text.length, range.text)
        }

        val builder = AnnotatedString.Builder(visible.toString())
        spans.forEach { span ->
            val start = mapping.originalToTransformed(span.start)
            val end = mapping.originalToTransformed(span.end)
            if (start < end) {
                builder.addStyle(span.style, start, end)
            }
        }
        links.forEach { link ->
            val start = mapping.originalToTransformed(link.start)
            val end = mapping.originalToTransformed(link.end)
            if (start < end) {
                builder.addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = link.url,
                        linkInteractionListener = { onOpenUrl?.invoke(link.url) },
                    ),
                    start = start,
                    end = end,
                )
            }
        }
        return builder.toAnnotatedString()
    }

}

/** 파싱 결과. [quoteBlocks]는 표시 좌표 기준이라 TextLayoutResult에 그대로 쓸 수 있다 */
data class MarkdownRendered(
    val transformed: TransformedText,
    val quoteBlocks: List<IntRange>,
)

/**
 * 마크다운 렌더링 결과를 원문 1건에 대해 캐시한다.
 * 편집기(BasicTextField)는 키 입력마다 filter를 다시 호출하므로,
 * 인용구 세로선을 그리는 쪽과 파싱을 공유해서 중복 계산을 막는다
 */
class MarkdownRenderer(
    markerColor: Color,
    linkColor: Color,
    isReadOnly: Boolean = false,
    onOpenUrl: ((String) -> Unit)? = null,
) {

    private val transformation = MarkdownVisualTransformation(
        markerColor = markerColor,
        linkColor = linkColor,
        isReadOnly = isReadOnly,
        onOpenUrl = onOpenUrl,
    )

    private var cachedRaw: String? = null
    private var cached: MarkdownRendered? = null

    fun render(raw: String): MarkdownRendered {
        cached?.takeIf { cachedRaw == raw }?.let { return it }
        return transformation.parse(raw).also {
            cachedRaw = raw
            cached = it
        }
    }

    /** BasicTextField에 넘길 VisualTransformation. 그리기 쪽과 같은 캐시를 쓴다 */
    val visualTransformation = VisualTransformation { render(it.text).transformed }
}

/**
 * 인용 블록 왼쪽의 세로 막대를 그린다. BasicTextField와 Text 양쪽에서 동일하게 사용.
 * 텍스트를 밀지 않고 왼쪽 여백(음수 x)에 그리므로 화면 레이아웃은 그대로 유지된다
 */
fun DrawScope.drawMarkdownQuoteBars(
    layout: TextLayoutResult,
    quoteBlocks: List<IntRange>,
    color: Color,
    barWidth: Dp = 3.dp,
    barGap: Dp = 11.dp,
    cornerRadius: Dp = 1.5.dp,
) {
    if (quoteBlocks.isEmpty()) return

    val width = barWidth.toPx()
    val gap = barGap.toPx()
    val radius = cornerRadius.toPx()
    val lastOffset = layout.layoutInput.text.length

    quoteBlocks.forEach { block ->
        val start = block.first.coerceIn(0, lastOffset)
        val end = block.last.coerceIn(start, lastOffset)
        val firstLine = layout.getLineForOffset(start)
        val lastLine = layout.getLineForOffset(end)
        val top = layout.getLineTop(firstLine)
        val bottom = layout.getLineBottom(lastLine)

        drawRoundRect(
            color = color,
            topLeft = Offset(-(width + gap), top),
            size = Size(width, bottom - top),
            cornerRadius = CornerRadius(radius, radius),
        )
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
private fun trimTrailingUrlPunctuation(url: String): String {
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
private val PLAIN_URL_REGEX = Regex("""https?://[^\s<>]+""", RegexOption.IGNORE_CASE)
private val LEGACY_BOLD_WHITESPACE = Regex("""(?<!\*)\*\*(?!\*)((?:\\.|(?!\*\*)[^\\\n])+)\*\*(?!\*)""")
private val LEGACY_STRIKE_WHITESPACE = Regex("""~~((?:\\.|(?!~~)[^\\\n])+)~~""")

/** 짝이 깨져 화면에 드러날 수 있는 태그형 마커 */
private val ORPHAN_MARKER = Regex("""(?:<mark(?:\s+color="[^"]*")?>|</mark>|<u>|</u>)""")

/** 화면에서 제거되는 마커 범위. [mapAfter]는 경계 커서를 범위 뒤(토큰 안쪽)로 보낼지 여부 */
private data class HiddenRange(val start: Int, val end: Int, val mapAfter: Boolean)

/** 화면에서 다른 문자로 바뀌는 마커. 길이가 보존되어야 OffsetMapping이 항등으로 유지된다 */
private data class ReplacedRange(val start: Int, val end: Int, val text: String) {
    init {
        require(text.length == end - start) { "치환은 길이를 보존해야 합니다" }
    }
}

private data class SpanRange(
    val style: SpanStyle,
    val start: Int,
    val end: Int,
    val isInline: Boolean = true,
    val preserveOnCode: Boolean = false,
)
private data class LinkRange(val url: String, val start: Int, val end: Int)

/**
 * 숨김 범위 기준 원문 ↔ 표시 좌표 매핑.
 * 표시 좌표가 마커 경계에 걸치면 여는 마커는 뒤로, 닫는 마커는 앞으로 보내
 * 커서가 항상 토큰 안쪽에 놓이게 한다
 */
private class MarkdownOffsetMapping(
    private val originalLength: Int,
    private val hidden: List<HiddenRange>,
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        var removed = 0
        for (range in hidden) {
            if (offset <= range.start) break
            removed += minOf(offset, range.end) - range.start
        }
        return offset - removed
    }

    override fun transformedToOriginal(offset: Int): Int {
        var remaining = offset
        var original = 0
        for (range in hidden) {
            val keptBefore = range.start - original
            if (remaining < keptBefore || (remaining == keptBefore && !range.mapAfter)) {
                return original + remaining
            }
            remaining -= keptBefore
            original = range.end
        }
        return (original + remaining).coerceAtMost(originalLength)
    }
}

/**
 * 줄 단위 블록 문법. 판별 순서상 긴 prefix(###)가 먼저 와야 함.
 *
 * iOS는 대시(`– ` U+2013)와 번호 목록(`1. `)의 마커를 평문 그대로 남기므로
 * 별도 처리 없이 렌더링하면 결과가 일치한다
 */
private enum class BlockSpec(
    val prefix: String,
    val fontSize: TextUnit? = null,
    val fontWeight: FontWeight = FontWeight.Bold,
    /** 마커를 숨기는 대신 같은 길이의 문자로 바꿀 경우의 치환 문자열 */
    val replacement: String? = null,
) {
    TITLE3("### ", fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    TITLE2("## ", fontSize = 22.sp, fontWeight = FontWeight.Bold),
    TITLE1("# ", fontSize = 28.sp, fontWeight = FontWeight.Bold),
    QUOTE("> "),
    BULLET("- ", replacement = "• "),
}

/**
 * 인라인 토큰 종류. 선언 순서가 동일 위치에서의 우선순위 (iOS 파서와 동일).
 * EMPTY_*는 툴바로 삽입 직후의 빈 마커 쌍 — 편집 모드에서만 내용 없이 숨겨 커서만 남긴다
 */
private enum class InlineTokenKind(
    val regex: Regex,
    val decorations: List<TextDecoration> = emptyList(),
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
    LINK(MARKDOWN_LINK_REGEX, listOf(TextDecoration.Underline)),
    UNDERLINE(Regex("""<u>((?:\\.|[^\\\n])+?)</u>"""), listOf(TextDecoration.Underline)),
    STRIKETHROUGH(Regex("""~~"""), listOf(TextDecoration.LineThrough), closingMarker = "~~"),
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
        if (this == ITALIC_UNDERSCORE && (isIntrawordUnderscore(text, at) ||
                text.getOrNull(at - 1) == '_' || text.getOrNull(innerStart) == '_')) return fail()

        var cursor = innerStart
        while (cursor < text.length) {
            if (text[cursor] == '\n') return fail()
            if (text[cursor] == '\\' && cursor + 1 < text.length) {
                cursor += 2
                continue
            }
            if (this == ITALIC_UNDERSCORE && isIntrawordUnderscore(text, cursor)) {
                cursor++
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

    fun spanStyle(
        linkColor: Color,
        decorations: List<TextDecoration>,
        argument: String? = null,
    ): SpanStyle? {
        val decoration = decorations.takeIf { it.isNotEmpty() }?.let { TextDecoration.combine(it) }
        return when (this) {
            EMPTY_UNDERLINE, EMPTY_HIGHLIGHT, EMPTY_ITALIC_UNDERSCORE, EMPTY_STRIKETHROUGH,
            EMPTY_BOLD, EMPTY_BOLD_ITALIC -> null

            CODE -> SpanStyle(fontFamily = FontFamily.Monospace)
            HIGHLIGHT -> SpanStyle(background = parseMarkColor(argument))
            LINK -> SpanStyle(color = linkColor, textDecoration = decoration)
            UNDERLINE, STRIKETHROUGH -> SpanStyle(textDecoration = decoration)
            BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            ITALIC_ASTERISK, ITALIC_UNDERSCORE -> SpanStyle(fontStyle = FontStyle.Italic)
            BOLD_ITALIC_MIXED, BOLD_ITALIC_TRIPLE ->
                SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
        }
    }
}

/** iOS가 색상 없이 보낸 형광펜의 기본값 (노랑 40%) */
private val HIGHLIGHT_FALLBACK_COLOR = Color(1f, 1f, 0f, 0.4f)

/** `<mark color="R,G,B,A">`의 색상 코드. 각 값은 0~1 실수. 기존 7색 포함 모든 RGBA를 그대로 렌더링한다 */
internal fun parseMarkColor(code: String?): Color {
    val components = code?.split(',') ?: return HIGHLIGHT_FALLBACK_COLOR
    if (components.size != 4) return HIGHLIGHT_FALLBACK_COLOR
    val parts = components.map {
        it.trim().toFloatOrNull()?.takeIf { value -> value.isFinite() } ?: return HIGHLIGHT_FALLBACK_COLOR
    }
    return Color(
        red = parts[0].coerceIn(0f, 1f),
        green = parts[1].coerceIn(0f, 1f),
        blue = parts[2].coerceIn(0f, 1f),
        alpha = parts[3].coerceIn(0f, 1f),
    )
}

private data class InlineToken(
    val start: Int,
    val end: Int,
    val openLength: Int,
    val closeLength: Int,
    val kind: InlineTokenKind,
    val argument: String? = null,
)
