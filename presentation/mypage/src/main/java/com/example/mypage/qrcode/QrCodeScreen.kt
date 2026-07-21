package com.example.mypage.qrcode

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.qrcode.QrCodeUtils
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
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
    val qrContent = uiState.myQrcodeData.ifEmpty { UserCard("테스트 이름","테스트 닉네임").toJson() }
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
            .padding(horizontal = 20.dp)
    ) {
        // 1. 상단 바 (뒤로가기 + 타이틀)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back), // 프로젝트의 뒤로가기 아이콘 사용
                contentDescription = "Back",
                tint = grey800(),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            UText(
                text = "명함 공유하기",
                style = UmcTypographyTokens.Title2Bold,
                color = grey800()
            )

            // [추가] 상대방 QR 스캔 버튼 (카메라)
            Icon(
                painter = painterResource(id = R.drawable.ic_add), // 카메라/스캔 아이콘으로 대체하세요
                contentDescription = "Scan QR",
                tint = grey800(),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onOpenScannerClick() }
            )

        }

        // 2. 상단 프로필 카드 (파란색 배경)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4C5BF7)) // 이미지상의 파란색
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지
                AsyncImage(
                    model = uiState.userInfo.profileImageLink,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    placeholder = painterResource(R.drawable.ic_profile_default),
                    error = painterResource(R.drawable.ic_profile_default)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    UText(
                        text = "${uiState.userInfo.name}/${uiState.userInfo.nickname}",
                        style = UmcTypographyTokens.Title3Bold,
                        color = Color.White
                    )
                    UText(
                        text = uiState.userInfo.schoolName.ifEmpty { "00대학교" },
                        style = UmcTypographyTokens.Caption1Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    UText(
                        text = "Admin · 10기", // 위치나 소속 표시
                        style = UmcTypographyTokens.Caption1,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. 중앙 QR 코드 카드
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = grey100()),
                elevation = CardDefaults.cardElevation(0.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            UText(
                text = "QR을 스캔하면 내 명함이 저장돼요",
                style = UmcTypographyTokens.Body,
                color = grey500()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. 하단 버튼 2개 (공유하기 / 이미지 저장)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UButton(
                text = "공유하기",
                modifier = Modifier.weight(1f),
                backgroundColor = Color(0xFF4C5BF7),
                textColor = Color.White,
                textStyle = UmcTypographyTokens.BodyBold,
                cornerRadius = 12.dp,
                onClick = onShareClick
            )

            UButton(
                text = "이미지 저장",
                modifier = Modifier.weight(1f),
                backgroundColor = grey100(),
                textColor = grey800(),
                textStyle = UmcTypographyTokens.BodyBold,
                cornerRadius = 12.dp,
                onClick = onSaveImageClick
            )
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