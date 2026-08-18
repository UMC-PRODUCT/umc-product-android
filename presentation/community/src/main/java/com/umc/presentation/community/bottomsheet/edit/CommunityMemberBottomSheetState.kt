package com.umc.presentation.community.bottomsheet.edit

import com.umc.presentation.community.DEFAULT_COMMUNITY_MAX_MEMBER_COUNT
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.presentation.community.model.CommunityChallengerUiModel

/**
 * 커뮤니티 스레드 멤버 관리 BottomSheet의 UI 상태입니다.
 *
 * 주요 상태
 * - 현재 스레드에 참여 중인 멤버
 * - 챌린저 검색 결과
 * - 검색 화면에서 선택된 최종 멤버
 * - 페이지네이션 상태
 * - 멤버 추가/삭제 진행 상태
 */
data class CommunityMemberBottomSheetState(

    /** 현재 관리 중인 스레드 ID */
    val threadId: String = "",

    /** 현재 입력된 챌린저 검색어 */
    val query: String = "",

    /** 챌린저 검색 화면 진입 여부 */
    val isSearching: Boolean = false,

    /** 최초 멤버 조회 또는 검색 결과 로딩 여부 */
    val isLoading: Boolean = false,

    /** 검색 결과 다음 페이지 로딩 여부 */
    val isLoadingMore: Boolean = false,

    /** 멤버 추가/삭제 변경사항을 서버에 반영 중인지 여부 */
    val isUpdatingMembers: Boolean = false,

    /**
     * 서버 기준으로 현재 스레드에 실제 참여 중인 멤버 목록
     *
     * 검색 화면에서 기존 멤버의 체크 상태를 결정하거나,
     * 최종 선택 목록과 비교해 추가/삭제 대상을 계산할 때 사용합니다.
     */
    val currentMembers: List<CommunityChallengerUiModel> = emptyList(),

    /**
     * 초대 및 삭제 대상에서 제외할 스레드 OWNER의 memberId 목록
     *
     * OWNER는 일반 멤버 관리 대상이 아니므로
     * 챌린저 검색 결과에서도 제외합니다.
     */
    val ownerMemberIds: Set<Long> = emptySet(),

    /**
     * 검색 가능한 전체 챌린저 목록
     *
     * 현재 구조에서는 검색 API 결과를 searchResults로 직접 사용하고 있어
     * 실제 사용 여부를 확인한 뒤 불필요하다면 제거 가능합니다.
     */
    val invitableMembers: List<CommunityChallengerUiModel> = emptyList(),

    /** 현재 검색어에 대한 챌린저 검색 결과 */
    val searchResults: List<CommunityChallengerUiModel> = emptyList(),

    /**
     * 검색 화면에서 최종적으로 체크된 멤버 목록
     *
     * 현재 멤버와 비교하여
     * 새로 추가된 멤버와 삭제된 멤버를 계산합니다.
     */
    val selectedMembers: List<CommunityChallengerUiModel> = emptyList(),

    /**
     * 현재 삭제 API가 진행 중인 멤버의 memberId
     *
     * null이면 삭제 중인 멤버가 없으며,
     * 해당 ID를 이용해 특정 멤버의 삭제 버튼 상태를 표시합니다.
     */
    val deletingMemberId: Long? = null,

    /** 챌린저 검색 다음 페이지 조회에 사용할 cursor */
    val nextCursor: Long? = null,

    /** 다음 검색 페이지 존재 여부 */
    val hasNext: Boolean = false,

    /** 스레드에 포함할 수 있는 최대 멤버 수 */
    val maxMemberCount: Int = DEFAULT_COMMUNITY_MAX_MEMBER_COUNT,

    /** 멤버 조회/검색/변경 실패 시 표시할 에러 메시지 */
    val errorMessage: String? = null,
) : UiState {

    /**
     * 현재 화면에 실제로 표시할 멤버 목록
     *
     * 검색 중:
     * - 검색 결과 표시
     *
     * 검색 중이 아닐 때:
     * - 현재 스레드 멤버 표시
     */
    val displayedMembers: List<CommunityChallengerUiModel>
        get() = if (isSearching) {
            searchResults
        } else {
            currentMembers
        }

    /**
     * 현재 화면에 표시할 멤버가 없는 상태인지 여부
     *
     * 로딩이 끝난 뒤 displayedMembers가 비어 있으면 true
     */
    val isEmpty: Boolean
        get() = !isLoading && displayedMembers.isEmpty()

    /**
     * 현재 서버 멤버 목록과 최종 선택 목록에
     * 변경사항이 있는지 확인합니다.
     *
     * 순서와 관계없이 memberId 집합을 비교합니다.
     */
    val hasMemberChanges: Boolean
        get() {
            val currentIds = currentMembers
                .map { member ->
                    member.memberId
                }
                .toSet()

            val selectedIds = selectedMembers
                .map { member ->
                    member.memberId
                }
                .toSet()

            return currentIds != selectedIds
        }

    /**
     * 새롭게 추가할 멤버 목록
     *
     * selectedMembers에는 존재하지만
     * currentMembers에는 존재하지 않는 멤버를 반환합니다.
     *
     * 해당 목록은 멤버 초대 API 호출에 사용합니다.
     */
    val addedMembers: List<CommunityChallengerUiModel>
        get() {
            val currentIds = currentMembers
                .map { member ->
                    member.memberId
                }
                .toSet()

            return selectedMembers.filter { member ->
                member.memberId !in currentIds
            }
        }

    /**
     * 기존 스레드에서 삭제할 멤버 목록
     *
     * currentMembers에는 존재하지만
     * selectedMembers에는 존재하지 않는 멤버를 반환합니다.
     *
     * 해당 목록은 멤버 삭제 API 호출에 사용합니다.
     */
    val removedMembers: List<CommunityChallengerUiModel>
        get() {
            val selectedIds = selectedMembers
                .map { member ->
                    member.memberId
                }
                .toSet()

            return currentMembers.filter { member ->
                member.memberId !in selectedIds
            }
        }

    /**
     * 검색 화면의 확인 버튼 활성화 여부
     *
     * 아래 조건을 모두 만족해야 활성화됩니다.
     * - 검색 화면에 진입한 상태
     * - 실제 멤버 변경사항이 존재
     * - 멤버 변경 API가 진행 중이지 않음
     */
    val isConfirmEnabled: Boolean
        get() = isSearching &&
                hasMemberChanges &&
                !isUpdatingMembers

    /** 현재 선택 인원 / 최대 선택 가능 인원을 표시하는 문자열 */
    val selectedCountText: String
        get() = "${selectedMembers.size}/$maxMemberCount"
}

/**
 * 커뮤니티 멤버 관리 BottomSheet에서 발생하는
 * 일회성 UI 이벤트입니다.
 */
sealed interface CommunityMemberBottomSheetEvent : UiEvent {

    /**
     * 멤버 추가/삭제 변경사항이 모두 정상 반영된 경우 발생합니다.
     *
     * 추가된 인원과 삭제된 인원 수를 전달하여
     * UI에서 완료 Toast 메시지를 구성할 때 사용합니다.
     */
    data class MemberUpdateSuccess(
        val addedMemberCount: Int,
        val removedMemberCount: Int,
    ) : CommunityMemberBottomSheetEvent

    /**
     * 현재 멤버 목록에서 특정 멤버를
     * 즉시 삭제하는 API가 성공한 경우 발생합니다.
     */
    data class MemberKickSuccess(
        val memberId: Long,
    ) : CommunityMemberBottomSheetEvent

    /**
     * 사용자에게 안내 또는 에러 Toast를 표시할 때 사용합니다.
     */
    data class ShowToast(
        val message: String,
    ) : CommunityMemberBottomSheetEvent
}