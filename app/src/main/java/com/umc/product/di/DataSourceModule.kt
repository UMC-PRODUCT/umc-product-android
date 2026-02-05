package com.umc.product.di

import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.dataSource.remote.AuthRemoteDataSourceImpl
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSource
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSourceImpl
import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.dataSource.remote.member.MemberRemoteDataSourceImpl
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

}
