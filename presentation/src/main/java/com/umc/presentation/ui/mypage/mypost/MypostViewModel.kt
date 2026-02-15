package com.umc.presentation.ui.mypage.mypost

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.ContentItem
import com.umc.domain.usecase.community.GetMyCommentedPostsUseCase
import com.umc.domain.usecase.community.GetMyPostsUseCase
import com.umc.domain.usecase.community.GetMyScrappedPostsUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.profile.ProfileFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class MypostViewModel @Inject
constructor(
    private val getMyPostsUseCase: GetMyPostsUseCase,
    private val getMyScrappedPostsUseCase: GetMyScrappedPostsUseCase,
    private val getMyCommentedPostsUseCase: GetMyCommentedPostsUseCase,
) : BaseViewModel<MypostFragmentUiState, MypostFragmentEvent>(
    MypostFragmentUiState()){


    fun onClickBackPressed(){
        emitEvent(MypostFragmentEvent.ClickBackPressed)
    }

    //초기 타입 설정
    fun initShowType(showType: String){
        //오류 핸들
        if (showType.isBlank()) {
            emitEvent(MypostFragmentEvent.ShowErrorToast("오류가 발생했습니다."))
            return
        }
        updateState { copy(showType = showType) }
        settingPost(isRefresh = true)
    }

    //타입에 따라 서버에서 게시글 정보 가져오기(무한 스크롤 대응)
    fun settingPost(isRefresh : Boolean = false){
        val state = uiState.value

        // 브레이크 로직: 로딩 중이거나, 새로고침이 아닌데 이미 마지막 페이지면 실행 안 함
        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return

        // 호출 전 상태 업데이트
        if (isRefresh) {
            updateState { copy(isPageLoading = true, currentPage = 0, isLastPage = false) }
        } else {
            updateState { copy(isPageLoading = true) }
        }

        // 가져오기
        viewModelScope.launch {
            val pageToFetch = uiState.value.currentPage //가져올 페이지
            val size = 20

            // 타입에 맞춰 수행
            val response = when (state.showType) {
                "MYPOST" -> getMyPostsUseCase(pageToFetch, size)
                "MYCOMMENT" -> getMyCommentedPostsUseCase(pageToFetch, size)
                "MYSCRAP" -> getMyScrappedPostsUseCase(pageToFetch, size)
                else -> null
            }

            // 실패 시 로딩 뜨고 리턴
            if (response == null) {
                updateState { copy(isPageLoading = false) }
                return@launch
            }

            // 그 후에는 핸들링 로직
            resultResponse(
                response = response,
                successCallback = { pageModel ->
                    updateState {
                        copy(
                            nowContents = if (isRefresh) pageModel.posts else nowContents + pageModel.posts,
                            currentPage = pageToFetch + 1,
                            isPageLoading = false,
                            isLastPage = !pageModel.hasNext,
                            isContents = if (isRefresh) pageModel.posts.isNotEmpty() else nowContents.isNotEmpty()
                        )
                    }
                },
                errorCallback = {
                    updateState { copy(isPageLoading = false) }
                }
            )
        }


    }

}


data class MypostFragmentUiState(
    //현재 보고 있는 타입
    val showType: String = "",
    //현재 받아온 컨텐츠
    val nowContents: List<ContentItem> = emptyList(),
    //컨텐츠가 있는지 여부
    val isContents : Boolean = false,

    // 무한 스크롤 및 로딩 제어
    val currentPage: Int = 0,           // 현재 페이지 인덱스 (0부터 시작)
    val isPageLoading: Boolean = false,  // 중복 호출 방지용 로딩 플래그
    val isLastPage: Boolean = false      // 서버 응답의 hasNext 기반 마지막 여부

): UiState

sealed interface MypostFragmentEvent : UiEvent {
    object ClickBackPressed : MypostFragmentEvent

    data class ShowErrorToast(val errorMessage : String) : MypostFragmentEvent
}
