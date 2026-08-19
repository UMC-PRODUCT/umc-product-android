package com.umc.presentation.act.normal.challenger

import com.umc.presentation.act.normal.challenger.dialog.NormalChallengerInfoDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo300
import com.umc.component.theme.indigo500
import com.umc.domain.model.enums.UserPart
private val challengerPartFilters = listOf(
    UserPart.PLAN,
    UserPart.DESIGN,
    UserPart.WEB,
    UserPart.ANDROID,
    UserPart.IOS,
    UserPart.NODEJS,
    UserPart.SPRINGBOOT,
)

@Composable
fun NormalChallengerRoute(
    isActive: Boolean = true,
    viewModel: NormalChallengerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isActive) {
        if (isActive) viewModel.refresh()
    }

    NormalChallengerScreen(
        uiState = uiState,
        onSearchKeywordChange = viewModel::onSearchKeywordChanged,
        onPartFilterClick = viewModel::openPartFilter,
        onMemberClick = viewModel::getChallengerDetail,
        onDismissDialog = viewModel::dismissChallengerDetail
    )

    if (uiState.isPartFilterVisible) {
        NormalChallengerPartBottomSheet(
            selectedPart = uiState.selectedPart,
            onDismissRequest = viewModel::dismissPartFilter,
            onPartSelected = viewModel::selectPartFilter,
        )
    }
}


@Composable
private fun EmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = grey600(),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            UText(
                text = AppStrings.EMPTY_SEARCH_RESULT,
                style = Body,
                color = grey600()
            )
        }
    }
}

@Composable
fun NormalChallengerScreen(
    uiState: NormalChallengerUiState = NormalChallengerUiState(),
    onSearchKeywordChange: (String) -> Unit = {},
    onPartFilterClick: () -> Unit = {},
    onMemberClick: (Long) -> Unit = {},
    onDismissDialog: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(grey100()),
        contentPadding = PaddingValues(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SearchBar(
                searchKeyword = uiState.searchKeyword,
                selectedPart = uiState.selectedPart,
                onSearchKeywordChange = onSearchKeywordChange,
                onPartFilterClick = onPartFilterClick,
            )
        }
        if (uiState.sections.isEmpty()) {
            item { EmptyScreen() }
        } else {
            items(uiState.sections) { section ->
                ChallengerSection(
                    section = section,
                    onMemberClick = onMemberClick
                )
            }
        }
    }

    uiState.selectedChallenger?.let { selectedChallenger ->
        NormalChallengerInfoDialog(
            model = selectedChallenger,
            onDismissRequest = onDismissDialog
        )
    }
}

@Composable
private fun SearchBar(
    searchKeyword: String,
    selectedPart: UserPart?,
    onSearchKeywordChange: (String) -> Unit,
    onPartFilterClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(grey000())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        UTextField(
            value = searchKeyword,
            onValueChange = onSearchKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = AppStrings.CHALLENGER_SEARCH_PLACEHOLDER,
            placeholderColor = grey400(),
            textColor = grey800(),
            textStyle = Body,
            backgroundColor = grey100(),
            strokeColor = grey100(),
            focusStrokeColor = grey900(),
            prevIcon = painterResource(R.drawable.ic_search),
            prevIconTint = grey400()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (selectedPart == null) grey100() else grey800())
                .clickable(onClick = onPartFilterClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UText(
                text = selectedPart?.filterLabel ?: "파트",
                style = Footnote,
                color = if (selectedPart == null) grey600() else grey000(),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_dropdown_down),
                contentDescription = "파트 선택",
                tint = if (selectedPart == null) grey600() else grey000(),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalChallengerPartBottomSheet(
    selectedPart: UserPart?,
    onDismissRequest: () -> Unit,
    onPartSelected: (UserPart) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = grey000(),
        dragHandle = { BottomSheetDefaults.DragHandle(color = grey600()) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            UText(
                text = "파트를 선택하세요",
                style = Title3Bold,
                color = grey800(),
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(challengerPartFilters, key = UserPart::name) { part ->
                    UText(
                        text = part.filterLabel,
                        style = Body,
                        color = if (selectedPart == part) indigo500() else grey800(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPartSelected(part) }
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}

private val UserPart.filterLabel: String
    get() = when (this) {
        UserPart.PLAN -> "PM"
        UserPart.IOS -> "iOS"
        UserPart.SPRINGBOOT -> "Spring Boot"
        else -> label
    }

@Composable
private fun ChallengerSection(
    section: NormalChallengerSectionUi,
    onMemberClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = section.partName,
                style = HeadlineBold,
                color = grey800()
            )
            Spacer(Modifier.width(6.dp))
            UText(
                text = "(${section.members.size})",
                style = Subheadline,
                color = grey800()
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.members.forEach { member ->
                ChallengerMemberRow(
                    member = member,
                    onClick = { onMemberClick(member.id) }
                )
            }
        }
    }
}

@Composable
private fun ChallengerMemberRow(
    member: NormalChallengerMemberUi,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(grey000())
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, grey200(), CircleShape)
                .padding(1.dp)
                .clip(CircleShape)
                .background(grey000()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = member.nicknameWithName,
                modifier = Modifier.weight(1f, fill = false),
                style = BodyBold,
                color = grey800(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            UText(
                text = member.generation,
                style = Footnote,
                color = grey600()
            )
        }

        member.roleBadge?.let { role ->
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .widthIn(min = 39.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(indigo100())
                    .border(1.dp, indigo300(), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = role,
                    style = Caption1Bold,
                    color = indigo500()
                )
            }
            Spacer(Modifier.width(10.dp))
        }


        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ){
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(7.dp,14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun preview() {
    UmcTheme(darkTheme = false) {
        EmptyScreen()
    }
}

@Preview(showBackground = true, name = "Unfocused State")
@Composable
fun previewMainUnfocused() {
    UmcTheme(darkTheme = false) {
        NormalChallengerScreen(
            uiState = NormalChallengerUiState()
        )
    }
}
