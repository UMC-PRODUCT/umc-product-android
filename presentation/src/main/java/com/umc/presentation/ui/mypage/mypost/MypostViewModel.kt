package com.umc.presentation.ui.mypage.mypost

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.profile.ProfileFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject



@HiltViewModel
class MypostViewModel @Inject
constructor() : BaseViewModel<MypostFragmentUiState, MypostFragmentEvent>(
    MypostFragmentUiState()){


    fun onClickBackPressed(){
        emitEvent(MypostFragmentEvent.ClickBackPressed)
    }

    //타입에 따라 서버에서 게시글 정보 가져오기
    fun settingPost(showType: String){
        if(showType == "MYPOST"){

        }
        else if(showType == "MYCOMMENT"){

        }
        else if(showType == "MYSCRAP"){
           

        }
        else{
            emitEvent(MypostFragmentEvent.ShowErrorToast("오류"))
        }

    }

}


data class MypostFragmentUiState(
    val nowContents: List<ContentItem> = emptyList(),

    val isContents : Boolean = false,

): UiState

sealed interface MypostFragmentEvent : UiEvent {
    object ClickBackPressed : MypostFragmentEvent

    data class ShowErrorToast(val errorMessage : String) : MypostFragmentEvent
}
