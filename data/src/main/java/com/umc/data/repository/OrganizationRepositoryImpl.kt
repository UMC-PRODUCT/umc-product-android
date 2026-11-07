package com.umc.data.repository

import com.umc.data.dataSource.OrganizationDataSource
import com.umc.data.response.organization.*
import com.umc.data.response.organization.GisuListResponse.Companion.toModel
import com.umc.data.response.organization.GisuInfoResponse.Companion.toModel
import com.umc.data.response.organization.SchoolDetailResponse.Companion.toModel
import com.umc.data.response.organization.SchoolNameResponse.Companion.toModel
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.base.mapCatching
import com.umc.domain.model.base.FailState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.organization.ChapterWithSchool
import com.umc.domain.model.organization.GisuList
import com.umc.domain.model.home.GisuInfo
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.organization.StudyGroupDetail
import com.umc.domain.model.organization.StudyGroupPage
import com.umc.domain.model.request.organization.*
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.repository.OrganizationRepository
import com.umc.domain.model.organization.ManagedStudyGroupPage
import com.umc.data.response.organization.GisuItemResponse.Companion.toModel

import javax.inject.Inject

class OrganizationRepositoryImpl @Inject constructor(
    private val organizationDataSource: OrganizationDataSource
) : OrganizationRepository {

    // DELETE
    override suspend fun deleteSchool(request: SchoolIdRequest): ApiState<Unit> =
        organizationDataSource.deleteSchool(request)

    override suspend fun deleteStudyGroup(groupId: Long): ApiState<Unit> =
        organizationDataSource.deleteStudyGroup(groupId)

    override suspend fun deleteGisu(gisuId: Int): ApiState<Unit> =
        organizationDataSource.deleteGisu(gisuId)

    override suspend fun deleteStudyGroupMentor(
        studyGroupId: Long,
        mentorId: Long,
    ): ApiState<Unit> =
        organizationDataSource.deleteStudyGroupMentor(
            studyGroupId = studyGroupId,
            mentorId = mentorId,
        )


    override suspend fun deleteStudyGroupMember(
        studyGroupId: Long,
        memberId: Long,
    ): ApiState<Unit> =
        organizationDataSource.deleteStudyGroupMember(
            studyGroupId = studyGroupId,
            memberId = memberId,
        )


    // GET
    override suspend fun getMyStudyGroup(cursor: Long?, size: Int): ApiState<StudyGroupPage> =
        organizationDataSource.getMyStudyGroup(cursor, size).map { dto ->
            (dto ?: StudyGroupListResponse()).toModel()
        }

    override suspend fun getManagedStudyGroups(
        cursor: Long?,
        size: Int,
    ): ApiState<ManagedStudyGroupPage> =
        organizationDataSource.getManagedStudyGroups(
            cursor = cursor,
            size = size,
        ).map { dto ->
            dto.toModel()
        }


    override suspend fun getSchoolByKeyword(
        keyword: String,
        chapterId: Int,
        page: Int,
        size: Int,
        sort: String
    ): ApiState<Unit> =
        organizationDataSource.getSchoolByKeyword(keyword, chapterId, page, size, sort)
            .map { Unit } //임시

    override suspend fun getAllChapter(): ApiState<List<Chapter>> =
        organizationDataSource.getAllChapter().map { response ->
            response.chapters.map { Chapter(it.id, it.name) }
        }

    override suspend fun getChapterDetail(chapterId: Long): ApiState<Chapter> {
        return organizationDataSource.getChapterDetail(chapterId).map { response ->
            Chapter(response.id, response.name)
        }
    }

    override suspend fun getStudyGroupDetail(groupId: Long): ApiState<StudyGroupDetail> =
        organizationDataSource.getStudyGroupDetail(groupId).map { dto ->
            dto?.toModel() ?: StudyGroupDetail(
                groupId = groupId,
                name = "",
                part = "",
                partDisplayName = "",
                schools = emptyList(),
                createdAt = "",
                memberCount = 0,
                leader = com.umc.domain.model.organization.StudyGroupMember(0L, 0L, "", null),
                members = emptyList()
            )
        }

    override suspend fun getSchoolDetail(schoolId: Long): ApiState<SchoolInfo> =
        organizationDataSource.getSchoolDetail(schoolId).map { dto ->
            dto.toModel()
        } //임시

    override suspend fun getMyStudyGroupList(): ApiState<Unit> =
        organizationDataSource.getMyStudyGroupList().map { Unit } //임시

    override suspend fun getUnassignedSchool(gisuId: Int): ApiState<Unit> =
        organizationDataSource.getUnassignedSchool(gisuId).map { Unit } //임시

    override suspend fun getSchoolLink(schoolId: Int): ApiState<Unit> =
        organizationDataSource.getSchoolLink(schoolId).map { Unit }

    override suspend fun getAllSchool(): ApiState<List<SchoolInfo>> =
        organizationDataSource.getAllSchool().map {
            it.schools.map { item -> item.toModel() }
                .sortedBy { school -> school.schoolName }
        }

    override suspend fun getAllGisu(): ApiState<GisuList> =
        organizationDataSource.getAllGisu().map { it.toModel() }

    override suspend fun getSearchGisus(): ApiState<GisuList> {
        val gisus = mutableListOf<GisuItem>()
        // 잘못된 hasNext 응답으로 무한 조회하거나 일부 기수만 성공으로 반환하지 않는다.
        repeat(100) { page ->
            val response = organizationDataSource.getGisuPage(page, 100).mapCatching { result ->
                val responsePage = checkNotNull(result.page)
                val hasNext = checkNotNull(result.hasNext)
                check(responsePage == page && (!hasNext || result.content.isNotEmpty()))
                result.content.map { it.toModel() } to hasNext
            }
            currentCoroutineContext().ensureActive()
            when (response) {
                is ApiState.Fail -> return response
                is ApiState.Success -> {
                    gisus += response.data.first
                    if (!response.data.second) return ApiState.Success(GisuList(gisus.distinctBy { it.gisuId }))
                }
            }
        }
        return ApiState.Fail(FailState(code = "GISU_LIST_INCOMPLETE", message = "기수 목록을 모두 불러오지 못했어요. 다시 시도해주세요."))
    }

    override suspend fun getActiveGisu(): ApiState<GisuItem> =
        organizationDataSource.getActiveGisu().map { response ->
            response.toModel()
        }

    override suspend fun getChapterWithSchool(gisuId: Int): ApiState<ChapterWithSchool> =
        organizationDataSource.getChapterWithSchool(gisuId).mapCatching { it.toModel() }

    override suspend fun getGisuInfo(gisuId: Long): ApiState<GisuInfo> =
        organizationDataSource.getGisuInfo(gisuId).map { it.toModel() }


    // PATCH
    override suspend fun editGroup(groupId: Long, request: EditStudyGroupRequest): ApiState<Unit> =
        organizationDataSource.editGroup(groupId, request)

    override suspend fun editSchool(schoolId: Int, request: EditSchoolRequest): ApiState<Unit> =
        organizationDataSource.editSchool(schoolId, request)

    override suspend fun unassignSchool(
        schoolId: Int,
        request: UnAssignSchoolRequest
    ): ApiState<Unit> =
        organizationDataSource.unassignSchool(schoolId, request)

    override suspend fun assignSchool(schoolId: Int, request: AssignSchoolRequest): ApiState<Unit> =
        organizationDataSource.assignSchool(schoolId, request)

    override suspend fun addStudyGroupMember(
        studyGroupId: Long,
        memberId: Long,
    ): ApiState<Unit> =
        organizationDataSource.addStudyGroupMember(
            studyGroupId = studyGroupId,
            memberId = memberId,
        )

    override suspend fun addStudyGroupMentor(
        studyGroupId: Long,
        mentorId: Long,
    ): ApiState<Unit> =
        organizationDataSource.addStudyGroupMentor(
            studyGroupId = studyGroupId,
            mentorId = mentorId,
        )


    // POST
    override suspend fun createStudyGroup(request: CreateStudyGroupRequest): ApiState<Unit> =
        organizationDataSource.createStudyGroup(request)

    override suspend fun createStudyGroupSchedule(
        request: CreateStudyGroupScheduleRequest,
    ): ApiState<Unit> =
        organizationDataSource.createStudyGroupSchedule(
            request = request,
        )

    override suspend fun createSchool(request: SchoolRegistrationRequest): ApiState<Unit> =
        organizationDataSource.createSchool(request)

    override suspend fun createGisu(request: CreateGisuRequest): ApiState<Unit> =
        organizationDataSource.createGisu(request)

    override suspend fun createChapter(request: CreateChapterRequest): ApiState<Unit> =
        organizationDataSource.createChapter(request)

    override suspend fun changeActiveGisu(gisuId: Int): ApiState<Unit> =
        organizationDataSource.changeActiveGisu(gisuId)

    override suspend fun updateStudyGroup(
        studyGroupId: Long,
        request: UpdateStudyGroupRequest,
    ): ApiState<Unit> =
        organizationDataSource.updateStudyGroup(
            studyGroupId = studyGroupId,
            request = request,
        )


    // PUT
    override suspend fun changeGroupMember(
        groupId: Long,
        request: ChallengerListRequest
    ): ApiState<Unit> =
        organizationDataSource.changeGroupMember(groupId, request)
}
