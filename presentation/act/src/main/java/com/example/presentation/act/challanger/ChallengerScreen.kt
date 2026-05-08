package com.example.presentation.act.challanger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
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
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.warning100
import com.umc.component.theme.warning300
import com.umc.component.theme.warning500


@Composable
fun ChallengerRoute() {
    ChallengerScreen()
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
fun ChallengerScreen(
) {
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    var isSearchFocused by rememberSaveable { mutableStateOf(false) }

    val sectionTestData = listOf(
        ChallengerSectionUi(
            partName = "PM",
            members = listOf(
                ChallengerMemberUi(
                    nicknameWithName = "홍길동(닉네임)",
                    generation = "기수",
                    roleBadge = "회장"
                )
            )
        ),
        ChallengerSectionUi(
            partName = "Design",
            members = listOf(
                ChallengerMemberUi("홍길동(닉네임)", "기수"),
                ChallengerMemberUi("홍길동(닉네임)", "기수")
            )
        ),
        ChallengerSectionUi(
            partName = "Web",
            members = listOf(
                ChallengerMemberUi("홍길동(닉네임)", "기수"),
                ChallengerMemberUi("홍길동(닉네임)", "기수")
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SearchBar(
                searchKeyword = searchKeyword,
                onSearchKeywordChange = { searchKeyword = it },
                effectiveSearchFocused = isSearchFocused,
                onSearchFocusChange = { focused ->
                    isSearchFocused = focused
                }
            )
        }
        items(sectionTestData) { section ->
            ChallengerSection(section = section)
        }
    }

}

@Composable
private fun SearchBar(
    searchKeyword: String,
    onSearchKeywordChange: (String) -> Unit,
    effectiveSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .background(neutral000())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (effectiveSearchFocused) neutral000() else neutral100())
                .border(
                    width = 1.dp,
                    color = if (effectiveSearchFocused) neutral900() else neutral100(),
                    shape = RoundedCornerShape(8.dp)
                )
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (!effectiveSearchFocused) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = neutral400()
                )
                Spacer(Modifier.width(10.dp))
            }

            BasicTextField(
                value = searchKeyword,
                onValueChange = onSearchKeywordChange,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { onSearchFocusChange(it.isFocused) }
                    .wrapContentHeight(),
                singleLine = true,
                textStyle = Body,
                cursorBrush = SolidColor(neutral800()),
                decorationBox = { innerTextField ->
                    if (searchKeyword.isBlank()) {
                        UText(
                            text = AppStrings.CHALLENGER_SEARCH_PLACEHOLDER,
                            style = Body,
                            color = if (effectiveSearchFocused) neutral900() else neutral400()
                        )
                    }
                    innerTextField()
                }
            )

            if (effectiveSearchFocused) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_failed),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = neutral300()
                )
            }
        }
    }
}

@Composable
private fun ChallengerSection(section: ChallengerSectionUi) {
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
                ChallengerMemberRow(member = member)
            }
        }
    }
}

@Composable
private fun ChallengerMemberRow(member: ChallengerMemberUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

        member.roleBadge?.let { role ->
            Box(
                modifier = Modifier
                    .width(37.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(warning100())
                    .border(1.dp, warning300(), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = role,
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

private data class ChallengerSectionUi(
    val partName: String,
    val members: List<ChallengerMemberUi>
)

private data class ChallengerMemberUi(
    val nicknameWithName: String,
    val generation: String,
    val roleBadge: String? = null
)

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
        ChallengerScreen()
    }
}

