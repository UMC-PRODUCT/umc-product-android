package com.example.mypage.qrcode

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.mycard.MycardUiState
import com.example.mypage.qrcode.QrCodeUtils
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo400
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.domain.model.mypage.UserCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QrCodeRoute(
    viewModel: QrCodeViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //val localEndpointName = Build.MODEL

    //권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.values.all { it }) {
            Toast.makeText(context, "주변 기기 연결을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //첫 권한 체크
    LaunchedEffect(Unit) {
        permissionLauncher.launch(getQrPermissions())
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is QrCodeEvent.NavigateBack -> onNavigateToBack()
                is QrCodeEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is QrCodeEvent.ShareQrCode -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "내 UMC 명함을 확인해 보세요: ${uiState.userInfo.nickname}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "명함 공유하기"))
                }
            }
        }
    }


    uiState.receivedCard?.let { card ->
        UDialog(
            title = "명함 수신 완료!",
            content = "${card.name}(${card.nickname})님의 명함을 성공적으로 전달받았습니다.",
            isTwoButton = false,
            positiveText = "확인",
            onPositive = { viewModel.clearReceivedCard() },
            onDismissRequest = { viewModel.clearReceivedCard() }
        )
    }


    //QR 생성 (UserCard Json 데이터 기반)
    val qrContent = uiState.myQrcodeData.ifEmpty { UserCard(name = "테스트 이름", nickname = "테스트 닉네임").toJson() }
    val qrBitmap = remember(qrContent) { QrCodeUtils.generateQrCode(qrContent, 600) }

    QrCodeScreen(
        uiState = uiState,
        qrBitmap = qrBitmap,
        onBackClick = viewModel::navigateBack,
        onShareClick = viewModel::shareQrCode,
        onSaveImageClick = { viewModel.saveQrImage(qrBitmap) },
        onOpenScannerClick = viewModel::startScanner
    )

    // 카메라 스캐너 팝업 다이얼로그
    if (uiState.isScannerOpen) {
        Dialog(onDismissRequest = viewModel::closeScanner) {
            Card(
                modifier = Modifier.size(320.dp, 420.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    QrCodeScannerView(
                        onQrCodeScanned = { scannedValue ->
                            viewModel.onQrScanned(scannedValue)
                        }
                    )
                }
            }
        }
    }

}

@Composable
fun QrCodeScreen(
    uiState: QrCodeUiState,
    qrBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveImageClick: () -> Unit,
    onOpenScannerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
        
    ) {
        
        
        
        // 1. 상단 바 (뒤로가기 + 타이틀)
        QrCodeScreenTopBar(
            onBackClick = onBackClick,
            onOpenScannerClick = onOpenScannerClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            
        ) {
            //프로필 명함
            item{
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                
                MycardProfileCard(uiState)
            }
            
            //QR 코드
            item{
                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.size(280.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = white())
                        ,
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, grey300())
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            qrBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "User Card QR",
                                    modifier = Modifier.size(220.dp)
                                )
                            }
                        }
                    }
                }
            }

            //QR 설명 텍스트
            item{
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UText(
                        text = AppStrings.QRCODE_CONTENT,
                        style = UmcTypographyTokens.Subheadline,
                        color = grey500()
                    )
                }
            }

            //버튼
            item{
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UButton(
                        text = AppStrings.QRCODE_SHARE,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        backgroundColor = indigo500(),
                        textColor = Color.White,
                        textStyle = UmcTypographyTokens.HeadlineBold,
                        cornerRadius = 8.dp,
                        onClick = onShareClick
                    )

                    UButton(
                        text = AppStrings.QRCODE_SAVE,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        backgroundColor = grey100(),
                        textColor = grey700(),
                    textStyle = UmcTypographyTokens.HeadlineBold,
                        cornerRadius = 8.dp,
                        onClick = onSaveImageClick
                    )
                }
            }
            
        }
    }
}

/**설정(내 카드) Top bar**/
@Composable
fun QrCodeScreenTopBar(
    onBackClick: () -> Unit, //뒤로 가기
    onOpenScannerClick: () -> Unit //스캐너 열기
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
            text = AppStrings.QRCODE_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            modifier = Modifier
                .padding(horizontal = 6.dp)

        )

        Icon(
            painter = painterResource(id = R.drawable.ic_add), // 카메라/스캔 아이콘으로 대체하세요
            contentDescription = "Scan QR",
            tint = grey800(),
            modifier = Modifier
                .size(28.dp)
                .clickable { onOpenScannerClick() }
                .padding(horizontal = 12.dp)
        )


    }
}



/**QR 코드에 나올 명함 프로필**/
@Composable
fun MycardProfileCard(
    uiState: QrCodeUiState,
) {

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
            .padding(top = 16.dp),
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
            Column {
                
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
                            color = white()
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
            }
        }
    }
}


private fun getQrPermissions(): Array<String> {
    val list = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        list.add(Manifest.permission.BLUETOOTH_SCAN)
        list.add(Manifest.permission.BLUETOOTH_CONNECT)
        list.add(Manifest.permission.BLUETOOTH_ADVERTISE)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    return list.toTypedArray()
}