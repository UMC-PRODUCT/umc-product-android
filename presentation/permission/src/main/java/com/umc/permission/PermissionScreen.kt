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
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.primary600
import com.umc.component.theme.primary700
import kotlinx.coroutines.flow.collectLatest

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

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                PermissionEvent.MoveToBack -> navigateToBack()
                PermissionEvent.ShowPermissionDialog -> {
                    val permissions = mutableListOf<String>()
                    if (viewModel.uiState.value.isAlarmCheck) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissions += Manifest.permission.POST_NOTIFICATIONS
                            }
                        }
                    }
                    if (viewModel.uiState.value.isLocationCheck) {
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissions += Manifest.permission.ACCESS_FINE_LOCATION
                        }
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
            .background(neutral000())
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
                color = neutral800(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = AppStrings.PERMISSION_SUBTITLE,
                style = UmcTypographyTokens.Body,
                color = neutral600(),
            )

            Spacer(modifier = Modifier.height(48.dp))

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(neutral000())
                    .border(
                        width = 1.dp,
                        color = if (isAllChecked) primary500() else neutral300(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable(onClick = onClickAllCheck)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(16.dp))
                UText(
                    text = AppStrings.PERMISSION_ACCEPT_ALL,
                    style = UmcTypographyTokens.SubheadlineBold,
                    color = neutral800(),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_check_box),
                    contentDescription = null,
                    tint = if (isAllChecked) primary500() else neutral300(),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
        }

        UButton(
            text = AppStrings.SIGN_UP_COMPLETE,
            onClick = onClickSignUp,
            enabled = uiState.isAlarmCheck,
            backgroundColor = if (uiState.isAlarmCheck) primary500() else neutral300(),
            pressedColor = primary700(),
            textColor = neutral000(),
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
            .background(if (isChecked) primary100() else neutral100())
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(16.dp))

        Icon(
            painter = icon,
            contentDescription = null,
            tint = if (isChecked) primary600() else neutral400(),
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
                color = neutral800(),
            )

            Spacer(modifier = Modifier.size(4.dp))

            UText(
                text = content,
                style = UmcTypographyTokens.Footnote,
                color = neutral600(),
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_check_box),
            contentDescription = null,
            tint = if (isChecked) primary500() else neutral300(),
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
