package com.umc.presentation.notice.write

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownVisualTransformationTest {

    @Test
    fun `레거시 정규화는 코드와 이스케이프된 마커를 보존한다`() {
        val raw = """`** a ** ~~ x ~~` **일시: ** \** 리터럴 \**"""
        assertEquals(
            """`** a ** ~~ x ~~` **일시:**  \** 리터럴 \**""",
            normalizeLegacyWhitespaceMarkers(raw),
        )
        assertEquals("** a ** ~~ x ~~", readOnlyParser().parse("`** a ** ~~ x ~~`").transformed.text.text)
        assertEquals("<u>태그</u>", editorParser().parse("`<u>태그</u>`").transformed.text.text)
    }

    @Test
    fun `코드는 바깥 인라인 서식을 무시하고 링크는 유지한다`() {
        val raw = "[<u>**`코드`**</u>](https://umc.dev)"
        val text = readOnlyParser().parse(raw).transformed.text
        assertEquals("코드", text.text)
        assertFalse(text.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(text.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
        assertEquals(1, text.getLinkAnnotations(0, text.length).size)
        assertFalse(text.spanStyles.any { it.item.textDecoration == TextDecoration.LineThrough })
    }

    @Test
    fun `백슬래시 홀짝과 이스케이프된 닫는 마커를 구분한다`() {
        val parser = editorParser()
        val even = parser.parse("""\\**굵게**""").transformed.text
        assertEquals("""\굵게""", even.text)
        assertTrue(even.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        val odd = parser.parse("""\**리터럴\**""").transformed.text
        assertEquals("**리터럴**", odd.text)
        assertTrue(odd.spanStyles.isEmpty())

        listOf(
            """**a\** b**""" to "a** b",
            """~~a\~~ b~~""" to "a~~ b",
            """*a\*b*""" to "a*b",
            """_a\_b_""" to "a_b",
            """***a\*** b***""" to "a*** b",
            """**_a\_** b_**""" to "a_** b",
            """<u>a\</u>b</u>""" to "a</u>b",
            """<mark>a\</mark>b</mark>""" to "a</mark>b",
        ).forEach { (raw, expected) ->
            assertEquals(raw, expected, readOnlyParser().parse(raw).transformed.text.text)
        }
    }

    @Test
    fun `공백 인접 복합 마커는 다른 서식으로 재해석하지 않는다`() {
        listOf("*** ***", "*** 강조 ***", "**_ _**", "**_ 강조 _**", "**강조 **").forEach { raw ->
            val text = editorParser().parse(raw).transformed.text
            assertEquals(raw, text.text)
            assertTrue(raw, text.spanStyles.isEmpty())
        }
    }

    private val markerColor = Color.Gray
    private val linkColor = Color.Blue

    private fun editorParser() = MarkdownVisualTransformation(
        markerColor = markerColor,
        linkColor = linkColor,
        isReadOnly = false,
    )

    private fun readOnlyParser(openedUrls: MutableList<String> = mutableListOf()) =
        MarkdownVisualTransformation(
            markerColor = markerColor,
            linkColor = linkColor,
            isReadOnly = true,
            onOpenUrl = { openedUrls += it },
        )

    @Test
    fun `형광펜 팔레트는 iOS 기준 5색과 opacity 0_35로 직렬화된다`() {
        assertEquals(
            listOf(
                MarkdownHighlightColor.PURPLE,
                MarkdownHighlightColor.PINK,
                MarkdownHighlightColor.ORANGE,
                MarkdownHighlightColor.MINT,
                MarkdownHighlightColor.BLUE,
            ),
            MarkdownHighlightColor.entries,
        )
        assertEquals("0.686,0.322,0.871,0.350", MarkdownHighlightColor.PURPLE.markColorCode)
        assertEquals("1.000,0.176,0.333,0.350", MarkdownHighlightColor.PINK.markColorCode)
        assertEquals("1.000,0.584,0.000,0.350", MarkdownHighlightColor.ORANGE.markColorCode)
        assertEquals("0.000,0.780,0.745,0.350", MarkdownHighlightColor.MINT.markColorCode)
        assertEquals("0.000,0.478,1.000,0.350", MarkdownHighlightColor.BLUE.markColorCode)
    }

    @Test
    fun `형광펜 읽기 파서는 기존 7색 RGBA와 fallback을 그대로 유지한다`() {
        val yellow = parseMarkColor("1.000,0.800,0.000,0.400")
        assertEquals(1.0f, yellow.red, 0.001f)
        assertEquals(0.8f, yellow.green, 0.001f)
        assertEquals(0.0f, yellow.blue, 0.001f)
        assertEquals(0.4f, yellow.alpha, 0.001f)

        val fallback = parseMarkColor(null)
        assertEquals(1.0f, fallback.red, 0.001f)
        assertEquals(1.0f, fallback.green, 0.001f)
        assertEquals(0.0f, fallback.blue, 0.001f)
        assertEquals(0.4f, fallback.alpha, 0.001f)
    }

    @Test
    fun `블록 8종 스타일과 H3 SemiBold 및 연속 인용구 병합을 검증한다`() {
        val raw = listOf(
            "# 제목",
            "## 머리말",
            "### 부머리말",
            "> 인용1",
            "> 인용2",
            "- 글머리",
            "– 대시",
            "1. 번호",
            "일반 본문",
        ).joinToString("\n")

        val rendered = readOnlyParser().parse(raw)
        val text = rendered.transformed.text

        assertEquals(
            listOf("제목", "머리말", "부머리말", "인용1", "인용2", "• 글머리", "– 대시", "1. 번호", "일반 본문")
                .joinToString("\n"),
            text.text,
        )
        assertTrue(text.spanStyles.any { it.item.fontSize == 28.sp && it.item.fontWeight == FontWeight.Bold })
        assertTrue(text.spanStyles.any { it.item.fontSize == 22.sp && it.item.fontWeight == FontWeight.Bold })
        assertTrue(text.spanStyles.any { it.item.fontSize == 17.sp && it.item.fontWeight == FontWeight.SemiBold })
        assertEquals(1, rendered.quoteBlocks.size)
    }

    @Test
    fun `이스케이프된 마커는 서식으로 해석되지 않고 15자 앞 백슬래시만 숨긴다`() {
        val raw = """\# 공지 1\. 안내 \*별표\* \(필수\) 10:00\~12:00 C:\Users"""
        val rendered = readOnlyParser().parse(raw)
        val text = rendered.transformed.text

        assertEquals("""# 공지 1. 안내 *별표* (필수) 10:00~12:00 C:\Users""", text.text)
        assertFalse(text.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
        assertFalse(text.spanStyles.any { it.item.fontSize == 28.sp })
    }

    @Test
    fun `코드 스팬 내부는 마크다운을 무시하고 모든 백슬래시 이스케이프를 해제한다`() {
        val raw = """`\*notItalic\* \`tick\` \a`"""
        val rendered = readOnlyParser().parse(raw)
        val text = rendered.transformed.text

        assertEquals("""*notItalic* `tick` a""", text.text)
        assertTrue(text.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
        assertFalse(text.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    }

    @Test
    fun `마커 안쪽 공백은 편집 모드에서 서식으로 인식하지 않고 읽기 모드에서는 레거시 공백 마커를 구제한다`() {
        val raw = "**일시: ** 및 ~~취소 ~~ 그리고 ** ** 와 2 * 3"

        val editorText = editorParser().parse(raw).transformed.text
        assertEquals(raw, editorText.text)
        assertFalse(editorText.spanStyles.any { it.item.fontWeight == FontWeight.Bold })

        val readOnlyText = readOnlyParser().parse(raw).transformed.text
        assertEquals("일시:  및 취소  그리고   와 2 * 3", readOnlyText.text)
        assertTrue(readOnlyText.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(readOnlyText.spanStyles.any { it.item.textDecoration == TextDecoration.LineThrough })
    }

    @Test
    fun `읽기 전용 모드에서 허용 스킴 링크만 클릭 가능하고 비허용 스킴은 라벨만 표시한다`() {
        val opened = mutableListOf<String>()
        val raw = """[**UMC**](https://umc.example.com/a\(1\)) [메일](mailto:test@umc.dev) [위험](javascript:alert(1))"""
        val rendered = readOnlyParser(opened).parse(raw)
        val text = rendered.transformed.text

        assertEquals("UMC 메일 위험", text.text)
        val links = text.getLinkAnnotations(0, text.length)
        assertEquals(2, links.size)
        assertEquals("https://umc.example.com/a(1)", (links[0].item as LinkAnnotation.Clickable).tag)
        assertEquals("mailto:test@umc.dev", (links[1].item as LinkAnnotation.Clickable).tag)
        assertTrue(text.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun `읽기 전용 모드에서 코드와 링크 밖의 Plain URL을 자동 링크화한다`() {
        val raw = "안내 https://umc.makeus.in/notice(1). 코드 `https://ignored.dev` 끝"
        val rendered = readOnlyParser().parse(raw)
        val text = rendered.transformed.text

        assertEquals("안내 https://umc.makeus.in/notice(1). 코드 https://ignored.dev 끝", text.text)
        val links = text.getLinkAnnotations(0, text.length)
        assertEquals(1, links.size)
        assertEquals("https://umc.makeus.in/notice(1)", (links[0].item as LinkAnnotation.Clickable).tag)
    }

    @Test
    fun `빈 줄 1대1 보존과 CRLF 개행 정규화 및 OffsetMapping 안전성을 검증한다`() {
        val raw = "첫줄\r\n\r\n\r\n둘째줄"
        val readOnlyText = readOnlyParser().parse(raw).transformed.text
        assertEquals("첫줄\n\n\n둘째줄", readOnlyText.text)

        val editorTransformed = editorParser().parse(raw).transformed
        assertEquals("첫줄\n\n\n둘째줄", editorTransformed.text.text)
        for (i in 0..raw.length) {
            val mapped = editorTransformed.offsetMapping.originalToTransformed(i)
            assertTrue(mapped in 0..editorTransformed.text.length)
        }
        for (i in 0..editorTransformed.text.length) {
            val mapped = editorTransformed.offsetMapping.transformedToOriginal(i)
            assertTrue(mapped in 0..raw.length)
        }
    }
}
