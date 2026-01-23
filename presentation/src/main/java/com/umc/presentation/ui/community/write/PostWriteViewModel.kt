package com.umc.presentation.ui.community.write

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


    //category(지역) 변경
    fun setCategory(nowCategory: CategoryItem) {
        updateState {
            val selectedCategories = regionCategories.map{
                //만약 터치한 놈이 리스트 중 하나랑 같으면
                if(it.name == nowCategory.name){
                    it.copy(isChecked = !it.isChecked)
                }
                //아니면 싹 다 false로 바꾸기
                else{
                    it.copy(isChecked = false)
                }
            }
            copy(regionCategories = selectedCategories)
        }
    }

    //category(글 카테고리) 선택
    fun onClickContentCategorySelect(){
        emitEvent(PostWriteFragmentEvent.ClickCategorySelect)
    }

    //카테고리 선택을 반영
    fun updateContentCategory(category: CommunityCategoryType) {
        updateState {
            // 초기값 "카테고리 선택"이 실제 선택된 라벨(예: "번개")로 바뀝니다.
            copy(selectContentCategory = CategoryItem(category.label, true))
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

    }



    //뒤로 가기
    fun onClickBackPressed(){
        emitEvent(PostWriteFragmentEvent.ClickBackPressed)
    }
}

data class PostWriteFragmentUiState(

    //카테고리 리스트
    val regionCategories: List<CategoryItem> = listOf(
        CategoryItem("서울"),
        CategoryItem("경기/인천"),
        CategoryItem("대전/충청"),
        CategoryItem("부산/경남"),
    ),

    val selectContentCategory : CategoryItem = CategoryItem("카테고리 선택"),

    //작성한 내용들
    val title: String = "",
    val content: String = ""


    ) : UiState

sealed interface PostWriteFragmentEvent : UiEvent {

    //카테고리 선택 눌렀을 때 뾰료룡
    object ClickCategorySelect : PostWriteFragmentEvent


    object ClickBackPressed : PostWriteFragmentEvent

}