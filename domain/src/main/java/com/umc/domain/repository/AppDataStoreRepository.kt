package com.umc.domain.repository
import kotlinx.coroutines.flow.Flow

/**Data Store 정의를 위한 local 부분**/
interface AppDataStoreRepository {

    fun getUserOutLink(): Flow<Map<String, String>>
    suspend fun saveUserOutLink(github: String, linkedin: String, blog: String)

    fun getRecentSearchesPlace(): Flow<List<String>>
    suspend fun addRecentSearchPlace(query: String)

}