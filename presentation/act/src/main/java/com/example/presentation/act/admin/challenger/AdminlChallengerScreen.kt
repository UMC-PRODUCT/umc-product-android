package com.example.presentation.act.admin.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.warning100
import com.umc.component.theme.warning500


@Composable
fun AdminChallengerRoute(
    viewModel: AdminChallengerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminChallengerScreen(
        uiState = uiState,
        onSearchKeywordChange = viewModel::onSearchKeywordChanged,
        onMemberClick = viewModel::getChallengerDetail
    )
}


@Composable
private fun EmptyScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = neutral600(),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            UText(
                text = AppStrings.EMPTY_SEARCH_RESULT,
                style = Body,
                color = neutral600()
            )
        }
    }
}

@Composable
fun AdminChallengerScreen(
    uiState: AdminChallengerUiState = AdminChallengerUiState(),
    onSearchKeywordChange: (String) -> Unit = {},
    onMemberClick: (Long) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SearchBar(
                searchKeyword = uiState.searchKeyword,
                onSearchKeywordChange = onSearchKeywordChange
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

}

@Composable
private fun SearchBar(
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .background(neutral000())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        UTextField(
            value = searchKeyword,
            onValueChange = onSearchKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = AppStrings.CHALLENGER_SEARCH_PLACEHOLDER,
            placeholderColor = neutral400(),
            textColor = neutral800(),
            textStyle = Body,
            backgroundColor = neutral100(),
            strokeColor = neutral100(),
            focusStrokeColor = neutral100(),
            prevIcon = painterResource(R.drawable.ic_search),
            prevIconTint = neutral400()
        )
    }
}

@Composable
private fun ChallengerSection(
    section: AdminChallengerSectionUi,
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
                color = neutral800()
            )
            Spacer(Modifier.width(6.dp))
            UText(
                text = "(${section.members.size})",
                style = Subheadline,
                color = neutral800()
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(neutral000())
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
    member: AdminChallengerMemberUi,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, neutral200(), CircleShape)
                .padding(1.dp)
                .clip(CircleShape)
                .background(neutral000()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = neutral400(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = member.nicknameWithName,
                style = BodyBold,
                color = neutral800()
            )
            Spacer(Modifier.width(8.dp))
            UText(
                text = member.generation,
                style = Footnote,
                color = neutral600()
            )
        }

        member.totalScore.let { score ->
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(warning100())
                    .border(1.dp, warning100(), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = "총점수 $score",
                    style = Caption1Bold,
                    color = warning500()
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
                tint = neutral400(),
                modifier = Modifier.size(7.dp,14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun preview() {
    UmcTheme(darkTheme = false) {
        EmptyScreen()
    }
}

@Preview(showBackground = true, name = "Unfocused State")
@Composable
private fun PreviewMainUnfocused() {
    UmcTheme(darkTheme = false) {
        AdminChallengerScreen()
    }
}
