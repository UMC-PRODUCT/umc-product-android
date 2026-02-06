package com.umc.product.di

import com.umc.data.repository.AppDataStoreRepositoryImpl
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.data.repository.AuthRepositoryImpl
import com.umc.data.repository.kakao.KakaoSearchRepositoryImpl
import com.umc.data.repository.member.MemberRepositoryImpl
import com.umc.domain.repository.AuthRepository
import com.umc.domain.repository.kakao.KakaoSearchRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.data.repository.challenger.ChallengerRepositoryImpl
import com.umc.domain.repository.ChallengerRepository
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
//    @Singleton
//    @Binds
//    abstract fun provides머시기머시기(repositoryImpl: RepositoryImpl): Repository

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
    abstract fun providesAuthRepository(repositoryImpl: AuthRepositoryImpl): AuthRepository

    @Singleton
    @Binds
    abstract fun bindsChallengerRepository(repositoryImpl: ChallengerRepositoryImpl): ChallengerRepository
}
