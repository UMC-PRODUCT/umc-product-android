package com.umc.presentation.ui.community

import com.umc.domain.model.community.TrophyBody
import com.umc.domain.model.community.TrophyItem
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState

@HiltViewModel
class CommunityListViewModel @Inject constructor(

) :
BaseViewModel<CommunityListFragmentUiState, CommunityListFragmentEvent>(
    CommunityListFragmentUiState()
) {
}




data class CommunityListFragmentUiState(

    val trophyList : List<TrophyBody> = listOf(),


    ) : UiState


sealed interface CommunityListFragmentEvent : UiEvent {

}