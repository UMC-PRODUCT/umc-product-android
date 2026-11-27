package com.umc.presentation.notice.write

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet

/** 툴바에서 활성 표시(main500)를 할 수 있는 마크다운 스타일 */
enum class MarkdownStyle {
    BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, HIGHLIGHT, CODE,
}

/** 열려 있는 마커의 여닫는 문자열 */
data class MarkdownMarker(val open: String, val close: String)

internal data class MarkdownOpenMarker(
    val open: String,
    val close: String,
    val start: Int,
    val isCode: Boolean,
    val closeStart: Int? = null,
)

data class MarkdownActiveStyles(
    val inline: ImmutableSet<MarkdownStyle> = persistentSetOf(),
    val isBullet: Boolean = false,
    val isQuote: Boolean = false,
    val heading: MarkdownHeading = MarkdownHeading.BODY,
) {
    operator fun contains(style: MarkdownStyle): Boolean = style in inline
}

/**
 * 마크다운 마커를 줄 단위로 스캔한다.
 *
 * [MarkdownVisualTransformation]의 파서는 줄 단위로만 인라인 토큰을 찾기 때문에
 * 토큰이 개행을 넘어가면 여닫는 마커가 화면에 그대로 드러난다.
 * 개행 시점에 열려 있는 마커를 모두 닫아 그 상황 자체를 만들지 않는다
 */
object MarkdownScanner {

    /** `<mark>`는 색상 인자가 없어도 유효하다 */
    private val MARK_OPEN = MARKDOWN_HIGHLIGHT_OPEN

    /** 여닫는 마커가 같은 문법. 긴 마커를 먼저 검사해야 `***`가 `**`로 잘리지 않는다 */
    private val SYMMETRIC = listOf(
        "***" to setOf(MarkdownStyle.BOLD, MarkdownStyle.ITALIC),
        "**" to setOf(MarkdownStyle.BOLD),
        "~~" to setOf(MarkdownStyle.STRIKETHROUGH),
        "*" to setOf(MarkdownStyle.ITALIC),
        "_" to setOf(MarkdownStyle.ITALIC),
        "`" to setOf(MarkdownStyle.CODE),
    )

    private val headingPrefixes = mapOf(
        "### " to MarkdownHeading.TITLE3,
        "## " to MarkdownHeading.TITLE2,
        "# " to MarkdownHeading.TITLE1,
    )

    /** 열려 있는 마커 하나. [close]는 이 마커를 닫는 문자열 */
    private data class OpenMarker(
        val marker: String,
        val close: String,
        val styles: Set<MarkdownStyle>,
        val start: Int,
        val closeStart: Int? = null,
    )

    internal fun openMarkersAt(text: String, at: Int): List<MarkdownOpenMarker> {
        val lineStart = text.lastIndexOf('\n', at - 1) + 1
        return openMarkers(text, lineStart, at).map {
            MarkdownOpenMarker(it.marker, it.close, it.start, MarkdownStyle.CODE in it.styles, it.closeStart)
        }
    }

    /** 커서 위치 기준 활성 스타일 */
    fun activeStyles(value: TextFieldValue): MarkdownActiveStyles {
        val text = value.text
        val at = value.selection.min.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', at - 1) + 1
        val line = text.substring(lineStart, text.indexOf('\n', lineStart).let {
            if (it == -1) text.length else it
        })

        val headingPrefix = headingPrefixes.keys.firstOrNull { line.startsWith(it) }
        val body = line.removePrefix(headingPrefix ?: "")
        val markers = openMarkers(text, lineStart, at)
        val active = markers.lastOrNull { MarkdownStyle.CODE in it.styles }?.let { listOf(it) } ?: markers

        return MarkdownActiveStyles(
            inline = active.flatMap { it.styles }.toImmutableSet(),
            isBullet = body.startsWith("- "),
            isQuote = body.startsWith("> "),
            heading = headingPrefix?.let { headingPrefixes[it] } ?: MarkdownHeading.BODY,
        )
    }

    /** [at]에서 열려 있는 마커를 닫는 문자열. 없으면 빈 문자열 */
    fun closersAt(text: String, at: Int): String {
        val lineStart = text.lastIndexOf('\n', at - 1) + 1
        // 안쪽 마커부터 닫아야 중첩이 어긋나지 않는다
        return openMarkers(text, lineStart, at).reversed().joinToString("") { it.close }
    }

    /** 커서 위치에서 [style]로 열려 있는 마커. 없으면 null */
    fun activeMarkerAt(value: TextFieldValue, style: MarkdownStyle): MarkdownMarker? {
        val text = value.text
        val at = value.selection.min.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', at - 1) + 1
        val open = openMarkers(text, lineStart, at).lastOrNull { style in it.styles } ?: return null
        return MarkdownMarker(open = open.marker, close = open.close)
    }

