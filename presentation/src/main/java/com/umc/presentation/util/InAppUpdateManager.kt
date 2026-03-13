package com.umc.presentation.util

import android.app.Activity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

import com.google.android.play.core.install.InstallStateUpdatedListener

/**
 * Play In-App Update 라이브러리를 사용하여 앱 업데이트를 관리하는 클래스
 *
 * FLEXIBLE 업데이트: 사용자가 앱을 계속 사용하면서 백그라운드에서 업데이트 다운로드
 * IMMEDIATE 업데이트: 사용자에게 즉시 업데이트를 요청 (긴급 업데이트 시 사용)
 */
class InAppUpdateManager(private val activity: Activity) {

    companion object {
        private const val TAG = "InAppUpdateManager"
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 1001
        const val REQUEST_CODE_IMMEDIATE_UPDATE = 1002
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    /**
     * 업데이트 가능 여부 확인 및 업데이트 시작
     * @param onUpdateFailed 업데이트 실패 시 콴백
     */
    fun checkForUpdate(onUpdateFailed: (() -> Unit)? = null) {
        // Google Play Services 사용 가능 여부 확인
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (resultCode != ConnectionResult.SUCCESS) {
            ULog.d("Google Play Services is not available")
            onUpdateFailed?.invoke()
            return
        }

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            handleUpdateInfo(appUpdateInfo, onUpdateFailed)
        }.addOnFailureListener { exception ->
            ULog.d("Failed to get app update info $exception")
            onUpdateFailed?.invoke()
        }
    }

    /**
     * 업데이트 정보를 처리하고 필요한 업데이트 시작
     */
    private fun handleUpdateInfo(
        appUpdateInfo: AppUpdateInfo,
        onUpdateFailed: (() -> Unit)?
    ) {
        when (appUpdateInfo.updateAvailability()) {
            UpdateAvailability.UPDATE_AVAILABLE -> {
                // 업데이트 사용 가능
                when {
                    // 즉시 업데이트가 필요한 경우 (예: 긴급 보안 패치)
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
                        shouldForceImmediateUpdate(appUpdateInfo) -> {
                        startImmediateUpdate(appUpdateInfo, onUpdateFailed)
                    }
                    // 유연한 업데이트 허용
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                        startFlexibleUpdate(appUpdateInfo, onUpdateFailed)
                    }
                    else -> {
                        ULog.d("No suitable update type available")
                        onUpdateFailed?.invoke()
                    }
                }
            }
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                // 이미 업데이트가 진행 중인 경우 (IMMEDIATE 업데이트)
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startImmediateUpdate(appUpdateInfo, onUpdateFailed)
                }
            }
            UpdateAvailability.UPDATE_NOT_AVAILABLE,
            UpdateAvailability.UNKNOWN -> {
                ULog.d("No update available or unknown status")
                onUpdateFailed?.invoke()
            }
        }
    }

    /**
     * 즉시 업데이트가 필요한지 판단
     * (version code 기반으로 판단하거나, 서버에서 긴급 업데이트 플래그를 받을 수 있음)
     */
    private fun shouldForceImmediateUpdate(appUpdateInfo: AppUpdateInfo): Boolean {
        // TODO: 필요시 긴급 업데이트 조건 추가 (예: 특정 버전 이하인 경우)
        // 예시: availableVersionCode 가 현재 versionCode 보다 5 이상 높은 경우 즉시 업데이트
        // return (appUpdateInfo.availableVersionCode() - currentVersionCode) >= 5
        return false
    }

    /**
     * FLEXIBLE 업데이트 시작 (권장)
     * 사용자가 앱을 계속 사용하면서 백그라운드에서 업데이트 다운로드
     */
    private fun startFlexibleUpdate(
        appUpdateInfo: AppUpdateInfo,
        onUpdateFailed: (() -> Unit)?
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                REQUEST_CODE_FLEXIBLE_UPDATE
            )
            Log.d(TAG, "Flexible update flow started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start flexible update", e)
            onUpdateFailed?.invoke()
        }
    }

    /**
     * IMMEDIATE 업데이트 시작 (긴급 업데이트 시 사용)
     * 사용자에게 즉시 업데이트를 요청하고 완료 전까지 앱 사용 불가
     */
    private fun startImmediateUpdate(
        appUpdateInfo: AppUpdateInfo,
        onUpdateFailed: (() -> Unit)?
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                REQUEST_CODE_IMMEDIATE_UPDATE
            )
            Log.d(TAG, "Immediate update flow started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start immediate update", e)
            onUpdateFailed?.invoke()
        }
    }

    /**
     * FLEXIBLE 업데이트 완료 후 설치 대기 중인 업데이트가 있는지 확인
     */
    fun checkForPendingInstall() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // 업데이트 다운로드 완료, 설치 대기 중
                showInstallConfirmation()
            }
        }
    }

    /**
     * 다운로드된 업데이트 설치
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * 설치 확인 UI 표시 (선택사항 - 커스텀 UI로 대체 가능)
     */
    private fun showInstallConfirmation() {
        // 기본적으로 completeUpdate()를 호출하면 자동으로 설치됨
        // 커스텀 UI를 원하면 이 메서드를 수정하여 다이얼로그 등을 표시
        Log.d(TAG, "Update downloaded and ready to install")
        completeUpdate()
    }

    /**
     * Activity 결과 처리
     * Activity의 onActivityResult에서 호출
     */
    fun onActivityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            REQUEST_CODE_FLEXIBLE_UPDATE,
            REQUEST_CODE_IMMEDIATE_UPDATE -> {
                if (resultCode != Activity.RESULT_OK) {
                    Log.w(TAG, "Update flow failed or cancelled by user, resultCode: $resultCode")
                } else {
                    Log.d(TAG, "Update flow completed successfully")
                }
            }
        }
    }

    /**
     * 업데이트 리스너 등록 (선택사항 - 다운로드 상태 모니터링)
     */
    fun registerListener(listener: InstallStateUpdatedListener) {
        appUpdateManager.registerListener(listener)
    }

    /**
     * 업데이트 리스너 해제
     */
    fun unregisterListener(listener: InstallStateUpdatedListener) {
        appUpdateManager.unregisterListener(listener)
    }
}
