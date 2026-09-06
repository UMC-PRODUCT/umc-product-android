package com.umc.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

/**
 * 이 프로젝트에서 실제로 장애를 일으킨 패턴을 규칙으로 고정한 레지스트리.
 * AI 보조로 코드를 작성할 때 같은 실수가 되풀이되지 않도록, 리뷰가 아닌 빌드에서 막는다.
 */
class UmcIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(
        OneShotEventFlowDetector.ISSUE,
        LifecycleUnawareCollectDetector.ISSUE,
        BlockingCallInRequestPathDetector.ISSUE,
    )

    override val api: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "UMC Product Android",
        identifier = "com.umc.lint",
        feedbackUrl = "https://github.com/UMC-PRODUCT/umc-product-android/issues",
    )
}
