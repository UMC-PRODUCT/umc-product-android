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
 * 작성 중인 마크다운 원문에 스타일을 입혀 보여주는 VisualTransformation.
 * 토큰 문자는 지우지 않고 흐린 색으로 남겨 offset 매핑을 항등으로 유지한다.
 * 제목 크기(# 28 / ## 22 / ### 17)는 iOS 렌더링 스펙과 동일
 */
class MarkdownVisualTransformation(
    private val markerColor: Color,
    private val linkColor: Color,
) : VisualTransformation {

    private val markerStyle = SpanStyle(color = markerColor)

    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(styleMarkdown(text.text), OffsetMapping.Identity)

    private fun styleMarkdown(raw: String): AnnotatedString {
        val builder = AnnotatedString.Builder(raw)
        var lineStart = 0
        raw.split('\n').forEach { line ->
            val lineEnd = lineStart + line.length
            val heading = HeadingSpec.entries.firstOrNull { line.startsWith(it.prefix) }
            var contentStart = lineStart

            if (heading != null) {
                builder.addStyle(
                    SpanStyle(fontSize = heading.fontSize, fontWeight = FontWeight.Bold),
                    lineStart, lineEnd,
                )
                builder.addStyle(markerStyle, lineStart, lineStart + heading.prefix.length)
                contentStart = lineStart + heading.prefix.length
            }

            styleInline(builder, raw, contentStart, lineEnd, emptyList())
            lineStart = lineEnd + 1
        }
        return builder.toAnnotatedString()
    }

    /** [regionStart, regionEnd) 구간의 인라인 토큰을 찾아 스타일 적용. 내부 토큰은 재귀 처리 */
    private fun styleInline(
        builder: AnnotatedString.Builder,
        raw: String,
        regionStart: Int,
        regionEnd: Int,
        inheritedDecorations: List<TextDecoration>,
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

            builder.addStyle(markerStyle, openStart, innerStart)
            builder.addStyle(markerStyle, innerEnd, closeEnd)

            val decorations = inheritedDecorations + token.kind.decorations
            builder.addStyle(token.kind.spanStyle(linkColor, decorations), innerStart, innerEnd)
            styleInline(builder, raw, innerStart, innerEnd, decorations)

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
                val innerRange = match.groups[1]?.range ?: return@forEachIndexed
                best = InlineToken(
                    start = match.range.first,
                    end = match.range.last + 1,
                    openLength = innerRange.first - match.range.first,
                    closeLength = match.range.last - innerRange.last,
                    kind = kind,
                )
                bestPriority = priority
            }
        }
        return best
    }
}

/** 제목 prefix와 렌더링 크기. 판별 순서상 긴 prefix(###)가 먼저 와야 함 */
private enum class HeadingSpec(val prefix: String, val fontSize: TextUnit) {
    TITLE3("### ", 17.sp),
    TITLE2("## ", 22.sp),
    TITLE1("# ", 28.sp),
}

/** 인라인 토큰 종류. 선언 순서가 동일 위치에서의 우선순위 (iOS 파서와 동일) */
private enum class InlineTokenKind(
    val regex: Regex,
    val decorations: List<TextDecoration> = emptyList(),
) {
    LINK(Regex("\\[(.+?)]\\((.+?)\\)"), listOf(TextDecoration.Underline)),
    UNDERLINE(Regex("<u>(.+?)</u>"), listOf(TextDecoration.Underline)),
    STRIKETHROUGH(Regex("~~(.+?)~~"), listOf(TextDecoration.LineThrough)),
    BOLD_ITALIC_MIXED(Regex("\\*\\*_(.+?)_\\*\\*")),
    BOLD_ITALIC_TRIPLE(Regex("\\*\\*\\*(.+?)\\*\\*\\*")),
    BOLD(Regex("\\*\\*(.+?)\\*\\*")),
    ITALIC_ASTERISK(Regex("\\*(.+?)\\*")),
    ITALIC_UNDERSCORE(Regex("_(.+?)_"));

    fun spanStyle(linkColor: Color, decorations: List<TextDecoration>): SpanStyle {
        val decoration = decorations.takeIf { it.isNotEmpty() }?.let { TextDecoration.combine(it) }
        return when (this) {
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
