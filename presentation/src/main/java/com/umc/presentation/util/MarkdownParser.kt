package com.umc.presentation.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.umc.presentation.R

object MarkdownParser {

    private val numListRegex = Regex("^(\\d+\\.\\s)(.*)")

    private object InlinePatterns {
        val underline = Regex("<u>(.+?)</u>")
        val strikethrough = Regex("~~(.+?)~~")
        val highlight = Regex("<mark color=\"([^\"]+)\">(.+?)</mark>")
        val boldItalicTriple = Regex("\\*\\*\\*(.+?)\\*\\*\\*")
        val boldItalicMixed = Regex("\\*\\*_(.+?)_\\*\\*")
        val bold = Regex("\\*\\*(.+?)\\*\\*")
        val italicAsterisk = Regex("\\*(.+?)\\*")
        val italicUnderscore = Regex("_(.+?)_")
    }

    private object PlainTextPatterns {
        val underline = Regex("<u>(.*?)</u>")
        val strikethrough = Regex("~~(.*?)~~")
        val highlight = Regex("<mark[^>]*>(.*?)</mark>")
        val boldItalicTriple = Regex("\\*\\*\\*(.*?)\\*\\*\\*")
        val boldItalicMixed = Regex("\\*\\*_(.*?)_\\*\\*")
        val bold = Regex("\\*\\*(.*?)\\*\\*")
        val italicAsterisk = Regex("\\*(.*?)\\*")
        val italicUnderscore = Regex("_(.*?)_")
        val numList = Regex("^\\d+\\.\\s")
    }

