package com.umc.product.di

import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.dataSource.NoticeRemoteDataSource
import com.umc.data.dataSource.NotificationRemoteDataSource
import com.umc.data.dataSource.OrganizationDataSource
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.dataSource.remote.AuthRemoteDataSourceImpl
import com.umc.data.dataSource.remote.NoticeRemoteDataSourceImpl
import com.umc.data.dataSource.remote.NotificationRemoteDataSourceImpl
import com.umc.data.dataSource.remote.OrganizationRemoteDataSourceImpl
import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSource
import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSourceImpl
import com.umc.data.dataSource.remote.authorize.AuthorizeRemoteDataSource
import com.umc.data.dataSource.remote.authorize.AuthorizeRemoteDataSourceImpl
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSource
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSourceImpl
import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.dataSource.remote.member.MemberRemoteDataSourceImpl
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSourceImpl
import com.umc.data.dataSource.remote.community.CommunityRemoteDataSource
import com.umc.data.dataSource.remote.community.CommunityRemoteDataSourceImpl
import com.umc.data.dataSource.remote.curriculum.CurriculumRemoteDataSource
import com.umc.data.dataSource.remote.curriculum.CurriculumRemoteDataSourceImpl
import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSourceImpl
import com.umc.data.dataSource.remote.storage.StorageRemoteDataSource
import com.umc.data.dataSource.remote.storage.StorageRemoteDataSourceImpl
import com.umc.data.dataSource.remote.terms.TermsRemoteDataSource
import com.umc.data.dataSource.remote.terms.TermsRemoteDataSourceImpl
import com.umc.data.dataSource.remote.workbook.WorkbookRemoteDataSource
import com.umc.data.dataSource.remote.workbook.WorkbookRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**dataSource <-> dataSourceImpl를 연결
 * API 직접 호출 결과를 ApiState<T> 에 넣는 걸 impl에서 하고 이를 dataSource와 연결
 * **/

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun providesAuthDataSource(dataSourceImpl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Singleton
    @Binds
    abstract fun providesOrganizationDataSource(dataSourceImpl: OrganizationRemoteDataSourceImpl): OrganizationDataSource

    @Singleton
    @Binds
    abstract fun bindsMemberRemoteDataSource(
        dataSourceImpl: MemberRemoteDataSourceImpl
    ): MemberRemoteDataSource


    @Singleton
    @Binds
    abstract fun bindsKakaoRemoteDataSource(
        dataSourceImpl: KakaoRemoteDataSourceImpl
    ): KakaoRemoteDataSource


    @Singleton
    @Binds
    abstract fun bindsCurriculumRemoteDataSource(
        impl: CurriculumRemoteDataSourceImpl
    ): CurriculumRemoteDataSource


    @Singleton
    @Binds
    abstract fun bindsChallengerRemoteDataSource(dataSourceImpl: ChallengerRemoteDataSourceImpl): ChallengerRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsAttendanceRemoteDataSource(dataSourceImpl: AttendanceRemoteDataSourceImpl): AttendanceRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsScheduleRemoteDataSource(dataSourceImpl: ScheduleRemoteDataSourceImpl): ScheduleRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsCommunityRemoteDataSource(dataSourceImpl: CommunityRemoteDataSourceImpl): CommunityRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsStorageRemoteDataSource(dataSourceImpl: StorageRemoteDataSourceImpl): StorageRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsTermsRemoteDataSource(dataSourceImpl: TermsRemoteDataSourceImpl): TermsRemoteDataSource


    @Singleton
    @Binds
    abstract fun bindWorkbookRemoteDataSource(
        impl: WorkbookRemoteDataSourceImpl
    ): WorkbookRemoteDataSource



    @Singleton
    @Binds
    abstract fun bindsNoticeRemoteDataSource(dataSourceImpl: NoticeRemoteDataSourceImpl): NoticeRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsAuthorizeRemoteDataSource(dataSourceImpl: AuthorizeRemoteDataSourceImpl): AuthorizeRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsNotificationRemoteDataSource(dataSourceImpl: NotificationRemoteDataSourceImpl): NotificationRemoteDataSource
}
