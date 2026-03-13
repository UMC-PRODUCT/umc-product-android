package com.umc.data.dataSource

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
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.AssignSchoolRequest
import com.umc.domain.model.request.organization.ChallengerListRequest
import com.umc.domain.model.request.organization.CreateChapterRequest
import com.umc.domain.model.request.organization.CreateGisuRequest
import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.model.request.organization.EditSchoolRequest
import com.umc.domain.model.request.organization.EditStudyGroupRequest
import com.umc.domain.model.request.organization.SchoolIdRequest
import com.umc.domain.model.request.organization.SchoolRegistrationRequest
import com.umc.domain.model.request.organization.UnAssignSchoolRequest

interface OrganizationDataSource {

    // DELETE
    suspend fun deleteSchool(request: SchoolIdRequest): ApiState<Unit>
    suspend fun deleteStudyGroup(groupId: Long): ApiState<Unit>
    suspend fun deleteGisu(gisuId: Int): ApiState<Unit>

    // GET
    suspend fun getMyStudyGroup(cursor: Long?, size: Int): ApiState<StudyGroupListResponse>
    suspend fun getSchoolByKeyword(
        keyword: String,
        chapterId: Int,
        page: Int,
        size: Int,
        sort: String
    ): ApiState<SchoolPageResponse>
    suspend fun getAllChapter(): ApiState<ChapterResponse>
    suspend fun getChapterDetail(chapterId: Long): ApiState<Chapter>
    suspend fun getStudyGroupDetail(groupId: Long): ApiState<StudyGroupDetailResponse>
    suspend fun getSchoolDetail(schoolId: Long): ApiState<SchoolDetailResponse>
    suspend fun getMyStudyGroupList(): ApiState<MyStudyGroupListResponse>
    suspend fun getUnassignedSchool(gisuId: Int): ApiState<SchoolListResponse>
    suspend fun getSchoolLink(schoolId: Int): ApiState<SchoolLinksResponse>
    suspend fun getAllSchool(): ApiState<SchoolListResponse>
    suspend fun getAllGisu(): ApiState<GisuListResponse>
    suspend fun getActiveGisu(): ApiState<GisuItemResponse>
    suspend fun getChapterWithSchool(gisuId: Int): ApiState<ChapterBySchoolListResponse>
    suspend fun getGisuInfo(gisuId: Long): ApiState<GisuInfoResponse>


    // PATCH
    suspend fun editGroup(groupId: Long, request: EditStudyGroupRequest): ApiState<Unit>
    suspend fun editSchool(schoolId: Int, request: EditSchoolRequest): ApiState<Unit>
    suspend fun unassignSchool(schoolId: Int, request: UnAssignSchoolRequest): ApiState<Unit>
    suspend fun assignSchool(schoolId: Int, request: AssignSchoolRequest): ApiState<Unit>

    // POST
    suspend fun createStudyGroup(request: CreateStudyGroupRequest): ApiState<Unit>
    suspend fun createSchool(request: SchoolRegistrationRequest): ApiState<Unit>
    suspend fun createGisu(request: CreateGisuRequest): ApiState<Unit>
    suspend fun createChapter(request: CreateChapterRequest): ApiState<Unit>
    suspend fun changeActiveGisu(gisuId: Int): ApiState<Unit>

    // PUT
    suspend fun changeGroupMember(groupId: Long, request: ChallengerListRequest): ApiState<Unit>
}