package com.umc.presentation.ui.community.detail

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.community.DeleteCommunityCommentUseCase
import com.umc.domain.usecase.community.DeleteCommunityPostUseCase
import com.umc.domain.usecase.community.GetCommunityPostCommentUseCase
import com.umc.domain.usecase.community.GetCommunityPostDetailUseCase
import com.umc.domain.usecase.community.UpdateLikePostUseCase
import com.umc.domain.usecase.community.UpdateScrapPostUseCase
import com.umc.domain.usecase.community.WriteCommunityPostCommentUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.home.PlanDetailFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostDetailViewModel @Inject
constructor(
    private val getCommunityPostDetailUseCase: GetCommunityPostDetailUseCase, //게시글 상세 불러오기
    private val getCommunityPostCommentUseCase: GetCommunityPostCommentUseCase, //게시글 댓글들 불러오기
    private val writeCommunityPostCommentUseCase: WriteCommunityPostCommentUseCase, //댓글 작성하기
    private val getUserInfoUseCase: GetUserInfoUseCase, //유저 정보 불러오기
    private val deleteCommunityPostUseCase: DeleteCommunityPostUseCase, //게시글 삭제하기
    private val deleteCommunityCommentUseCase: DeleteCommunityCommentUseCase, //댓글 삭제하기
    private val updateLikePostUseCase: UpdateLikePostUseCase, //좋아요 토글
    private val updateScrapPostUseCase: UpdateScrapPostUseCase, //스크랩 토글

    ) : BaseViewModel<PostDetailFragmentUiState, PostDetailFragmentEvent>(
    PostDetailFragmentUiState()
) {

    //시작 시 한 번 rebuild
    init {
        rebuildDetailList(uiState.value.nowContent, uiState.value.nowCommentList)
    }


    // 리스트 재조립 로직
    private fun rebuildDetailList(content: ContentItem, comments: List<CommentItem>) {
        val uiList = mutableListOf<PostDetailItem>()

        //본문
        uiList.add(PostDetailItem.Header(content))
        //댓글 수
        uiList.add(PostDetailItem.CommentHeader(comments.size))
        //댓글이 비면 엠티
        if (comments.isEmpty()) {
            uiList.add(PostDetailItem.EmptyComment)
        } 
        //그렇지 않으면 댓글 뾰로롱
        else {
            uiList.addAll(comments.map { PostDetailItem.Comment(it) })
        }

        //얘를 업데이트
        updateState {
            copy(
                nowDetailList = uiList, //결과물 저장
                nowContent = content, // 현재 게시글 데이터 저장
                nowCommentList = comments // 현재 댓글 리스트 저장
            )
        }
    }

    //게시글 정보 + 댓글 정보 + 유저 정보 가져오기
    fun initPostDetailData(postId: Long){
        viewModelScope.launch {
            //0. 유저 정보 가져오기 (별도 분리)
            launch {
                getUserInfoUseCase().collect { userInfo ->
                    updateState { copy(myId = userInfo.id) }
                }
            }

            //1. 서로 다른 usecase를 비동기로 실행
            val detailDeferred = async {
                getCommunityPostDetailUseCase(postId) }
            val commentsDeferred = async {
                getCommunityPostCommentUseCase(postId) }

            //2. 대기
            val detailResult = detailDeferred.await()
            val commentsResult = commentsDeferred.await()

            //3. 결과를 담은 item 생성 = 둘 다 정상으로 받아올 때 수행
            var fetchedContent: ContentItem? = null
            var fetchedComments: List<CommentItem>? = null

            //3. 결과를 따로 처리
            resultResponse(
                response = detailResult,
                successCallback = {
                    fetchedContent = it
                },
                errorCallback = {
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast)
                    emitEvent(PostDetailFragmentEvent.MoveBackPressed)

                }
            )
            resultResponse(
                response = commentsResult,
                successCallback = {
                    fetchedComments = it
                },
                errorCallback = {
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast)
                    emitEvent(PostDetailFragmentEvent.MoveBackPressed)

                }
            )

            //4. 둘다 정상이면 한 번에 재조립
            if (fetchedContent != null && fetchedComments != null) {
                rebuildDetailList(fetchedContent, fetchedComments)
            }

        }
    }

    //댓글 추가하는 함수
    fun addComment(text: String) {
        if(text.length < 1){return}

        viewModelScope.launch {
            val postId = uiState.value.nowContent.postId
            val myId = uiState.value.myId

            resultResponse(
                response = writeCommunityPostCommentUseCase(postId, myId, text),
                successCallback = {
                    //작성 성공 시 refresh
                    refreshComments(postId)
                },
                errorCallback = {}
            )
        }

    }

    //댓글만 갱신할 때 함수
    private fun refreshComments(postId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = getCommunityPostCommentUseCase(postId),
                successCallback = { updatedComments ->
                    // 최신 댓글 목록으로 UI 재조립
                    rebuildDetailList(uiState.value.nowContent, updatedComments)
                }
            )
        }
    }

    //게시글만 갱신할 때 함수
    /**게시글 수정 및 갱신은 별도의 페이지에서 작성하므로 쓰일 일은 없었다,,,**/
    private fun refreshContent(postId: Long){
        viewModelScope.launch {
            resultResponse(
                response = getCommunityPostDetailUseCase(postId),
                successCallback = { updatedContent ->
                    // 최신 포스트 목록으로 재조립
                    rebuildDetailList(updatedContent, uiState.value.nowCommentList)
                }
            )
        }
    }



    // 좋아요 토글
    fun toggleLike() {
        val current = uiState.value.nowContent
        val newIsLiked = !current.isLiked
        val newLikes = if (newIsLiked) current.likes + 1 else current.likes - 1

        val updatedContent = current.copy(isLiked = newIsLiked, likes = newLikes)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)
        /**TODO 서버에 반영**/
        viewModelScope.launch {
            resultResponse(
                response = updateLikePostUseCase(uiState.value.nowContent.postId),
                successCallback = {},
                errorCallback = {}
            )
        }
    }

    // 스크랩 토글
    fun toggleScrap(){
        val current = uiState.value.nowContent
        val newIsScrapped = !current.isScrapped
        val newScraps = if (newIsScrapped) current.scraps + 1 else current.scraps - 1

        val updatedContent = current.copy(isScrapped = newIsScrapped, scraps = newScraps)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)
        /**TODO 서버에 반영**/
    }



    //게시글 삭제 로직
    fun deletePost(){
        viewModelScope.launch {
            resultResponse(
                response = deleteCommunityPostUseCase(uiState.value.nowContent.postId),
                successCallback = {
                    emitEvent(PostDetailFragmentEvent.MoveBackPressed)
                },
                errorCallback = {}
            )
        }
    }

    //댓글 추가
    fun onClickCommentAdd(){
        emitEvent(PostDetailFragmentEvent.OnClickCommentAdd)
    }

    //메뉴 버튼 열기
    fun onClickOpenMenu(){
        updateState { copy(isMenuVisible = !isMenuVisible) }
    }
    
    //게시글 신고
    fun onClickReportPost(){
        emitEvent(PostDetailFragmentEvent.ReportPost)
    }
    //게시글 수정
    fun onClickEditPost(){
        emitEvent(PostDetailFragmentEvent.EditPost)
    }
    //게시글 삭제
    fun onClickDeletePost(){
        emitEvent(PostDetailFragmentEvent.DeletePost)
    }

    //댓글 신고
    fun onClickReportComment(){
        emitEvent(PostDetailFragmentEvent.ReportComment)
    }

    //댓글 삭제
    fun onClickeDeleteComment(item: CommentItem){
        val postId = uiState.value.nowContent.postId
        val commentId = item.commentId
        val challengerId = item.challengerId //이미 id 비교 로직을 거쳤기 때문에, 댓글의 challengerId 사용해도 부압
        viewModelScope.launch {
            resultResponse(
                response =deleteCommunityCommentUseCase(postId, commentId, challengerId),
                successCallback = {
                    //댓글만 다시 갱신하기
                    refreshComments(postId)
                },
                errorCallback = {
                    
                }
            )
        }

    }

    //뒤로가기
    fun moveBackPressed(){
        emitEvent(PostDetailFragmentEvent.MoveBackPressed)
    }





}


