package com.umc.data.repository

import android.util.Log
import com.umc.data.dataSource.local.AppDataStore
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.mypage.UserOutLink
import com.umc.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//Impl = data 와 domain을 연결!

class AppDataStoreRepositoryImpl @Inject constructor(
    private val appDataStore: AppDataStore
) : AppDataStoreRepository {

    //외부 링크 가져오고 받아오기 연결
    override fun getUserOutLink(): Flow<UserOutLink> = appDataStore.userOutLinkFlow

    override suspend fun saveUserOutLink(outLink: UserOutLink) {
        appDataStore.saveOutLink(outLink)
    }

    //일정 추가 -> 최근 장소 검색 기록 연결
    override fun getRecentSearchesPlace(): Flow<List<String>> = appDataStore.recentSearchesPlaceFlow

    override suspend fun addRecentSearchPlace(query: String) {
        appDataStore.addSearchPlaceHistory(query)
    }

    //유저 정보 가져오고 받아오기 연결
    override fun getUserInfo(): Flow<UserInfo> = appDataStore.userInfoFlow

    override suspend fun saveUserInfo(userInfo: UserInfo) {
        appDataStore.saveUserInfo(userInfo)
    }

    override suspend fun getAccessToken(): String {
        return appDataStore.getAccessToken()
    }

    override suspend fun getRefreshToken(): String {
        return appDataStore.getRefreshToken()
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String): ApiState<Unit> {
        return try {
            appDataStore.saveTokens(accessToken, refreshToken)
            ApiState.Success(Unit)
        } catch (e: Exception) {
            ApiState.Fail(
                FailState(
                    message = e.message ?: "Token Save Error",
                    code = "LOCAL_ERROR"
                )
            )
        }
    }

    override suspend fun clearTokens() {
        appDataStore.clearTokens()
    }
}