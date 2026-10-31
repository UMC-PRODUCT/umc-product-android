package com.umc.domain.usecase.community

import com.umc.domain.model.community.CommunityThreadPage
import com.umc.domain.repository.community.CommunityThreadRepository

import javax.inject.Inject

class GetCommunityThreadsUseCase @Inject constructor(
    private val communityThreadRepository: CommunityThreadRepository,
) {

    suspend operator fun invoke(
        filter: String,
        query: String? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): Result<CommunityThreadPage> {
        return communityThreadRepository.getCommunityThreads(
            filter = filter,
            query = query,
            offset = offset,
            limit = limit,
        ).map { page ->
            // 서버가 초대되지 않은 스레드도 내려주므로 목록·검색에서 걸러낸다. nextOffset 은 서버 기준이라 그대로 둔다.
            page.copy(
                pinnedThreads = page.pinnedThreads.filterNot { it.isUninvited },
                threads = page.threads.filterNot { it.isUninvited },
            )
        }
    }
}