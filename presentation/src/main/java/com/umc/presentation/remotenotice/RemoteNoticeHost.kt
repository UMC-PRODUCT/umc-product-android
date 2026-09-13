package com.umc.presentation.remotenotice

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.component.UDialog
import com.umc.component.theme.AppStrings
import com.umc.domain.model.remoteconfig.RemoteNoticeTemplate
import java.time.LocalDate

/**
 * 원격 설정으로 켠 안내를 현재 화면 위에 띄운다
 *
 * 앱 최상단(MainActivity)에 하나만 두면 각 화면은 이 기능을 몰라도 된다.
 * 어느 화면에 무엇을 띄울지는 UMC-PRODUCT/umc-product-android-config 의 app-config.json 이 정한다.
 *
 * 이용을 막는 안내([RemoteNoticeTemplate.BLOCKING])는 하단바까지 덮어야 하므로 Scaffold 바깥에 두어야 한다.
 *
 * @param currentRoute 현재 내비게이션 route 문자열
 */
@Composable
fun RemoteNoticeHost(
    currentRoute: String?,
    viewModel: RemoteNoticeViewModel = hiltViewModel(),
) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.refresh()
    }

    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val dismissed by viewModel.dismissed.collectAsStateWithLifecycle()

    val screen = currentRoute?.toScreenName() ?: return
    val notice = remember(screen, notices, dismissed) {
        val today = LocalDate.now()
        val candidates = notices.filter { it.targets(screen) && it.isShowable(today) }

        // 이용을 막는 안내가 있으면 무엇보다 먼저 보여준다. 닫을 수 없는 안내라 '닫은 안내' 기록과는 상관없다
        candidates.firstOrNull { it.template == RemoteNoticeTemplate.BLOCKING }
            ?: candidates.firstOrNull { it.template == RemoteNoticeTemplate.INFO && it.key !in dismissed }
    } ?: return

    when (notice.template) {
        RemoteNoticeTemplate.INFO -> UDialog(
            title = notice.title,
            content = notice.body,
            confirmText = AppStrings.CONFIRM,
            onDismissRequest = { viewModel.dismiss(notice) },
        )

        RemoteNoticeTemplate.BLOCKING -> {
            val activity = LocalContext.current as? Activity
            RemoteBlockingScreen(
                title = notice.title,
                body = notice.body,
                onExitApp = { activity?.finish() },
            )
        }

        RemoteNoticeTemplate.UNKNOWN -> Unit
    }
}
