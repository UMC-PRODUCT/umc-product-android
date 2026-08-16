package com.example.mypage.mycard

import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.dialog.CardExchangeBottomSheet
import com.example.mypage.mypage.MypageUiState
import com.example.mypage.mypage.SocialBadge
import com.example.mypage.nearby.NearbyEvent
import com.example.mypage.nearby.NearbyViewModel
import com.example.mypage.qrcode.QrCodeUtils
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UToast
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo400
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.mypage.NearbyUserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.toUserCard
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MycardRoute(
    targetMemberId: String? = null, //유저 qr 딥링크로 받았을 때
    openExchangeDialog: Boolean = false, //홈에서 명함 교환 받을 시
    viewModel: MycardViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(), //nearbyConnection 전용 관리 viewModel
    onNavigateToMypage: () -> Unit, //설정으로 이동
    onNavigateToMyqrCode:() -> Unit, // QR 코드 화면으로 이동
    onNavigateToEditCard: () -> Unit, //명함 편집
    onNavigateToReceivedCard: () -> Unit, //받은 명함 이동

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nearbyUiState by nearbyViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    //화면이 resume에서 복귀할떄마다 재호출
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.getUserInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    

    /**TODO. 딥링크 전달 시 API 호출 및 저장**/
    LaunchedEffect(targetMemberId) {
        if (!targetMemberId.isNullOrEmpty()) {
            viewModel.searchUser(targetMemberId.toLong())
        }
    }

    //처음 홈 실행 시 체크
    LaunchedEffect(openExchangeDialog) {
        if(openExchangeDialog == true){
            nearbyViewModel.openBottomSheet()
        }
    }
    
    

    //내 프로필 정보가 업데이트 될 때 NearbyViewModel에도 이를 반영
    LaunchedEffect(uiState.userInfo) {
        if (uiState.userInfo.name.isNotEmpty()) {
            val nearbyInfo = NearbyUserInfo(
                name = uiState.userInfo.name,
                info = "${uiState.userInfo.schoolName} · ${uiState.myRecentInfoString.ifEmpty { "0기" }}",
                profileImageLink = uiState.userInfo.profileImageLink
            )
            //내 카드 업데이트
            val myCard = uiState.userInfo.toUserCard()
            nearbyViewModel.setMyUserInfo(nearbyInfo)
            nearbyViewModel.setMyUserCard(myCard)
        }
    }

    //Nearby 이벤트 처리 (Toast 메시지 오픈)
    LaunchedEffect(nearbyViewModel) {
        nearbyViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NearbyEvent.ShowToast -> {
                    //UToast(event.message, UToastState.NONE)
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is MycardEvent.NavigateToMypage -> {

                }
                else -> {}
            }
        }
    }

    MycardScreen(
        uiState = uiState,
        onNavigateToMypage = onNavigateToMypage,
        onNavigateToMyqrCode = onNavigateToMyqrCode,
        onNavigateToReceivedCard = onNavigateToReceivedCard,
        onNavigateToEditCard = onNavigateToEditCard,
        onNavigateToMyStudy = {},
        onNavigateToMyActivity = {},
        onOpenExchangeBottomSheet = { nearbyViewModel.openBottomSheet() },
    )

    //유저 명함 교환 다이얼로그
    if(nearbyUiState.isBottomSheetOpen){
        CardExchangeBottomSheet(
            sheetState = sheetState,
            currentStep = nearbyUiState.exchangeStep,
            discoveredDevices = nearbyUiState.devices,
            selectedTargetUser = nearbyUiState.selectedTargetUser,
            onDismissRequest = { nearbyViewModel.closeBottomSheet() },
            onWifiAwareClick = { nearbyViewModel.startAdvertisingAndDiscovery() },
            onQrCodeClick = {
                nearbyViewModel.closeBottomSheet()
                onNavigateToMyqrCode()
            },
            onUserSelect = { target -> nearbyViewModel.selectTargetUser(target) },
            onConfirmConnect = { nearbyViewModel.confirmAndConnect() },
            onCancelDialog = { nearbyViewModel.selectTargetUser(null) }
        )
    }

    //유저 명함 성공 오버레이 (nearbyconnection)
    if (nearbyUiState.isSuccessOverlayOpen) {
        CardExchangeSuccessOverlay(
            receivedCard = nearbyUiState.receivedCard,
            onContinueExchange = { nearbyViewModel.continueExchange() },
            onConfirm = { nearbyViewModel.dismissSuccessOverlay() }
        )
    }

    //유저 명함 성공 오버레이 (qr 코드)
    if(uiState.isSuccessOverlayOpen){
        CardExchangeSuccessOverlay(
            receivedCard = uiState.receivedUserCard,
            onContinueExchange = {viewModel.dismissSuccessOverlay()},
            onConfirm = {viewModel.dismissSuccessOverlay()}
        )
    }


}

