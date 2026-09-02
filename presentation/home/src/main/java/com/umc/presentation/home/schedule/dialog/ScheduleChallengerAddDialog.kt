package com.umc.presentation.home.schedule.dialog


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.domain.model.home.ParticipantItem
import com.umc.component.R
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.domain.model.home.SearchResultItem

/**
 * 일정 생성 및 수정에서 참여 챌린저를 검색하고 다중 선택하는 바텀시트 다이얼로그 컴포저블
 * 지만 챌린저 검색이 필요한 곳에서 응용이 가능합니다.
 * [주의]
 * 사용을 위해 ScheduleChallengerAddDialogViewModel을 별도로 사용해야 합니다. (추가 기록 저장)
 * 사용 예시는 ShcedueleAddScreen을 참고하세요.
 *
 * 실시간 검색 쿼리에 따라 회원 리스트를 페이징 조회하고 파트(Android, Server 등)별로 그룹화하여 렌더링하며,
 * 선택된 챌린저 명단(selectedParticipants)을 동기화하여 확정 버튼 클릭 시 상위 일정 추가 스크린으로 전달합니다.
 *
 * 주요 동작 흐름:
 * 1. 검색창(searchQuery)이 비어있는 일반 상태(!isSearching)에서는 현재 선택 완료된 챌린저 리스트(SelectedParticipantList)를 노출합니다.
 * 2. 검색어를 입력 중인 상태(isSearching == true)에서는 서버 API에서 조회된 검색 결과 리스트(SearchParticipantList)를 파트별 그룹 헤더와 함께 렌더링합니다.
 * 3. LazyColumn 스크롤 시 바닥에서 2번째 항목 노출 시점에서 derivedStateOf 및 LaunchedEffect를 이용해 다음 페이지 데이터(onLoadMore)를 무한 스크롤로 로드합니다.
 * 4. 바텀시트 내부 스크롤 시 모달 레이아웃이 닫히지 않도록 NestedScrollConnection을 선언하여 내부 리스트 스크롤 영역을 격리합니다.
 *
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleChallengerAddBottomSheet(
    //UI 및 인자 전달 용도
    searchQuery: String, //검색 인자
    isSearching: Boolean, //검색 여부
    isLoading: Boolean, //로딩 여부
    hasNext: Boolean, //다음 유저 리스트 시 여부
    selectedParticipants: List<ParticipantItem>, //선택된 챌린저 리스트
    selectedParticipantsString: String, //선택된 챌린저 이름들(UI 용도)
    searchResults: List<ParticipantItem>, //검색 결과 리스트

    //사용자 액션 이벤트 콜백
    onQueryChanged: (String) -> Unit,
    onLoadMore: () -> Unit, //스크롤 바닥에 닿을 시 유저 데이터 재호출 로직
    onToggleParticipant: (ParticipantItem) -> Unit, //토클 시 로직
    onConfirm: (List<ParticipantItem>, String) -> Unit,
    onDismissRequest: () -> Unit
){

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    //LazyColumn 내부 스크롤 시 바텀시트 전체가 끌려 내려가지 않도록 차단하는 Connection
    val lazyColumnNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 아래로 스크롤할 때(available.y > 0) 바텀시트가 이 이벤트를 훔쳐가지 못하도록
                // LazyColumn 영역에서는 오직 리스트 스크롤만 동작하게 이벤트를 격리합니다.
                return Offset.Zero
            }
        }
    }

    // 페이징 스크롤 상태 추적을 위한 LazyListState 객체
    val listState = rememberLazyListState()

    // 리스트 하단 진입 시(스크롤) 다음 페이지 추가 로드를 트리거하는 상태 감지 변수
    val shouldLoadMore = remember {
        derivedStateOf {
            //현재 화면에 렌더링된 거 중 제일 마지막 리스트
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            //전체 아이템 수
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            //바닥에서 5번째 도달 시 OK
            lastVisibleItemIndex >= totalItemsCount - 2 && totalItemsCount > 0
        }
    }

    // 추가 데이터 로드 조건 만족 시 onLoadMore 콜백 호출
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && hasNext) {
            onLoadMore()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = grey000(),
        dragHandle = { BottomSheetDefaults.DragHandle(color = grey600()) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {

            //1. 다이얼로그 헤더 및 확정 버튼 컴포저블
            ChallengerHeader(
                selectedParticipants = selectedParticipants,
                selectedParticipantsString = selectedParticipantsString,
                onConfirm = onConfirm,
                onDismissRequest = onDismissRequest
            )

            Spacer(modifier = Modifier
                .height(24.dp)
            )

            //2. 챌린저 이름 및 닉네임 검색 텍스트 필드 컴포저블
            UTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = AppStrings.HOME_PLAN_ADD_PLAN_PARTICIPANT_PLACEHOLDER,
                modifier = Modifier
                    .fillMaxWidth(),
                prevIcon = painterResource(R.drawable.ic_search),
                prevIconTint = grey400(),
                prevIconSize = 20.dp
            )

            Spacer(modifier = Modifier
                .height(24.dp)
            )

            //3. 검색 중 여부에 따른 리스트 스위칭 박스 (NestedScroll 수신기 적용)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(lazyColumnNestedScrollConnection)
            ) {
                //분기 A: 검색창이 비어있을 때 -> 이미 선택된 챌린저 목록 노출
                if (!isSearching) {
                    //그런데 선택된 인원이 없어
                    if (selectedParticipants.isEmpty()) {
                        EmptyParticipantContent()
                    }
                    // 선택된 인원이 있을 때: 삭제(X) 버튼이 포함된 목록 렌더링
                    else {
                        SelectedParticipantList(
                            participants = selectedParticipants,
                            onRemoveClick = onToggleParticipant
                        )
                    }
                }
                //분기 B: 검색 중일 때 -> API로 검색된 결과 리스트 노출
                else {
                    SearchParticipantList(
                        listState = listState,
                        searchResults = searchResults,
                        selectedParticipants = selectedParticipants,
                        onToggleClick = onToggleParticipant
                    )
                }

                //로딩 바
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = indigo500()
                    )
                }

            }

        }
    }

}

/**
 * 참여자 다이얼로그의 타이틀과 최종 선택 확정 버튼을 포함하는 헤더 컴포저블
 */
