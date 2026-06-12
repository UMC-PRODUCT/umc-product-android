package com.umc.domain.repository

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.model.organization.GisuList
import com.umc.domain.model.home.GisuInfo
import com.umc.domain.model.request.organization.AssignSchoolRequest
import com.umc.domain.model.request.organization.ChallengerListRequest
import com.umc.domain.model.request.organization.CreateChapterRequest
import com.umc.domain.model.request.organization.CreateGisuRequest
import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.model.request.organization.EditSchoolRequest
import com.umc.domain.model.request.organization.EditStudyGroupRequest
import com.umc.domain.model.home.schedule.CreateStudyGroupSchedule
import com.umc.domain.model.request.organization.SchoolIdRequest
import com.umc.domain.model.request.organization.SchoolRegistrationRequest
import com.umc.domain.model.request.organization.UnAssignSchoolRequest
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.model.organization.StudyGroupDetail
import com.umc.domain.model.organization.StudyGroupPage

interface OrganizationRepository {

    // DELETE
    suspend fun deleteSchool(request: SchoolIdRequest): ApiState<Unit>

    suspend fun deleteStudyGroup(groupId: Long): ApiState<Unit>

    suspend fun deleteStudyGroupMentor(studyGroupId: Long, mentorId: Long): ApiState<Unit>
    suspend fun deleteStudyGroupMember(studyGroupId: Long, memberId: Long): ApiState<Unit>
    
    suspend fun deleteGisu(gisuId: Int): ApiState<Unit>

    // GET
    suspend fun getMyStudyGroup(cursor: Long?, size: Int): ApiState<StudyGroupPage>

    suspend fun getSchoolByKeyword(
        keyword: String,
        chapterId: Int,
        page: Int,
        size: Int,
        sort: String
    ): ApiState<Unit> // SchoolPageResponse

    suspend fun getAllChapter(): ApiState<List<Chapter>>

    suspend fun getChapterDetail(chapterId: Long): ApiState<Chapter>


    suspend fun getStudyGroupDetail(groupId: Long): ApiState<StudyGroupDetail>

    suspend fun getSchoolDetail(schoolId: Long): ApiState<SchoolInfo> //SchoolDetailResponse -> 기존에 작성한 SchoolInfo 사용

    suspend fun getMyStudyGroupList(): ApiState<Unit> //MyStudyGroupListResponse

    suspend fun getUnassignedSchool(gisuId: Int): ApiState<Unit> //SchoolListResponse

    suspend fun getSchoolLink(schoolId: Int): ApiState<Unit> //SchoolLinksResponse

    suspend fun getAllSchool(): ApiState<List<SchoolInfo>>

    suspend fun getAllGisu(): ApiState<GisuList>

    suspend fun getActiveGisu(): ApiState<Long>

    suspend fun getChapterWithSchool(gisuId: Int): ApiState<ChapterWithSchool>

    suspend fun getGisuInfo(gisuId: Long): ApiState<GisuInfo> //GisuInfoResponse

    // PATCH
    suspend fun editGroup(groupId: Long, request: EditStudyGroupRequest): ApiState<Unit>
    suspend fun addStudyGroupMentor(studyGroupId: Long, mentorId: Long): ApiState<Unit>
    suspend fun addStudyGroupMember(studyGroupId: Long, memberId: Long): ApiState<Unit>


    suspend fun editSchool(schoolId: Int, request: EditSchoolRequest): ApiState<Unit>

    suspend fun unassignSchool(schoolId: Int, request: UnAssignSchoolRequest): ApiState<Unit>

    suspend fun assignSchool(schoolId: Int, request: AssignSchoolRequest): ApiState<Unit>

    // POST
    suspend fun createStudyGroup(request: CreateStudyGroupRequest): ApiState<Unit>

    suspend fun createSchool(request: SchoolRegistrationRequest): ApiState<Unit>

    suspend fun createGisu(request: CreateGisuRequest): ApiState<Unit>

    suspend fun createChapter(request: CreateChapterRequest): ApiState<Unit>

    suspend fun changeActiveGisu(gisuId: Int): ApiState<Unit>

    suspend fun createStudyGroupSchedule(request: CreateStudyGroupSchedule): ApiState<Long>

    // PUT
    suspend fun changeGroupMember(groupId: Long, request: ChallengerListRequest): ApiState<Unit>
}