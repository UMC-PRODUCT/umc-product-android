package com.umc.data.repository

import com.umc.data.dataSource.local.AppDataStore
import com.umc.domain.model.home.LocationItem
import com.umc.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//Impl = data 와 domain을 연결!

class AppDataStoreRepositoryImpl @Inject constructor(
    private val appDataStore: AppDataStore
) : AppDataStoreRepository {

    override fun getUserOutLink(): Flow<Map<String, String>> = appDataStore.userOutLinkFlow

    override suspend fun saveUserOutLink(github: String, linkedin: String, blog: String) {
        appDataStore.saveOutLink(github, linkedin, blog)
    }

    override fun getRecentSearchesPlace(): Flow<List<String>> = appDataStore.recentSearchesPlaceFlow

    override suspend fun addRecentSearchPlace(query: String) {
        appDataStore.addSearchPlaceHistory(query)
    }
}