@Composable
fun ChallengerHeader(
    selectedParticipants: List<ParticipantItem>,
    selectedParticipantsString: String,
    onConfirm: (List<ParticipantItem>, String) -> Unit,
    onDismissRequest: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UText(
            text = AppStrings.HOME_PLAN_ADD_PLAN_PARTICIPANT_TITLE,
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier
                .weight(1f)
        )

        UButton(
            text = AppStrings.CONFIRM,
            backgroundColor = indigo500(),
            textColor = grey000(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            textStyle = UmcTypographyTokens.Caption1Bold,
            cornerRadius = 4.dp,
            onClick = {
                onConfirm(selectedParticipants, selectedParticipantsString)
                onDismissRequest()
            }
        )
    }
}

/**
 * 선택된 챌린저가 없을 때 중앙에 아이콘과 안내 텍스트를 노출하는 뷰 컴포저블
 */
@Composable
fun EmptyParticipantContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_people),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp),
            tint = grey400()
        )

        Spacer(
            modifier = Modifier
                .height(16.dp)
        )

        UText(
            text = AppStrings.HOME_PLAN_ADD_PLAN_PARTICIPANT_NO_CHALLENGER,
            style = UmcTypographyTokens.Body,
            color = grey600()
        )
    }
}

/**
 * 현재 추가된 챌린저들의 리스트(삭제 버튼 포함)를 보여주는 컴포저블
 */
@Composable
fun SelectedParticipantList(
    participants: List<ParticipantItem>,
    onRemoveClick: (ParticipantItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(participants) { _, item ->
            AddedParticipantRow(
                item = item,
                onRemoveClick = { onRemoveClick(item) }
            )
        }
    }
}

/**
 * 검색된 챌린저 결과를 파트별 헤더와 체크박스 항목으로 렌더링하는 컴포저블
 */
