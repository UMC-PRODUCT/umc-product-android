package com.umc.data.repository.ai

import android.content.Context
import android.os.Build
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.SummarizerOptions
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.enums.AiFeatureStatus
import com.umc.domain.model.enums.AiTextFeature
import com.umc.domain.repository.ai.AiTextRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * ML Kit GenAI(Gemini Nano) 기반 온디바이스 AI 텍스트 처리 구현.
 * AICore 지원 기기에서만 동작하며, 미지원 기기에서는 UNAVAILABLE을 반환한다.
 * 추론 요청 시 모델이 준비되지 않았으면 다운로드 후 실행한다
 */
class AiTextRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AiTextRepository {

    private val summarizer by lazy {
        Summarization.getClient(
            SummarizerOptions.builder(context)
                .setInputType(SummarizerOptions.InputType.ARTICLE)
                .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS)
                .setLanguage(SummarizerOptions.Language.KOREAN)
                .build()
        )
    }

    private val generator by lazy { Generation.getClient() }

    override suspend fun checkStatus(feature: AiTextFeature): AiFeatureStatus {
        // ML Kit GenAI는 API 26 이상 필요
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return AiFeatureStatus.UNAVAILABLE

        return runCatching {
            val status = when (feature) {
                AiTextFeature.SUMMARIZATION -> summarizer.checkFeatureStatus().await()
                AiTextFeature.GENERATION -> generator.checkStatus()
            }
            when (status) {
                FeatureStatus.AVAILABLE -> AiFeatureStatus.AVAILABLE
                FeatureStatus.DOWNLOADABLE -> AiFeatureStatus.DOWNLOADABLE
                FeatureStatus.DOWNLOADING -> AiFeatureStatus.DOWNLOADING
                else -> AiFeatureStatus.UNAVAILABLE
            }
        }.getOrDefault(AiFeatureStatus.UNAVAILABLE)
    }

    override suspend fun summarize(text: String): ApiState<String> {
        if (checkStatus(AiTextFeature.SUMMARIZATION) == AiFeatureStatus.UNAVAILABLE) {
            return unavailable()
        }

        return runCatching {
            ensureSummarizerDownloaded()

            val result = summarizer
                .runInference(SummarizationRequest.builder(text).build())
                .await()
            ApiState.Success(result.summary)
        }.getOrElse { error ->
            fail(error)
        }
    }

    override suspend fun generate(prompt: String): ApiState<String> {
        if (checkStatus(AiTextFeature.GENERATION) == AiFeatureStatus.UNAVAILABLE) {
            return unavailable()
        }

        return runCatching {
            ensureGeneratorDownloaded()

            val response = generator.generateContent(prompt)
            val text = response.candidates.firstOrNull()?.text.orEmpty()
            ApiState.Success(text)
        }.getOrElse { error ->
            fail(error)
        }
    }

    /** 요약 모델이 준비되지 않았으면 다운로드가 끝날 때까지 대기 */
    private suspend fun ensureSummarizerDownloaded() {
        when (summarizer.checkFeatureStatus().await()) {
            FeatureStatus.AVAILABLE -> return

            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                suspendCancellableCoroutine<Unit> { continuation ->
                    summarizer.downloadFeature(object : DownloadCallback {
                        override fun onDownloadStarted(bytesToDownload: Long) {}

                        override fun onDownloadProgress(totalBytesDownloaded: Long) {}

                        override fun onDownloadCompleted() {
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        override fun onDownloadFailed(e: GenAiException) {
                            if (continuation.isActive) continuation.cancel(e)
                        }
                    })
                }
            }

            else -> throw IllegalStateException(UNAVAILABLE_MESSAGE)
        }
    }

    /** 생성 모델이 준비되지 않았으면 다운로드가 끝날 때까지 대기 */
    private suspend fun ensureGeneratorDownloaded() {
        when (generator.checkStatus()) {
            FeatureStatus.AVAILABLE -> return

            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                val terminal = generator.download().first { status ->
                    status is DownloadStatus.DownloadCompleted || status is DownloadStatus.DownloadFailed
                }
                if (terminal is DownloadStatus.DownloadFailed) throw terminal.e
            }

            else -> throw IllegalStateException(UNAVAILABLE_MESSAGE)
        }
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
