package com.umc.data.api

import com.umc.data.response.organization.SchoolPageResponse
import com.umc.data.response.organization.StudyGroupListResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.organization.SchoolIdRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OrganizationApi {

    companion object {
        const val PATH_GROUP_ID = "groupId"
        const val PATH_GISU_ID = "gisuId"
        const val QUERY_CURSOR = "cursor"
        const val QUERY_SIZE = "size"
        const val QUERY_KEYWORD = "keyword"
        const val QUERY_CHAPTER_ID = "chapterId"
        const val QUERY_PAGE = "page"
        const val QUERY_SORT = "sort"
    }

    @DELETE(Endpoints.Organization.SCHOOL)
    suspend fun deleteSchool(
        @Body request: SchoolIdRequest
    ): ApiResponse<Unit>

    @DELETE(Endpoints.Organization.STUDY_GROUD_ID)
    suspend fun deleteStudyGroup(
        @Path(PATH_GROUP_ID) groupId: Int
    ): ApiResponse<Unit>

    @DELETE(Endpoints.Organization.GISU_ID)
    suspend fun deleteGisu(
        @Path(PATH_GISU_ID) gisuId: Int
    ): ApiResponse<Unit>

    @GET(Endpoints.Organization.STUDY_GROUP)
    suspend fun getMyStudyGroup(
        @Query(QUERY_CURSOR) cursor: Int,
        @Query(QUERY_SIZE) size: Int,
    ): ApiResponse<StudyGroupListResponse>

    @GET(Endpoints.Organization.SCHOOL)
    suspend fun getSchoolByKeyword(
        @Query(QUERY_KEYWORD) keyword: String,
        @Query(QUERY_CHAPTER_ID) chapterId: Int,
        @Query(QUERY_PAGE) page: Int,
        @Query(QUERY_SIZE) size: Int,
        @Query(QUERY_SORT) sort: String,
    ): ApiResponse<SchoolPageResponse>

}