@Composable
fun SearchParticipantList(
    listState: LazyListState,
    searchResults: List<ParticipantItem>,
    selectedParticipants: List<ParticipantItem>,
    onToggleClick: (ParticipantItem) -> Unit
) {
    // Recomposition이 발생해도 searchResults가 같으면 재가공하지 않도록 캐싱
    val processedResults = remember(searchResults) {
        processSearchResults(searchResults)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(processedResults) { _, searchItem ->
            when (searchItem) {
                is SearchResultItem.Header -> {
                    PartHeaderRow(title = searchItem.partName)
                }
                is SearchResultItem.Participant -> {
                    val isChecked = selectedParticipants.any { it.id == searchItem.user.id }
                    SearchParticipantRow(
                        item = searchItem.user,
                        isChecked = isChecked,
                        onToggleClick = { onToggleClick(searchItem.user) }
                    )
                }
            }
        }
    }
}


/**
 * 이미 일정 참여자로 추가된 챌린저의 프로필 및 삭제(X) 버튼 행을 표시하는 컴포저블
 */
@Composable
fun AddedParticipantRow(
    item: ParticipantItem,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        //프로필 이미지
        AsyncImage(
            model = item.profileImage,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = grey200(), shape = CircleShape),
            placeholder = painterResource(R.drawable.ic_profile_default),
            error = painterResource(R.drawable.ic_profile_default)
        )

        Spacer(modifier = Modifier
            .width(8.dp)
        )

        //이름 및 학교 텍스트
        Column(modifier = Modifier
            .weight(1f)
        ) {
            UText(
                text = "${item.name}/${item.nickname} (${item.gisu}기)",
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )

            Spacer(modifier = Modifier
                .height(4.dp)
            )

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = grey800()
            )
        }

        Spacer(modifier = Modifier
            .width(8.dp)
        )

        //삭제 버튼 (빨간색 테두리 및 텍스트)
        UButton(
            text = AppStrings.DELETE,
            onClick = onRemoveClick,
            backgroundColor = red100(),
            textColor = red500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 4.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),

        )
    }
}

/**
 * 검색 목록 내 개별 챌린저 정보 및 선택 체크박스 행을 표시하는 컴포저블
 */
@Composable
fun SearchParticipantRow(
    item: ParticipantItem,
    isChecked: Boolean,
    onToggleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleClick() } //행 전체 터치 시 체크 토글
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        //프로필 이미지
        AsyncImage(
            model = item.profileImage,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = grey200(), shape = CircleShape),
            placeholder = painterResource(R.drawable.ic_profile_default),
            error = painterResource(R.drawable.ic_profile_default)
        )

        Spacer(modifier = Modifier.width(8.dp))

        //이름 및 학교 텍스트
        Column(modifier = Modifier
            .weight(1f)
        ) {
            UText(
                text = "${item.name}/${item.nickname} (${item.gisu}기)",
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )

            Spacer(modifier = Modifier
                .height(4.dp)
            )

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = grey800()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        //체크박스
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggleClick() },
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(checkedColor = indigo500())
        )
    }
}

/**
 * 파트(Android, Server 등) 구분을 표현하는 헤더 셀 컴포저블
 */
@Composable
fun PartHeaderRow(title: String) {
    UText(
        text = title,
        style = UmcTypographyTokens.Body,
        color = grey800(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}


/**
 * API로 받아온 단순 유저 리스트를 파트(UserPart) 기준으로 그룹핑하고,
 * 각각의 그룹 상단에 'Header' 타입의 아이템을 꽂아넣어 UI 렌더링에 최적화된 리스트(SearchResultItem)로 변환하는 메서드
 */
private fun processSearchResults(results: List<ParticipantItem>): List<SearchResultItem> {
    if (results.isEmpty()) return emptyList()

    return results.groupBy { it.userPart }
        .flatMap { (part, members) ->
            // 그룹핑된 파트 라벨을 헤더로 추가하고, 그 뒤에 해당 파트의 멤버들을 붙임
            listOf(SearchResultItem.Header(part.label)) +
                    members.map { SearchResultItem.Participant(it) }
        }
}
    