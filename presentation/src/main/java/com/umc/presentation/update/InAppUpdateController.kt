package com.umc.presentation.update

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Play 인앱 업데이트(#362).
 *
 * MainActivity 가 onCreate 에서 만든다. 결과 런처를 그때 등록해야 하고, 리스너는 Activity 생명주기에 맞춰 스스로 정리한다.
 * 앱이 화면에 올라올 때마다(onResume) Play 에 업데이트 정보를 묻고 [InAppUpdatePolicy.decide] 로 정한다.
 * - 평소: 유연한 업데이트. 동의 창은 화면이 처음 뜰 때, 앱 프로세스마다 한 번만 띄운다. 다 받으면 다시 시작을 묻는다.
 * - 우선순위 [InAppUpdatePolicy.IMMEDIATE_PRIORITY] 이상: 즉시 업데이트. 사용자가 취소하면 앱을 닫고, 다시 열면 또 띄운다.
 *
 * Play 에서 설치하지 않은 빌드(디버그·직접 설치)는 업데이트 정보 조회가 실패하거나 업데이트 없음으로 와서 아무것도 하지 않는다.
 */
internal class InAppUpdateController(private val activity: ComponentActivity) : DefaultLifecycleObserver {
    private val appUpdateManager = AppUpdateManagerFactory.create(activity)
    private val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 다 받은 유연한 업데이트를 다시 시작해 적용할지 물을 차례면 true */
    var isReadyToInstall by mutableStateOf(false)
        private set

    /** 이 인스턴스가 띄운 Play 화면의 결과를 기다리는 중인 업데이트 방식. 같은 화면을 두 번 띄우지 않게 한다 */
    private var inFlightType: Int? = null

    /** 즉시 업데이트를 취소했다는 결과가 왔다. onResume 에서 앱을 닫는다 */
    private var cancelPending = false

    /** 이번 onResume 전에 새 인텐트(알림 탭·아이콘 재진입 등)가 왔다. 그때 함께 오는 취소는 사용자가 누른 것이 아니다 */
    private var interruptedByNewIntent = false

    /**
     * 이 화면에서 즉시 업데이트가 실패했다. 실패가 반복될 때 화면이 계속 막히지 않게 이 화면에서는 다시 띄우지 않는다.
     * 화면(Activity) 단위라 앱을 다시 열면 다시 시도한다
     */
    private var immediateFailed = false

    /**
     * 유연한 업데이트 동의 창을 띄울 차례가 남았는지. 화면이 뜬 뒤 처음 성공한 조회에서만 써서
     * 글쓰기·채팅 도중에 끼어들지 않게 한다. 첫 조회가 실패하면 다음에 성공한 조회까지 차례를 남긴다
     */
    private var flexibleTurnPending = true

    /** 업데이트 정보 조회 번호. Task 완료 순서는 보장되지 않으므로 늦게 도착한 이전 조회 결과는 버린다 */
    private var querySeq = 0

    // 런처는 방식별로 하나씩, 항상 이 순서로 등록한다. 등록 순서가 같아야 화면 회전이나 프로세스 복원 뒤에도
    // 결과가 같은 방식의 콜백으로 돌아온다(ActivityResultRegistry 가 등록 순서로 키를 만든다)
    private val immediateLauncher = activity.registerForActivityResult(StartIntentSenderForResult()) { result ->
        inFlightType = null
        val launchFailed = result.data?.hasExtra(StartIntentSenderForResult.EXTRA_SEND_INTENT_EXCEPTION) == true
        when {
            result.resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED || launchFailed -> {
                Log.w(TAG, "즉시 업데이트 실패")
                immediateFailed = true
            }
            // 결과는 onResume 보다 먼저 온다. 새 인텐트로 화면이 걷힌 경우를 가려내려고 닫기는 onResume 으로 미룬다
            result.resultCode == Activity.RESULT_CANCELED -> cancelPending = true
        }
    }

    private val flexibleLauncher = activity.registerForActivityResult(StartIntentSenderForResult()) {
        inFlightType = null
    }

    private val installListener = InstallStateUpdatedListener { state ->
        // 우리가 띄운 즉시 업데이트는 Play 가 설치까지 맡으므로 미룰 수 있는 안내를 띄우지 않는다
        val immediateRecorded = prefs.contains(KEY_IMMEDIATE_VERSION)
        if (state.installStatus() == InstallStatus.DOWNLOADED && !installPostponed && !immediateRecorded) {
            isReadyToInstall = true
        }
    }

    init {
        appUpdateManager.registerListener(installListener)
        activity.lifecycle.addObserver(this)
    }

    /**
     * MainActivity.onNewIntent 에서 부른다.
     * 화면이 새로 만들어진 경우(회전·프로세스 복원 뒤)에는 새 인텐트가 결과보다 먼저 오므로 조건 없이 표시한다.
     * 사용자가 뒤로 가기로 진짜 취소할 때는 새 인텐트가 끼어들지 않는다
     */
    fun onNewIntent() {
        interruptedByNewIntent = true
    }

    override fun onResume(owner: LifecycleOwner) {
        val interrupted = interruptedByNewIntent
        interruptedByNewIntent = false
        if (cancelPending) {
            cancelPending = false
            // 긴급 업데이트를 사용자가 취소했으면 앱을 닫는다. 다시 열면 또 묻는다
            if (!interrupted) {
                activity.finish()
                return
            }
        }

        val seq = ++querySeq
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener(activity) { info ->
                if (seq != querySeq) return@addOnSuccessListener
                val allowFlexiblePrompt = flexibleTurnPending
                flexibleTurnPending = false
                handle(info, allowFlexiblePrompt)
            }
            .addOnFailureListener(activity) { error ->
                // Play 설치본이 아니면(ERROR_APP_NOT_OWNED 등) 여기로 온다
                Log.d(TAG, "업데이트 정보를 가져오지 못함", error)
            }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        appUpdateManager.unregisterListener(installListener)
    }

    /** 다시 시작해 받아 둔 업데이트를 적용한다. Play 가 설치 화면을 띄우고 앱을 다시 연다 */
    fun completeUpdate() {
        isReadyToInstall = false
        appUpdateManager.completeUpdate()
    }

    /** 나중에: 앱 프로세스가 살아 있는 동안은 다시 묻지 않는다. 받아 둔 업데이트는 남아 있어 프로세스가 새로 뜨면 다시 묻는다 */
    fun postponeInstall() {
        installPostponed = true
        isReadyToInstall = false
    }

    private fun handle(info: AppUpdateInfo, allowFlexiblePrompt: Boolean) {
        if (inFlightType != null || activity.isFinishing) return
        val availability = info.updateAvailability()
        if (availability == UpdateAvailability.UPDATE_NOT_AVAILABLE) prefs.edit { remove(KEY_IMMEDIATE_VERSION) }

        val action = InAppUpdatePolicy.decide(
            updateAvailability = availability,
            installStatus = info.installStatus(),
            priority = info.updatePriority(),
            immediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
            flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
            immediateStarted = prefs.getInt(KEY_IMMEDIATE_VERSION, NO_VERSION) == info.availableVersionCode(),
            immediateFailed = immediateFailed,
            flexibleAsked = flexibleAsked || !allowFlexiblePrompt,
        )
        when (action) {
            InAppUpdatePolicy.Action.PROMPT_INSTALL -> if (!installPostponed) isReadyToInstall = true
            InAppUpdatePolicy.Action.IMMEDIATE -> launch(info, AppUpdateType.IMMEDIATE)
            InAppUpdatePolicy.Action.FLEXIBLE -> if (launch(info, AppUpdateType.FLEXIBLE)) flexibleAsked = true
            InAppUpdatePolicy.Action.NONE -> Unit
        }
    }

    /** Play 업데이트 화면을 띄웠으면 true */
    private fun launch(info: AppUpdateInfo, type: Int): Boolean {
        val immediate = type == AppUpdateType.IMMEDIATE
        val launcher: ActivityResultLauncher<IntentSenderRequest> = if (immediate) immediateLauncher else flexibleLauncher
        val started = appUpdateManager.startUpdateFlowForResult(info, launcher, AppUpdateOptions.defaultOptions(type))
        if (!started) return false
        inFlightType = type
        // 진행 중 상태에서는 우선순위를 알 수 없어서, 이어 띄울 즉시 업데이트인지 버전 코드로 기억해 둔다
        prefs.edit {
            if (immediate) putInt(KEY_IMMEDIATE_VERSION, info.availableVersionCode()) else remove(KEY_IMMEDIATE_VERSION)
        }
        return true
    }

    private companion object {
        const val TAG = "InAppUpdate"
        const val PREFS_NAME = "in_app_update"
        const val KEY_IMMEDIATE_VERSION = "immediate_version_code"
        const val NO_VERSION = -1

        // 앱 프로세스 단위 상태. 화면 회전에는 유지되고, 프로세스가 새로 뜨면 초기화된다
        var flexibleAsked = false
        var installPostponed = false
    }
}
