package com.umc.product.di

import com.umc.domain.repository.AuthRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.repository.ChallengerRepository
import com.umc.domain.repository.schedule.ScheduleRepository
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.Schedule.GetScheduleDetailUseCase
import com.umc.domain.usecase.Schedule.GetScheduleListUseCase
import com.umc.domain.usecase.Schedule.GetScheduleMonthUseCase
import com.umc.domain.usecase.appDataStore.UpdateUserInfoUseCase
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**UseCase 모듈 관리를 위해!**/

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Singleton
    @Provides
    fun providesPostLoginUseCase(repository: AuthRepository): PostLoginUseCase {
        return PostLoginUseCase(repository)
    }


    /**member usecase**/
    @Singleton
    @Provides
    fun providesGetMyProfileUseCase(
        memberRepository: MemberRepository,
    ): GetMyProfileUseCase {
        return GetMyProfileUseCase(memberRepository)
    }

    @Singleton
    @Provides
    fun providesGetMemberProfileUseCase(
        memberRepository: MemberRepository
    ): GetMemberProfileUseCase {
        return GetMemberProfileUseCase(memberRepository)
    }


    /**schedule usecase**/
    @Singleton
    @Provides
    fun providesGetScheduleListUseCase(repository: ScheduleRepository): GetScheduleListUseCase {
        return GetScheduleListUseCase(repository)
    }

    @Singleton
    @Provides
    fun providesGetScheduleMonthUseCase(repository: ScheduleRepository): GetScheduleMonthUseCase {
        return GetScheduleMonthUseCase(repository)
    }

    @Singleton
    @Provides
    fun providesGetScheduleDetailUseCase(repository: ScheduleRepository): GetScheduleDetailUseCase {
        return GetScheduleDetailUseCase(repository)
    }


    /**challenger usecase**/
    @Singleton
    @Provides
    fun providesGetChallengerDetailUseCase(repository: ChallengerRepository): GetChallengerDetailUseCase {
        return GetChallengerDetailUseCase(repository)
    }

    @Singleton
    @Provides
    fun providesGetScheduleDetailUseCase(repository: ScheduleRepository): GetScheduleDetailUseCase {
        return GetScheduleDetailUseCase(repository)
    }
}
