package com.umc.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo600
import com.umc.component.theme.indigo700
import com.umc.component.theme.white
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents

@Composable
fun PermissionRoute(
    viewModel: PermissionViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToMain: () -> Unit = {},
    navigateToFail: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.signUp()
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            PermissionEvent.MoveToBack -> navigateToBack()
            PermissionEvent.ShowPermissionDialog -> {
                val permissions = buildList {
                    if (uiState.isAlarmCheck && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (uiState.isLocationCheck) {
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                    // 사진은 시스템 사진 선택 도구(PickVisualMedia)로만 접근하므로
                    // 저장소 권한을 요청하지 않는다. 광범위 저장소 권한을 선언·요청하면
                    // Play 정책 위반으로 심사에서 거부된다
                }.filter { permission ->
                    ContextCompat.checkSelfPermission(
                        context, permission
                    ) != PackageManager.PERMISSION_GRANTED
                }

                if (permissions.isNotEmpty()) {
                    requestPermissionsLauncher.launch(permissions.toTypedArray())
                } else {
                    viewModel.signUp()
                }
            }
            PermissionEvent.MoveToMainEvent -> navigateToMain()
            PermissionEvent.MoveToFailEvent -> navigateToFail()
        }
    }

    PermissionScreen(
        uiState = uiState,
        onClickBack = viewModel::onClickBack,
        onClickAlarmCheck = viewModel::onClickAlarmCheck,
        onClickLocationCheck = viewModel::onClickLocationCheck,
        onClickPhotoCheck = viewModel::onClickPhotoCheck,
        onClickAllCheck = viewModel::onClickAllCheck,
        onClickSignUp = viewModel::onClickSignUp,
    )
}

@Composable
fun PermissionScreen(
    uiState: PermissionUiState = PermissionUiState(),
    onClickBack: () -> Unit = {},
    onClickAlarmCheck: () -> Unit = {},
    onClickLocationCheck: () -> Unit = {},
    onClickPhotoCheck: () -> Unit = {},
    onClickAllCheck: () -> Unit = {},
    onClickSignUp: () -> Unit = {},
) {
    val isAllChecked = uiState.isAlarmCheck && uiState.isLocationCheck && uiState.isPhotoCheck

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .padding(12.dp)
                    .clickable { onClickBack() },
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = AppStrings.PERMISSION_TITLE,
                style = UmcTypographyTokens.Title1Bold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = AppStrings.PERMISSION_SUBTITLE,
                style = UmcTypographyTokens.Body,
                color = grey600(),
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(grey000())
                    .border(
                        width = 1.dp,
                        color = if (isAllChecked) indigo500() else grey300(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable(onClick = onClickAllCheck)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(16.dp))

                UText(
                    text = AppStrings.PERMISSION_ACCEPT_ALL,
                    style = UmcTypographyTokens.BodyBold,
                    color = if (isAllChecked) indigo500() else grey950(),
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    painter = painterResource(R.drawable.ic_check_box),
                    contentDescription = null,
                    tint = if (isAllChecked) indigo500() else grey300(),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                )

                Spacer(modifier = Modifier.size(4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = painterResource(R.drawable.ic_alarm_filled),
                title = AppStrings.PERMISSION_NOTIFICATION,
                content = AppStrings.PERMISSION_NOTIFICATION_CONTENT,
                isChecked = uiState.isAlarmCheck,
                onClick = onClickAlarmCheck,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = painterResource(R.drawable.ic_location),
                title = AppStrings.PERMISSION_LOCATION,
                content = AppStrings.PERMISSION_LOCATION_CONTENT,
                isChecked = uiState.isLocationCheck,
                onClick = onClickLocationCheck,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = painterResource(R.drawable.ic_photo),
                title = AppStrings.PERMISSION_PHOTO,
                content = AppStrings.PERMISSION_PHOTO_CONTENT,
                isChecked = uiState.isPhotoCheck,
                onClick = onClickPhotoCheck,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        UButton(
            text = AppStrings.SIGN_UP_COMPLETE,
            onClick = onClickSignUp,
            enabled = uiState.isAlarmCheck,
            backgroundColor = if (uiState.isAlarmCheck) indigo500() else grey100(),
            pressedColor = indigo700(),
            textColor = if (uiState.isAlarmCheck) grey000() else grey300(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionItem(
    icon: Painter,
    title: String,
    content: String,
    isChecked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isChecked) indigo100() else grey100())
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(16.dp))

        Icon(
            painter = icon,
            contentDescription = null,
            tint = grey950(),
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            UText(
                text = title,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.size(4.dp))

            UText(
                text = content,
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_check_box),
            contentDescription = null,
            tint = if (isChecked) indigo500() else grey300(),
            modifier = Modifier
                .padding(12.dp)
                .size(24.dp),
        )

        Spacer(modifier = Modifier.size(4.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionScreenPreview() {
    PermissionScreen()
}

@Preview(showBackground = true)
@Composable
private fun PermissionScreenAllCheckedPreview() {
    PermissionScreen(
        uiState = PermissionUiState(
            isAlarmCheck = true,
            isLocationCheck = true,
            isPhotoCheck = true,
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionScreenAlarmOnlyPreview() {
    PermissionScreen(
        uiState = PermissionUiState(isAlarmCheck = true)
    )
}
