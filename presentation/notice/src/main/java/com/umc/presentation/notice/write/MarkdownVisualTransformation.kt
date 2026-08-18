package com.umc.presentation.notice.write

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
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
 * 문법은 iOS(umc-product-iOS)의 공지 에디터 파서와 일치시킨다.
 *
 * 커서가 숨겨진 마커 경계에 놓이면 토큰 "안쪽"으로 매핑해서
 * 제목 줄 맨 앞이나 굵게 단어 끝에서 이어서 타이핑해도 스타일이 유지된다.
 * 링크(`[라벨](url)`)는 url 편집이 가능하도록 숨기지 않고 흐리게 표시만 한다
 */
class MarkdownVisualTransformation(
    private val markerColor: Color,
    private val linkColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText = parse(text.text).transformed

    /** 표시 문자열과 함께 인용 블록 좌표까지 계산 (인용구 세로선을 그리려면 좌표가 필요) */
    fun parse(raw: String): MarkdownRendered {
        val hidden = mutableListOf<HiddenRange>()
        val replaced = mutableListOf<ReplacedRange>()
        val spans = mutableListOf<SpanRange>()
        val quoteLines = mutableListOf<IntRange>()

        collectBlocks(raw, hidden, replaced, spans, quoteLines)
        hideOrphanMarkers(raw, hidden)
        hidden.sortBy { it.start }

        val mapping = MarkdownOffsetMapping(originalLength = raw.length, hidden = hidden)
        val transformed = TransformedText(
            buildTransformed(raw, hidden, replaced, spans, mapping),
            mapping,
        )

        return MarkdownRendered(
            transformed = transformed,
            quoteBlocks = mergeQuoteBlocks(quoteLines).map { block ->
                mapping.originalToTransformed(block.first)..mapping.originalToTransformed(block.last)
            },
        )
    }

    /** 줄 단위 블록 → 인라인 순서로 숨김/치환/스타일 범위를 수집 */
    private fun collectBlocks(
        raw: String,
        hidden: MutableList<HiddenRange>,
        replaced: MutableList<ReplacedRange>,
        spans: MutableList<SpanRange>,
        quoteLines: MutableList<IntRange>,
    ) {
        var lineStart = 0
        raw.split('\n').forEach { line ->
            val lineEnd = lineStart + line.length
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
                        SpanStyle(fontSize = fontSize, fontWeight = FontWeight.Bold),
                        contentStart, lineEnd,
                    )
                }

                if (block == BlockSpec.QUOTE) quoteLines += contentStart..lineEnd
            }

            collectInline(raw, contentStart, lineEnd, emptyList(), hidden, spans)
            lineStart = lineEnd + 1
        }
    }

    /**
     * 토큰으로 짝이 맞지 않아 남은 마커를 숨긴다.
     *
     * 글자를 지우다 한쪽 마커만 남으면(`<mark ...>가나` 처럼) 토큰이 성립하지 않아
     * 마커가 화면에 그대로 드러난다. 에디터에는 어떤 경우에도 마커가 보이면 안 되므로
     * 마지막에 한 번 훑어 남은 것을 걷어낸다.
     *
     * 별표·물결 같은 짧은 기호는 사용자가 일부러 쓴 글자일 수 있어 건드리지 않고,
     * 본문에 쓸 일이 없는 태그형 마커만 대상으로 한다
     */
    private fun hideOrphanMarkers(raw: String, hidden: MutableList<HiddenRange>) {
        ORPHAN_MARKER.findAll(raw).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val alreadyHidden = hidden.any { it.start <= start && end <= it.end }
            if (!alreadyHidden) {
                // 여는 태그는 커서를 뒤(안쪽)로, 닫는 태그는 앞으로 보내야 이어서 입력하기 자연스럽다
                hidden += HiddenRange(start, end, mapAfter = !match.value.startsWith("</"))
            }
        }
    }

    /** 연속된 인용 줄을 하나의 블록으로 병합 (iOS도 연속 인용을 한 덩어리로 그린다) */
    private fun mergeQuoteBlocks(lines: List<IntRange>): List<IntRange> {
        if (lines.isEmpty()) return emptyList()

        val merged = mutableListOf<IntRange>()
        var current = lines.first()

        lines.drop(1).forEach { line ->
            // 원문에서 바로 다음 줄이면(개행 1개 차이) 이어붙인다. prefix 길이만큼 간격이 있을 수 있음
            val isAdjacent = line.first > current.last && line.first - current.last <= MAX_QUOTE_GAP
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
        hidden: MutableList<HiddenRange>,
        spans: MutableList<SpanRange>,
    ) {
        if (regionStart >= regionEnd) return
        val region = raw.substring(regionStart, regionEnd)
        var searchFrom = 0

        while (searchFrom < region.length) {
            val token = findEarliestToken(region, searchFrom) ?: break
            val openStart = regionStart + token.start
            val innerStart = openStart + token.openLength
            val innerEnd = regionStart + token.end - token.closeLength
            val closeEnd = regionStart + token.end

            if (token.kind == InlineTokenKind.LINK) {
                // 링크는 마커를 숨기지 않고 흐리게 표시 (url 확인/편집 가능해야 함)
                spans += SpanRange(SpanStyle(color = markerColor), openStart, innerStart)
                spans += SpanRange(SpanStyle(color = markerColor), innerEnd, closeEnd)
            } else {
                hidden += HiddenRange(openStart, innerStart, mapAfter = true)
                hidden += HiddenRange(innerEnd, closeEnd, mapAfter = false)
            }

            val decorations = inheritedDecorations + token.kind.decorations
            token.kind.spanStyle(linkColor, decorations, token.argument)?.let { style ->
                spans += SpanRange(style, innerStart, innerEnd)
            }

            // 코드 스팬 내부는 리터럴 — 다른 마크다운 문법을 해석하지 않는다
            if (token.kind != InlineTokenKind.CODE) {
                collectInline(raw, innerStart, innerEnd, decorations, hidden, spans)
            }

            searchFrom = token.end
        }
    }

    private fun findEarliestToken(region: String, from: Int): InlineToken? {
        var best: InlineToken? = null
        var bestPriority = Int.MAX_VALUE

        InlineTokenKind.entries.forEachIndexed { priority, kind ->
            val match = kind.regex.find(region, from) ?: return@forEachIndexed
            val current = best
            if (current == null || match.range.first < current.start ||
                (match.range.first == current.start && priority < bestPriority)
            ) {
                val token = kind.toToken(match) ?: return@forEachIndexed
                best = token
                bestPriority = priority
            }
        }
        return best
    }

    /** 숨김 범위를 제외한 문자열을 만들고, 치환을 덮어쓴 뒤 스타일을 변환 좌표로 적용 */
    private fun buildTransformed(
        raw: String,
        hidden: List<HiddenRange>,
        replaced: List<ReplacedRange>,
        spans: List<SpanRange>,
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
            builder.addStyle(
                span.style,
                mapping.originalToTransformed(span.start),
                mapping.originalToTransformed(span.end),
            )
        }
        return builder.toAnnotatedString()
    }

    private companion object {
        /** 인용 줄 병합 시 허용하는 원문 간격 (개행 1 + prefix 2) */
        const val MAX_QUOTE_GAP = 3
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
class MarkdownRenderer(markerColor: Color, linkColor: Color) {

    private val transformation = MarkdownVisualTransformation(markerColor, linkColor)

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
private val ORPHAN_MARKER = Regex("""<mark(?:\s+color="[^"]*")?>|</mark>|<u>|</u>""")

/** 화면에서 제거되는 마커 범위. [mapAfter]는 경계 커서를 범위 뒤(토큰 안쪽)로 보낼지 여부 */
private data class HiddenRange(val start: Int, val end: Int, val mapAfter: Boolean)

/** 화면에서 다른 문자로 바뀌는 마커. 길이가 보존되어야 OffsetMapping이 항등으로 유지된다 */
private data class ReplacedRange(val start: Int, val end: Int, val text: String) {
    init {
        require(text.length == end - start) { "치환은 길이를 보존해야 합니다" }
    }
}

private data class SpanRange(val style: SpanStyle, val start: Int, val end: Int)

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
 * 별도 처리 없이 지금처럼 렌더링하면 결과가 일치한다
 */
private enum class BlockSpec(
    val prefix: String,
    val fontSize: TextUnit? = null,
    /** 마커를 숨기는 대신 같은 길이의 문자로 바꿀 경우의 치환 문자열 */
    val replacement: String? = null,
) {
    TITLE3("### ", fontSize = 17.sp),
    TITLE2("## ", fontSize = 22.sp),
    TITLE1("# ", fontSize = 28.sp),
    QUOTE("> "),
    BULLET("- ", replacement = "• "),
}

/**
 * 인라인 토큰 종류. 선언 순서가 동일 위치에서의 우선순위 (iOS 파서와 동일).
 * EMPTY_*는 툴바로 삽입 직후의 빈 마커 쌍 — 내용 없이도 숨겨서 커서만 남긴다
 */
private enum class InlineTokenKind(
    val regex: Regex,
    val decorations: List<TextDecoration> = emptyList(),
    /** 본문에 해당하는 정규식 그룹 번호 (형광펜은 색상이 1번이라 2번이 본문) */
    val innerGroup: Int = 1,
) {
    EMPTY_UNDERLINE(Regex("<u></u>")),
    // 형광펜 안 글자를 모두 지운 경우. 마커가 길어 그대로 드러나면 가장 눈에 띈다
    EMPTY_HIGHLIGHT(Regex("""<mark(?:\s+color="[^"]*")?></mark>""")),
    // 밑줄 기울임 빈 쌍 (`_` + `_`)
    EMPTY_ITALIC_UNDERSCORE(Regex("__")),
    EMPTY_STRIKETHROUGH(Regex("~~~~")),
    // 굵게+기울임 빈 쌍(`***` + `***`). 굵게 빈 쌍보다 먼저 잡아야 뒤 두 개가 남지 않는다
    EMPTY_BOLD_ITALIC(Regex("\\*{6}")),
    EMPTY_BOLD(Regex("\\*\\*\\*\\*")),
    CODE(Regex("(?<!\\\\)`((?:\\\\.|[^`\\n])+?)`")),
    LINK(Regex("\\[(.+?)]\\((.+?)\\)"), listOf(TextDecoration.Underline)),
    UNDERLINE(Regex("<u>(.+?)</u>"), listOf(TextDecoration.Underline)),
    STRIKETHROUGH(Regex("~~(.+?)~~"), listOf(TextDecoration.LineThrough)),
    HIGHLIGHT(Regex("<mark(?:\\s+color=\"([^\"]*)\")?>(.+?)</mark>"), innerGroup = 2),
    BOLD_ITALIC_MIXED(Regex("\\*\\*_(.+?)_\\*\\*")),
    BOLD_ITALIC_TRIPLE(Regex("\\*\\*\\*(.+?)\\*\\*\\*")),
    BOLD(Regex("\\*\\*(.+?)\\*\\*")),
    ITALIC_ASTERISK(Regex("\\*(.+?)\\*")),
    ITALIC_UNDERSCORE(Regex("_(.+?)_"));

    /** 매치 결과를 마커 길이가 계산된 토큰으로 변환 */
    fun toToken(match: MatchResult): InlineToken? {
        val start = match.range.first
        val end = match.range.last + 1
        return when (this) {
            EMPTY_UNDERLINE -> InlineToken(start, end, openLength = 3, closeLength = 4, kind = this)
            // 여는 태그 길이는 색상 유무에 따라 달라지므로 전체에서 `</mark>`를 뺀 만큼
            EMPTY_HIGHLIGHT -> InlineToken(
                start, end,
                openLength = (end - start) - 7,
                closeLength = 7,
                kind = this,
            )
            EMPTY_ITALIC_UNDERSCORE, EMPTY_STRIKETHROUGH, EMPTY_BOLD, EMPTY_BOLD_ITALIC -> {
                val half = (end - start) / 2
                InlineToken(start, end, openLength = half, closeLength = half, kind = this)
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

/** `<mark color="R,G,B,A">`의 색상 코드. 각 값은 0~1 실수 */
private fun parseMarkColor(code: String?): Color {
    val parts = code?.split(',')?.mapNotNull { it.toFloatOrNull() } ?: return HIGHLIGHT_FALLBACK_COLOR
    if (parts.size != 4) return HIGHLIGHT_FALLBACK_COLOR
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
