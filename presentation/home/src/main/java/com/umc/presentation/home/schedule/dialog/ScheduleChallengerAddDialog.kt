package com.umc.presentation.home.schedule.dialog


import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import com.umc.domain.model.home.ParticipantItem
import com.umc.component.R
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral400
import com.umc.domain.model.home.SearchResultItem

/**일정 생성에서 챌린저를 선택하는 다이얼로그**/

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

    //리스트 추적
    val listState = rememberLazyListState()

    //무한 스크롤 트리거 로직
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

    //shouldLoadMore이 true일 때 go
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && hasNext) {
            onLoadMore()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = neutral000(),
        dragHandle = { BottomSheetDefaults.DragHandle(color = neutral600()) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {

            //1. 다이얼로그 헤더
            ChallengerHeader(
                selectedParticipants = selectedParticipants,
                selectedParticipantsString = selectedParticipantsString,
                onConfirm = onConfirm,
                onDismissRequest = onDismissRequest
            )

            Spacer(modifier = Modifier
                .height(24.dp)
            )

            //2. 검색 창
            UTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = AppStrings.HOME_PLAN_ADD_PLAN_PARTICIPANT_PLACEHOLDER,
                modifier = Modifier
                    .fillMaxWidth(),
                prevIcon = painterResource(R.drawable.ic_search),
                prevIconTint = neutral400(),
                prevIconSize = 20.dp
            )

            Spacer(modifier = Modifier
                .height(24.dp)
            )

            //3. 검색 유무에 따른 분기 처리
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        color = primary500()
                    )
                }

            }

        }
    }

}

/**
 * 다이얼로그의 제목 및 확인 버튼이 포함되 페더
 * **/    
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
            color = neutral800(),
            modifier = Modifier
                .weight(1f)
        )

        UButton(
            text = AppStrings.CONFIRM,
            backgroundColor = primary500(),
            textColor = neutral000(),
            onClick = {
                onConfirm(selectedParticipants, selectedParticipantsString)
                onDismissRequest()
            }
        )
    }
}

/**
 * 챌린저 없을 때 보여주는 빈 화면
 * **/
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
            tint = neutral400()
        )

        Spacer(
            modifier = Modifier
                .height(16.dp)
        )

        UText(
            text = AppStrings.HOME_PLAN_ADD_PLAN_PARTICIPANT_NO_CHALLENGER,
            style = UmcTypographyTokens.Body,
            color = neutral600()
        )
    }
}

/**
 * 선택된 챌린저들을 보여주는 화면
 * **/
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
 * 인원 검색 리스트 뷰
 * **/
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
 * 이미 추가된 참석자 항목 뷰 (이름, 기수 정보 및 우측 X 삭제 버튼)
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
                .border(width = 1.dp, color = neutral200(), shape = CircleShape),
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
                text = item.name,
                style = UmcTypographyTokens.SubheadlineBold,
                color = neutral800()
            )

            Spacer(modifier = Modifier
                .height(4.dp)
            )

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = neutral800()
            )
        }

        Spacer(modifier = Modifier
            .width(8.dp)
        )

        //삭제 버튼 (빨간색 테두리 및 텍스트)
        UButton(
            text = AppStrings.DELETE,
            onClick = onRemoveClick,
            backgroundColor = neutral000(),
            textColor = danger500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            borderColor = danger500(),
            borderWidth = 1.dp,
            cornerRadius = 8.dp,

        )
    }
}

/**
 * 검색 결과 목록의 개별 유저 뷰 (우측 체크박스 포함)
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
                .border(width = 1.dp, color = neutral200(), shape = CircleShape),
            placeholder = painterResource(R.drawable.ic_profile_default),
            error = painterResource(R.drawable.ic_profile_default)
        )

        Spacer(modifier = Modifier.width(8.dp))

        //이름 및 학교 텍스트
        Column(modifier = Modifier
            .weight(1f)
        ) {
            UText(
                text = item.name,
                style = UmcTypographyTokens.SubheadlineBold,
                color = neutral800()
            )

            Spacer(modifier = Modifier
                .height(4.dp)
            )

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = neutral800()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        //체크박스
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggleClick() },
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(checkedColor = primary500())
        )
    }
}

/**
 * 파트별(Android, Server 등) 구분을 지어주는 헤더 셀
 */
@Composable
fun PartHeaderRow(title: String) {
    UText(
        text = title,
        style = UmcTypographyTokens.Body,
        color = neutral800(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}


/**
 * API로 받아온 단순 유저 리스트를 파트(UserPart) 기준으로 그룹핑하고,
 * 각각의 그룹 상단에 'Header' 타입의 아이템을 꽂아넣어 UI 렌더링에 최적화된 리스트로 변환
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
    