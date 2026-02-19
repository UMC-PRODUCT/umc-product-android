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
    private val getCommunityPostUseCase: GetCommunityPostUseCase,
) : BaseViewModel<CommunityFragmentUiState, CommunityFragmentEvent>(
    CommunityFragmentUiState()
) {


    //탭 바꿀 때마다 탭 변화하고 필터링
    fun setNowTab(whichTab: ContentType) {
        updateState { copy(whichTab = whichTab) }
        fetchPosts(isRefresh = true)
    }
    

    //게시글 가져오기(isRefesh = 다 지우고 새로 가져오기)
    fun fetchPosts(isRefresh: Boolean = false) {
        val state = uiState.value

        val filteredList = when (state.whichTab) {
            ContentType.ALL -> uiState.value.communityList
            ContentType.QUESTION -> uiState.value.communityList.filter { it.category == CommunityCategoryType.QUESTION }
            ContentType.LIGHTNING -> uiState.value.communityList.filter { it.category == CommunityCategoryType.LIGHTNING }
            // 나머지 카테고리에 대해서도 필터링이 필요하다면 아래와 같이 추가 가능합니다.
            // ContentType.HABIT -> communityList.filter { it.category == CommunityCategoryType.HABIT }
            else -> uiState.value.communityList
        }

        updateState { copy(
            // 2. 새로고침이면 리스트 교체, 아니면 기존 리스트에 누적
            nowContents = filteredList,
            isPageLoading = false,
        ) }


        /*
        // 브레이크 로직: 로딩 중이거나, 새로고침이 아닌데 이미 마지막 페이지면 실행 안 함
        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return


        // 호출 전 상태 업데이트
        if (isRefresh) {
            updateState { copy(isPageLoading = true, currentPage = 0, isLastPage = false) }
        } else {
            updateState { copy(isPageLoading = true) }
        }

        viewModelScope.launch {
            val category = when (state.whichTab) {
                ContentType.ALL -> null
                ContentType.QUESTION -> CommunityCategoryType.QUESTION.name
                ContentType.LIGHTNING -> CommunityCategoryType.LIGHTNING.name
                else -> null
            }

            val pageToFetch = if (isRefresh) 0 else state.currentPage

            resultResponse(
                response = getCommunityPostUseCase(category, pageToFetch, 20),
                successCallback = { pageModel ->
                    // 1. 질문 탭이면 클라이언트에서 한 번 더 필터링
                    val rawPosts = pageModel.posts
                    val filteredPosts = if (state.whichTab == ContentType.QUESTION) {
                        rawPosts.filter { it.category == CommunityCategoryType.QUESTION }
                    } else {
                        rawPosts
                    }

                    updateState { copy(
                        // 2. 새로고침이면 리스트 교체, 아니면 기존 리스트에 누적
                        nowContents = if (isRefresh) filteredPosts else nowContents + filteredPosts,
                        currentPage = pageToFetch + 1, //다음 탭을 표시
                        isPageLoading = false,
                        isLastPage = !pageModel.hasNext // 다음 페이지 유무 업데이트
                    ) }
                },
                errorCallback = {
                    updateState { copy(
                        isPageLoading = false,
                        nowContents = emptyList()
                    ) }
                }
            )
        }

         */
    }


    //이동 로직
    fun navigateWrite(){
        emitEvent(CommunityFragmentEvent.NavigateWrite)
    }

    fun navigateSearch(){
        emitEvent(CommunityFragmentEvent.NavigateSearch)
    }


    
    

}


data class CommunityFragmentUiState(

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
            comments = 8,
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
            comments = 12,
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
            comments = 10,
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
            comments = 15,
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
            comments = 20,
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

    // 게시글 필터링 용도
    val whichTab: ContentType = ContentType.ALL, //얘는 tabLayout 선택 여부

    // 현재 탭에 맞는 게시글들
    val nowContents: List<ContentItem> = listOf(),

    // 무한 스크롤 및 로딩 제어
    val currentPage: Int = 0,           // 현재 페이지 인덱스 (0부터 시작)
    val isPageLoading: Boolean = false,  // 중복 호출 방지용 로딩 플래그
    val isLastPage: Boolean = false      // 서버 응답의 hasNext 기반 마지막 여부


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object NavigateWrite : CommunityFragmentEvent
    object NavigateSearch : CommunityFragmentEvent




}