package com.umc.data.dataSource.remote

import com.umc.data.api.OrganizationApi
import com.umc.data.dataSource.OrganizationDataSource
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.organization.*
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.*
import javax.inject.Inject

class OrganizationRemoteDataSourceImpl @Inject constructor(
    private val organizationApi: OrganizationApi
) : OrganizationDataSource {

    override suspend fun deleteSchool(request: SchoolIdRequest): ApiState<Unit> {
        return apiCall { organizationApi.deleteSchool(request) }
    }

    override suspend fun deleteStudyGroup(groupId: Int): ApiState<Unit> {
        return apiCall { organizationApi.deleteStudyGroup(groupId) }
    }

    override suspend fun deleteGisu(gisuId: Int): ApiState<Unit> {
        return apiCall { organizationApi.deleteGisu(gisuId) }
    }

    override suspend fun getMyStudyGroup(cursor: Int, size: Int): ApiState<StudyGroupListResponse> {
        return apiCall { organizationApi.getMyStudyGroup(cursor, size) }
    }

    override suspend fun getSchoolByKeyword(
        keyword: String,
        chapterId: Int,
        page: Int,
        size: Int,
        sort: String
    ): ApiState<SchoolPageResponse> {
        return apiCall {
            organizationApi.getSchoolByKeyword(keyword, chapterId, page, size, sort)
        }
    }

    override suspend fun getAllChapter(): ApiState<ChapterResponse> {
        return apiCall { organizationApi.getAllChapter() }
    }

    override suspend fun getStudyGroupDetail(groupId: Int): ApiState<StudyGroupDetailResponse> {
        return apiCall { organizationApi.getStudyGroupDetail(groupId) }
    }

    override suspend fun getSchoolDetail(schoolId: Int): ApiState<SchoolDetailResponse> {
        return apiCall { organizationApi.getSchoolDetail(schoolId) }
    }

    override suspend fun getMyStudyGroupList(): ApiState<MyStudyGroupListResponse> {
        return apiCall { organizationApi.getMyStudyGroupList() }
    }

    override suspend fun getUnassignedSchool(gisuId: Int): ApiState<SchoolListResponse> {
        return apiCall { organizationApi.getUnassignedSchool(gisuId) }
    }

    override suspend fun getSchoolLink(schoolId: Int): ApiState<SchoolLinksResponse> {
        return apiCall { organizationApi.getSchoolLink(schoolId) }
    }

    override suspend fun getAllSchool(): ApiState<SchoolListResponse> {
        return apiCall { organizationApi.getAllSchool() }
    }

    override suspend fun getAllGisu(): ApiState<GisuListResponse> {
        return apiCall { organizationApi.getAllGisu() }
    }

    override suspend fun getActiveGisu(): ApiState<GisuItemResponse> {
        return apiCall { organizationApi.getActiveGisu() }
    }

    override suspend fun getChapterWithSchool(gisuId: Int): ApiState<ChapterBySchoolListResponse> {
        return apiCall { organizationApi.getActiveGisu(gisuId) }
    }

    override suspend fun editGroup(groupId: Int, request: EditStudyGroupRequest): ApiState<Unit> {
        return apiCall { organizationApi.editGroup(groupId, request) }
    }

    override suspend fun editSchool(schoolId: Int, request: EditSchoolRequest): ApiState<Unit> {
        return apiCall { organizationApi.editSchool(schoolId, request) }
    }

    override suspend fun unassignSchool(schoolId: Int, request: UnAssignSchoolRequest): ApiState<Unit> {
        return apiCall { organizationApi.unassignSchool(schoolId, request) }
    }

    override suspend fun assignSchool(schoolId: Int, request: AssignSchoolRequest): ApiState<Unit> {
        return apiCall { organizationApi.assignSchool(schoolId, request) }
    }

    override suspend fun createStudyGroup(request: CreateStudyGroupRequest): ApiState<Unit> {
        return apiCall { organizationApi.createStudyGroup(request) }
    }

    override suspend fun createSchool(request: SchoolRegistrationRequest): ApiState<Unit> {
        return apiCall { organizationApi.createSchool(request) }
    }

    override suspend fun createGisu(request: CreateGisuRequest): ApiState<Unit> {
        return apiCall { organizationApi.createGisu(request) }
    }

    override suspend fun createChapter(request: CreateChapterRequest): ApiState<Unit> {
        return apiCall { organizationApi.createChapter(request) }
    }

    override suspend fun changeActiveGisu(gisuId: Int): ApiState<Unit> {
        return apiCall { organizationApi.changeActiveGisu(gisuId) }
    }

    override suspend fun changeGroupMember(groupId: Int, request: ChallengerListRequest): ApiState<Unit> {
        return apiCall { organizationApi.changeGroupMember(groupId, request) }
    }
}