package com.umc.domain.repository

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.GisuList
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
import com.umc.domain.model.school.SchoolInfo

interface OrganizationRepository {

    // DELETE
    suspend fun deleteSchool(request: SchoolIdRequest): ApiState<Unit>
    
    suspend fun deleteStudyGroup(groupId: Int): ApiState<Unit>
    
    suspend fun deleteGisu(gisuId: Int): ApiState<Unit>

    // GET
    suspend fun getMyStudyGroup(cursor: Int, size: Int): ApiState<Unit> // StudyGroupListResponse

    suspend fun getSchoolByKeyword(
        keyword: String,
        chapterId: Int,
        page: Int,
        size: Int,
        sort: String
    ): ApiState<Unit> // SchoolPageResponse

    suspend fun getAllChapter(): ApiState<List<Chapter>>

    suspend fun getStudyGroupDetail(groupId: Int): ApiState<Unit> //StudyGroupDetailResponse

    suspend fun getSchoolDetail(schoolId: Int): ApiState<Unit> //SchoolDetailResponse

    suspend fun getMyStudyGroupList(): ApiState<Unit> //MyStudyGroupListResponse

    suspend fun getUnassignedSchool(gisuId: Int): ApiState<Unit> //SchoolListResponse

    suspend fun getSchoolLink(schoolId: Int): ApiState<Unit> //SchoolLinksResponse

    suspend fun getAllSchool(): ApiState<List<SchoolInfo>>

    suspend fun getAllGisu(): ApiState<GisuList>

    suspend fun getActiveGisu(): ApiState<Unit> //GisuItemResponse

    suspend fun getChapterWithSchool(gisuId: Int): ApiState<Unit> //ChapterBySchoolListResponse

    // PATCH
    suspend fun editGroup(groupId: Int, request: EditStudyGroupRequest): ApiState<Unit>

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
    suspend fun changeGroupMember(groupId: Int, request: ChallengerListRequest): ApiState<Unit>
}