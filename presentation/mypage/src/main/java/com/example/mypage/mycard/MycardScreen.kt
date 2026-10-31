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
import androidx.compose.ui.tooling.preview.Preview
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
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UToast
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.grey50
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.main100
import com.umc.component.theme.main400
import com.umc.component.theme.main500
import com.umc.component.theme.main600
import com.umc.component.theme.white
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.mypage.NearbyUserInfo
import com.umc.domain.model.mypage.UserCard
import com.umc.domain.model.toUserCard
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents


/**
 * 마이페이지 탭 진입 시 표출되는 최초 진입점(메인 화면) 및 라우트 컴포저블
 *
 * 내 명함 카드(앞/뒷면 뒤집기), 명함 교환 바텀시트, Nearby Connections 및 QR 딥링크 교환 성공 오버레이를 통합 제어하고,
 * 받은 명함함, 명함 편집, 나의 스터디 및 활동 화면으로의 네비게이션 라우팅을 수행합니다.
 *
 * [유의사항]
 * - 하단 바의 마이페이지 탭 클릭 시 최초 렌더링되는 화면은 MycardScreen이며, 우상단 설정 아이콘 클릭 시 기존의 MypageScreen(설정 전용 화면)으로 이동하는 구조입니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MycardRoute(
    targetMemberId: String? = null, //유저 qr 딥링크로 받았을 때
    viewProfileOnly: Boolean = false,
    onNavigateBack: () -> Unit = {},
    openExchangeDialog: Boolean = false, //홈에서 명함 교환 받을 시
    viewModel: MycardViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(), //nearbyConnection 전용 관리 viewModel
    onNavigateToMypage: () -> Unit, //설정으로 이동
    onNavigateToMyqrCode:() -> Unit, // QR 코드 화면으로 이동
    onNavigateToEditCard: () -> Unit, //명함 편집
    onNavigateToReceivedCard: () -> Unit, //받은 명함 이동
    onNavigateToMyStudy: () -> Unit, //나의 스터디(활동 탭 > 스터디 커리큘럼)로 이동
    onNavigateToActivityHistory: () -> Unit, //활동 이력 화면으로 이동

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nearbyUiState by nearbyViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 화면 복귀(Restore) 시에도 교환 다이얼로그의 중복 실행을 방지하는 플래그
    // rememberSaveable
    var hasOpenedExchangeDialog by androidx.compose.runtime.saveable.rememberSaveable {
        mutableStateOf(false)
    }

    // [임시] 명함 교환 차단 — iOS 구현 전까지 교환으로 가는 모든 입구에서 안내 다이얼로그만 띄운다.
    // 차단 지점: 홈 배너 진입(openExchangeDialog), 명함 교환 버튼, 내 QR 코드, QR 딥링크 수신.
    // 되돌릴 때: 이 상태와 아래 UDialog 블록을 지우고 각 지점의 원래 호출을 주석에서 되살린다.
    var showExchangeBlockedDialog by remember { mutableStateOf(false) }


    // 화면 복귀(ON_RESUME) 라이프사이클 시 최신 내 정보를 동기화하는 관찰자
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


    // QR 딥링크로 전달된 상대방 회원 ID 존재 시 회원 조회 수행
    LaunchedEffect(targetMemberId) {
        if (!viewProfileOnly && !targetMemberId.isNullOrEmpty()) {
            // [임시] 명함 교환 차단 — 원래 동작: viewModel.searchUser(targetMemberId.toLong())
            showExchangeBlockedDialog = true
        }
    }

    // 홈 화면 등 외부에서 명함 교환 요청 플래그를 가지고 진입 시 다이얼로그 즉시 오픈
    LaunchedEffect(openExchangeDialog) {
        if (!viewProfileOnly && openExchangeDialog && !hasOpenedExchangeDialog) {
            // [임시] 명함 교환 차단 — 원래 동작: nearbyViewModel.openBottomSheet()
            showExchangeBlockedDialog = true
            hasOpenedExchangeDialog = true //오픈 후 즉시 처리 완료 상태로 변경!
        }
    }
    

    //내 프로필 정보가 업데이트 될 때 NearbyViewModel에도 이를 반영
    LaunchedEffect(uiState.userInfo) {
        if (!viewProfileOnly && uiState.userInfo.name.isNotEmpty()) {
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

    // Nearby Connections 전용 단발성 이벤트(토스트) 수신
    CollectUiEvents(nearbyViewModel.uiEvent) { event ->
        when (event) {
            is NearbyEvent.ShowToast -> {
                //UToast(event.message, UToastState.NONE)
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is MycardEvent.NavigateToMypage -> {

            }

            is MycardEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            is MycardEvent.ProfileLoadFailed -> {
                Toast.makeText(context, AppStrings.MYCARD_PROFILE_LOAD_ERROR_TITLE, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    MycardScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToMypage = onNavigateToMypage,
        // [임시] 명함 교환 차단 — 원래 동작: onNavigateToMyqrCode (QR 공유도 교환 경로라 함께 막는다)
        onNavigateToMyqrCode = { showExchangeBlockedDialog = true },
        onNavigateToReceivedCard = onNavigateToReceivedCard,
        onNavigateToEditCard = onNavigateToEditCard,
        onNavigateToMyStudy = onNavigateToMyStudy,
        onNavigateToActivityHistory = onNavigateToActivityHistory,
        onRetryProfile = viewModel::getUserInfo,
        // [임시] 명함 교환 차단 — 원래 동작: { nearbyViewModel.openBottomSheet() }
        onOpenExchangeBottomSheet = { showExchangeBlockedDialog = true },
    )

    // [임시] 명함 교환 차단 안내
    if (showExchangeBlockedDialog) {
        UDialog(
            title = AppStrings.EXCHANGE_CARD_BLOCKED_TITLE,
            content = AppStrings.EXCHANGE_CARD_BLOCKED_CONTENT,
            onDismissRequest = { showExchangeBlockedDialog = false },
            confirmText = AppStrings.CONFIRM,
            onConfirm = { showExchangeBlockedDialog = false },
        )
    }

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
    onNavigateToActivityHistory: () -> Unit, //활동 이력 이동,
    onOpenExchangeBottomSheet: () -> Unit, //명함 교환 BottomSheet 열기,
    onNavigateBack: () -> Unit = {},
    onRetryProfile: () -> Unit, //내 프로필 조회 실패 시 다시 시도
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
            // 이미 불러온 정보가 있으면(id != 0) 재조회에 실패해도 카드를 유지한다
            if (uiState.isProfileLoading && uiState.userInfo.id == 0L) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else if (uiState.isProfileLoadFailed && uiState.userInfo.id == 0L) {
                MycardProfileLoadError(
                    isLoading = uiState.isProfileLoading,
                    onRetry = onRetryProfile
                )
            } else {
                MycardProfileCard(
                    uiState = uiState,
                    onExchangeCardClick = onOpenExchangeBottomSheet,
                    onMyQrcodeClick = onNavigateToMyqrCode
                )
            }
        }

        if (!uiState.isViewingMemberProfile) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                MycardSectionTitle(AppStrings.MYCARD_CARD_TITLE)
                uiState.cardStorageError?.let { UText(it, modifier = Modifier.padding(horizontal = 16.dp)) }
                MycardListCard {
                    MycardListItem(
                        iconRes = R.drawable.ic_received_card,
                        text = AppStrings.MYCARD_CARD_CONTENT_RECEIVED,
                        cardCount = if (uiState.cardStorageError == null && uiState.cardStatistics != null) uiState.cardCount else -1,
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
                MycardSectionTitle(AppStrings.MYCARD_ACTVITY_TITLE)
                MycardListCard {
                    MycardListItem(
                        iconRes = R.drawable.ic_my_study,
                        text = AppStrings.MYCARD_ACTVITY_MYSTUDY,
                        onClick = onNavigateToMyStudy
                    )
                    MycardListItem(
                        iconRes = R.drawable.ic_activity_history,
                        text = AppStrings.MYCARD_ACTVITY_HISTORY,
                        onClick = onNavigateToActivityHistory
                    )
                }
            }

        }

        // 바닥 여백
        item { Spacer(modifier = Modifier
            .height(64.dp)
        ) }


    }

}

/**
 * 내 프로필 조회에 실패했을 때 명함 카드 자리에 표시하는 실패 안내 + 다시 시도 컴포저블
 */
