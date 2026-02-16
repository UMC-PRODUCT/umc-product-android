package com.umc.product.di

import com.umc.data.api.AttendanceApi
import com.umc.data.api.AuthApi
import com.umc.data.api.MemberApi
import com.umc.data.api.ChallengerApi
import com.umc.data.api.CommunityApi
import com.umc.data.api.OrganizationApi
import com.umc.data.api.ScheduleApi
import com.umc.data.api.CurriculumApi
import com.umc.data.api.StorageApi
import com.umc.data.api.TermsApi
import com.umc.data.api.WorkbookApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**여기에다가 data->api에서 정의한 API를 retrofit을 이용해 연결**/

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideAuthApi(@NormalRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }


    @Singleton
    @Provides
    fun provideMemberApi(@AuthRetrofit retrofit: Retrofit): MemberApi {
        return retrofit.create(MemberApi::class.java)
    }

    @Singleton
    @Provides
    fun provideChallengerApi(@AuthRetrofit retrofit: Retrofit): ChallengerApi {
        return retrofit.create(ChallengerApi::class.java)
    }

    @Singleton
    @Provides
    fun provideScheduleApi(@AuthRetrofit retrofit: Retrofit): ScheduleApi {
        return retrofit.create(ScheduleApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAttendanceApi(@AuthRetrofit retrofit: Retrofit): AttendanceApi {
        return retrofit.create(AttendanceApi::class.java)
    }

    @Singleton
    @Provides
    fun provideOrganizationApi(@AuthRetrofit retrofit: Retrofit): OrganizationApi {
        return retrofit.create(OrganizationApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCommunityApi(@AuthRetrofit retrofit: Retrofit): CommunityApi {
        return retrofit.create(CommunityApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCurriculumApi(@AuthRetrofit retrofit: Retrofit): CurriculumApi {
        return retrofit.create(CurriculumApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkbookApi(
        @AuthRetrofit retrofit: Retrofit
    ): WorkbookApi =
        retrofit.create(WorkbookApi::class.java)



    @Singleton
    @Provides
    fun provideStorageApi(@AuthRetrofit retrofit: Retrofit): StorageApi {
        return retrofit.create(StorageApi::class.java)
    }

    @Singleton
    @Provides
    fun provideTermsApi(@AuthRetrofit retrofit: Retrofit): TermsApi {
        return retrofit.create(TermsApi::class.java)
    }


}
