package com.umc.presentation.util

import com.umc.domain.model.organization.GisuItem

/**
 * 전역 기수 캐시 (Singleton)
 * 홈 화면에서 로드한 전체 기수 리스트를 저장하여 어디서든 접근 가능
 */
object GisuCache {
    private var gisuMap: Map<Int, String> = emptyMap()

    /**
     * 기수 리스트를 캐시에 저장
     * @param gisuList 전체 기수 리스트
     */
    fun setGisuList(gisuList: List<GisuItem>) {
        gisuMap = gisuList.associate { it.gisuId to "${it.generation}기" }
    }

    /**
     * 기수 ID로 기수 이름 조회
     * @param gisuId 기수 ID
     * @return 기수 이름 (예: "9기") 또는 null
     */
    fun getGisuName(gisuId: Int): String? {
        return gisuMap[gisuId]
    }

    /**
     * 현재 캐시된 기수 맵 조회
     */
    fun getGisuMap(): Map<Int, String> {
        return gisuMap
    }

    /**
     * 캐시 초기화
     */
    fun clear() {
        gisuMap = emptyMap()
    }
}
