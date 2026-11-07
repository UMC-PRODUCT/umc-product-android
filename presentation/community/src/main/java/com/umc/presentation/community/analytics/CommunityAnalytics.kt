package com.umc.presentation.community.analytics

import com.umc.domain.model.community.thread.CommunityMessageType

/**
 * 커뮤니티 이용 지표 이벤트(#354).
 *
 * 이름과 파라미터는 iOS(AppAnalytics.track)와 같다. iOS 에 없는 것은 AI 자동 분류와 목록 접속뿐이다.
 * 값은 iOS 처럼 문자열이다(Boolean 은 "true"/"false", 카테고리는 서버 값의 소문자 study·qna·project·free).
 */
internal object CommunityAnalytics {
    // 1. 스레드 생성·삭제
    const val THREAD_CREATE_ATTEMPTED = "community_thread_create_attempted"
    const val THREAD_CREATED = "community_thread_created"
    const val THREAD_CREATE_FAILED = "community_thread_create_failed"
    /** 내 삭제 요청의 HTTP 성공 응답에서 한 번(채팅·수정 화면). 소켓 thread.deleted 로는 세지 않는다 */
    const val THREAD_DELETED = "community_thread_deleted"

    // 1. AI 카테고리 자동 분류 (iOS 미전송, Android 에서 새로 정함)
    const val AI_CLASSIFY_ATTEMPTED = "community_ai_classify_attempted"
    const val AI_CLASSIFY_COMPLETED = "community_ai_classify_completed"
    const val AI_CLASSIFY_FAILED = "community_ai_classify_failed"

    // 2. 커뮤니티 접속·메시지
    /** 커뮤니티 탭(스레드 목록)을 볼 때마다. 목록 접속은 iOS 미전송 */
    const val LIST_VIEWED = "community_list_viewed"
    /** 채팅방 화면마다 첫 로드가 성공했을 때 한 번 */
    const val THREAD_VIEWED = "community_thread_viewed"
    /** 내가 보낸 메시지를 서버가 확인했을 때(ack·생성 이벤트) 메시지당 한 번. 확인 전에 채팅방을 나가면 세지 않는다 */
    const val MESSAGE_SENT = "community_message_sent"

    // 3. AI 대화 요약
    const val SUMMARY_ATTEMPTED = "community_summary_attempted"
    const val SUMMARY_COMPLETED = "community_summary_completed"
    const val SUMMARY_FAILED = "community_summary_failed"

    const val PARAM_CATEGORY = "category"
    const val PARAM_HAS_INVITEES = "has_invitees"
    const val PARAM_IS_JOINED = "is_joined"
    const val PARAM_MESSAGE_TYPE = "message_type"
    const val PARAM_HAS_REPLY = "has_reply"
    const val PARAM_HAS_MENTION = "has_mention"
    /** AI 분류를 연 방법: auto(입력 후 자동) · manual(분류하기·다시 분류하기 버튼) */
    const val PARAM_TRIGGER = "trigger"
    /** AI 분류 방식: rule(키워드 규칙으로 확정, 모델 미호출) · model(기기 AI 모델) */
    const val PARAM_METHOD = "method"

    const val TRIGGER_AUTO = "auto"
    const val TRIGGER_MANUAL = "manual"
    const val METHOD_RULE = "rule"
    const val METHOD_MODEL = "model"

    /** 서버 카테고리 값(STUDY·QNA·PROJECT·FREE)을 iOS 와 같은 소문자로 바꾼다. 알 수 없는 값은 iOS 처럼 free 로 둔다 */
    fun categoryValue(serverCategory: String): String =
        serverCategory.trim().uppercase()
            .takeIf { it in KNOWN_CATEGORIES }
            ?.lowercase()
            ?: "free"

    fun threadCreatedParams(category: String, hasInvitees: Boolean): Map<String, String> = mapOf(
        PARAM_CATEGORY to categoryValue(category),
        PARAM_HAS_INVITEES to hasInvitees.toString(),
    )

    fun threadViewedParams(category: String, isJoined: Boolean): Map<String, String> = mapOf(
        PARAM_CATEGORY to categoryValue(category),
        PARAM_IS_JOINED to isJoined.toString(),
    )

    fun messageSentParams(type: CommunityMessageType, hasReply: Boolean, hasMention: Boolean): Map<String, String> = mapOf(
        PARAM_MESSAGE_TYPE to if (type == CommunityMessageType.IMAGE) "image" else "text",
        PARAM_HAS_REPLY to hasReply.toString(),
        PARAM_HAS_MENTION to hasMention.toString(),
    )

    fun classifyParams(trigger: String, method: String, category: String? = null): Map<String, String> = buildMap {
        put(PARAM_TRIGGER, trigger)
        put(PARAM_METHOD, method)
        if (category != null) put(PARAM_CATEGORY, categoryValue(category))
    }

    private val KNOWN_CATEGORIES = setOf("STUDY", "QNA", "PROJECT", "FREE")
}
