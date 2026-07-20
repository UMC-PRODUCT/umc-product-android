package com.umc.presentation.notice.write

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 마크다운 원문에서 마커 문자를 숨기고 스타일만 입혀 보여주는 VisualTransformation.
 * 화면에는 마커가 보이지 않지만 실제 TextFieldValue(서버 전송값)에는 그대로 남는다.
 * 제목 크기(# 28 / ## 22 / ### 17)는 iOS 렌더링 스펙과 동일.
 *
 * 커서가 숨겨진 마커 경계에 놓이면 토큰 "안쪽"으로 매핑해서
 * 제목 줄 맨 앞이나 굵게 단어 끝에서 이어서 타이핑해도 스타일이 유지되게 한다.
 * 링크(`[라벨](url)`)는 url 편집이 가능하도록 숨기지 않고 흐리게 표시만 한다
 */
class MarkdownVisualTransformation(
    private val markerColor: Color,
    private val linkColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val hidden = mutableListOf<HiddenRange>()
        val spans = mutableListOf<SpanRange>()

        parse(raw, hidden, spans)
        hidden.sortBy { it.start }

        val mapping = MarkdownOffsetMapping(originalLength = raw.length, hidden = hidden)
        return TransformedText(buildTransformed(raw, hidden, spans, mapping), mapping)
    }

    /** 줄 단위(제목) → 인라인 순서로 숨김 범위와 스타일 범위를 수집 */
    private fun parse(
        raw: String,
        hidden: MutableList<HiddenRange>,
        spans: MutableList<SpanRange>,
    ) {
        var lineStart = 0
        raw.split('\n').forEach { line ->
            val lineEnd = lineStart + line.length
            val heading = HeadingSpec.entries.firstOrNull { line.startsWith(it.prefix) }
            var contentStart = lineStart

            if (heading != null) {
                contentStart = lineStart + heading.prefix.length
                hidden += HiddenRange(lineStart, contentStart, mapAfter = true)
                spans += SpanRange(
                    SpanStyle(fontSize = heading.fontSize, fontWeight = FontWeight.Bold),
                    contentStart, lineEnd,
                )
            }

            collectInline(raw, contentStart, lineEnd, emptyList(), hidden, spans)
            lineStart = lineEnd + 1
        }
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
            token.kind.spanStyle(linkColor, decorations)?.let { style ->
                spans += SpanRange(style, innerStart, innerEnd)
            }
            collectInline(raw, innerStart, innerEnd, decorations, hidden, spans)

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

    /** 숨김 범위를 제외한 문자열을 만들고 스타일을 변환 좌표로 적용 */
    private fun buildTransformed(
        raw: String,
        hidden: List<HiddenRange>,
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
}

/** 화면에서 제거되는 마커 범위. [mapAfter]는 경계 커서를 범위 뒤(토큰 안쪽)로 보낼지 여부 */
private data class HiddenRange(val start: Int, val end: Int, val mapAfter: Boolean)

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

/** 제목 prefix와 렌더링 크기. 판별 순서상 긴 prefix(###)가 먼저 와야 함 */
private enum class HeadingSpec(val prefix: String, val fontSize: TextUnit) {
    TITLE3("### ", 17.sp),
    TITLE2("## ", 22.sp),
    TITLE1("# ", 28.sp),
}

/**
 * 인라인 토큰 종류. 선언 순서가 동일 위치에서의 우선순위 (iOS 파서와 동일).
 * EMPTY_*는 툴바로 삽입 직후의 빈 마커 쌍 — 내용 없이도 숨겨서 커서만 남긴다
 */
private enum class InlineTokenKind(
    val regex: Regex,
    val decorations: List<TextDecoration> = emptyList(),
) {
    EMPTY_UNDERLINE(Regex("<u></u>")),
    EMPTY_STRIKETHROUGH(Regex("~~~~")),
    EMPTY_BOLD(Regex("\\*\\*\\*\\*")),
    LINK(Regex("\\[(.+?)]\\((.+?)\\)"), listOf(TextDecoration.Underline)),
    UNDERLINE(Regex("<u>(.+?)</u>"), listOf(TextDecoration.Underline)),
    STRIKETHROUGH(Regex("~~(.+?)~~"), listOf(TextDecoration.LineThrough)),
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
            EMPTY_STRIKETHROUGH, EMPTY_BOLD -> {
                val half = (end - start) / 2
                InlineToken(start, end, openLength = half, closeLength = half, kind = this)
            }

            else -> {
                val innerRange = match.groups[1]?.range ?: return null
                InlineToken(
                    start = start,
                    end = end,
                    openLength = innerRange.first - start,
                    closeLength = match.range.last - innerRange.last,
                    kind = this,
                )
            }
        }
    }

    fun spanStyle(linkColor: Color, decorations: List<TextDecoration>): SpanStyle? {
        val decoration = decorations.takeIf { it.isNotEmpty() }?.let { TextDecoration.combine(it) }
        return when (this) {
            EMPTY_UNDERLINE, EMPTY_STRIKETHROUGH, EMPTY_BOLD -> null
            LINK -> SpanStyle(color = linkColor, textDecoration = decoration)
            UNDERLINE, STRIKETHROUGH -> SpanStyle(textDecoration = decoration)
            BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
            ITALIC_ASTERISK, ITALIC_UNDERSCORE -> SpanStyle(fontStyle = FontStyle.Italic)
            BOLD_ITALIC_MIXED, BOLD_ITALIC_TRIPLE ->
                SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
        }
    }
}

private data class InlineToken(
    val start: Int,
    val end: Int,
    val openLength: Int,
    val closeLength: Int,
    val kind: InlineTokenKind,
)
