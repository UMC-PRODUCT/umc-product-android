package com.umc.domain.usecase.ai

/**
 * 커뮤니티 스레드 카테고리를 규칙으로 먼저 판정한다.
 *
 * 분류는 네 값 중 하나를 고르는 좁은 문제여서, 대부분은 제목·본문의 신호어만으로 확정된다.
 * 그런데도 매번 온디바이스 모델을 부르면 (1) 미지원 기기에서는 항상 실패해 전부 기본값으로
 * 떨어지고 (2) 생성 파라미터를 고정하지 않아 같은 입력에 다른 답이 나올 수 있으며 (3) 추론
 * 대기 시간이 입력 흐름을 끊는다.
 *
 * 그래서 신호가 뚜렷하면 규칙이 확정하고, 애매한 경우에만 모델에 넘긴다.
 * 추론은 규칙이 못 푸는 나머지에만 쓰는 것이 목적이다.
 */
object CommunityCategoryRules {

    private val QNA = listOf(
        "질문", "궁금", "문의", "여쭤", "알려주", "어떻게", "왜", "가능한가요", "인가요", "인가여",
        "help", "?",
    )
    private val STUDY = listOf(
        "스터디", "공부", "study", "강의", "수업", "인강", "완강", "정리", "복습", "예습",
        "cs", "알고리즘", "코테", "코딩테스트", "면접준비",
    )
    private val PROJECT = listOf(
        "프로젝트", "project", "팀원", "팀빌딩", "모집", "사이드", "해커톤", "출시", "배포",
        "기획", "디자이너", "프론트", "백엔드", "협업",
    )

    /** 규칙만으로 확정할 수 있으면 카테고리를, 애매하면 null 을 돌려준다. */
    fun classify(title: String, description: String): String? {
        val text = (title + "\n" + description).lowercase()
        if (text.isBlank()) return "FREE"

        val qna = QNA.count { text.contains(it) }
        val study = STUDY.count { text.contains(it) }
        val project = PROJECT.count { text.contains(it) }

        val scores = listOf("QNA" to qna, "STUDY" to study, "PROJECT" to project)
        val best = scores.maxBy { it.second }
        if (best.second == 0) return null

        // 1위가 2위를 두 단계 이상 앞설 때만 확정한다. 신호가 비슷하면 모델에 넘긴다.
        val runnerUp = scores.filter { it.first != best.first }.maxOf { it.second }
        return if (best.second > runnerUp + 1) best.first else null
    }
}