    fun parse(text: String, context: Context): SpannableStringBuilder {
        val boldFont = ResourcesCompat.getFont(context, R.font.pretendard_bold) ?: Typeface.DEFAULT_BOLD
        val semiboldFont = ResourcesCompat.getFont(context, R.font.pretendard_semibold) ?: Typeface.DEFAULT_BOLD
        val displayMetrics = context.resources.displayMetrics
        val borderColor = ContextCompat.getColor(context, R.color.neutral300)
        val stripeWidthPx = (4 * displayMetrics.density + 0.5f).toInt()
        val gapWidthPx = (10 * displayMetrics.density + 0.5f).toInt()

        fun spToPx(sp: Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics).toInt()

        val result = SpannableStringBuilder()
        val lines = text.replace("\u200B", "").split("\n")

        for ((index, line) in lines.withIndex()) {
            if (index > 0) result.append("\n")
            val lineStart = result.length

            when {
                line.startsWith("### ") -> {
                    appendInline(result, line.removePrefix("### "), boldFont)
                    val end = result.length
                    result.setSpan(AbsoluteSizeSpan(spToPx(17f)), lineStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    result.setSpan(CustomTypefaceSpan(semiboldFont), lineStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                line.startsWith("## ") -> {
                    appendInline(result, line.removePrefix("## "), boldFont)
                    val end = result.length
                    result.setSpan(AbsoluteSizeSpan(spToPx(22f)), lineStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    result.setSpan(CustomTypefaceSpan(boldFont), lineStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                line.startsWith("# ") -> {
                    appendInline(result, line.removePrefix("# "), boldFont)
                    val end = result.length
                    result.setSpan(AbsoluteSizeSpan(spToPx(28f)), lineStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    result.setSpan(CustomTypefaceSpan(boldFont), lineStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                line.startsWith(">") -> {
                    val body = line.removePrefix(">").let {
                        val trimmed = if (it.startsWith(" ")) it.drop(1) else it
                        if (trimmed.isEmpty()) " " else trimmed
                    }
                    appendInline(result, body, boldFont)
                    val end = result.length
                    if (end > lineStart) {
                        result.setSpan(
                            BlockquoteLeftBorderSpan(borderColor, stripeWidthPx, gapWidthPx),
                            lineStart, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                line.startsWith("- ") -> {
                    result.append("• ")
                    appendInline(result, line.removePrefix("- "), boldFont)
                }
                line.startsWith("– ") -> {
                    result.append("– ")
                    appendInline(result, line.removePrefix("– "), boldFont)
                }
                else -> {
                    val numMatch = numListRegex.find(line)
                    if (numMatch != null) {
                        result.append(numMatch.groupValues[1])
                        appendInline(result, numMatch.groupValues[2], boldFont)
                    } else {
                        appendInline(result, line, boldFont)
                    }
                }
            }
        }

        return result
    }

    fun toPlainText(text: String): String {
        return text.replace("\u200B", "").lines().joinToString("\n") { line ->
            val body = when {
                line.startsWith("### ") -> line.removePrefix("### ")
                line.startsWith("## ") -> line.removePrefix("## ")
                line.startsWith("# ") -> line.removePrefix("# ")
                line.startsWith(">") -> line.removePrefix(">").trimStart()
                line.startsWith("- ") -> line.removePrefix("- ")
                line.startsWith("– ") -> line.removePrefix("– ")
                else -> line.replace(PlainTextPatterns.numList, "")
            }
            body
                .replace(PlainTextPatterns.underline, "$1")
                .replace(PlainTextPatterns.strikethrough, "$1")
                .replace(PlainTextPatterns.highlight, "$1")
                .replace(PlainTextPatterns.boldItalicTriple, "$1")
                .replace(PlainTextPatterns.boldItalicMixed, "$1")
                .replace(PlainTextPatterns.bold, "$1")
                .replace(PlainTextPatterns.italicAsterisk, "$1")
                .replace(PlainTextPatterns.italicUnderscore, "$1")
        }
    }

    private fun appendInline(ssb: SpannableStringBuilder, text: String, boldFont: Typeface) {
        ssb.append(parseInline(text, boldFont))
    }

    private fun parseInline(text: String, boldFont: Typeface): SpannableStringBuilder {
        if (text.isEmpty()) return SpannableStringBuilder()

        val token = findEarliestToken(text) ?: return SpannableStringBuilder(text)

        val ssb = SpannableStringBuilder()
        ssb.append(parseInline(text.substring(0, token.start), boldFont))
        val innerStart = ssb.length
        ssb.append(parseInline(token.inner, boldFont))
        val innerEnd = ssb.length

        when (token.kind) {
            TokenKind.BOLD ->
                ssb.setSpan(CustomTypefaceSpan(boldFont), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            TokenKind.ITALIC ->
                ssb.setSpan(SkewItalicSpan(), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            TokenKind.BOLD_ITALIC -> {
                ssb.setSpan(CustomTypefaceSpan(boldFont), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSpan(SkewItalicSpan(), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            TokenKind.STRIKETHROUGH ->
                ssb.setSpan(StrikethroughSpan(), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            TokenKind.UNDERLINE ->
                ssb.setSpan(UnderlineSpan(), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            TokenKind.HIGHLIGHT ->
                ssb.setSpan(BackgroundColorSpan(token.highlightColor ?: 0x66FFFF00), innerStart, innerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        ssb.append(parseInline(text.substring(token.end), boldFont))
        return ssb
    }

    private fun findEarliestToken(text: String): InlineToken? {
        data class Candidate(val token: InlineToken, val priority: Int)
        val candidates = mutableListOf<Candidate>()

        InlinePatterns.underline.find(text)?.let {
            candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[1], TokenKind.UNDERLINE), 1))
        }
        InlinePatterns.strikethrough.find(text)?.let {
            candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[1], TokenKind.STRIKETHROUGH), 2))
        }
        InlinePatterns.highlight.find(text)?.let {
            candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[2], TokenKind.HIGHLIGHT, parseMarkColor(it.groupValues[1])), 3))
        }
        listOf(InlinePatterns.boldItalicTriple, InlinePatterns.boldItalicMixed).forEach { regex ->
            regex.find(text)?.let {
                candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[1], TokenKind.BOLD_ITALIC), 4))
            }
        }
        InlinePatterns.bold.find(text)?.let {
            candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[1], TokenKind.BOLD), 5))
        }
        InlinePatterns.italicAsterisk.find(text)?.let {
            candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[1], TokenKind.ITALIC), 6))
        }
        InlinePatterns.italicUnderscore.find(text)?.let {
            candidates.add(Candidate(InlineToken(it.range.first, it.range.last + 1, it.groupValues[1], TokenKind.ITALIC), 7))
        }

        if (candidates.isEmpty()) return null
        val minStart = candidates.minOf { it.token.start }
        return candidates.filter { it.token.start == minStart }.minByOrNull { it.priority }?.token
    }

    private fun parseMarkColor(colorStr: String): Int {
        val parts = colorStr.split(",").mapNotNull { it.trim().toFloatOrNull() }
        return if (parts.size == 4) {
            Color.argb(
                (parts[3] * 255).toInt().coerceIn(0, 255),
                (parts[0] * 255).toInt().coerceIn(0, 255),
                (parts[1] * 255).toInt().coerceIn(0, 255),
                (parts[2] * 255).toInt().coerceIn(0, 255)
            )
        } else 0x66FFFF00
    }

    private data class InlineToken(
        val start: Int,
        val end: Int,
        val inner: String,
        val kind: TokenKind,
        val highlightColor: Int? = null
    )

    private enum class TokenKind { BOLD, ITALIC, BOLD_ITALIC, STRIKETHROUGH, UNDERLINE, HIGHLIGHT }
}

private class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) { ds.typeface = typeface }
    override fun updateMeasureState(textPaint: TextPaint) { textPaint.typeface = typeface }
}

private class SkewItalicSpan : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) { ds.textSkewX = -0.25f }
    override fun updateMeasureState(textPaint: TextPaint) { textPaint.textSkewX = -0.25f }
}

private class BlockquoteLeftBorderSpan(
    private val color: Int,
    private val stripeWidth: Int,
    private val gapWidth: Int
) : LeadingMarginSpan {

    override fun getLeadingMargin(first: Boolean): Int = stripeWidth + gapWidth

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout
    ) {
        val prevStyle = p.style
        val prevColor = p.color
        p.style = Paint.Style.FILL
        p.color = color
        c.drawRect(x.toFloat(), top.toFloat(), (x + dir * stripeWidth).toFloat(), bottom.toFloat(), p)
        p.style = prevStyle
        p.color = prevColor
    }
}
