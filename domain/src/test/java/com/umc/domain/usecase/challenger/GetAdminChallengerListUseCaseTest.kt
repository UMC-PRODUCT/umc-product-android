package com.umc.domain.usecase.challenger

import com.umc.domain.model.act.challenger.AdminChallengerList
import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ChallengerRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GetAdminChallengerListUseCaseTest {
    @Test fun `선택 기수가 없으면 저장소 검색을 호출하지 않는다`() = runBlocking {
        var calls = 0
        val repository = repository { calls++; error("검색하면 안 됨") }
        listOf(null, 0L, -1L).forEach { gisuId ->
            val result = GetAdminChallengerListUseCase(repository)(null, 100, null, gisuId)
            assertTrue(result is ApiState.Fail)
        }
        assertEquals(0, calls)
    }

    @Test fun `선택한 기수 ID와 조직 파트 검색어를 그대로 전달한다`() = runBlocking {
        var received: List<Any?> = emptyList()
        val repository = repository { args ->
            received = args.take(7)
            ApiState.Success(AdminChallengerList(emptyList(), emptyList(), null, false))
        }
        GetAdminChallengerListUseCase(repository)(3, 100, 10, 2, "이름", "ANDROID", 4)
        assertEquals(listOf(3L, 100, 10L, 2L, "이름", "ANDROID", 4L), received)
    }

    private fun repository(block: (Array<out Any?>) -> Any): ChallengerRepository = Proxy.newProxyInstance(
        ChallengerRepository::class.java.classLoader, arrayOf(ChallengerRepository::class.java),
    ) { _, method, args ->
        check(method.name == "getAdminChallengerList")
        block(args ?: emptyArray())
    } as ChallengerRepository
}
