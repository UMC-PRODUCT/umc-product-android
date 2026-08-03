package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel

data class CommunityState(
    val isLoading: Boolean = true,
    val selectedCategory: CommunityCategory = CommunityCategory.ALL,
    val threads: List<CommunityThreadUiModel> = emptyList(),
    val errorMessage: String? = null,

    val selectedThread: CommunityThreadUiModel? = null,
    val showThreadMenuDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
) {
    val pinnedThreads: List<CommunityThreadUiModel>
        get() = threads.filter { thread ->
            thread.isPinned
        }

    val normalThreads: List<CommunityThreadUiModel>
        get() {
            val unpinnedThreads = threads.filterNot { thread ->
                thread.isPinned
            }

            return when (selectedCategory) {
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
        }

    val isError: Boolean
        get() = errorMessage != null

    val isEmpty: Boolean
        get() = !isLoading &&
                !isError &&
                pinnedThreads.isEmpty() &&
                normalThreads.isEmpty()

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