@Composable
fun MycardScreen(
    uiState: MycardUiState,
    onNavigateToMypage: () -> Unit, //설정으로 이동
    onNavigateToMyqrCode:() -> Unit, // QR 코드 화면으로 이동
    onNavigateToReceivedCard: ()-> Unit, //받은 명함 이동
    onNavigateToEditCard: () -> Unit, //명함 편집 이동,
    onNavigateToMyStudy: () -> Unit, //나의 스터디 이동,
    onNavigateToMyActivity: () -> Unit, //나의 활동 이동,
    onOpenExchangeBottomSheet: () -> Unit, //명함 교환 BottomSheet 열기,
){

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(grey100())

    ) {

        item{
            MycardTopBar(
                onMypageClick = onNavigateToMypage
            )
        }

        item {
            MycardProfileCard(
                uiState = uiState,
                onExchangeCardClick = onOpenExchangeBottomSheet,
                onMyQrcodeClick = onNavigateToMyqrCode
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            MycardSectionTitle(AppStrings.MYCARD_CARD_TITLE)
            MycardListCard {
                MycardListItem(
                    iconRes = R.drawable.ic_received_card,
                    text = AppStrings.MYCARD_CARD_CONTENT_RECEIVED,
                    cardCount = uiState.cardCount,
                    onClick = onNavigateToReceivedCard
                )
                MycardListItem(
                    iconRes = R.drawable.ic_edit_card,
                    text = AppStrings.MYCARD_CARD_EDIT,
                    onClick = onNavigateToEditCard
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            MycardSectionTitle(AppStrings.MYCARD_CARD_TITLE)
            MycardListCard {
                MycardListItem(
                    iconRes = R.drawable.ic_my_study,
                    text = AppStrings.MYCARD_ACTVITY_MYSTUDY,
                    studyCount = 0,
                    onClick = onNavigateToMyStudy
                )
                MycardListItem(
                    iconRes = R.drawable.ic_my_actvity,
                    text = AppStrings.MYCARD_ACTVITY_MYACTIVTY_PROJECT,
                    onClick = onNavigateToMyActivity
                )
            }
        }

        // 바닥 여백
        item { Spacer(modifier = Modifier
            .height(64.dp)
        ) }


    }

}

/**마이페이지(내 카드) Top bar**/
@Composable
fun MycardTopBar(
    onMypageClick: () -> Unit //마이페이지 클릭
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey000())
        .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UText(
            text = AppStrings.MYPAGE_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)

        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            //설정 버튼
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = Color.Transparent, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(
                        onClick = onMypageClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_setting_outline
                    ),
                    contentDescription = "Setting",
                    tint = grey950(),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**내 카드 프로필**/
@Composable
fun MycardProfileCard(
    uiState: MycardUiState,
    onExchangeCardClick: () -> Unit, //명함 교환 화면으로 이동
    onMyQrcodeClick: () -> Unit, //내 QR 코드 화면으로 이동

) {

    //카드가 뒤집혔는지 여부 (Front / Back)
    var isFlipped by remember { mutableStateOf(false) }

    //3D 뒤집기 회전 애니메이션 (0도 -> 180도)
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "CardFlipAnimation"
    )

    val color1 = indigo400()
    val color2 = indigo500()

    //원형 그라데이션 배경
    val radialGradient = remember {
        Brush.radialGradient(
            colors = listOf(color1, color2),
            radius = 600f
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(radialGradient)
                .padding(20.dp)
        ) {
            //90도를 기준으로 앞면/뒷면 분기
            if (rotation <= 90f) {
                //---------------- [ 앞면 (DEFAULT) ] ----------------
                CardFrontContent(
                    uiState = uiState,
                    onFlipClick = { isFlipped = !isFlipped },
                    onExchangeCardClick = onExchangeCardClick,
                    onMyQrcodeClick = onMyQrcodeClick
                )
            } else {
                //---------------- [ 뒷면 ] ----------------
                //뒷면은 Y축으로 180도 추가 회전시켜 글씨가 좌우 반전되는 현상을 방지
                Box(
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    CardBackContent(
                        uiState = uiState,
                        onFlipClick = { isFlipped = !isFlipped },
                        onExchangeCardClick = onExchangeCardClick,
                        onMyQrcodeClick = onMyQrcodeClick
                    )
                }
            }
        }
    }
}

/**앞면 레이아웃 **/
@Composable
private fun CardFrontContent(
    uiState: MycardUiState,
    onFlipClick: () -> Unit,
    onExchangeCardClick: () -> Unit, //명함 교환 화면으로 이동
    onMyQrcodeClick: () -> Unit, //내 QR 코드 화면으로 이동
    ) {
        Column {
            //[상단 Header] 로고 + Business card 텍스트 + 우상단 전환 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    //로고 이미지
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_umc),
                        contentDescription = "UMC Logo",
                        tint = grey000()
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    UText(
                        text = AppStrings.MYCARD_INTRODUCE,
                        style = UmcTypographyTokens.Caption2,
                        color = grey000()
                    )
                }

                // 우상단 전환 버튼 (뒤집기)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(indigo400())
                        .clickable { onFlipClick() },
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_flip),
                        contentDescription = "flip",
                        tint = grey000(),
                        modifier = Modifier
                            .padding(6.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            //프로필 이미지 + 유저 정보
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = uiState.userInfo.profileImageLink,
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(1.dp, grey200(), CircleShape),
                    placeholder = painterResource(R.drawable.ic_profile_default),
                    error = painterResource(R.drawable.ic_profile_default)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    //이름 및 닉네임
                    UText(
                        text = "${uiState.userInfo.name}/${uiState.userInfo.nickname}",
                        style = UmcTypographyTokens.Title3Bold,
                        color = grey000()
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )
                    //학교
                    UText(
                        text = uiState.userInfo.schoolName,
                        style = UmcTypographyTokens.Subheadline,
                        color = indigo100()
                    )
                    //내 최신 커리어
                    UText(
                        text = uiState.myRecentInfoString,
                        style = UmcTypographyTokens.Subheadline,
                        color = indigo100()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //명함 교환 & QR 코드 버튼
            CardBottomButtons(
                onExchangeCardClick = onExchangeCardClick,
                onMyQrcodeClick = onMyQrcodeClick
            )
        }
    }

