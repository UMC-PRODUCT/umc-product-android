package com.umc.presentation.ui.community.detail

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.mypage.CommentItem
import com.umc.domain.model.mypage.ContentItem
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
            username = "새 유저",
            writeTime = "방금 전",
            comment = text
        )
        val updatedComments = uiState.value.nowCommentList+ newComment
        rebuildDetailList(uiState.value.nowContent, updatedComments)
    }

    // 좋아요 토글
    fun toggleLike() {
        val current = uiState.value.nowContent
        val newIsLiked = !current.isLiked
        val newLikes = if (newIsLiked) current.likes + 1 else current.likes - 1

        val updatedContent = current.copy(isLiked = newIsLiked, likes = newLikes)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)
    }

    // 스크랩 토글
    fun toggleScrap(){
        val current = uiState.value.nowContent
        val newIsScrapped = !current.isScrapped
        val newScraps = if (newIsScrapped) current.scraps + 1 else current.scraps - 1

        val updatedContent = current.copy(isScrapped = newIsScrapped, scraps = newScraps)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)
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
        category = CategoryType.STUDY,
        region = "인천",
        title = "질문있습니다.",
        username = "박유수(어헛차)",
        writeTime = "1시간 전",
        likes = 5,
        comments = 0,
        scraps = 5,
        content = "내용입니다...",
        userPart = UserPart.ANDROID,
        isLiked = false,
        isScrapped = false,
        contentType = ContentType.QUESTION,
        recruitType = RecruitType.RECRUIT,
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