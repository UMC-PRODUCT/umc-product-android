package com.umc.presentation.ui.community.write

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.usecase.community.CreateCommunityLightningPostUseCase
import com.umc.domain.usecase.community.CreateCommunityPostUseCase
import com.umc.domain.usecase.community.GetCommunityPostDetailUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.suggest.SuggestWriteFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostWriteViewModel @Inject
constructor(
    private val createCommunityPostUseCase: CreateCommunityPostUseCase, //게시글 생성
    private val createCommunityLightningPostUseCase: CreateCommunityLightningPostUseCase, //번개 게시글 생성
    private val updateCommunityPostUseCase: CreateCommunityPostUseCase, //게시글 수정
    private val getCommunityPostDetailUseCase: GetCommunityPostDetailUseCase, //게시글 상세 불러오기 (게시글 수정)

) : BaseViewModel<PostWriteFragmentUiState, PostWriteFragmentEvent>(
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
            copy(selectCommunityCategory = category)
        }

        //추가 카테고리가 번개냐?
        if(category.label == CommunityCategoryType.LIGHTNING.label){
            setLightCardView(true)
        }
        else{
            setLightCardView(false)
        }

    }

    //게시글 수정일 시 게시글 데이터를 받와야 작성 채우기
    fun settingUpdatePost(postId : Long){
        viewModelScope.launch {
            resultResponse(
                response = getCommunityPostDetailUseCase(postId),
                successCallback = {
                    //번개 글인지 확인
                    val isLightPost = if(it.lightningInfo != null) true else false
                    setLightCardView(isLightPost)
                    val time = it.lightningInfo?.meetAt ?: ""
                    val people = it.lightningInfo?.maxParticipants.toString() ?: ""
                    val place = it.lightningInfo?.location ?: ""
                    val openChat = it.lightningInfo?.openChatUrl ?: ""

                    updateState {
                        copy(
                            updatePostId = postId,
                            title = it.title,
                            content = it.content,
                            selectCommunityCategory = it.category,
                            isLight = isLightPost,
                            lightTime = time,
                            lightPeople = people,
                            lightPlace = place,
                            lightOpenChat = openChat
                        )
                    }

                    emitEvent(PostWriteFragmentEvent.SetTextfields)

                },
                errorCallback = {}
            )
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


    //게시글 등록 시 로직
    fun createPost(){
        //분기 로직 (1. 게시글 수정인가 / 2. 일반 글인가 / 3. 번개 글인가)
        val postId = uiState.value.updatePostId

        // 게시글 수정
        if(postId != -1L){


            return
        }



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

    //뒤로 가기
    fun onClickBackPressed(){
        emitEvent(PostWriteFragmentEvent.ClickBackPressed)
    }
}

data class PostWriteFragmentUiState(

    //게시글 수정을 위한 게시글 ID
    val updatePostId : Long = -1L,

    val selectCommunityCategory : CommunityCategoryType = CommunityCategoryType.FREE,

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


    //뒤로 갈 때
    object ClickBackPressed : PostWriteFragmentEvent

    //채울 떄
    object SetTextfields : PostWriteFragmentEvent



}