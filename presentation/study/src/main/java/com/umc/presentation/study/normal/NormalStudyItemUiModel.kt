package com.umc.presentation.study.normal

import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.domain.model.enums.WorkbookMissionType

/**
 * 스터디/활동 화면의 각 주차별 아이템 UI 모델
 *
 * @param id 워크북 원본 ID (서버 식별자)
 * @param platform 제출 플랫폼 (Github, Notion 등)
 * @param title 주차 제목
 * @param status 학습 상태 (PASS, FAIL, IN_PROGRESS)
 * @param week 주차 번호
 * @param isExpanded 카드 펼침 여부
 * @param link 제출 링크
 * @param submitState 제출 단계 (IDLE, READY, CONFIRMING, REQUESTED)
 * @param isLocked 잠금 여부 (아직 오픈 안 된 주차)
 * @param description 주차 설명
 * @param isBest Best 선정 여부
 * @param missionType 미션 타입 (LINK, FILE, TEXT)
 */
data class NormalStudyItemUiModel(
    val id: Long,
    val platform: String,
    val title: String,
    val status: StudyStatus,
    val week: Int,
    val isExpanded: Boolean = false,
    val link: String = "",
    val input: String = "",
    val submitState: SubmitState = SubmitState.IDLE,
    val isLocked: Boolean = false,
    val description: String,
    val isBest: Boolean = false,
    val missionType: WorkbookMissionType = WorkbookMissionType.UNKNOWN,
)