/** ## 2. 뒷면 레이아웃**/
@Composable
private fun CardBackContent(
    uiState: MycardUiState,
    onFlipClick: () -> Unit,
    onExchangeCardClick: () -> Unit, //명함 교환 화면으로 이동
    onMyQrcodeClick: () -> Unit, //내 QR 코드 화면으로 이동
) {

    //비동기로 qr코드 만들기
    val qrBitmap = remember(uiState.myQrcodeData) {
        if (uiState.myQrcodeData.isNotEmpty()) {
            QrCodeUtils.generateQrCode(uiState.myQrcodeData)
        } else null
    }


    Column {
        //[상단 Header] 로고 + Business card 텍스트 + 우상단 전환 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                //로고 이미지
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo_umc),
                    contentDescription = "UMC Logo",
                    tint = grey000()
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                UText(
                    text = AppStrings.MYCARD_INTRODUCE,
                    style = UmcTypographyTokens.Caption2,
                    color = grey000()
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(indigo400())
                    .clickable { onFlipClick() },
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_flip),
                    contentDescription = "flip",
                    tint = grey000(),
                    modifier = Modifier
                        .padding(6.dp)
                )
            }

        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // [중단 Info] QR 코드 이미지 + 소셜/연락처 리스트
        Row(verticalAlignment = Alignment.CenterVertically) {
            //QR 코드 공간
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(grey000())
                    .padding(7.dp), // QR 코드 테두리 여백
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "UserCard QR Code",
                        modifier = Modifier.size(55.dp)
                    )
                } else {
                    // QR 데이터 생성 중이거나 예외 발생 시 로딩/플래스홀더
                    UText(
                        text = "QR",
                        style = UmcTypographyTokens.Caption1Bold,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 아이콘 + 링크 리스트 (GitHub, LinkedIn, Blog 등)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SocialLinkItem(iconRes = R.drawable.ic_github_link, text = uiState.userInfo.profile.github)
                SocialLinkItem(iconRes = R.drawable.ic_linkedin_link, text = uiState.userInfo.profile.linkedIn)
                SocialLinkItem(iconRes = R.drawable.ic_blog_link, text = uiState.userInfo.profile.blog)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CardBottomButtons(
            onExchangeCardClick = onExchangeCardClick,
            onMyQrcodeClick = onMyQrcodeClick
        )
    }
}