data class PostDetailFragmentUiState(
    //메뉴 창 열기
    val isAuthor : Boolean = true,
    val isMenuVisible : Boolean = false,

    //보여줄 view들이 담긴 곳 (recyclerview가 inflate할것들 - 게시글하고 댓글들 조립한 곳들)
    val nowDetailList : List<PostDetailItem> = emptyList(),

    //내 ID
    /**TODO. 얘는 MemberId인지 ChallengerId인지 확인 필요**/
    val myId : Long = -1L,

    //현재 게시글
    val nowContent: ContentItem = ContentItem(
        // 1. 리스트 및 API 핵심 데이터
        postId = -1L,
        title = "",
        category = CommunityCategoryType.FREE,

        // 2. API 미제공 (기본값/X)
        username = "",           // X: 익명 여부에 따라 매퍼에서 채울 예정
        writeTime = "",         // X: 서버 시간 파싱 전까지 임시값
        likes = 0,                   // X: 현재 API엔 없지만 UI 확인용
        comments = 0,                 // X: 현재 API엔 없지만 UI 확인용

        // 3. 본문 및 세부 데이터
        content = "",
        lightningInfo = null,         // 일반 게시글이므로 null

        // 4. 기타 연동 예정 필드 (X)
        userPart = UserPart.ANDROID,  // X: 작성자 파트 정보
        isLiked = false,              // 다른 API 연동 전까지 기본값
        isScrapped = false,             // UI 테스트를 위해 true 설정
        scraps = 0
    ),

    //현재 댓글 리스트
    val nowCommentList : List<CommentItem> = emptyList(),


    ) : UiState

sealed interface PostDetailFragmentEvent : UiEvent {
    
    //댓글 추가 이벤트
    object OnClickCommentAdd : PostDetailFragmentEvent

    //신고하기 이벤트
    object ReportPost : PostDetailFragmentEvent
    //수정하기 이벤트
    object EditPost : PostDetailFragmentEvent
    //삭제하기 이벤트
    object DeletePost : PostDetailFragmentEvent

    //댓글 신고하기 이벤트
    object ReportComment : PostDetailFragmentEvent

    //오류 토스트 이벤트
    object ShowErrorToast : PostDetailFragmentEvent

    object MoveBackPressed : PostDetailFragmentEvent



}