package com.umc.lint

import com.android.tools.lint.detector.api.Category

/**
 * 이 저장소의 커스텀 규칙만 묶는 카테고리.
 *
 * CI의 "UMC 커스텀 Lint 게이트"는 lint XML 리포트의 category 값으로 커스텀 규칙을 골라낸다.
 * 규칙 ID 목록을 워크플로에 따로 적어 두지 않으므로, 새 규칙도 이 카테고리만 쓰면 게이트 대상이 된다.
 * 모든 규칙이 이 카테고리를 쓰는지는 UmcIssueRegistryTest가 확인한다.
 */
val UMC_CATEGORY: Category = Category.create(name = "UMC", priority = 110)