/**내 명함에 들어갈 공통 버튼 2종**/
@Composable
private fun CardBottomButtons(
    onExchangeCardClick: () -> Unit,
    onMyQrcodeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //명함 교환 버튼
        UButton(
            text = AppStrings.MYCARD_EXCHANGE_CARD,
            modifier = Modifier
                .weight(1f)
                .height(42.dp),
            backgroundColor = grey000(),
            textColor = indigo500(),
            textStyle = UmcTypographyTokens.CalloutBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_wifi), // 추후 아이콘 리소스 연결
            prevIconTint = indigo500(),
            onClick = onExchangeCardClick
        )

        //QR 코드 버튼
        UButton(
            text = AppStrings.MYCARD_QRCODE,
            modifier = Modifier
                .weight(1f)
                .height(42.dp),
            backgroundColor = grey000(),
            textColor = indigo500(),
            textStyle = UmcTypographyTokens.CalloutBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_qrcode), // 추후 아이콘 리소스 연결
            prevIconTint = indigo500(),
            onClick = onMyQrcodeClick
        )
    }
}

/**내 명함에서 각 소셜 링크 item**/
@Composable
private fun SocialLinkItem(
    @DrawableRes iconRes: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = grey000(),
            modifier = Modifier.size(16.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        UText(
            text = text.ifEmpty { "링크 없음" },
            style = UmcTypographyTokens.Footnote,
            color = grey000()
        )
    }
}

/**각 섹션의 헤더 제목**/
@Composable
fun MycardSectionTitle(text: String) {
    UText(
        text = text,
        style = UmcTypographyTokens.HeadlineBold,
        color = grey800(),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(horizontal = 16.dp)
    )
}

/**여러 메뉴를 감싸는 아이템 카드**/
@Composable
fun MycardListCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey000()),
        elevation = CardDefaults.cardElevation(0.dp),
        content = content
    )
}

/**메뉴 1개**/
@Composable
fun MycardListItem(
    iconRes: Int,
    text: String,
    showArrow: Boolean = true,
    cardCount: Int = -1,
    studyCount: Int = -1,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
            tint = grey950()
        )
        UText(
            text = text,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            style = UmcTypographyTokens.Body,
            color = grey800()
        )

        if (cardCount > -1) {
            UButton(
                text = "${cardCount}장",
                enabled = false,
                backgroundColor = indigo100(),
                textColor = indigo500(),
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 4.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                onClick = {}
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        if (studyCount> -1) {
            UButton(
                text = "${studyCount}건",
                enabled = false,
                backgroundColor = indigo100(),
                textColor = indigo500(),
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 4.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                onClick = {}
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        if (showArrow) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400()
            )
        }
    }
}
