package com.umc.data.repository

import com.umc.data.dataSource.OrganizationDataSource
import com.umc.data.response.organization.*
import com.umc.data.response.organization.SchoolNameResponse.Companion.toModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.request.organization.*
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.repository.OrganizationRepository
import javax.inject.Inject

class OrganizationRepositoryImpl @Inject constructor(
    private val organizationDataSource: OrganizationDataSource
) : OrganizationRepository {

    // DELETE
    override suspend fun deleteSchool(request: SchoolIdRequest): ApiState<Unit> =
        organizationDataSource.deleteSchool(request)

    override suspend fun deleteStudyGroup(groupId: Int): ApiState<Unit> =
        organizationDataSource.deleteStudyGroup(groupId)

    override suspend fun deleteGisu(gisuId: Int): ApiState<Unit> =
        organizationDataSource.deleteGisu(gisuId)

    // GET
    override suspend fun getMyStudyGroup(cursor: Int, size: Int): ApiState<Unit> =
        organizationDataSource.getMyStudyGroup(cursor, size).map { Unit } // 임시

    override suspend fun getSchoolByKeyword(
        keyword: String,
        chapterId: Int,
        page: Int,
        size: Int,
        sort: String
    ): ApiState<Unit> =
        organizationDataSource.getSchoolByKeyword(keyword, chapterId, page, size, sort).map { Unit } //임시

    override suspend fun getAllChapter(): ApiState<Unit> =
        organizationDataSource.getAllChapter().map { Unit } //임시

    override suspend fun getStudyGroupDetail(groupId: Int): ApiState<Unit> =
        organizationDataSource.getStudyGroupDetail(groupId).map { Unit } //임시

    override suspend fun getSchoolDetail(schoolId: Int): ApiState<Unit> =
        organizationDataSource.getSchoolDetail(schoolId).map { Unit } //임시

    override suspend fun getMyStudyGroupList(): ApiState<Unit> =
        organizationDataSource.getMyStudyGroupList().map { Unit } //임시

    override suspend fun getUnassignedSchool(gisuId: Int): ApiState<Unit> =
        organizationDataSource.getUnassignedSchool(gisuId).map { Unit } //임시

    override suspend fun getSchoolLink(schoolId: Int): ApiState<Unit> =
        organizationDataSource.getSchoolLink(schoolId).map { Unit }

    override suspend fun getAllSchool(): ApiState<List<SchoolInfo>> =
        organizationDataSource.getAllSchool().map {
            it.schools.map { item -> item.toModel() }
        }

    override suspend fun getAllGisu(): ApiState<Unit> =
        organizationDataSource.getAllGisu().map { Unit } //임시

    override suspend fun getActiveGisu(): ApiState<Unit> =
        organizationDataSource.getActiveGisu().map { Unit } //임시

    override suspend fun getChapterWithSchool(gisuId: Int): ApiState<Unit> =
        organizationDataSource.getChapterWithSchool(gisuId).map { Unit } //임시

    // PATCH
    override suspend fun editGroup(groupId: Int, request: EditStudyGroupRequest): ApiState<Unit> =
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

    // POST
    override suspend fun createStudyGroup(request: CreateStudyGroupRequest): ApiState<Unit> =
        organizationDataSource.createStudyGroup(request)

    override suspend fun createSchool(request: SchoolRegistrationRequest): ApiState<Unit> =
        organizationDataSource.createSchool(request)

    override suspend fun createGisu(request: CreateGisuRequest): ApiState<Unit> =
        organizationDataSource.createGisu(request)

    override suspend fun createChapter(request: CreateChapterRequest): ApiState<Unit> =
        organizationDataSource.createChapter(request)

    override suspend fun changeActiveGisu(gisuId: Int): ApiState<Unit> =
        organizationDataSource.changeActiveGisu(gisuId)

    // PUT
    override suspend fun changeGroupMember(
        groupId: Int,
        request: ChallengerListRequest
    ): ApiState<Unit> =
        organizationDataSource.changeGroupMember(groupId, request)
}