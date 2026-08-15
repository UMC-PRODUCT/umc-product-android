package com.umc.presentation.notice.write

import androidx.compose.ui.text.input.TextFieldValue

/** 툴바에서 활성 표시(indigo500)를 할 수 있는 마크다운 스타일 */
enum class MarkdownStyle {
    BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, HIGHLIGHT, CODE,
}

/**
 * 커서 위치에서 켜져 있는 마크다운 상태.
 * 인라인 스타일은 마커 스캔으로, 줄 단위 스타일은 줄 prefix로 판단한다
 */
data class MarkdownActiveStyles(
    val inline: Set<MarkdownStyle> = emptySet(),
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
    private val MARK_OPEN = Regex("""<mark(?:\s+color="[^"]*")?>""")

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
    )

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

        return MarkdownActiveStyles(
            inline = openMarkers(text, lineStart, at).flatMap { it.styles }.toSet(),
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

    /** [style]이 켜져 있고 커서 바로 뒤에 닫는 마커가 붙어 있으면 그 마커의 길이 */
    fun closingMarkerLengthAt(value: TextFieldValue, style: MarkdownStyle): Int? {
        val text = value.text
        val at = value.selection.min.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', at - 1) + 1
        val open = openMarkers(text, lineStart, at).lastOrNull { style in it.styles } ?: return null
        return open.close.takeIf { text.startsWith(it, at) }?.length
    }

    /**
     * [start]부터 [end]까지 훑어 아직 닫히지 않은 마커를 바깥→안쪽 순서로 반환한다.
     * 같은 마커가 다시 나오면 닫힌 것으로 보고 목록에서 제거한다
     */
    private fun openMarkers(text: String, start: Int, end: Int): List<OpenMarker> {
        val open = mutableListOf<OpenMarker>()
        var i = start

        while (i < end) {
            val markOpen = MARK_OPEN.matchAt(text, i)

            when {
                markOpen != null -> {
                    open += OpenMarker(markOpen.value, "</mark>", setOf(MarkdownStyle.HIGHLIGHT))
                    i += markOpen.value.length
                }

                text.startsWith("</mark>", i) -> {
                    open.removeLastMatching { MarkdownStyle.HIGHLIGHT in it.styles }
                    i += "</mark>".length
                }

                text.startsWith("<u>", i) -> {
                    open += OpenMarker("<u>", "</u>", setOf(MarkdownStyle.UNDERLINE))
                    i += "<u>".length
                }

                text.startsWith("</u>", i) -> {
                    open.removeLastMatching { MarkdownStyle.UNDERLINE in it.styles }
                    i += "</u>".length
                }

                else -> {
                    val symmetric = SYMMETRIC.firstOrNull { text.startsWith(it.first, i) }
                    if (symmetric == null) {
                        i++
                    } else {
                        val (marker, styles) = symmetric
                        // 같은 마커가 이미 열려 있으면 이번 등장은 닫는 마커다
                        if (!open.removeLastMatching { it.marker == marker }) {
                            open += OpenMarker(marker, marker, styles)
                        }
                        i += marker.length
                    }
                }
            }
        }
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
