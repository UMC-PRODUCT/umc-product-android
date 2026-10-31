package com.umc.component.fcm

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import com.umc.domain.usecase.notification.UnregisterFcmInstallationUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class FcmTokenSynchronizer @Inject constructor(
    private val store: AppDataStoreRepository,
    private val register: RegisterFcmTokenUseCase,
    private val unregister: UnregisterFcmInstallationUseCase,
    @param:ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gate = FcmSessionGate()
    private val notificationMutex = Mutex()
    private val preferences = context.getSharedPreferences("fcm_session", Context.MODE_PRIVATE)
    private var deletion: Task<Void>? = null

    // 로그인 완료·포그라운드 복귀 시에만 토큰을 얻고 등록한다.
    fun refresh() {
        val generation = gate.generation
        scope.launch {
            gate.runIfCurrent(generation) {
                if (store.getAccessToken().isBlank()) return@runIfCurrent
                try {
                    val messaging = FirebaseMessaging.getInstance()
                    // 이전 로그아웃에서 끝내지 못한 폐기를 재개한다. 실패 시 기존 토큰을 재등록하지 않는다.
                    if (preferences.getBoolean("delete_pending", false)) {
                        val task = deletion ?: messaging.deleteToken().also { deletion = it }
                        try {
                            withTimeout(8_000) { task.awaitResult() }
                        } finally {
                            if (task.isComplete) deletion = null
                        }
                        preferences.edit().putBoolean("delete_pending", false).commit()
                    }
                    messaging.isAutoInitEnabled = true
                    registerToken(withTimeout(8_000) { messaging.token.awaitResult() }, generation)
                } catch (_: TimeoutCancellationException) {
                    Log.w("log_fcm", "FCM token refresh timed out; will retry on foreground")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    Log.w("log_fcm", "FCM token refresh failed; will retry on foreground")
                }
            }
        }
    }

    fun sync(token: String) {
        val generation = gate.generation
        scope.launch {
            gate.runIfCurrent(generation) {
                if (!preferences.getBoolean("delete_pending", false)) registerToken(token, generation)
            }
        }
    }

    private suspend fun registerToken(token: String, generation: Long) {
        repeat(3) { attempt ->
            try {
                if (generation != gate.generation || store.getAccessToken().isBlank()) return
                if (withTimeoutOrNull(8_000) { register(token) } is ApiState.Success) return
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                Log.w("log_fcm", "FCM token synchronization failed; will retry")
            }
            if (attempt < 2) delay(1_000L shl attempt)
        }
    }

    /** 인증이 남아 있을 때 서버 등록 해제 후 로컬 세션과 표시된 알림을 정리한다. */
    suspend fun logout(clearSession: suspend () -> Unit) = withContext(NonCancellable + Dispatchers.IO) {
        gate.logout {
            try {
                if (store.getAccessToken().isNotBlank()) {
                    val result = withTimeoutOrNull(8_000) { unregister() }
                    if (result !is ApiState.Success) Log.w("log_fcm", "FCM installation removal failed; retiring local token")
                }
            } catch (_: Exception) {
                Log.w("log_fcm", "FCM installation removal failed; retiring local token")
            } finally {
                notificationMutex.withLock {
                    try {
                        clearSession()
                    } finally {
                        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
                        val messaging = FirebaseMessaging.getInstance()
                        messaging.isAutoInitEnabled = false
                        preferences.edit().putBoolean("delete_pending", true).commit()
                        deletion = messaging.deleteToken()
                    }
                }
            }
        }
    }

    /** 알림 저장·표시도 로그아웃과 같은 경계를 사용한다. */
    suspend fun withActiveSession(action: suspend () -> Unit) {
        val generation = gate.generation
        notificationMutex.withLock {
            if (generation == gate.generation && store.getAccessToken().isNotBlank()) action()
        }
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
    addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
