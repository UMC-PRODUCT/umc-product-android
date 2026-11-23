package com.umc.presentation.update

import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * 인앱 업데이트를 언제 어떤 방식으로 띄울지 정한다(#362).
 *
 * Play 라이브러리의 상수(Int)만 받는 순수 함수라 JVM 단위 테스트로 확인한다.
 */
internal object InAppUpdatePolicy {
    /**
     * Play 업데이트 우선순위(0~5)가 이 값 이상이면 즉시 업데이트로 앱 이용을 막는다.
     * 우선순위는 Play Console 화면에서는 못 정하고 Play Developer API(edits.tracks 의 inAppUpdatePriority)로만 준다.
     * 주지 않으면 0 이라 평소에는 유연한 업데이트다.
     */
    const val IMMEDIATE_PRIORITY = 4

    enum class Action { NONE, PROMPT_INSTALL, IMMEDIATE, FLEXIBLE }

    /**
     * 앱이 화면에 올라올 때(onResume)마다 판정한다.
     *
     * @param immediateStarted 지금 진행 중인 업데이트가 이 앱이 띄운 즉시 업데이트인지(시작할 때 기록한 버전 코드로 판단)
     * @param immediateFailed 이 화면에서 즉시 업데이트가 실패했는지. 실패가 반복될 때 화면이 계속 막히지 않게 한다
     * @param flexibleAsked 유연한 업데이트 동의 창을 지금 띄우면 안 되는지(이번 실행에서 이미 띄웠거나 화면이 처음 뜬 때가 아님)
     */
    fun decide(
        updateAvailability: Int,
        installStatus: Int,
        priority: Int,
        immediateAllowed: Boolean,
        flexibleAllowed: Boolean,
        immediateStarted: Boolean,
        immediateFailed: Boolean,
        flexibleAsked: Boolean,
    ): Action = when {
        // 진행 중 상태는 유연한 업데이트 다운로드에도 온다. 또 이 상태의 우선순위 값은 정의되지 않으므로
        // 우선순위가 아니라 "우리가 띄운 즉시 업데이트인지"로 이어 띄울지 정한다.
        // 다 받은 상태라도 즉시 업데이트면 미룰 수 있는 안내 대신 즉시 업데이트 화면을 다시 띄운다
        updateAvailability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
            immediateStarted && !immediateFailed -> Action.IMMEDIATE
        // 다 받아 둔 유연한 업데이트는 다시 시작해 적용할지 묻는다
        installStatus == InstallStatus.DOWNLOADED -> Action.PROMPT_INSTALL
        updateAvailability != UpdateAvailability.UPDATE_AVAILABLE -> Action.NONE
        priority >= IMMEDIATE_PRIORITY && immediateAllowed && !immediateFailed -> Action.IMMEDIATE
        flexibleAllowed && !flexibleAsked -> Action.FLEXIBLE
        else -> Action.NONE
    }
}
