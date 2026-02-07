package com.umc.data.dataSource.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.umc.domain.model.UserInfo
import com.umc.domain.model.mypage.UserOutLink
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()


    // 아웃링크 Flow
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

    // 유저 정보 Flow
    val userInfoFlow: Flow<UserInfo> = context.dataStore.data.map { prefs ->
        UserInfo(
            id = prefs[KEY_ID] ?: 0L,
            name = prefs[KEY_NAME] ?: "",
            nickname = prefs[KEY_NICKNAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            schoolId = prefs[KEY_SCHOOL_ID] ?: 0L,
            schoolName = prefs[KEY_SCHOOL_NAME] ?: "",
            profileImageLink = prefs[KEY_PROFILE_IMAGE] ?: "",
            status = prefs[KEY_STATUS] ?: "ACTIVE"
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

        //일정 추가에서 장소 기록 KEY
        val KEY_RECENT_SEARCHES_PLACE = stringPreferencesKey("recent_searches_place")
    }

}