package com.umc.product.di

import com.umc.domain.repository.AuthRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.repository.ChallengerRepository
import com.umc.domain.repository.OrganizationRepository
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationCompleteUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationUseCase
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.member.RegisterUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**UseCase 모듈 관리를 위해!**/

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**auth usecase**/
    @Singleton
    @Provides
    fun providesPostLoginUseCase(repository: AuthRepository): PostLoginUseCase {
        return PostLoginUseCase(repository)
    }

    @Singleton
    @Provides
    fun providesPostEmailVerificationUseCase(repository: AuthRepository): PostEmailVerificationUseCase {
        return PostEmailVerificationUseCase(repository)
    }

    @Singleton
    @Provides
    fun providesPostEmailVerificationCompleteUseCase(repository: AuthRepository): PostEmailVerificationCompleteUseCase {
        return PostEmailVerificationCompleteUseCase(repository)
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

    @Singleton
    @Provides
    fun providesRegisterUseCase(repository: MemberRepository): RegisterUseCase {
        return RegisterUseCase(repository)
    }



    /**schedule usecase**/
    /*
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


     */


    /**challenger usecase**/
    @Singleton
    @Provides
    fun providesGetChallengerDetailUseCase(repository: ChallengerRepository): GetChallengerDetailUseCase {
        return GetChallengerDetailUseCase(repository)
    }



    /**organization usecase**/
    @Singleton
    @Provides
    fun providesGetAllSchoolUseCase(repository: OrganizationRepository): GetAllSchoolUseCase {
        return GetAllSchoolUseCase(repository)
    }

}
