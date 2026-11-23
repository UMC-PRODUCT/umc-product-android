package com.umc.lint

import com.android.SdkConstants.ANDROID_URI
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Element

/**
 * 매니페스트에서 화면 방향을 고정하거나 창 크기 조절을 막는 것을 금지한다.
 *
 * 내장 LockedOrientationActivity 는 세로 계열 값만 보고, category 가 Correctness 라 UMC 게이트로 막을 수 없다.
 * 레이아웃은 창 크기를 기준으로 만들기로 했으므로(CLAUDE.md) 방향·크기를 고정하는 선언 자체를 막는다.
 */
class LockedScreenOrientationDetector : Detector(), XmlScanner {

    override fun getApplicableElements(): Collection<String> = listOf("application", "activity", "activity-alias")

    override fun visitElement(context: XmlContext, element: Element) {
        element.getAttributeNodeNS(ANDROID_URI, "screenOrientation")?.let { attribute ->
            if (attribute.value.trim() !in FREE_ORIENTATIONS) {
                context.report(
                    issue = ISSUE,
                    scope = attribute,
                    location = context.getLocation(attribute),
                    message = "screenOrientation=\"${attribute.value}\"는 화면 방향을 고정합니다. 속성을 지우고 회전·창 크기 변화에 대응하세요.",
                )
            }
        }
        element.getAttributeNodeNS(ANDROID_URI, "resizeableActivity")?.let { attribute ->
            // @bool/… 나 ${'$'}{…} 처럼 값을 알 수 없으면 false 일 수 있으므로 함께 막는다.
            val value = attribute.value.trim()
            if (value.equals("false", ignoreCase = true) || value.startsWith("@") || value.startsWith("${'$'}{")) {
                context.report(
                    issue = ISSUE,
                    scope = attribute,
                    location = context.getLocation(attribute),
                    message = "resizeableActivity=\"${attribute.value}\"는 분할 화면·자유 창에서 크기 조절을 막을 수 있습니다. 속성을 지우세요.",
                )
            }
        }
    }

    companion object {
        /** 기기 회전과 사용자 설정을 그대로 따르는 값. 리소스 참조(@integer/…)처럼 값을 알 수 없는 것도 고정으로 본다. */
        private val FREE_ORIENTATIONS = setOf("unspecified", "user", "fullUser", "sensor", "fullSensor", "behind")

        @JvmField
        val ISSUE: Issue = Issue.create(
            id = "LockedScreenOrientation",
            briefDescription = "화면 방향 고정 또는 창 크기 조절 금지",
            explanation = """
                android:screenOrientation 을 portrait·landscape·locked·nosensor 등으로 고정하거나 \
                android:resizeableActivity="false" 를 두면, 태블릿·폴더블·분할 화면에서 앱이 레터박스로 갇히거나 \
                회전을 따르지 않습니다. Android 16(targetSdk 36)부터는 600dp 이상 화면에서 방향 고정이 무시되므로, \
                고정에 기대어 만든 레이아웃은 그 기기에서 깨집니다.

                속성을 지우고, 레이아웃은 창 크기를 기준으로 만드세요(widthIn(max = …), WindowSizeClass 등).
            """.trimIndent(),
            category = UMC_CATEGORY,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(LockedScreenOrientationDetector::class.java, Scope.MANIFEST_SCOPE),
        )
    }
}
