package com.umc.component.fcm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit

/**
 * 알림 권한(API 33+)을 이번 설치에서 요청했는지 기록한다.
 *
 * 권한 보유 여부는 이 플래그가 아니라 checkSelfPermission 으로 판단한다. 플래그는 같은 설치에서 여러 번 묻지 않게 하는 용도다.
 * SharedPreferences 는 백업 대상에서 빠져 있어(data_extraction_rules.xml) 재설치하면 초기화되고, 재설치한 사용자에게 다시 묻게 된다.
 */
object NotificationPermission {
    private const val PREFS_NAME = "notification_permission"
    private const val KEY_REQUESTED = "requested"

    // API 33 미만 or 이미 권한이 허용됨 or 이전에 요청한 경우일 때 false를 반환한다.
    // API 33 이상이고 권한이 비허용인데 권한을 요청한 적이 없는 경우 true를 반환한다.
    fun shouldRequest(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false

        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        return !granted && !preferences(context).getBoolean(KEY_REQUESTED, false)
    }

    // 알림 권한 요청 여부를 저장해 동일 설치에서 중복 요청하지 않도록 한다.
    fun markRequested(context: Context) {
        preferences(context).edit { putBoolean(KEY_REQUESTED, true) }
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

/**
 * 로그인된 사용자가 도착하는 화면에서 호출한다.
 * 재설치·새 기기·데이터 삭제 시,
 * 사용자의 인지 없이 푸시 알림이 뜨지 않는 현상을 막는다.
 */
@Composable
fun NotificationPermissionRequestEffect() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (NotificationPermission.shouldRequest(context)) {
            NotificationPermission.markRequested(context)
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
