package com.umc.presentation.ui.mypage.mypost

import android.util.Log
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

        // 1. 로딩 상태 시작
        updateState { copy(isPageLoading = true) }

        val commentedPostIds = listOf(101L, 201L, 202L, 302L, 402L)

        // 2. 필터링 로직 (기존 코드에서 뒤바뀐 부분 수정)
        val filteredResponse = when (state.showType) {
            "MYPOST" -> state.communityList.filter { it.userId == 20201469L }
            "MYSCRAP" -> state.communityList.filter { it.isScrapped } // 스크랩 필터
            "MYCOMMENT" -> state.communityList.filter { it.postId in commentedPostIds } // 댓글 필터
            else -> emptyList()
        }

        // 3. 더미 로직이므로 즉시 업데이트 (viewModelScope 없이도 가능)
        updateState {
            copy(
                nowContents = filteredResponse,
                isContents = filteredResponse.isNotEmpty(), // 데이터 유무 업데이트 필수!
                isPageLoading = false,
                isLastPage = true, // 더미 데이터는 한 페이지뿐이므로 true
                currentPage = if (isRefresh) 1 else currentPage + 1
            )
        }



            /*
            // 그
            // 후에는 핸들링 로직
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

             */



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
    val isLastPage: Boolean = false,      // 서버 응답의 hasNext 기반 마지막 여부


    val communityList: List<ContentItem> = listOf(
        // --- LIGHTNING (번개) ---
        ContentItem(
            postId = 101,
            title = "오늘 저녁 정보과학관 앞에서 서브웨이 드실 분?",
            category = CommunityCategoryType.LIGHTNING,
            userId = 20201469,
            username = "박유수",
            writeTime = "10분 전",
            likes = 3,
            comments = 5,
            content = "과제 하다가 너무 배고파서요... 같이 드실 분 구합니다!",
            lightningInfo = null,
            userPart = UserPart.ANDROID,
            isLiked = false,
            isScrapped = false,
            scraps = 1
        ),
        ContentItem(
            postId = 102,
            title = "내일 아침 카공(카페 공부) 번개!",
            category = CommunityCategoryType.LIGHTNING,
            userId = 202,
            username = "코딩왕",
            writeTime = "1시간 전",
            likes = 1,
            comments = 2,
            content = "상도동 투썸에서 같이 알고리즘 풀 사람 구해요.",
            lightningInfo = null,
            userPart = UserPart.SPRINGBOOT,
            isLiked = true,
            isScrapped = false,
            scraps = 0
        ),

        // --- HABIT (취미) ---
        ContentItem(
            postId = 201,
            title = "요즘 푹 빠진 키보드 커스텀",
            category = CommunityCategoryType.HABIT,
            userId = 301,
            username = "디자인러브",
            writeTime = "3시간 전",
            likes = 15,
            comments = 3,
            content = "HHKB 배열 써보신 분 계신가요? 타건감이 예술이네요.",
            lightningInfo = null,
            userPart = UserPart.DESIGN,
            isLiked = false,
            isScrapped = true,
            scraps = 4
        ),
        ContentItem(
            postId = 202,
            title = "오운완! 개발자도 체력이 국력입니다.",
            category = CommunityCategoryType.HABIT,
            userId = 302,
            username = "스쿼트장인",
            writeTime = "5시간 전",
            likes = 22,
            comments = 2,
            content = "오늘 하체 조지고 왔습니다. 다들 거북목 방지 운동 하세요.",
            lightningInfo = null,
            userPart = UserPart.WEB,
            isLiked = true,
            isScrapped = false,
            scraps = 2
        ),

        // --- QUESTION (질문) ---
        ContentItem(
            postId = 301,
            title = "Jetpack Compose에서 리컴포지션 최적화 질문",
            category = CommunityCategoryType.QUESTION,
            userId = 20201469,
            username = "박유수",
            writeTime = "2시간 전",
            likes = 5,
            comments = 1,
            content = "LazyColumn에서 특정 아이템만 업데이트하고 싶은데 자꾸 전체가 다시 그려지네요. remember 유의점 있을까요?",
            lightningInfo = null,
            userPart = UserPart.ANDROID,
            isLiked = false,
            isScrapped = true,
            scraps = 5
        ),
        ContentItem(
            postId = 302,
            title = "Spring Boot Security 설정 오류",
            category = CommunityCategoryType.QUESTION,
            userId = 402,
            username = "백엔드꿈나무",
            writeTime = "4시간 전",
            likes = 2,
            comments = 4,
            content = "JWT 필터 추가했는데 403 에러가 계속 뜨네요. 설정 코드 봐주실 분?",
            lightningInfo = null,
            userPart = UserPart.SPRINGBOOT,
            isLiked = false,
            isScrapped = false,
            scraps = 1
        ),

        // --- INFORMATION (정보) ---
        ContentItem(
            postId = 401,
            title = "[공유] 피그마 단축키 모음",
            category = CommunityCategoryType.INFORMATION,
            userId = 501,
            username = "아트디렉터",
            writeTime = "어제",
            likes = 45,
            comments = 1,
            content = "협업 효율 200% 올려주는 피그마 꿀팁 정리해봤습니다.",
            lightningInfo = null,
            userPart = UserPart.DESIGN,
            isLiked = true,
            isScrapped = true,
            scraps = 30
        ),
        ContentItem(
            postId = 402,
            title = "UMC Demo Day 부스 배치도 및 일정",
            category = CommunityCategoryType.INFORMATION,
            userId = 502,
            username = "운영진",
            writeTime = "2일 전",
            likes = 12,
            comments = 3,
            content = "이번 주말 열리는 데모데이 공지입니다. 안드로이드 파트는 A구역입니다!",
            lightningInfo = null,
            userPart = UserPart.PLAN,
            isLiked = false,
            isScrapped = false,
            scraps = 8
        ),

        // --- FREE (자유) ---
        ContentItem(
            postId = 501,
            title = "드디어 종강까지 한 달 남았네요",
            category = CommunityCategoryType.FREE,
            userId = 601,
            username = "자유로운영혼",
            writeTime = "3일 전",
            likes = 30,
            comments = 0,
            content = "이번 학기 정말 고생 많으셨습니다. 종강하면 바로 제주도 갈 거예요.",
            lightningInfo = null,
            userPart = UserPart.IOS,
            isLiked = true,
            isScrapped = false,
            scraps = 0
        ),
        ContentItem(
            postId = 502,
            title = "컴퓨터학부 학생증 잃어버리신 분?",
            category = CommunityCategoryType.FREE,
            userId = 602,
            username = "친절한시민",
            writeTime = "4일 전",
            likes = 8,
            comments = 2,
            content = "형남공학관 엘리베이터 앞에서 주웠습니다. 과사무실에 맡겨둘게요!",
            lightningInfo = null,
            userPart = UserPart.NODEJS,
            isLiked = false,
            isScrapped = false,
            scraps = 1
        )
    ),



): UiState

sealed interface MypostFragmentEvent : UiEvent {
    object ClickBackPressed : MypostFragmentEvent

    data class ShowErrorToast(val errorMessage : String) : MypostFragmentEvent
}
