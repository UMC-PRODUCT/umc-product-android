package com.umc.domain.repository
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.UserOutLink
import kotlinx.coroutines.flow.Flow

/**Data Store 정의를 위한 local 부분**/
interface AppDataStoreRepository {

    //외부 링크(깃허브,블로그,링크드인) getter/setter
    fun getUserOutLink(): Flow<UserOutLink>
    suspend fun saveUserOutLink(outLink: UserOutLink)

    //일정 추가 -> 최근 장소 검색 기록 저장
    fun getRecentSearchesPlace(): Flow<List<String>>
    suspend fun addRecentSearchPlace(query: String)

    //커뮤니티 -> 최근 검색 기록 저장 / 삭제 / 전체 삭제
    fun getRecentSearchesPost(): Flow<List<String>>
    suspend fun addRecentSearchPost(query: String)
    suspend fun removeRecentSearchPost(query: String)
    suspend fun clearRecentSearchPost()


    //유저 정보 getter/setter
    fun getUserInfo(): Flow<UserInfo>
    suspend fun saveUserInfo(userInfo: UserInfo)

    suspend fun getAccessToken(): String
    suspend fun getRefreshToken(): String
    suspend fun saveTokens(accessToken: String, refreshToken: String): ApiState<Unit>
    suspend fun clearTokens()

    //싹 다 초기화
    suspend fun clearAllData()
}