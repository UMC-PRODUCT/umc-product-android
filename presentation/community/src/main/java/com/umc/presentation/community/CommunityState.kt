package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel

data class CommunityState(
    val isLoading: Boolean = true,
    val selectedCategory: CommunityCategory = CommunityCategory.ALL,
    val threads: List<CommunityThreadUiModel> = emptyList(),
    val errorMessage: String? = null,

    // 롱프레스한 스레드
    val selectedThread: CommunityThreadUiModel? = null,

    // 스레드 메뉴 다이얼로그 표시 여부
    val showThreadMenuDialog: Boolean = false,

    // 나가기 확인 다이얼로그 표시 여부
    val showLeaveDialog: Boolean = false,
) {
    val filteredThreads: List<CommunityThreadUiModel>
        get() = when (selectedCategory) {
            CommunityCategory.ALL -> {
                threads
            }

            CommunityCategory.UNREAD -> {
                threads.filter { thread ->
                    !thread.isRead
                }
            }

            else -> {
                threads.filter { thread ->
                    thread.category == selectedCategory
                }
            }
        }

    val pinnedThreads: List<CommunityThreadUiModel>
        get() = filteredThreads.filter { thread ->
            thread.isPinned
        }

    val normalThreads: List<CommunityThreadUiModel>
        get() = filteredThreads.filterNot { thread ->
            thread.isPinned
        }

    val isError: Boolean
        get() = errorMessage != null

    val isEmpty: Boolean
        get() = !isLoading &&
                !isError &&
                filteredThreads.isEmpty()

    val selectedSectionTitle: String
        get() = when (selectedCategory) {
            CommunityCategory.ALL -> "전체"
            CommunityCategory.UNREAD -> "안읽음"
            CommunityCategory.PART_NOTICE -> "파트공지"
            CommunityCategory.STUDY -> "스터디"
            CommunityCategory.QUESTION -> "질문"
            CommunityCategory.FREE -> "자유"
        }
}