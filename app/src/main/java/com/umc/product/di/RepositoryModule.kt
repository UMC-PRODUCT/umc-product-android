package com.umc.product.di

import com.umc.data.repository.AppDataStoreRepositoryImpl
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.data.repository.AuthRepositoryImpl
import com.umc.data.repository.OrganizationRepositoryImpl
import com.umc.data.repository.attendance.AttendanceRepositoryImpl
import com.umc.data.repository.kakao.KakaoSearchRepositoryImpl
import com.umc.data.repository.member.MemberRepositoryImpl
import com.umc.domain.repository.AuthRepository
import com.umc.domain.repository.kakao.KakaoSearchRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.data.repository.challenger.ChallengerRepositoryImpl
import com.umc.data.repository.community.CommunityRepositoryImpl
import com.umc.data.repository.curriculum.CurriculumRepositoryImpl
import com.umc.data.repository.schedule.ScheduleRepositoryImpl
import com.umc.data.repository.storage.StorageRepositoryImpl
import com.umc.data.repository.terms.TermsRepositoryImpl
import com.umc.data.repository.workbook.WorkbookRepositoryImpl
import com.umc.domain.repository.ChallengerRepository
import com.umc.domain.repository.OrganizationRepository
import com.umc.domain.repository.attendance.AttendanceRepository
import com.umc.domain.repository.community.CommunityRepository
import com.umc.domain.repository.curriculum.CurriculumRepository
import com.umc.domain.repository.schedule.ScheduleRepository
import com.umc.domain.repository.storage.StorageRepository
import com.umc.domain.repository.terms.TermsRepository
import com.umc.domain.repository.workbook.WorkbookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**얘는 data <-> domain을 연결하는 역할
 * data 영역에서 api 호출 결과를 domain에 정의된 data class에 맞게 이쁘게 만들어서 보내주기
 * **/

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**아래는 Local DataStore를 사용하기 위한 세팅**/
    @Binds
    @Singleton
    abstract fun bindAppDataStoreRepository(
        appDataStoreRepositoryImpl: AppDataStoreRepositoryImpl
    ): AppDataStoreRepository


    @Singleton
    @Binds
    abstract fun bindsMemberRepository(
        repositoryImpl: MemberRepositoryImpl
    ): MemberRepository

    @Binds
    @Singleton
    abstract fun bindKakaoSearchRepository(
        kakaoSearchRepositoryImpl: KakaoSearchRepositoryImpl
    ): KakaoSearchRepository


    @Singleton
    @Binds
    abstract fun bindsCurriculumRepository(
        impl: CurriculumRepositoryImpl
    ): CurriculumRepository


    @Singleton
    @Binds
    abstract fun providesAuthRepository(repositoryImpl: AuthRepositoryImpl): AuthRepository

    @Singleton
    @Binds
    abstract fun bindsChallengerRepository(repositoryImpl: ChallengerRepositoryImpl): ChallengerRepository

    @Singleton
    @Binds
    abstract fun bindsAttendanceRepository(repositoryImpl: AttendanceRepositoryImpl): AttendanceRepository

    @Singleton
    @Binds
    abstract fun bindsScheduleRepository(repositoryImpl: ScheduleRepositoryImpl): ScheduleRepository

    @Singleton
    @Binds
    abstract fun bindsCommunityRepository(repositoryImpl: CommunityRepositoryImpl): CommunityRepository

    @Singleton
    @Binds
    abstract fun bindsStorageRepository(repositoryImpl: StorageRepositoryImpl): StorageRepository

    @Singleton
    @Binds
    abstract fun providesOrganizationRepository(repositoryImpl: OrganizationRepositoryImpl): OrganizationRepository

    @Singleton
    @Binds
    abstract fun bindsTermsRepository(repositoryImpl: TermsRepositoryImpl): TermsRepository

    @Singleton
    @Binds
    abstract fun bindWorkbookRepository(impl: WorkbookRepositoryImpl): WorkbookRepository


}
