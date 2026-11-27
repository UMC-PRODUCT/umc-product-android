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

            val block = MarkdownBlock.at(line)
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

                if (block == MarkdownBlock.QUOTE) quoteLines += contentStart..lineEnd
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
        tokens: List<InlineToken> = MarkdownParser.parseInline(
            raw, regionStart, regionEnd, includeEmptyPairs = !isReadOnly,
            detectPlainUrls = isReadOnly, insideLink = insideLink,
        ),
    ) {
        var searchFrom = regionStart
        for (token in tokens) {
            collectPlainText(raw, searchFrom, token.start, insideLink, hidden, spans, links)
            val openStart = token.start
            val innerStart = token.innerStart
            val innerEnd = token.innerEnd
            val closeEnd = token.end

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
                            tokens = token.children,
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
                            tokens = token.children,
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
                        tokens = token.children,
                    )
                }
            }

            searchFrom = token.end
        }
        collectPlainText(raw, searchFrom, regionEnd, insideLink, hidden, spans, links)
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
private val MarkdownBlock.fontSize: TextUnit?
    get() = when (this) {
        MarkdownBlock.TITLE3 -> 17.sp
        MarkdownBlock.TITLE2 -> 22.sp
        MarkdownBlock.TITLE1 -> 28.sp
        else -> null
    }

private val MarkdownBlock.fontWeight: FontWeight
    get() = if (this == MarkdownBlock.TITLE3) FontWeight.SemiBold else FontWeight.Bold

private val InlineTokenKind.decorations: List<TextDecoration>
    get() = when (this) {
        InlineTokenKind.LINK, InlineTokenKind.UNDERLINE -> listOf(TextDecoration.Underline)
        InlineTokenKind.STRIKETHROUGH -> listOf(TextDecoration.LineThrough)
        else -> emptyList()
    }

private fun InlineTokenKind.spanStyle(
    linkColor: Color,
    decorations: List<TextDecoration>,
    argument: String? = null,
): SpanStyle? {
    val decoration = decorations.takeIf { it.isNotEmpty() }?.let { TextDecoration.combine(it) }
    return when (this) {
        InlineTokenKind.EMPTY_UNDERLINE, InlineTokenKind.EMPTY_HIGHLIGHT,
        InlineTokenKind.EMPTY_ITALIC_UNDERSCORE, InlineTokenKind.EMPTY_STRIKETHROUGH,
        InlineTokenKind.EMPTY_BOLD, InlineTokenKind.EMPTY_BOLD_ITALIC -> null
        InlineTokenKind.CODE -> SpanStyle(fontFamily = FontFamily.Monospace)
        InlineTokenKind.HIGHLIGHT -> SpanStyle(background = parseMarkColor(argument))
        InlineTokenKind.LINK -> SpanStyle(color = linkColor, textDecoration = decoration)
        InlineTokenKind.UNDERLINE, InlineTokenKind.STRIKETHROUGH -> SpanStyle(textDecoration = decoration)
        InlineTokenKind.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        InlineTokenKind.ITALIC_ASTERISK, InlineTokenKind.ITALIC_UNDERSCORE -> SpanStyle(fontStyle = FontStyle.Italic)
        InlineTokenKind.BOLD_ITALIC_MIXED, InlineTokenKind.BOLD_ITALIC_TRIPLE ->
            SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
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
