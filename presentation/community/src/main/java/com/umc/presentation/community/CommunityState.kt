package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel

data class CommunityState(
    val isLoading: Boolean = true,
    val selectedCategory: CommunityCategory = CommunityCategory.ALL,
    val threads: List<CommunityThreadUiModel> = emptyList(),
    val errorMessage: String? = null,
) {
    val filteredThreads: List<CommunityThreadUiModel>
        get() = when (selectedCategory) {
            CommunityCategory.ALL -> {
                threads
            }

            CommunityCategory.UNREAD -> {
                threads.filterNot { thread ->
                    thread.isRead
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

    val selectedSectionTitle: String
        get() = selectedCategory.label

    val isError: Boolean
        get() = errorMessage != null

    val isEmpty: Boolean
        get() = !isLoading &&
                !isError &&
                filteredThreads.isEmpty()
}