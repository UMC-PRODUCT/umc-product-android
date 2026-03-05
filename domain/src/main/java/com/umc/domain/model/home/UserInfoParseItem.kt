package com.umc.domain.model.home

import com.umc.domain.model.UserInfo


/**
 * 해당 data class는 UserInfo를 바탕으로 역할이랑 기수 등을 파싱해 저장하는
 * 클래스입니다. 주로 UI 작업에 사용하며, 최신 기수나 challengerId를 얻는데 사용합니다.
 *
 * @param UserChallengerRole 을 RolePartItem의 role에서 from으로 이용해 찾아본다.
 * **/

// 1. 개별 활동 아이템 (역할, 파트, 그리고 챌린저 ID)
data class RolePartItem(
    val role: String, //역할 (SCHOOL_PART_LEADER)
    val responsiblePart: String?, //ANDROID ...
    val challengerId: Long,
    val organizationId: Long,
)

// 2. 기수별 최종 요약 모델
data class GisuSummary(
    val gisu: Long,           // 기수 (예: 9)
    val gisuId: Long,         // 기수 고유 ID
    val fromRoles: List<RolePartItem>,    // 운영진 활동 내역 (권한 있어요)
    val fromRecords: List<RolePartItem>   // 챌린저 활동 내역
)

//UserInfo 데이터를 기수별로 그룹화하고 정렬
fun UserInfo.getGisuSummaryList(): List<GisuSummary> {

    // 기수(gisu)와 기수ID(gisuId) 쌍을 추출하여 고유 리스트 생성
    val gisuPairs = (roles.map { it.gisu to it.gisuId } +
            challengerRecords.map { it.gisu to it.gisuId })
        .distinctBy { it.first } // 기수 번호 기준으로 중복 제거
        .sortedByDescending { it.first } // 최신 기수 순으로 정렬

    // 각 기수별 데이터 분류
    return gisuPairs.map { (gisuValue, gisuIdValue) ->

        // 해당 기수의 운영진(Roles) 데이터 가공
        val roleItems = roles
            .filter { it.gisu == gisuValue }
            .map {
                RolePartItem(
                    role = it.roleType,
                    responsiblePart = it.responsiblePart,
                    challengerId = it.challengerId,
                    organizationId = it.organizationId ?: -1,
                )
            }

        // 해당 기수의 챌린저(Records) 데이터 가공
        val recordItems = challengerRecords
            .filter { it.gisu == gisuValue }
            .map {
                RolePartItem(
                    role = "CHALLENGER",
                    responsiblePart = it.part,
                    challengerId = it.challengerId,
                    organizationId = -1, //챌린저는 조직 ID 제거
                )
            }

        GisuSummary(
            gisu = gisuValue,
            gisuId = gisuIdValue,
            fromRoles = roleItems,
            fromRecords = recordItems
        )
    }
}
