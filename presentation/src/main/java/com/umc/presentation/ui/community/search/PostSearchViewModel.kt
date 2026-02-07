package com.umc.presentation.ui.community.search

import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.SearchMode
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.home.PlanDetailFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PostSearchViewModel @Inject
constructor() : BaseViewModel<PostSearchFragmentUiState, PostSearchFragmentEvent>(
    PostSearchFragmentUiState()
) {


    // 검색 창 엔터 관련 메서드 (검색 결과 시 처리)
    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            updateState {
                copy(
                    mode = SearchMode.RESULT,
                )
            }
            /**TODO 서버에서 처리한 결과가 들어가기**/
            emitEvent(PostSearchFragmentEvent.ShowSearchResult(uiState.value.query))

            return true
        }
        return false
    }

    // 실시간 텍스트 변경 처리
    fun onQueryChanged(query: String) {
        updateState {
            copy(query = query)
        }
    }

    // 최근 검색어 선택 시 쿼리 변경 및 결과 모드 처리
    fun selectRecentSearch(keyword: String) {
        updateState {
            copy(
                query = keyword,
                mode = SearchMode.RESULT
            )
        }
        // 검색 로직 실행 (엔터 친 것과 동일)
        emitEvent(PostSearchFragmentEvent.ShowSearchResult(uiState.value.query))
    }

    // 최근 검색어 리스트에서 X 클릭 시 -> 해당 키워드 제거
    fun deleteRecentSearch(keyword: String) {
        updateState {
            val updatedList = recentSearches.filter { it != keyword }
            copy(recentSearches = updatedList)
        }
        /**TODO 서버 로직**/
    }

    // 최근 검색어 키워드 전체 삭제
    fun deleteAllRecentSearch(){
        updateState {
            copy(recentSearches = listOf())
        }
        /**TODO 서버 로직**/
    }

    //뷰모드 변경하기
    fun onChangeViewMode(mode: SearchMode){
        updateState {
            copy(mode = mode)
        }
    }


    fun onClickBackPressed(){
        emitEvent(PostSearchFragmentEvent.MoveBackPressedEvent)
    }

}


data class PostSearchFragmentUiState(

    //현재 검색창에 입력한 내용
    val query : String = "",
    //뷰모드
    val mode : SearchMode = SearchMode.EMPTY,
    //최근 검색 내용
    val recentSearches: List<String> = listOf("중앙", "중앙 해커톤"),
    //검색 결과
    val searchResults: List<ContentItem> = emptyList(),
        /*
        listOf(
        ContentItem(
            category = CommunityCategoryType.QUESTION,
            region = "서울",
            contentType = ContentType.ALL,
            recruitType = RecruitType.END,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차",
            writeTime = "방금 전",
            likes = 0,
            comments = 1,
            content = "이거는 본문 내용이에요!!!",
            userPart = UserPart.ANDROID,
        ),
        ContentItem(
            category = CommunityCategoryType.HABIT,
            region = "인천",
            contentType = ContentType.ALL,
            recruitType = RecruitType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차2호",
            writeTime = "1시간 전",
            likes = 2,
            comments = 2,
            content = "이거는 본문 내용이에요!!!",
            userPart = UserPart.WEB,
        ),
        ContentItem(
            category = CommunityCategoryType.ASK,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "사람",
            writeTime = "2016.01.19",
            likes = 200,
            comments = 123,
            content = "이거는 본문 내용이에요!!!",
            userPart = UserPart.IOS,
        ),
        ContentItem(
            category = CommunityCategoryType.LIGHTNING,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.END,
            title = "밥먹고개발하고쉬고개발하고게임하고개발하고자고개발하고나는개발이너무너무너무좋아헤헤헤헤헤헿",
            username = "사람",
            writeTime = "2016.01.19",
            likes = 10,
            comments = 1,
            content = "본문내용을늘려야하는데무슨내용을적어야잘적었다는소문이날까?내용을늘리기위해서는아무내용이나넣어야겠다.이쯤되면길어지겠지?",
            userPart = UserPart.SPRING_BOOT,
        ),
    ),


         */

    ) : UiState

sealed interface PostSearchFragmentEvent : UiEvent {
    data class ShowSearchResult(val query : String) : PostSearchFragmentEvent

    object MoveBackPressedEvent : PostSearchFragmentEvent

}