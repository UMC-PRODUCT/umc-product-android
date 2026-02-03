package com.umc.data.dataSource.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
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

    // 여기서 Datastore에 들어갈 key들 정의
    companion object {
        val KEY_GITHUB = stringPreferencesKey("github_url")
        val KEY_LINKEDIN = stringPreferencesKey("linkedin_url")
        val KEY_BLOG = stringPreferencesKey("blog_url")
        
        val KEY_RECENT_SEARCHES_PLACE = stringPreferencesKey("recent_searches_place")
    }

    // 아웃링크 Flow
    val userOutLinkFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        mapOf(
            "github" to (prefs[KEY_GITHUB] ?: ""),
            "linkedin" to (prefs[KEY_LINKEDIN] ?: ""),
            "blog" to (prefs[KEY_BLOG] ?: ""),
        )
    }

    // 아웃 링크 저장
    suspend fun saveOutLink(github: String, linkedin: String, blog: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GITHUB] = github
            prefs[KEY_LINKEDIN] = linkedin
            prefs[KEY_BLOG] = blog
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
}