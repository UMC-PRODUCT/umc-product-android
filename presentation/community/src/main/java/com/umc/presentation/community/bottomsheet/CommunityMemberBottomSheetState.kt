package com.umc.presentation.community.bottomsheet

import com.umc.presentation.community.DEFAULT_COMMUNITY_MAX_MEMBER_COUNT

import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.presentation.community.model.CommunityChallengerUiModel

data class CommunityMemberBottomSheetState(
    val threadId: String = "",
    val query: String = "",
    val isSearching: Boolean = false,

    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isUpdatingMembers: Boolean = false,

    // 서버에 실제로 참여 중인 멤버
    val currentMembers: List<CommunityChallengerUiModel> = emptyList(),

    // 초대/내보내기 대상에서 제외할 스레드 방장
    val ownerMemberIds: Set<Long> = emptySet(),

    // 검색 가능한 전체 챌린저
    val invitableMembers: List<CommunityChallengerUiModel> = emptyList(),

    // 검색 결과
    val searchResults: List<CommunityChallengerUiModel> = emptyList(),

    // 검색 화면에서 체크된 최종 멤버 목록
    val selectedMembers: List<CommunityChallengerUiModel> = emptyList(),

    val deletingMemberId: Long? = null,

    val nextCursor: Long? = null,
    val hasNext: Boolean = false,


    val maxMemberCount: Int = DEFAULT_COMMUNITY_MAX_MEMBER_COUNT,
    val errorMessage: String? = null,
) : UiState {

    val displayedMembers: List<CommunityChallengerUiModel>
        get() = if (isSearching) {
            searchResults
        } else {
            currentMembers
        }

    val isEmpty: Boolean
        get() = !isLoading && displayedMembers.isEmpty()

    val hasMemberChanges: Boolean
        get() {
            val currentIds = currentMembers
                .map { it.memberId }
                .toSet()

            val selectedIds = selectedMembers
                .map { it.memberId }
                .toSet()

            return currentIds != selectedIds
        }

    val addedMembers: List<CommunityChallengerUiModel>
        get() {
            val currentIds = currentMembers
                .map { it.memberId }
                .toSet()

            return selectedMembers.filter { member ->
                member.memberId !in currentIds
            }
        }

    val removedMembers: List<CommunityChallengerUiModel>
        get() {
            val selectedIds = selectedMembers
                .map { it.memberId }
                .toSet()

            return currentMembers.filter { member ->
                member.memberId !in selectedIds
            }
        }

    val isConfirmEnabled: Boolean
        get() = isSearching &&
                hasMemberChanges &&
                !isUpdatingMembers

    val selectedCountText: String
        get() = "${selectedMembers.size}/$maxMemberCount"
}

sealed interface CommunityMemberBottomSheetEvent : UiEvent {

    data class MemberUpdateSuccess(
        val addedMemberCount: Int,
        val removedMemberCount: Int,
    ) : CommunityMemberBottomSheetEvent

    data class MemberKickSuccess(
        val memberId: Long,
    ) : CommunityMemberBottomSheetEvent

    data class ShowToast(
        val message: String,
    ) : CommunityMemberBottomSheetEvent
}