@Composable
private fun MycardProfileLoadError(
    isLoading: Boolean,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = grey50()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_community_error),
                contentDescription = null,
                modifier = Modifier.size(46.dp),
                tint = grey400()
            )
            Spacer(modifier = Modifier.height(16.dp))
            UText(
                text = AppStrings.MYCARD_PROFILE_LOAD_ERROR_TITLE,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey800()
            )
            Spacer(modifier = Modifier.height(8.dp))
            UText(
                text = AppStrings.MYCARD_PROFILE_LOAD_ERROR_DESCRIPTION,
                style = UmcTypographyTokens.Subheadline,
                color = grey400()
            )
            Spacer(modifier = Modifier.height(24.dp))
            UButton(
                text = AppStrings.MYCARD_RETRY,
                onClick = onRetry,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                backgroundColor = main500(),
                pressedColor = main600(),
                textColor = white(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 12.dp,
            )
        }
    }
}

/**
 * 마이페이지 메인 상단 탑바 컴포저블
 */
@Composable
fun MycardTopBar(
    onMypageClick: () -> Unit //마이페이지 클릭
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey50())
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

/**
 * 3D 회전 애니메이션이 적용된 내 명함 프로필 카드 컴포저블
 */
@Composable
fun MycardProfileCard(
    uiState: MycardUiState,
    onExchangeCardClick: () -> Unit, //명함 교환 화면으로 이동
    onMyQrcodeClick: () -> Unit, //내 QR 코드 화면으로 이동

) {

    //카드 상태 토글 플래그 (Front / Back)
    var isFlipped by remember { mutableStateOf(false) }

    // 0도 ~ 180도 Y축 회전 애니메이션
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "CardFlipAnimation"
    )

    val color1 = main400()
    val color2 = main500()

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
            // 회전 각도 90도를 경계로 앞면/뒷면 분기
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