    /** 완성 토큰은 공통 파서로 판별하고, 토큰 사이의 미완성 입력만 보완한다. */
    private fun openMarkers(text: String, start: Int, end: Int): List<OpenMarker> {
        val open = mutableListOf<OpenMarker>()
        val lineEnd = text.indexOf('\n', start).let { if (it < 0) text.length else it }

        fun scanUnfinished(from: Int, until: Int) {
            var i = from

            // 미완성 구간의 끝을 넘는 마커는 아직 입력되지 않은 것으로 취급한다.
            fun fits(marker: String) = text.startsWith(marker, i) && i + marker.length <= until

            while (i < until) {
                if (isMarkdownEscaped(text, i)) {
                    i++
                    continue
                }

                if (open.any { MarkdownStyle.CODE in it.styles }) {
                    if (text[i] == '`') open.removeLastMatching { MarkdownStyle.CODE in it.styles }
                    i++
                    continue
                }

                val markOpen = MARK_OPEN.matchAt(text, i)?.value?.takeIf { i + it.length <= until }

                when {
                    markOpen != null -> {
                        open += OpenMarker(markOpen, "</mark>", setOf(MarkdownStyle.HIGHLIGHT), i)
                        i += markOpen.length
                    }

                    fits("</mark>") -> {
                        open.removeLastMatching { MarkdownStyle.HIGHLIGHT in it.styles }
                        i += "</mark>".length
                    }

                    fits("<u>") -> {
                        open += OpenMarker("<u>", "</u>", setOf(MarkdownStyle.UNDERLINE), i)
                        i += "<u>".length
                    }

                    fits("</u>") -> {
                        open.removeLastMatching { MarkdownStyle.UNDERLINE in it.styles }
                        i += "</u>".length
                    }

                    else -> {
                        val symmetric = SYMMETRIC.firstOrNull { fits(it.first) }
                        if (symmetric == null) {
                            i++
                        } else {
                            val (marker, styles) = symmetric
                            // 같은 마커가 이미 열려 있으면 이번 등장은 닫는 마커다
                            if (!open.removeLastMatching { it.marker == marker }) {
                                open += OpenMarker(marker, marker, styles, i)
                            }
                            i += marker.length
                        }
                    }
                }
            }
        }

        fun collect(from: Int, until: Int, tokens: List<InlineToken>) {
            var cursor = from
            for (token in tokens) {
                if (token.start >= until) break
                scanUnfinished(cursor, token.start)
                if (open.any { MarkdownStyle.CODE in it.styles }) {
                    scanUnfinished(token.start, minOf(token.end, until))
                } else if (until in token.innerStart..token.innerEnd) {
                    val styles = token.kind.editStyles
                    if (styles.isNotEmpty()) {
                        open += OpenMarker(
                            text.substring(token.start, token.innerStart),
                            text.substring(token.innerEnd, token.end),
                            styles, token.start, token.innerEnd,
                        )
                    }
                    if (token.kind != InlineTokenKind.CODE) collect(token.innerStart, until, token.children)
                    return
                } else if (until < token.end) {
                    // 숨겨진 닫는 마커 중간은 서식 본문에 포함하지 않는다.
                    return
                }
                cursor = token.end
                if (cursor >= until) return
            }
            scanUnfinished(cursor, until)
        }
        collect(start, end, MarkdownParser.parseInline(text, start, lineEnd))
        return open
    }

    /** 조건에 맞는 마지막 항목을 제거하고 제거 여부를 반환 */
    private fun MutableList<OpenMarker>.removeLastMatching(
        predicate: (OpenMarker) -> Boolean,
    ): Boolean {
        val index = indexOfLast(predicate)
        if (index == -1) return false
        removeAt(index)
        return true
    }
}

private val InlineTokenKind.editStyles: Set<MarkdownStyle>
    get() = when (this) {
        InlineTokenKind.BOLD, InlineTokenKind.EMPTY_BOLD -> setOf(MarkdownStyle.BOLD)
        InlineTokenKind.BOLD_ITALIC_MIXED, InlineTokenKind.BOLD_ITALIC_TRIPLE,
        InlineTokenKind.EMPTY_BOLD_ITALIC -> setOf(MarkdownStyle.BOLD, MarkdownStyle.ITALIC)
        InlineTokenKind.ITALIC_ASTERISK, InlineTokenKind.ITALIC_UNDERSCORE,
        InlineTokenKind.EMPTY_ITALIC_UNDERSCORE -> setOf(MarkdownStyle.ITALIC)
        InlineTokenKind.UNDERLINE, InlineTokenKind.EMPTY_UNDERLINE -> setOf(MarkdownStyle.UNDERLINE)
        InlineTokenKind.STRIKETHROUGH, InlineTokenKind.EMPTY_STRIKETHROUGH -> setOf(MarkdownStyle.STRIKETHROUGH)
        InlineTokenKind.HIGHLIGHT, InlineTokenKind.EMPTY_HIGHLIGHT -> setOf(MarkdownStyle.HIGHLIGHT)
        InlineTokenKind.CODE -> setOf(MarkdownStyle.CODE)
        InlineTokenKind.LINK -> emptySet()
    }
