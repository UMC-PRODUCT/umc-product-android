package com.umc.data.dataSource.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.ProfileInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.home.NotificationItem

import com.umc.domain.model.UserRole
import com.umc.domain.model.mypage.UserOutLink
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()


    // 아웃링크 Flow (마이페이지 깃허브/블로그/링크드인)
    val userOutLinkFlow: Flow<UserOutLink> = context.dataStore.data.map { prefs ->
        UserOutLink(
            github = prefs[KEY_GITHUB] ?: "",
            linkedin = prefs[KEY_LINKEDIN] ?: "",
            blog = prefs[KEY_BLOG] ?: ""
        )
    }

    // 아웃 링크 저장
    suspend fun saveOutLink(outLink: UserOutLink) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GITHUB] = outLink.github
            prefs[KEY_LINKEDIN] = outLink.linkedin
            prefs[KEY_BLOG] = outLink.blog
        }
    }

    // 유저 정보 Flow
    val userInfoFlow: Flow<UserInfo> = context.dataStore.data.map { prefs ->
        val rolesJson = prefs[KEY_ROLES] ?: "[]"
        val rolesList = gson.fromJson(rolesJson, Array<UserRole>::class.java).toList()

        val recordsJson = prefs[KEY_RECORDS] ?: "[]"
        val recordsList = gson.fromJson(recordsJson, Array<ChallengerRecord>::class.java).toList()

        val profileJson = prefs[KEY_PROFILE] ?: ""
        val profileData = if (profileJson.isNotEmpty()) {
            gson.fromJson(profileJson, ProfileInfo::class.java)
        } else {
            // 기본값 처리
            ProfileInfo(0, "", "", "", "", "")
        }

        UserInfo(
            id = prefs[KEY_ID] ?: 0L,
            name = prefs[KEY_NAME] ?: "",
            nickname = prefs[KEY_NICKNAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            schoolId = prefs[KEY_SCHOOL_ID] ?: 0L,
            schoolName = prefs[KEY_SCHOOL_NAME] ?: "",
            profileImageLink = prefs[KEY_PROFILE_IMAGE] ?: "",
            status = prefs[KEY_STATUS] ?: "ACTIVE",
            roles = rolesList,
            challengerRecords = recordsList,
            profile = profileData
        )
    }

    // 유저 정보를 저장 (User Class 이용)
    suspend fun saveUserInfo(userInfo: UserInfo) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ID] = userInfo.id
            prefs[KEY_NAME] = userInfo.name
            prefs[KEY_NICKNAME] = userInfo.nickname
            prefs[KEY_EMAIL] = userInfo.email
            prefs[KEY_SCHOOL_ID] = userInfo.schoolId
            prefs[KEY_SCHOOL_NAME] = userInfo.schoolName
            prefs[KEY_PROFILE_IMAGE] = userInfo.profileImageLink
            prefs[KEY_STATUS] = userInfo.status
            prefs[KEY_ROLES] = gson.toJson(userInfo.roles)
            prefs[KEY_RECORDS] = gson.toJson(userInfo.challengerRecords)
            prefs[KEY_PROFILE] = gson.toJson(userInfo.profile)
        }
    }

    // 일정 생성 : 최근 장소 검색어 Flow
    val recentSearchesPlaceFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_RECENT_SEARCHES_PLACE] ?: "[]"
        gson.fromJson(json, Array<String>::class.java).toList()
    }

    // 일정 생성 : 최근 장소 검색어 추가 = 최대 10개 저장
    suspend fun addSearchPlaceHistory(query: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_PLACE] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            currentList.add(0, query)
            prefs[KEY_RECENT_SEARCHES_PLACE] = gson.toJson(currentList.take(10))
        }
    }

    // 커뮤니티 : 게시글 검색 기록 flow
    val recentSearchesPostFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_RECENT_SEARCHES_POST] ?: "[]"
        gson.fromJson(json, Array<String>::class.java).toList()
    }

    // 커뮤니티 : 게시글 검색어 추가 (최대 20개, 중복 제거 후 맨 앞으로)
    suspend fun addSearchPostHistory(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_POST] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            currentList.add(0, query)
            prefs[KEY_RECENT_SEARCHES_POST] = gson.toJson(currentList.take(20))
        }
    }

    // 커뮤니티 : 개별 검색어 삭제 (X 버튼 로직)
    suspend fun removeSearchPostHistory(query: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_POST] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            prefs[KEY_RECENT_SEARCHES_POST] = gson.toJson(currentList)
        }
    }

    // 커뮤니티 : 전체 검색어 삭제 로직
    suspend fun clearSearchPostHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_RECENT_SEARCHES_POST)
        }
    }

    // 공지사항 : 검색 기록 flow
    val recentSearchesNoticeFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_RECENT_SEARCHES_NOTICE] ?: "[]"
        gson.fromJson(json, Array<String>::class.java).toList()
    }

    // 공지사항 : 검색어 추가 (최대 20개, 중복 제거 후 맨 앞으로)
    suspend fun addSearchNoticeHistory(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_NOTICE] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            currentList.add(0, query)
            prefs[KEY_RECENT_SEARCHES_NOTICE] = gson.toJson(currentList.take(20))
        }
    }

    // 공지사항 : 개별 검색어 삭제 (X 버튼 로직)
    suspend fun removeSearchNoticeHistory(query: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_RECENT_SEARCHES_NOTICE] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<String>::class.java).toMutableList()
            currentList.remove(query)
            prefs[KEY_RECENT_SEARCHES_NOTICE] = gson.toJson(currentList)
        }
    }

    // 공지사항 : 전체 검색어 삭제 로직
    suspend fun clearSearchNoticeHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_RECENT_SEARCHES_NOTICE)
        }
    }


    // 공지사항 : 읽은 공지 ID 목록 Flow
    val readNoticeIdsFlow: Flow<Set<Long>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_READ_NOTICE_IDS] ?: "[]"
        gson.fromJson(json, Array<Long>::class.java).toSet()
    }

    // 공지사항 : 읽은 공지 ID 추가
    suspend fun addReadNoticeId(noticeId: Long) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_READ_NOTICE_IDS] ?: "[]"
            val currentSet = gson.fromJson(currentJson, Array<Long>::class.java).toMutableSet()
            currentSet.add(noticeId)
            // 최대 1000개까지만 저장 (메모리 관리)
            val trimmedSet = if (currentSet.size > 1000) {
                currentSet.drop(currentSet.size - 1000).toSet()
            } else {
                currentSet
            }
            prefs[KEY_READ_NOTICE_IDS] = gson.toJson(trimmedSet)
        }
    }

    // 공지사항 : 읽은 공지 ID 목록 전체 삭제
    suspend fun clearReadNoticeIds() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_READ_NOTICE_IDS)
        }
    }


    //------------JWT 관련 로직-----------------//
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun getAccessToken(): String {
        return context.dataStore.data.first()[KEY_ACCESS_TOKEN] ?: ""
    }

    suspend fun getRefreshToken(): String {
        return context.dataStore.data.first()[KEY_REFRESH_TOKEN] ?: ""
    }

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // 알림 목록 Flow
    val notificationsFlow: Flow<List<NotificationItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_NOTIFICATIONS] ?: "[]"
        gson.fromJson(json, Array<NotificationItem>::class.java).toList()
    }

    // 알림 추가 (최대 50개 저장, 중복 제거)
    suspend fun addNotification(notification: NotificationItem) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_NOTIFICATIONS] ?: "[]"
            val currentList = gson.fromJson(currentJson, Array<NotificationItem>::class.java).toMutableList()
            // 중복 체크: 동일한 title과 content가 있으면 제거 후 새로 추가
            currentList.removeAll { it.title == notification.title && it.content == notification.content }
            currentList.add(0, notification)
            prefs[KEY_NOTIFICATIONS] = gson.toJson(currentList.take(50))
        }
    }

    // 알림 전체 삭제
    suspend fun clearNotifications() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_NOTIFICATIONS)
        }
    }


    // 여기서 Datastore에 들어갈 key들 정의
    companion object {
        //외부 링크 KEY
        val KEY_GITHUB = stringPreferencesKey("github_url")
        val KEY_LINKEDIN = stringPreferencesKey("linkedin_url")
        val KEY_BLOG = stringPreferencesKey("blog_url")

        //유저 정보 KEY
        val KEY_ID = longPreferencesKey("id")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_NICKNAME = stringPreferencesKey("nickname")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_SCHOOL_ID = longPreferencesKey("school_id")
        val KEY_SCHOOL_NAME = stringPreferencesKey("school_name")
        val KEY_PROFILE_IMAGE = stringPreferencesKey("profile_image")
        val KEY_STATUS = stringPreferencesKey("status")
        val KEY_ROLES = stringPreferencesKey("roles")
        val KEY_RECORDS = stringPreferencesKey("challenger_records")
        val KEY_PROFILE = stringPreferencesKey("profile_info")

        //일정 추가에서 장소 기록 KEY
        val KEY_RECENT_SEARCHES_PLACE = stringPreferencesKey("recent_searches_place")

        //게시글 검색에서 게시글 검색 기록 KEY
        val KEY_RECENT_SEARCHES_POST = stringPreferencesKey("recent_searches_post")

        //공지사항 검색에서 검색 기록 KEY
        val KEY_RECENT_SEARCHES_NOTICE = stringPreferencesKey("recent_searches_notice")

        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        // 알림 저장 KEY
        val KEY_NOTIFICATIONS = stringPreferencesKey("notifications")

        // 읽은 공지사항 ID 목록 KEY
        val KEY_READ_NOTICE_IDS = stringPreferencesKey("read_notice_ids")
    }

}