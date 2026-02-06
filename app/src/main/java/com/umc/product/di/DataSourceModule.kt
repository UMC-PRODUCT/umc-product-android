package com.umc.product.di

import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.dataSource.remote.AuthRemoteDataSourceImpl
import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSource
import com.umc.data.dataSource.remote.attendance.AttendanceRemoteDataSourceImpl
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSource
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSourceImpl
import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.dataSource.remote.member.MemberRemoteDataSourceImpl
import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSourceImpl
import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSource
import com.umc.data.dataSource.remote.schedule.ScheduleRemoteDataSourceImpl
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
    abstract fun bindsChallengerRemoteDataSource(dataSourceImpl: ChallengerRemoteDataSourceImpl): ChallengerRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsAttendanceRemoteDataSource(dataSourceImpl: AttendanceRemoteDataSourceImpl): AttendanceRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsScheduleRemoteDataSource(dataSourceImpl: ScheduleRemoteDataSourceImpl): ScheduleRemoteDataSource
}
