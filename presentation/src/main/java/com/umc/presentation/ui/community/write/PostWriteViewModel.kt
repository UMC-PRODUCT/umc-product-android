package com.umc.presentation.ui.community.write

import android.util.Log
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.home.CategoryItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.suggest.SuggestWriteFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostWriteViewModel @Inject
constructor() : BaseViewModel<PostWriteFragmentUiState, PostWriteFragmentEvent>(
    PostWriteFragmentUiState()
) {
    

    //category(글 카테고리) 선택
    fun onClickContentCategorySelect(){
        emitEvent(PostWriteFragmentEvent.ClickCategorySelect)
    }

    //카테고리 선택을 반영
    fun updateContentCategory(category: CommunityCategoryType) {
        updateState {
            // 초기값 "카테고리 선택"이 실제 선택된 라벨(예: "번개")로 바뀝니다.
            copy(selectContentCategory = CategoryItem(category.label, isChecked = true))
        }

        //추가 카테고리가 번개냐?
        if(category.label == CommunityCategoryType.LIGHTNING.label){
            setLightCardView(true)
        }
        else{
            setLightCardView(false)
        }

    }



    //제목 업데이트
    fun updateTitle(title: String) {
        updateState { copy(title = title) }
    }

    //본문 업데이트
    fun updateContent(content: String) {
        updateState { copy(content = content) }
    }

    //작성한 내용을 저장(서버로 전송)
    fun onClickRegister(){
        //TODO 서버 로직 작성
    }
    
    //번개 관련
    fun setLightCardView(nowLight : Boolean){
        updateState { copy(isLight = nowLight) }
    }

    //번개 날짜/시간 업데이트
    fun updateLightTime(time: String) {
        updateState { copy(lightTime = time) }
    }

    //번개 최대 인원 업데이트
    fun updateLightPeople(people: String) {
        updateState { copy(lightPeople = people) }
    }

    //번개 장소 업데이트
    fun updateLightPlace(place: String) {
        updateState { copy(lightPlace = place) }
    }

    //번개 오픈 채팅 링크 업데이트
    fun updateLightOpenChat(openChat: String) {
        updateState { copy(lightOpenChat = openChat) }
    }

    //게시글 등록 시 로직
    fun createPost(){
        
    }

    //뒤로 가기
    fun onClickBackPressed(){
        emitEvent(PostWriteFragmentEvent.ClickBackPressed)
    }
}

data class PostWriteFragmentUiState(

    
    val selectContentCategory : CategoryItem = CategoryItem("카테고리 선택"),

    //작성한 내용들
    val title: String = "",
    val content: String = "",


    //번개 관련 내용들
    val isLight : Boolean = false,
    val lightTime : String = "",
    val lightPeople : String = "",
    val lightPlace : String = "",
    val lightOpenChat : String = "",


    ) : UiState

sealed interface PostWriteFragmentEvent : UiEvent {

    //카테고리 선택 눌렀을 때 뾰료룡
    object ClickCategorySelect : PostWriteFragmentEvent


    object ClickBackPressed : PostWriteFragmentEvent

}