/**
* 명함 카드 앞면 레이아웃 컴포저블
*/
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
                        tint = grey50()
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    UText(
                        text = AppStrings.MYCARD_INTRODUCE,
                        style = UmcTypographyTokens.Caption2,
                        color = grey50()
                    )
                }

                // 우상단 전환 버튼 (뒤집기)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(main400())
                        .clickable { onFlipClick() },
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_flip),
                        contentDescription = "flip",
                        tint = grey50(),
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
                        color = grey50()
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )
                    //학교
                    UText(
                        text = uiState.userInfo.schoolName,
                        style = UmcTypographyTokens.Subheadline,
                        color = main100()
                    )
                    //내 최신 커리어
                    UText(
                        text = uiState.myRecentInfoString,
                        style = UmcTypographyTokens.Subheadline,
                        color = main100()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //명함 교환 & QR 코드 버튼
            if (!uiState.isViewingMemberProfile) CardBottomButtons(
                onExchangeCardClick = onExchangeCardClick,
                onMyQrcodeClick = onMyQrcodeClick
            )
        }
    }

/**
 * 명함 카드 뒷면 레이아웃 컴포저블
 */
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
                    tint = grey50()
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                UText(
                    text = AppStrings.MYCARD_INTRODUCE,
                    style = UmcTypographyTokens.Caption2,
                    color = grey50()
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(main400())
                    .clickable { onFlipClick() },
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_flip),
                    contentDescription = "flip",
                    tint = grey50(),
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
            if (!uiState.isViewingMemberProfile) Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    // QR 판독에 필요한 흰 여백이라 다크 모드에서도 흰색으로 둔다 (white()는 다크 모드에서 검정)
                    .background(Color.White)
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SocialLinkItem(iconRes = R.drawable.ic_github_link, text = uiState.userInfo.profile.github)
                SocialLinkItem(iconRes = R.drawable.ic_linkedin_link, text = uiState.userInfo.profile.linkedIn)
                SocialLinkItem(iconRes = R.drawable.ic_blog_link, text = uiState.userInfo.profile.blog)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.isViewingMemberProfile) CardBottomButtons(
            onExchangeCardClick = onExchangeCardClick,
            onMyQrcodeClick = onMyQrcodeClick
        )
    }
}


/**
 * 명함 카드 하단 기능 버튼(명함 교환 / 내 QR) 컴포저블
 */
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
            backgroundColor = grey50(),
            textColor = main500(),
            textStyle = UmcTypographyTokens.CalloutBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_wifi), // 추후 아이콘 리소스 연결
            prevIconTint = main500(),
            onClick = onExchangeCardClick
        )

        //QR 코드 버튼
        UButton(
            text = AppStrings.MYCARD_QRCODE,
            modifier = Modifier
                .weight(1f)
                .height(42.dp),
            backgroundColor = grey50(),
            textColor = main500(),
            textStyle = UmcTypographyTokens.CalloutBold,
            cornerRadius = 8.dp,
            prevIcon = painterResource(R.drawable.ic_qrcode), // 추후 아이콘 리소스 연결
            prevIconTint = main500(),
            onClick = onMyQrcodeClick
        )
    }
}

/**
 * 명함 뒷면 소셜 미디어 항목 행 컴포저블
 */
@Composable
private fun SocialLinkItem(
    @DrawableRes iconRes: Int,
    text: String
) {
    val link = com.umc.domain.model.mypage.validCardWebLink(text) ?: return
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Row(
        modifier = Modifier.clickable(onClickLabel = "링크 열기") { runCatching { uriHandler.openUri(link) } },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = grey50(),
            modifier = Modifier.size(16.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        UText(
            text = link,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            style = UmcTypographyTokens.Footnote,
            color = grey50()
        )
    }
}

/**
 * 각 메뉴 카드 상단 섹션 제목 컴포저블
 */
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

/**
 * 메뉴 목록을 둘러싸는 라운드 카드 컴포저블
 */
@Composable
fun MycardListCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey50()),
        elevation = CardDefaults.cardElevation(0.dp),
        content = content
    )
}

/**
 * 메뉴 항목 개별 행 컴포저블
 */
@Composable
fun MycardListItem(
    iconRes: Int,
    text: String,
    showArrow: Boolean = true,
    cardCount: Int = -1,
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
                backgroundColor = main100(),
                textColor = main500(),
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


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MycardScreenProfileLoadFailedPreview() {
    UmcTheme {
        MycardScreen(
            uiState = MycardUiState(isProfileLoadFailed = true),
            onNavigateToMypage = {},
            onNavigateToMyqrCode = {},
            onNavigateToReceivedCard = {},
            onNavigateToEditCard = {},
            onNavigateToMyStudy = {},
            onNavigateToActivityHistory = {},
            onOpenExchangeBottomSheet = {},
            onRetryProfile = {},
        )
    }
}
