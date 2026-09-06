package com.umc.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 홈 화면 스크롤의 프레임 지연을 측정한다.
 *
 * Compose 안정성 개선(UiState 의 가변 컬렉션 → 불변 컬렉션)의 효과는 컴파일러 리포트의
 * skippable 수치만으로는 사용자 체감과 연결되지 않는다. 같은 조작을 재현해 프레임 시간을
 * 재야 "리컴포지션이 줄어 실제로 덜 버벅인다"를 확인할 수 있다.
 *
 * FrameTimingMetric 은 프레임별 소요 시간의 분포(P50/P90/P95/P99)를 준다.
 * 개선 전후로 같은 시나리오를 돌려 P90·P99 를 비교한다.
 */
@RunWith(AndroidJUnit4::class)
class HomeScrollBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollHome() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = ITERATIONS,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        },
    ) {
        // 첫 화면이 그려질 때까지 기다린 뒤 스크롤을 반복한다.
        device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE).depth(0)), LAUNCH_TIMEOUT_MS)
        val content = device.findObject(By.scrollable(true)) ?: return@measureRepeated
        content.setGestureMargin(device.displayWidth / MARGIN_DIVISOR)
        repeat(SCROLL_COUNT) {
            content.scroll(Direction.DOWN, SCROLL_PERCENT)
            device.waitForIdle()
        }
        repeat(SCROLL_COUNT) {
            content.scroll(Direction.UP, SCROLL_PERCENT)
            device.waitForIdle()
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.umc.product"
        const val ITERATIONS = 10
        const val LAUNCH_TIMEOUT_MS = 10_000L
        const val SCROLL_COUNT = 3
        const val SCROLL_PERCENT = 0.8f
        const val MARGIN_DIVISOR = 5
    }
}
