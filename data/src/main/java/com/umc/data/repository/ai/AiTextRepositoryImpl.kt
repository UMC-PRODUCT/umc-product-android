package com.umc.data.repository.ai

import android.os.Build
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.enums.AiFeatureStatus
import com.umc.domain.repository.ai.AiTextRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * ML Kit GenAI(Gemini Nano) Prompt API 기반 온디바이스 텍스트 생성 구현.
 * AICore 지원 기기에서만 동작하며, 미지원 기기에서는 UNAVAILABLE을 반환한다.
 *
 * 클라이언트는 네이티브 자원을 잡으므로 호출마다 만들고 반드시 닫는다.
 * 모델 자체는 AICore가 관리하므로 재생성 비용은 크지 않다
 */
class AiTextRepositoryImpl @Inject constructor() : AiTextRepository {

    override suspend fun checkStatus(): AiFeatureStatus {
        // ML Kit GenAI는 API 26 이상 필요
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return AiFeatureStatus.UNAVAILABLE

        return runCatching {
            useGenerator { it.checkStatus() }.toFeatureStatus()
        }.getOrDefault(AiFeatureStatus.UNAVAILABLE)
    }

    override suspend fun generate(
        prompt: String,
        onDownloadProgress: (percent: Int) -> Unit,
    ): ApiState<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return unavailable()

        return runCatching {
            useGenerator { generator ->
                ensureGeneratorReady(generator, onDownloadProgress)

                val response = generator.generateContent(prompt)
                ApiState.Success(response.candidates.firstOrNull()?.text.orEmpty())
            }
        }.getOrElse { error -> fail(error) }
    }

    /** 생성 클라이언트를 만들어 사용하고 반드시 닫는다 */
    private suspend fun <T> useGenerator(block: suspend (GenerativeModel) -> T): T {
        val generator = Generation.getClient()
        return try {
            block(generator)
        } finally {
            generator.close()
        }
    }

    /** 모델이 준비되지 않았으면 진행률을 알리며 다운로드를 기다린다 */
    private suspend fun ensureGeneratorReady(
        generator: GenerativeModel,
        onDownloadProgress: (percent: Int) -> Unit,
    ) {
        when (generator.checkStatus()) {
            FeatureStatus.AVAILABLE -> return

            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                var totalBytes = 0L

                val terminal = generator.download()
                    .onEach { status ->
                        when (status) {
                            is DownloadStatus.DownloadStarted -> {
                                totalBytes = status.bytesToDownload
                                onDownloadProgress(0)
                            }

                            is DownloadStatus.DownloadProgress ->
                                onDownloadProgress(toPercent(status.totalBytesDownloaded, totalBytes))

                            else -> Unit
                        }
                    }
                    .first { it is DownloadStatus.DownloadCompleted || it is DownloadStatus.DownloadFailed }

                if (terminal is DownloadStatus.DownloadFailed) throw terminal.e
                onDownloadProgress(100)
            }

            else -> throw IllegalStateException(UNAVAILABLE_MESSAGE)
        }
    }

    private fun toPercent(downloaded: Long, total: Long): Int {
        if (total <= 0L) return 0
        return ((downloaded * 100) / total).toInt().coerceIn(0, 100)
    }

    private fun Int.toFeatureStatus(): AiFeatureStatus = when (this) {
        FeatureStatus.AVAILABLE -> AiFeatureStatus.AVAILABLE
        FeatureStatus.DOWNLOADABLE -> AiFeatureStatus.DOWNLOADABLE
        FeatureStatus.DOWNLOADING -> AiFeatureStatus.DOWNLOADING
        else -> AiFeatureStatus.UNAVAILABLE
    }

    private fun unavailable(): ApiState<String> =
        ApiState.Fail(FailState(code = AI_ERROR_CODE, message = UNAVAILABLE_MESSAGE))

    private fun fail(error: Throwable): ApiState<String> =
        ApiState.Fail(FailState(code = AI_ERROR_CODE, message = error.message ?: FAIL_MESSAGE))

    private companion object {
        const val AI_ERROR_CODE = "ON_DEVICE_AI"
        const val UNAVAILABLE_MESSAGE = "이 기기에서는 AI 기능을 사용할 수 없어요"
        const val FAIL_MESSAGE = "AI 처리에 실패했어요"
    }
}
