package com.umc.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 런타임 확인 스크립트(.claude/skills/run-umc-app)가 읽는 feature map 이 실제 화면 정의와 맞는지 확인한다.
 * 화면을 추가·삭제하거나 버튼 글자를 바꾸고 표를 고치지 않으면 여기서 실패한다.
 */
class FeatureMapTest {

    private val root: File = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, FEATURE_MAP).exists() }

    private val rows: List<Map<String, String>> = parseTable(File(root, FEATURE_MAP).readText())

    @Test
    fun `feature map 의 Route 는 MainDestination 의 화면과 같다`() {
        val declared = MainDestination::class.java.declaredClasses
            .filter { MainDestination::class.java.isAssignableFrom(it) }
            .map { it.simpleName }
            .toSet()
        val mapped = rows.map { it.getValue("Route") }.toSet()
        assertEquals(
            "feature-map.md 에 없는 화면: ${declared - mapped}, MainDestination 에 없는 행: ${mapped - declared}",
            declared,
            mapped,
        )
        assertEquals("Route 가 중복된 행이 있습니다", rows.size, mapped.size)
    }

    @Test
    fun `진입 경로의 탭·버튼 글자는 presentation 소스에 있다`() {
        val sources = File(root, "presentation").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && "/src/main/" in it.invariantSeparatorsPath }
            .joinToString("\n") { it.readText() }
        val missing = rows.flatMap { row ->
            row.getValue("진입").split(">").map { it.trim() }
                .filter { it.startsWith("tab:") || it.startsWith("tap:") }
                .flatMap { it.substringAfter(':').split(" / ").map(String::trim) }
                .filterNot { it.startsWith("{") }
                .filterNot { "\"$it\"" in sources }
                .map { "${row.getValue("Route")}: $it" }
        }
        assertTrue("소스에서 찾지 못한 진입 글자(feature-map.md 를 고치세요): $missing", missing.isEmpty())
    }

    private fun parseTable(markdown: String): List<Map<String, String>> {
        var header: List<String>? = null
        val result = mutableListOf<Map<String, String>>()
        for (line in markdown.lines()) {
            if (!line.startsWith("|")) {
                header = null
                continue
            }
            val cells = line.trim().trim('|').split('|').map { it.trim().trim('`') }
            when {
                cells.first() == "Route" -> header = cells
                header == null || cells.joinToString("").all { it in "-: " } -> Unit
                else -> result += header.zip(cells).toMap()
            }
        }
        return result
    }

    private companion object {
        const val FEATURE_MAP = ".claude/skills/run-umc-app/feature-map.md"
    }
}
