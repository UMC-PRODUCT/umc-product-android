package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * 커뮤니티 메인 화면에서 사용하는 UI 상태
 *
 * 스레드 목록, 선택된 카테고리,
 * 로딩/에러 및 스레드 관리 Dialog 상태를 관리합니다.
 */
data class CommunityState(
    val isLoading: Boolean = true,
    val selectedCategory: CommunityCategory = CommunityCategory.ALL,
    val threads: ImmutableList<CommunityThreadUiModel> = persistentListOf(),
    val errorMessage: String? = null,

    val selectedThread: CommunityThreadUiModel? = null,
    val showThreadMenuDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
) {
    /** 고정된 스레드 목록 */
    val pinnedThreads: ImmutableList<CommunityThreadUiModel>
        get() = threads.filter { thread ->
            thread.isPinned
        }.toImmutableList()

    /**
     * 현재 선택한 카테고리에 따라 필터링된 일반 스레드 목록
     *
     * 고정된 스레드는 별도 영역에서 표시하므로 제외합니다.
     */
    val normalThreads: ImmutableList<CommunityThreadUiModel>
        get() {
            val unpinnedThreads = threads.filterNot { thread ->
                thread.isPinned
            }

            val filtered = when (selectedCategory) {
                CommunityCategory.ALL -> {
                    unpinnedThreads
                }

                CommunityCategory.UNREAD -> {
                    unpinnedThreads.filter { thread ->
                        thread.unreadCount > 0
                    }
                }

                CommunityCategory.PROJECT -> {
                    unpinnedThreads.filter { thread ->
                        thread.category == CommunityCategory.PROJECT
                    }
                }

                CommunityCategory.STUDY -> {
                    unpinnedThreads.filter { thread ->
                        thread.category == CommunityCategory.STUDY
                    }
                }

                CommunityCategory.QNA -> {
                    unpinnedThreads.filter { thread ->
                        thread.category == CommunityCategory.QNA
                    }
                }

                CommunityCategory.FREE -> {
                    unpinnedThreads.filter { thread ->
                        thread.category == CommunityCategory.FREE
                    }
                }
            }

            return filtered.toImmutableList()
        }

    /** 스레드 목록 조회 에러 여부 */
    val isError: Boolean
        get() = errorMessage != null

    /** 현재 표시할 스레드가 없는 상태인지 여부 */
    val isEmpty: Boolean
        get() = !isLoading &&
                !isError &&
                pinnedThreads.isEmpty() &&
                normalThreads.isEmpty()

    /** 선택된 카테고리에 따라 목록에 표시할 섹션 제목 */
    val selectedSectionTitle: String
        get() = when (selectedCategory) {
            CommunityCategory.ALL -> "전체"
            CommunityCategory.UNREAD -> "안읽음"
            CommunityCategory.PROJECT -> "파트공지"
            CommunityCategory.STUDY -> "스터디"
            CommunityCategory.QNA -> "질문"
            CommunityCategory.FREE -> "자유"
        }
}