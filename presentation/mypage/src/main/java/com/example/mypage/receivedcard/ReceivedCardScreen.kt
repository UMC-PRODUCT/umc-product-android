package com.example.mypage.receivedcard


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.mycard.MycardViewModel
import com.example.mypage.nearby.NearbyViewModel
import com.example.mypage.qrcode.QrCodeScreenTopBar
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.black
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey500
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.mypage.UserCardPartType

data class PartTheme(
    val badgeColor: Color,
    val backgroundBrush: Brush
)

fun getPartTheme(partType: UserCardPartType): PartTheme {
    val baseColor = Color(partType.mainColorHex)

    //알파값 적용 (0% -> 0.0f, 10% -> 0.1f, 80% -> 0.8f)
    val badgeColor = baseColor.copy(alpha = 0.80f)
    val bgStartColor = Color.White // 배경 상단 Base (흰색과 조합하여 0% 표현) 또는 baseColor.copy(alpha = 0.0f)
    val bgEndColor = baseColor.copy(alpha = 0.10f)

    return PartTheme(
        badgeColor = badgeColor,
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.02f), // 0%에 가까운 연한 톤
                bgEndColor                      // 10% 톤으로 자연스러운 그라데이션
            )
        )
    )
}


@Composable
fun ReceivedCardRoute(
    viewModel: ReceivedCardViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit, //뒤로가기
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReceivedCardScreen(
        uiState = uiState,
        onBackClick = onNavigateToBack,
        onSearchQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
        onClearSearch = { viewModel.clearSearchQuery() },
    )

}

@Composable
fun ReceivedCardScreen(
    uiState: ReceivedCardUiState,
    onBackClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,

){

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() } //상호작용 수집
    val isFocused by interactionSource.collectIsFocusedAsState() //입력창 포커스 체크


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())

    ) {

        // 1. 상단 바 (뒤로가기)
        ReceivedCardScreenTopBar(
            onBackClick = onBackClick,
        )

        // 2. 검색 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = AppStrings.RECEIVED_CARD_TEXTHOLDER,
                backgroundColor = grey100(),
                strokeColor = Color.Transparent,
                focusStrokeColor = grey900(),
                interactionSource = interactionSource,
                prevIcon = painterResource(id = R.drawable.ic_search),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                nextIcon = if (uiState.searchQuery.isNotEmpty()) painterResource(id = R.drawable.ic_delete) else null,
                nextIconTint = grey500(),
                nextIconSize = 24.dp,
                onClickNextIcon = onClearSearch
            )

            // 포커스되어 있거나 검색어가 있을 때 [취소] 버튼 출현
            AnimatedVisibility(
                visible = isFocused || uiState.searchQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                UText(
                    text = AppStrings.CANCEL,
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950(),
                    modifier = Modifier
                        .padding(start = 18.dp)
                        .clickable {
                            onClearSearch()
                            focusManager.clearFocus()
                        }
                )
            }
        }

        Spacer(
            modifier = Modifier
                .height(16.dp)
        )

        //3. 명함 수
        UText(
            text = "${uiState.filteredCards.size}장",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(
            modifier = Modifier
                .height(16.dp)
        )

        //4. 명함 lazyGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = uiState.filteredCards,
                key = { it.id }
            ) { card ->
                ReceivedCardItem(
                    card = card,
                )
            }
        }
    }
}


/**받은 명함 Top bar**/
@Composable
fun ReceivedCardScreenTopBar(
    onBackClick: () -> Unit, //뒤로 가기
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey000()),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        //뒤로 가기 버튼
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = Color.Transparent, shape = CircleShape)
                .clip(CircleShape)
                .clickable(
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_back
                ),
                contentDescription = null,
                tint = grey950(),
                modifier = Modifier.size(24.dp)
            )
        }

        UText(
            text = AppStrings.RECEIVED_CARD_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            modifier = Modifier
                .padding(horizontal = 6.dp)

        )
    }
}

/** 개별 개별 명함 아이템 컴포저블 */
@Composable
private fun ReceivedCardItem(
    card: UserCard,
) {
    val partTheme = getPartTheme(card.part)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(partTheme.backgroundBrush)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //[상단] 프로필 이미지 + 파트 뱃지 + 기수 뱃지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                //프로필 로고
                AsyncImage(
                    model = card.profileImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, grey200(), CircleShape),
                    placeholder = painterResource(R.drawable.ic_profile_default),
                    error = painterResource(R.drawable.ic_profile_default)
                )

                // 뱃지 영역 (파트 + 기수)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 파트 뱃지
                    UButton(
                        text = card.part.label,
                        enabled = false,
                        backgroundColor = partTheme.badgeColor,
                        textColor = grey000(),
                        textStyle = UmcTypographyTokens.Caption1,
                        cornerRadius = 4.dp,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        onClick = {}
                    )

                    // 기수 뱃지
                    UButton(
                        text = "${card.generation}기",
                        enabled = false,
                        backgroundColor = partTheme.badgeColor,
                        textColor = grey000(),
                        textStyle = UmcTypographyTokens.Caption1,
                        cornerRadius = 4.dp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        onClick = {}
                    )
                }
            }

            //[하단] 이름/닉네임 + 학교
            Column {
                UText(
                    text = "${card.name}/${card.nickname}",
                    style = UmcTypographyTokens.Title3Bold,
                    color = black()
                )

                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                )

                UText(
                    text = card.university,
                    style = UmcTypographyTokens.Footnote,
                    color = grey500()
                )
            }
        }
    }
}