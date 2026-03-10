package com.umc.presentation.ui.community

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.ContentItem
import com.umc.domain.usecase.community.GetCommunityPostUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CommunityViewModel @Inject
constructor(
) : BaseViewModel<CommunityFragmentUiState, CommunityFragmentEvent>(
    CommunityFragmentUiState()
) {


    //이동 로직
    fun navigateSearch(){
        emitEvent(CommunityFragmentEvent.NavigateSearch)
    }

    

}


data class CommunityFragmentUiState(

    val dummyData : String = ""


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object NavigateSearch : CommunityFragmentEvent




}