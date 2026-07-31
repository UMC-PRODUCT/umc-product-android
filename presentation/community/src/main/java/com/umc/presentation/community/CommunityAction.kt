package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory

sealed interface CommunityAction {

    data class OnCategorySelected(
        val category: CommunityCategory,
    ) : CommunityAction

    data class OnThreadClick(
        val threadId: Long,
    ) : CommunityAction

    data object OnFilterClick : CommunityAction

    data object OnSearchClick : CommunityAction

    data object OnCreateThreadClick : CommunityAction

    data object OnRetryClick : CommunityAction
}