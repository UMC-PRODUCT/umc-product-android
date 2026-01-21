package com.umc.presentation.ui.mypage.suggest

import com.umc.domain.model.home.CategoryItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.profile.ProfileFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SuggestWriteViewModel @Inject
constructor() : BaseViewModel<SuggestWriteFragmentUiState, SuggestWriteFragmentEvent>(
    SuggestWriteFragmentUiState()){

    //anomy 설정
    fun setAnomy(isOn: Boolean) {
        updateState { copy(isAnomy = isOn)
        }
    }

    //category 변경
    fun setCategory(nowCategory: CategoryItem) {
        updateState {
            val selectedCategories = categories.map{
                //만약 터치한 놈이 리스트 중 하나랑 같으면
                if(it.name == nowCategory.name){
                    it.copy(isChecked = !it.isChecked)
                }
                //아니면 싹 다 false로 바꾸기
                else{
                    it.copy(isChecked = false)
                }
            }
            copy(categories = selectedCategories)
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
        // 최신상태 값 get
        val currentState = uiState.value

        // 선택된 지역(카테고리) 얻기
        // 리스트 중 isChecked가 true인 첫 번째 아이템의 이름을 가져옵니다.
        val selectedRegion = currentState.categories.find { it.isChecked }?.name ?: "지역 선택 안됨"

        // 제목 및 내용 획득
        val title = currentState.title
        val content = currentState.content
        val isAnomy = currentState.isAnomy
    
        /**TODO 서버로 보내야지**/

        emitEvent(SuggestWriteFragmentEvent.ClickBackPressed)
    }

    //뒤로 가기

    fun onClickBackPressed(){
        emitEvent(SuggestWriteFragmentEvent.ClickBackPressed)
    }



}



data class SuggestWriteFragmentUiState(
    val isAnomy : Boolean = true,

    //카테고리 리스트
    val categories: List<CategoryItem> = listOf(
        CategoryItem("서울"),
        CategoryItem("경기/인천"),
        CategoryItem("대전/충청"),
        CategoryItem("부산/경남"),
    ),

    //작성한 내용들
    val title: String = "",
    val content: String = ""

    
) : UiState

sealed interface SuggestWriteFragmentEvent : UiEvent {

    data class SelectCategory(val category: CategoryItem) : SuggestWriteFragmentEvent

    object ClickBackPressed : SuggestWriteFragmentEvent
}