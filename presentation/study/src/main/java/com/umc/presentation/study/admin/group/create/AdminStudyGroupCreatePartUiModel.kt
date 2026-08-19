package com.umc.presentation.study.admin.group.create

/**
 * 스터디 그룹 생성 화면에서 사용하는 파트 UI 모델
 *
 * 사용자에게 보여주는 파트 이름과
 * API 요청에 전달할 실제 파트 값을 분리하여 관리합니다.
 *
 * 예시
 * - label = "Android"
 * - value = "ANDROID"
 *
 * @param label 화면에 표시할 파트 이름
 * @param value 서버 요청에 사용할 파트 값
 */
data class AdminStudyGroupCreatePartUiModel(
    val label: String,
    val value: String,
)