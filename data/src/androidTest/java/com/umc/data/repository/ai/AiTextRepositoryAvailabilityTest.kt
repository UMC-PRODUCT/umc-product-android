package com.umc.data.repository.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.util.Log
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.AiFeatureStatus
import com.umc.domain.model.enums.AiTextFeature
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 실행 중인 기기에서 온디바이스 AI(Gemini Nano) 가용 상태를 확인하는 계측 테스트.
 *
 * AICore 미지원 기기(에뮬레이터 등)에서는 UNAVAILABLE이 반환되고 앱은 AI 메뉴를 숨기는 폴백으로
 * 동작해야 한다. 어떤 기기에서도 예외 없이 상태를 반환하는 것이 이 테스트의 계약이다.
 * 지원 기기에서 실행하면 로그로 실제 상태를 확인할 수 있다
 */
@RunWith(AndroidJUnit4::class)
class AiTextRepositoryAvailabilityTest {

    private val repository = AiTextRepositoryImpl(
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    )

    @Test
    fun 기기_상태와_무관하게_가용성_확인이_예외없이_끝난다() = runBlocking {
        AiTextFeature.entries.forEach { feature ->
            val status = repository.checkStatus(feature)
            Log.i(TAG, "[$feature] status = $status")
            assertNotNull(status)
        }
    }

    /**
     * 미지원 기기에서는 추론 호출이 크래시 없이 Fail로 떨어져야 한다.
     * (지원 기기에서는 모델 다운로드가 시작되므로 이 테스트는 건너뛴다)
     */
    @Test
    fun 미지원_기기에서는_추론이_크래시없이_실패로_처리된다() = runBlocking {
        if (repository.checkStatus(AiTextFeature.GENERATION) != AiFeatureStatus.UNAVAILABLE) {
            Log.i(TAG, "지원 기기이므로 미지원 폴백 검증을 건너뜀")
            return@runBlocking
        }

        val summary = repository.summarize("테스트 문장입니다. ".repeat(30))
        val generated = repository.generate("안녕하세요를 정중하게 다듬어주세요.")

        Log.i(TAG, "summarize = $summary")
        Log.i(TAG, "generate = $generated")

        assert(summary is ApiState.Fail) { "미지원 기기에서 summarize가 Fail이 아님: $summary" }
        assert(generated is ApiState.Fail) { "미지원 기기에서 generate가 Fail이 아님: $generated" }
    }

    private companion object {
        const val TAG = "AiAvailabilityTest"
    }
}
