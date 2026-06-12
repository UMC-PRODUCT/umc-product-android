package com.umc.data.api

import com.umc.data.response.organization.Chapter
import com.umc.data.response.organization.ChapterBySchoolListResponse
import com.umc.data.response.organization.ChapterResponse
import com.umc.data.response.organization.GisuInfoResponse
import com.umc.data.response.organization.GisuItemResponse
import com.umc.data.response.organization.GisuListResponse
import com.umc.data.response.organization.MyStudyGroupListResponse
import com.umc.data.response.organization.SchoolDetailResponse
import com.umc.data.response.organization.SchoolLinksResponse
import com.umc.data.response.organization.SchoolListResponse
import com.umc.data.response.organization.SchoolPageResponse
import com.umc.data.response.organization.StudyGroupDetailResponse
import com.umc.data.response.organization.StudyGroupListResponse
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.request.organization.AssignSchoolRequest
import com.umc.domain.model.request.organization.ChallengerListRequest
import com.umc.domain.model.request.organization.CreateChapterRequest
import com.umc.domain.model.request.organization.CreateGisuRequest
import com.umc.data.request.schedule.CreateStudyGroupScheduleRequest
import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.model.request.organization.EditSchoolRequest
import com.umc.domain.model.request.organization.EditStudyGroupRequest
import com.umc.domain.model.request.organization.SchoolIdRequest
import com.umc.domain.model.request.organization.SchoolRegistrationRequest
import com.umc.domain.model.request.organization.UnAssignSchoolRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrganizationApi {

    companion object {
        const val PATH_GROUP_ID = "studyGroupId"
        const val PATH_GISU_ID = "gisuId"
        const val PATH_SCHOOL_ID = "schoolId"
        const val PATH_MEMBER_ID = "memberId"
        const val PATH_MENTOR_ID = "mentorId"
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

    @DELETE(Endpoints.Organization.STUDY_GROUP_ID)
    suspend fun deleteStudyGroup(
        @Path(PATH_GROUP_ID) groupId: Long
    ): ApiResponse<Unit>

    @DELETE(Endpoints.Organization.GISU_ID)
    suspend fun deleteGisu(
        @Path(PATH_GISU_ID) gisuId: Int
    ): ApiResponse<Unit>

    //담당 파트장 제거
    @DELETE(Endpoints.Organization.STUDY_GROUP_MENTOR)
    suspend fun deleteStudyGroupMentor(
        @Path(PATH_GROUP_ID) studyGroupId: Long,
        @Path(PATH_MENTOR_ID) mentorId: Long
    ): ApiResponse<Unit>

    //스터디원 제거
    @DELETE(Endpoints.Organization.STUDY_GROUP_MEMBER)
    suspend fun deleteStudyGroupMember(
        @Path(PATH_GROUP_ID) studyGroupId: Long,
        @Path(PATH_MEMBER_ID) memberId: Long
    ): ApiResponse<Unit>

    @GET(Endpoints.Organization.MY_STUDY_GROUP)
    suspend fun getMyStudyGroup(
        @Query(QUERY_CURSOR) cursor: Long?,
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

    @GET(Endpoints.Organization.CHAPTER)
    suspend fun getAllChapter(): ApiResponse<ChapterResponse>

    @GET(Endpoints.Organization.CHAPTER_ID)
    suspend fun getChapterDetail(
        @Path("chapterId") chapterId: Long,
    ): ApiResponse<Chapter>


    @GET(Endpoints.Organization.STUDY_GROUP_ID)
    suspend fun getStudyGroupDetail(
        @Path(PATH_GROUP_ID) groupId: Long,
    ): ApiResponse<StudyGroupDetailResponse>

    @GET(Endpoints.Organization.SCHOOL_ID)
    suspend fun getSchoolDetail(
        @Path(PATH_SCHOOL_ID) schoolId : Long,
    ): ApiResponse<SchoolDetailResponse>

    @GET(Endpoints.Organization.STUDY_GROUP_NAME)
    suspend fun getMyStudyGroupList(): ApiResponse<MyStudyGroupListResponse>

    @GET(Endpoints.Organization.SCHOOL_UNASSIGNED)
    suspend fun getUnassignedSchool(
        @Query(PATH_GISU_ID) gisuId: Int,
    ): ApiResponse<SchoolListResponse>

    @GET(Endpoints.Organization.SCHOOL_LINK)
    suspend fun getSchoolLink(
        @Query(PATH_SCHOOL_ID) schoolId: Int,
    ): ApiResponse<SchoolLinksResponse>

    @GET(Endpoints.Organization.SCHOOL_ALL)
    suspend fun getAllSchool(): ApiResponse<SchoolListResponse>

    @GET(Endpoints.Organization.GISU_ALL)
    suspend fun getAllGisu(): ApiResponse<GisuListResponse>

    @GET(Endpoints.Organization.GISU_ACTIVE)
    suspend fun getActiveGisu(): ApiResponse<GisuItemResponse>

    @GET(Endpoints.Organization.CHAPTER_WITH_SCHOOL)
    suspend fun getActiveGisu(
        @Query(PATH_GISU_ID) gisuId: Int,
    ): ApiResponse<ChapterBySchoolListResponse>

    @PATCH(Endpoints.Organization.STUDY_GROUP_ID)
    suspend fun editGroup(
        @Path(PATH_GROUP_ID) groupId: Long,
        @Body request: EditStudyGroupRequest
    ): ApiResponse<Unit>

    //담당 파트장 추가
    @PATCH(Endpoints.Organization.STUDY_GROUP_MENTOR)
    suspend fun addStudyGroupMentor(
        @Path(PATH_GROUP_ID) studyGroupId: Long,
        @Path(PATH_MENTOR_ID) mentorId: Long
    ): ApiResponse<Unit>

    //스터디원 추가
    @PATCH(Endpoints.Organization.STUDY_GROUP_MEMBER)
    suspend fun addStudyGroupMember(
        @Path(PATH_GROUP_ID) studyGroupId: Long,
        @Path(PATH_MEMBER_ID) memberId: Long
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Organization.SCHOOL_ID)
    suspend fun editSchool(
        @Path(PATH_SCHOOL_ID) schoolId: Int,
        @Body request: EditSchoolRequest
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Organization.SCHOOL_UNASSIGN)
    suspend fun unassignSchool(
        @Path(PATH_SCHOOL_ID) schoolId: Int,
        @Body request: UnAssignSchoolRequest
    ): ApiResponse<Unit>

    @PATCH(Endpoints.Organization.SCHOOL_ASSIGN)
    suspend fun assignSchool(
        @Path(PATH_SCHOOL_ID) schoolId: Int,
        @Body request: AssignSchoolRequest
    ): ApiResponse<Unit>

    @POST(Endpoints.Organization.STUDY_GROUP)
    suspend fun createStudyGroup(
        @Body request: CreateStudyGroupRequest
    ): ApiResponse<Unit>

    @POST(Endpoints.Organization.SCHOOL)
    suspend fun createSchool(
        @Body request: SchoolRegistrationRequest
    ): ApiResponse<Unit>

    @POST(Endpoints.Organization.GISU)
    suspend fun createGisu(
        @Body request: CreateGisuRequest
    ): ApiResponse<Unit>

    @POST(Endpoints.Organization.CHAPTER)
    suspend fun createChapter(
        @Body request: CreateChapterRequest
    ): ApiResponse<Unit>

    @POST(Endpoints.Organization.GISU_ACTIVE_ID)
    suspend fun changeActiveGisu(
        @Path(PATH_GISU_ID) gisuId: Int,
    ): ApiResponse<Unit>

    @PUT(Endpoints.Organization.STUDY_MEMBER)
    suspend fun changeGroupMember(
        @Path(PATH_GROUP_ID) groupId: Long,
        @Body request: ChallengerListRequest
    ): ApiResponse<Unit>


    @GET(Endpoints.Organization.GISU_ID)
    suspend fun getGisuInfo(
        @Path("gisuId") gisuId: Long,
    ): ApiResponse<GisuInfoResponse>

    @POST(Endpoints.Organization.CREATE_STUDY_GROUP_SCHEDULE)
    suspend fun createStudyGroupSchedule(
        @Body request: CreateStudyGroupScheduleRequest
    ): ApiResponse<Long>

}