package com.umc.presentation.ui.community.detail

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.home.PlanDetailFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PostDetailViewModel @Inject
constructor() : BaseViewModel<PostDetailFragmentUiState, PostDetailFragmentEvent>(
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

    // 테스트용: 댓글 작성 기능


    fun addComment(text: String) {
        val newComment = CommentItem(
            postId = -1L,
            challengerId = -1L,
            challengerName = "새 유저",
            content = text,
            createdAt = "방금 전",
            commentId = -1L,
        )
        val updatedComments = uiState.value.nowCommentList+ newComment
        rebuildDetailList(uiState.value.nowContent, updatedComments)
        /**TODO 서버에 댓글 추가 로직**/
    }

    // 좋아요 토글
    fun toggleLike() {
        val current = uiState.value.nowContent
        val newIsLiked = !current.isLiked
        val newLikes = if (newIsLiked) current.likes + 1 else current.likes - 1

        val updatedContent = current.copy(isLiked = newIsLiked, likes = newLikes)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)
        /**TODO 서버에 반영**/
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

    //댓글  신고
    fun onClickReportComment(){
        emitEvent(PostDetailFragmentEvent.ReportComment)
    }

    //댓글 삭제
    fun onClickeDeleteComment(item: CommentItem){
        // 현재 저장된 댓글 리스트에서 선택한 아이템만 제외하고 새로운 리스트 생성
        val updatedCommentList = uiState.value.nowCommentList.filterNot { it == item }

        // 리스트 재조립 호출 (본문 + 업데이트된 댓글 리스트)
        rebuildDetailList(uiState.value.nowContent, updatedCommentList)
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

    //보여줄 view들이 담긴 곳 (recyclerview가 inflate할것들)
    val nowDetailList : List<PostDetailItem> = emptyList(),

    //현재 게시글
    val nowContent: ContentItem = ContentItem(
        // 1. 리스트 및 API 핵심 데이터
        postId = 100L,
        title = "UMC 안드로이드 파트장 박유수입니다!",
        category = CommunityCategoryType.FREE,

        // 2. API 미제공 (기본값/X)
        username = "어헛차",           // X: 익명 여부에 따라 매퍼에서 채울 예정
        writeTime = "방금 전",         // X: 서버 시간 파싱 전까지 임시값
        likes = 15,                   // X: 현재 API엔 없지만 UI 확인용
        comments = 5,                 // X: 현재 API엔 없지만 UI 확인용

        // 3. 본문 및 세부 데이터
        content = "이번 데모데이 프로젝트에서 안드로이드 개발자 가이드를 만들고 있습니다. 다들 화이팅!",
        lightningInfo = null,         // 일반 게시글이므로 null

        // 4. 기타 연동 예정 필드 (X)
        userPart = UserPart.ANDROID,  // X: 작성자 파트 정보
        isLiked = false,              // 다른 API 연동 전까지 기본값
        isScrapped = true,             // UI 테스트를 위해 true 설정
        scraps = 10
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



    object MoveBackPressed : PostDetailFragmentEvent



}