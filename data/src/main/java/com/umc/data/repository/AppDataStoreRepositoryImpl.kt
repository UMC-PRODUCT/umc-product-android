package com.umc.data.repository

import com.umc.data.dataSource.local.AppDataStore
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.home.NotificationItem
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

    //커뮤니티 -> 최근 게시글 검색 기록 연결
    override fun getRecentSearchesPost(): Flow<List<String>> {
        return appDataStore.recentSearchesPostFlow
    }
    override suspend fun addRecentSearchPost(query: String) {
        appDataStore.addSearchPostHistory(query)
    }
    override suspend fun removeRecentSearchPost(query: String) {
        appDataStore.removeSearchPostHistory(query)
    }
    override suspend fun clearRecentSearchPost() {
        appDataStore.clearSearchPostHistory()
    }

    //공지사항 -> 최근 검색 기록 저장 / 삭제 / 전체 삭제 연결
    override fun getRecentSearchesNotice(): Flow<List<String>> {
        return appDataStore.recentSearchesNoticeFlow
    }
    override suspend fun addRecentSearchNotice(query: String) {
        appDataStore.addSearchNoticeHistory(query)
    }
    override suspend fun removeRecentSearchNotice(query: String) {
        appDataStore.removeSearchNoticeHistory(query)
    }
    override suspend fun clearRecentSearchNotice() {
        appDataStore.clearSearchNoticeHistory()
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

    override suspend fun clearAllData() {
        appDataStore.clearAllData()
    }

    // 알림 관련 메서드 구현
    override fun getNotifications(): Flow<List<NotificationItem>> = appDataStore.notificationsFlow

    override suspend fun addNotification(notification: NotificationItem) {
        appDataStore.addNotification(notification)
    }

    override suspend fun clearNotifications() {
        appDataStore.clearNotifications()
    }
}