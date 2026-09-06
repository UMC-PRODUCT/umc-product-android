package com.umc.domain.usecase.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityCategoryRulesTest {

    /** 실제 커뮤니티에 올라올 법한 표본. 마지막 둘은 신호가 겹치거나 없는 경우. */
    private val samples = listOf(
        Triple("코테 스터디 모집", "매주 알고리즘 문제 풀이 같이 하실 분", "STUDY"),
        Triple("사이드 프로젝트 팀원 구합니다", "디자이너와 백엔드 모집 중이에요", "PROJECT"),
        Triple("Compose 질문있습니다", "리컴포지션이 왜 계속 일어나는지 궁금해요", "QNA"),
        Triple("CS 정리 같이 하실 분", "운영체제 복습 스터디 만들려고 합니다", "STUDY"),
        Triple("해커톤 나가실 분", "이번 주말 출시 목표로 팀빌딩 합니다", "PROJECT"),
        Triple("오늘 점심 뭐 먹지", "학식 추천 받아요", null),
        Triple("스터디 겸 프로젝트 하실 분", "공부도 하고 프로젝트도 만들어요", null),
    )

    @Test
    fun `신호가 뚜렷하면 규칙이 확정하고 애매하면 모델에 넘긴다`() {
        var resolved = 0
        samples.forEach { (title, desc, expected) ->
            val actual = CommunityCategoryRules.classify(title, desc)
            if (expected != null) {
                assertEquals("$title 는 $expected 로 확정돼야 한다", expected, actual)
                resolved++
            } else if (actual != null) {
                resolved++
            }
        }
        val rate = resolved * 100 / samples.size
        println("규칙 확정 ${resolved}/${samples.size} (${rate}%) — 나머지만 모델 호출")
        assertTrue("절반 이상은 규칙으로 확정돼야 한다", rate >= 50)
    }

    @Test
    fun `신호가 겹치면 확정하지 않는다`() {
        assertNull(CommunityCategoryRules.classify("스터디 겸 프로젝트 하실 분", "공부도 하고 프로젝트도 만들어요"))
    }

    @Test
    fun `빈 입력은 기본값으로 확정한다`() {
        assertEquals("FREE", CommunityCategoryRules.classify("", ""))
    }
}
