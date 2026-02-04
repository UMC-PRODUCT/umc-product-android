package com.umc.data.repository

import com.umc.data.dataSource.local.AppDataStore
import com.umc.domain.model.UserInfo
import com.umc.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//Impl = data 와 domain을 연결!

class AppDataStoreRepositoryImpl @Inject constructor(
    private val appDataStore: AppDataStore
) : AppDataStoreRepository {

    //외부 링크 가져오고 받아오기 연결
    override fun getUserOutLink(): Flow<Map<String, String>> = appDataStore.userOutLinkFlow

    override suspend fun saveUserOutLink(github: String, linkedin: String, blog: String) {
        appDataStore.saveOutLink(github, linkedin, blog)
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
}