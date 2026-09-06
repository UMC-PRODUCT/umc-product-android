package com.umc.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 온디바이스 모델이 실제로 자주 내놓는 형식 이탈을 모아 두고, 후처리가 이를 앱이
 * 렌더링할 수 있는 문법으로 되돌리는지 확인한다.
 */
class NoticeMarkdownSanitizerTest {

    /** 실제 관측되는 이탈 유형을 한 데 모은 표본. */
    private val samples = listOf(
        "```markdown\n# 정기 모임 공지\n내용입니다\n```",
        "다음은 요약입니다:\n\n# 정기 모임\n- 장소: 학생회관",
        "# 준비물\n1. 노트북\n2. 충전기\n3. 필기구",
        "# 일정\n\n---\n\n- 3월 2일 오후 7시",
        "# 참가자\n| 이름 | 파트 |\n|---|---|\n| 홍길동 | 안드로이드 |",
        "# 할 일\n- [ ] 자료 준비\n- [x] 장소 예약",
        "이번 모임은 ** 매우 중요 ** 합니다",
        "#제목입니다\n>인용입니다",
    )

    @Test
    fun `모델 출력의 형식 이탈을 전부 보정한다`() {
        var before = 0
        var after = 0
        samples.forEach {
            before += NoticeMarkdownSanitizer.countViolations(it)
            after += NoticeMarkdownSanitizer.countViolations(NoticeMarkdownSanitizer.sanitize(it))
        }
        println("형식 이탈 $before → $after")
        assertTrue("보정 전에는 이탈이 있어야 한다", before > 0)
        assertEquals("보정 후에는 이탈이 남지 않아야 한다", 0, after)
    }

    @Test
    fun `코드펜스와 머리말을 제거한다`() {
        assertEquals("# 정기 모임 공지\n내용입니다", NoticeMarkdownSanitizer.sanitize(samples[0]))
        assertEquals("# 정기 모임\n- 장소: 학생회관", NoticeMarkdownSanitizer.sanitize(samples[1]))
    }

    @Test
    fun `번호 목록과 체크박스를 지원하는 불릿으로 바꾼다`() {
        assertEquals("# 준비물\n- 노트북\n- 충전기\n- 필기구", NoticeMarkdownSanitizer.sanitize(samples[2]))
        assertEquals("# 할 일\n- 자료 준비\n- 장소 예약", NoticeMarkdownSanitizer.sanitize(samples[5]))
    }

    @Test
    fun `표는 불릿 한 줄로 펴고 수평선은 지운다`() {
        assertEquals("# 일정\n\n- 3월 2일 오후 7시", NoticeMarkdownSanitizer.sanitize(samples[3]))
        assertEquals(
            "# 참가자\n- 이름 · 파트\n- 홍길동 · 안드로이드",
            NoticeMarkdownSanitizer.sanitize(samples[4]),
        )
    }

    @Test
    fun `마커 안쪽 공백과 블록 마커 뒤 공백을 바로잡는다`() {
        assertEquals("이번 모임은 **매우 중요** 합니다", NoticeMarkdownSanitizer.sanitize(samples[6]))
        assertEquals("# 제목입니다\n> 인용입니다", NoticeMarkdownSanitizer.sanitize(samples[7]))
    }

    @Test
    fun `이미 올바른 본문은 바꾸지 않는다`() {
        val valid = "# 정기 모임\n\n- 일시: 3월 2일\n- 장소: 학생회관\n\n> 지각하지 마세요\n\n**필참**입니다"
        assertEquals(valid, NoticeMarkdownSanitizer.sanitize(valid))
        assertEquals(0, NoticeMarkdownSanitizer.countViolations(valid))
    }
}
