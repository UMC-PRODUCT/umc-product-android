package com.umc.presentation.ui.community.detail

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType
import com.umc.domain.usecase.GetAuthAccessUseCase
import com.umc.domain.usecase.GetChallengerIdUseCase
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.community.DeleteCommunityCommentUseCase
import com.umc.domain.usecase.community.DeleteCommunityPostUseCase
import com.umc.domain.usecase.community.GetCommunityPostCommentUseCase
import com.umc.domain.usecase.community.GetCommunityPostDetailUseCase
import com.umc.domain.usecase.community.ReportCommentUseCase
import com.umc.domain.usecase.community.ReportPostUseCase
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.collections.List


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
    private val reportPostUseCase: ReportPostUseCase, //게시글 신고하기
    private val getChallengerIdUseCase: GetChallengerIdUseCase, //내 ID 가져오기
    private val getAuthAccessUseCase: GetAuthAccessUseCase, //리소스 권한 조회
    private val reportCommentUseCase: ReportCommentUseCase, //댓글 신고하기

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


            //3. 결과를 담은 item 생성 = 둘 다 정상으로 받아올 때 수행
            var fetchedContent: ContentItem? = uiState.value.communityList.find { it.postId == postId }
            var fetchedComments: List<CommentItem>? = uiState.value.communityCommentsMap.getOrDefault(postId, emptyList())


            //4. 댓글만 받아왔거나 다 실패한 경우 나가기
            if(fetchedContent == null && fetchedComments == null){
                //emitEvent(PostDetailFragmentEvent.ShowErrorToast("게시글을 불러오는데 실패했습니다."))
                //emitEvent(PostDetailFragmentEvent.MoveBackPressed)
            }

            //5. 둘다 정상이면 한 번에 재조립
            else if (fetchedContent != null && fetchedComments != null) {
                checkIsAuthor(fetchedContent.postId)
                rebuildDetailList(fetchedContent, fetchedComments)
            }

            //6. 게시글만 받아왔을 경우 (일단 빌드)
            else if(fetchedContent != null){
                checkIsAuthor(fetchedContent.postId)
                rebuildDetailList(fetchedContent, uiState.value.nowCommentList)
                //emitEvent(PostDetailFragmentEvent.ShowErrorToast("댓글을 불러오는데 실패했습니다."))
            }



        }
    }

    //댓글 추가하는 함수
    fun addComment(text: String) {
        if(text.length < 1){return}

        viewModelScope.launch {
            val postId = uiState.value.nowContent.postId
            val myId = uiState.value.myChallengerId

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            val titie = current.format(formatter)

            val newComment = CommentItem(
                100, 102, 401, "박유수",
                "", text, titie, false)

            val updatedComments = uiState.value.nowCommentList + newComment
            rebuildDetailList(uiState.value.nowContent, updatedComments)


            resultResponse(
                response = writeCommunityPostCommentUseCase(postId, myId, text),
                successCallback = {
                    //작성 성공 시 refresh
                    //refreshComments(postId)
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

    //게시글이 현재 유저 것인지 비교
    fun checkIsAuthor(postId : Long){


        viewModelScope.launch {
            resultResponse(
                response = getAuthAccessUseCase(ResourceType.COMMUNITY_POST, postId),
                successCallback = { authAccess ->
                    //삭제 or 수정권한 있는지 체크
                    val isAuthor = authAccess.permissions.any { item ->
                        (item.type == PermissionType.DELETE || item.type == PermissionType.EDIT)
                                && item.hasPermission
                    }
                    Log.d("log_community", "권한 결과: $isAuthor")

                    updateState {
                        if(isAuthor){
                            copy(isAuthor = true)
                        }
                        else{
                            copy(isAuthor = false)
                        }
                    }
            
                },
                errorCallback = {

                }
            )
        }
    }


    // 좋아요 토글
    fun toggleLike() {

        val current = uiState.value.nowContent
        val newIsLiked = !current.isLiked
        var likecount = current.likes
        if(newIsLiked){
            likecount = current.likes + 1
        }else{
            likecount = current.likes - 1
        }
        val newLikes = likecount

        val updatedContent = current.copy(isLiked = newIsLiked, likes = newLikes)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)


        /**서버에 반영**/
        viewModelScope.launch {
            resultResponse(
                response = updateLikePostUseCase(uiState.value.nowContent.postId),
                successCallback = {

                    //성공하면 UI 업데이트
                    val current = uiState.value.nowContent
                    val newIsLiked = !current.isLiked
                    val newLikes = it.likeCount

                    val updatedContent = current.copy(isLiked = newIsLiked, likes = newLikes)
                    rebuildDetailList(updatedContent, uiState.value.nowCommentList)
                },
                errorCallback = {


                }
            )
        }
    }

    // 스크랩 토글
    fun toggleScrap(){

        //성공하면 UI 업데이트
        val current = uiState.value.nowContent
        val newIsScrapped = !current.isScrapped
        var nowsscrap = current.scraps
        if(newIsScrapped){
           nowsscrap = current.scraps + 1
        }
        else{
            nowsscrap = current.scraps - 1
        }
        val newScraps = nowsscrap

        val updatedContent = current.copy(isScrapped = newIsScrapped, scraps = newScraps)
        rebuildDetailList(updatedContent, uiState.value.nowCommentList)

        /**서버에 반영**/
        viewModelScope.launch {
            resultResponse(
                response = updateScrapPostUseCase(uiState.value.nowContent.postId),
                successCallback = {
                    //성공하면 UI 업데이트
                    val current = uiState.value.nowContent
                    val newIsScrapped = !current.isScrapped
                    val newScraps = it.scrapCount

                    val updatedContent = current.copy(isScrapped = newIsScrapped, scraps = newScraps)
                    rebuildDetailList(updatedContent, uiState.value.nowCommentList)
                },
                errorCallback = {
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast("스크랩을 실패했습니다."))

                }
            )
        }
    }


    //게시글 신고
    fun reportPost(){
        val postId = uiState.value.nowContent.postId
        viewModelScope.launch {
            resultResponse(
                response = reportPostUseCase(postId),
                successCallback = {
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast("게시글 신고가 완료되었습니다"))
                },
                errorCallback = { error ->
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast(error.message))
                }
            )
        }

    }

    //댓글 신고
    fun reportComment(commentId: Long){
        viewModelScope.launch {
            resultResponse(
                response = reportCommentUseCase(commentId),
                successCallback = {
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast("댓글 신고가 완료되었습니다"))
                },
                errorCallback = { error ->
                    emitEvent(PostDetailFragmentEvent.ShowErrorToast(error.message))
                }
            )
        }
    }


    //게시글 삭제 로직
    fun deletePost(){
        viewModelScope.launch {
            resultResponse(
                response = deleteCommunityPostUseCase(uiState.value.nowContent.postId),
                successCallback = {
                    emitEvent(PostDetailFragmentEvent.MoveBackPressed)
                },
                errorCallback = {
                    
                }
            )
        }
    }

    //댓글 메뉴 바 눌렀을 때, 자신 댓글인지 판단
    fun onCommentMenuClicked(item: CommentItem) {
        viewModelScope.launch {
            resultResponse(
                // 댓글 권한 조회를 위해 POST_COMMENT 타입 사용 (없다면 Enum에 추가 필요)
                response = getAuthAccessUseCase(ResourceType.COMMUNITY_COMMENT, item.commentId),
                successCallback = { authAccess ->
                    // DELETE 또는 EDIT 권한이 있는지 체크 (팀장님 로직 적용)
                    val isAuthor = authAccess.permissions.any { p ->
                        (p.type == PermissionType.DELETE || p.type == PermissionType.EDIT)
                                && p.hasPermission
                    }

                    // 권한 결과와 함께 이벤트를 던져 Fragment가 팝업을 띄우게 함
                    emitEvent(PostDetailFragmentEvent.ShowCommentMenu(item, isAuthor))
                },
                errorCallback = {
                    //emitEvent(PostDetailFragmentEvent.ShowErrorToast("권한 정보를 불러오지 못했습니다."))
                    emitEvent(PostDetailFragmentEvent.ShowCommentMenu(item, false))
                }
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
    
    //게시글 신고 다이얼로그 열기
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

    val communityCommentsMap: Map<Long, List<CommentItem>> = mapOf(
        // 101L: 서브웨이 번개 (댓글 5개)
        101L to listOf(
            CommentItem(1, 101, 202, "코딩왕", "", "저 지금 정보과학관 4층인데 5분 뒤에 내려갈게요!", "2026.02.19 18:15", false),
            CommentItem(2, 101, 20201469, "박유수", "", "좋아요! 1층 엘리베이터 앞에서 봬요.", "2026.02.19 18:17", true),
            CommentItem(3, 101, 305, "민트초코", "", "저도 가도 되나요? 지금 막 수업 끝났어요!", "2026.02.19 18:20", false),
            CommentItem(4, 101, 20201469, "박유수", "", "그럼요! 얼른 오세요 ㅎㅎ", "2026.02.19 18:22", true),
            CommentItem(5, 101, 305, "민트초코", "", "넵 3분 컷으로 뛰어갑니다!", "2026.02.19 18:25", false)
        ),

        // 102L: 카공 번개 (댓글 2개)
        102L to listOf(
            CommentItem(6, 102, 401, "열공중", "", "내일 몇 시쯤 모이시나요?", "2026.02.19 22:10", false),
            CommentItem(7, 102, 202, "코딩왕", "", "오전 9시쯤 생각 중입니다!", "2026.02.19 22:15", false)
        ),

        // 201L: 키보드 커스텀 (댓글 3개)
        201L to listOf(
            CommentItem(8, 201, 405, "기계식매니아", "", "HHKB는 역시 무접점이죠... 도각거리는 소리가 예술입니다.", "2026.02.19 14:30", false),
            CommentItem(9, 201, 20201469, "박유수", "", "혹시 저소음 적축이랑 비교하면 어떤가요?", "2026.02.19 14:45", true),
            CommentItem(10, 201, 405, "기계식매니아", "", "완전 다른 매력이에요! 나중에 기회 되면 타건 한 번 해보세요.", "2026.02.19 15:00", false)
        ),

        // 202L: 오운완 (댓글 2개)
        202L to listOf(
            CommentItem(11, 202, 20201469, "박유수", "", "운동 자극 받고 갑니다! 🔥", "2026.02.19 23:00", true),
            CommentItem(12, 202, 302, "스쿼트장인", "", "유수님도 오늘 득근하세요!", "2026.02.19 23:10", false)
        ),

        // 301L: Compose 질문 (댓글 1개)
        301L to listOf(
            CommentItem(13, 301, 701, "안드마스터", "", "LazyColumn의 key 값을 고유 ID로 명시해주셨나요?", "2026.02.19 21:05", false)
        ),

        // 302L: Spring Boot 오류 (댓글 4개)
        302L to listOf(
            CommentItem(14, 302, 801, "서버고수", "", "SecurityConfig 클래스 코드 공유 가능하신가요?", "2026.02.19 19:00", false),
            CommentItem(15, 302, 402, "백엔드꿈나무", "", "아! 필터 순서가 잘못되어 있었습니다. 해결했어요!", "2026.02.19 19:20", false),
            CommentItem(16, 302, 801, "서버고수", "", "오 다행이네요! 필터 체인 순서가 중요하긴 하죠.", "2026.02.19 19:30", false),
            CommentItem(17, 302, 20201469, "박유수", "", "저도 비슷한 에러 겪었었는데 도움 됐네요.", "2026.02.19 19:45", true)
        ),

        // 401L: 피그마 단축키 (댓글 1개)
        401L to listOf(
            CommentItem(18, 401, 305, "기획꿈나무", "", "정말 유용한 정보네요! 감사합니다.", "2026.02.18 10:20", false)
        ),

        // 402L: 데모데이 공지 (댓글 3개)
        402L to listOf(
            CommentItem(19, 402, 20201469, "박유수", "", "안드로이드 파트 부스 위치 확인했습니다!", "2026.02.17 11:00", true),
            CommentItem(20, 402, 502, "운영진", "", "네, 당일 10시까지 세팅 부탁드려요.", "2026.02.17 11:15", false),
            CommentItem(21, 402, 805, "UMC원", "", "준비물 리스트는 따로 올라오나요?", "2026.02.17 12:00", false)
        ),

        // 501L: 종강 (댓글 0개)
        501L to emptyList(),

        // 502L: 학생증 분실 (댓글 2개)
        502L to listOf(
            CommentItem(22, 502, 901, "동기A", "", "제 친구가 잃어버린 것 같아요! 과사에 확인해볼게요.", "2026.02.16 16:00", false),
            CommentItem(23, 502, 602, "친절한시민", "", "네, 학생회비 납부 확인 도장 찍혀있더라고요!", "2026.02.16 16:10", false)
        )
    ),

    //메뉴 창 열기
    val isAuthor : Boolean = false,
    val isMenuVisible : Boolean = false,

    //보여줄 view들이 담긴 곳 (recyclerview가 inflate할것들 - 게시글하고 댓글들 조립한 곳들)
    val nowDetailList : List<PostDetailItem> = emptyList(),

    //내 ID
    /**TODO. 얘는 MemberId인지 ChallengerId인지 확인 필요**/
    val myInfo : UserInfo? = null,
    val myChallengerId : Long = -1L,

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
    data class ReportComment(val commentId: Long) : PostDetailFragmentEvent

    //댓글 메뉴 누를시 권한 확인 이벤트
    data class ShowCommentMenu(val item: CommentItem, val canDelete: Boolean) : PostDetailFragmentEvent

    //오류 토스트 이벤트
    data class ShowErrorToast(val errorMessage: String) : PostDetailFragmentEvent

    object MoveBackPressed : PostDetailFragmentEvent



}