package com.umc.domain.repository
import com.umc.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

/**Data Store 정의를 위한 local 부분**/
interface AppDataStoreRepository {

    //외부 링크(깃허브,블로그,링크드인) getter/setter
    fun getUserOutLink(): Flow<Map<String, String>>
    suspend fun saveUserOutLink(github: String, linkedin: String, blog: String)

    //일정 추가 -> 최근 장소 검색 기록 저장
    fun getRecentSearchesPlace(): Flow<List<String>>
    suspend fun addRecentSearchPlace(query: String)

    //유저 정보 getter/setter
    fun getUserInfo(): Flow<UserInfo>
    suspend fun saveUserInfo(userInfo: UserInfo)
}