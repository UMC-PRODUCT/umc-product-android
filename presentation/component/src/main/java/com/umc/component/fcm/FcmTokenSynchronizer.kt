package com.umc.component.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import android.util.Log

@Singleton
class FcmTokenSynchronizer @Inject constructor(
    private val store: AppDataStoreRepository,
    private val register: RegisterFcmTokenUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    // Firebase persists the latest token; refresh again on foreground after a failed registration.
    fun refresh() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(::sync)
    }

    fun sync(token: String) {
        scope.launch {
            mutex.withLock {
                repeat(3) { attempt ->
                    try {
                        if (store.getAccessToken().isBlank()) return@withLock
                        if (register(token) is ApiState.Success) return@withLock
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        Log.w("log_fcm", "FCM token synchronization failed; will retry")
                    }
                    if (attempt < 2) delay(1_000L shl attempt)
                }
            }
        }
    }
}
