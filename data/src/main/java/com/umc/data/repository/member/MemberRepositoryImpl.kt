package com.umc.data.repository.member

import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.request.member.LinkItemRequest
import com.umc.data.request.member.UpdateMyLinkRequest
import com.umc.data.request.member.UpdateMyProfileRequest
import com.umc.data.response.JwtLoginResponse.Companion.toModel
import com.umc.data.response.member.MemberResponse.Companion.toDomain
import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.model.request.member.UpdateLinkRequest
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.usecase.appDataStore.ClearTokensUseCase
import com.umc.domain.usecase.appDataStore.UpdateUserInfoUseCase
import javax.inject.Inject

/**여기서 API의 response -> model의 data class로 변경**/

class MemberRepositoryImpl @Inject constructor(
    private val memberRemoteDataSource: MemberRemoteDataSource,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val clearTokensUseCase: ClearTokensUseCase,
) : MemberRepository {

    //내 정보 가져오기 + dataStore에 저장하기
    override suspend fun getMyProfile(): ApiState<UserInfo> {
        val response = memberRemoteDataSource.getMyProfile().map { it.toDomain() }

        //여기서 체크 후 dataStore에 저장하는 로직 정의
        if (response is ApiState.Success) {
            updateUserInfoUseCase(response.data)
        }

        return response
    }

    override suspend fun getMemberProfile(id: Long): ApiState<UserInfo> {
        return memberRemoteDataSource.getMemberProfile(id).map { it.toDomain() }
    }

    override suspend fun register(request: RegisterRequest): ApiState<JwtToken> {
        return memberRemoteDataSource.register(request).map { it.toModel() }
    }

    override suspend fun updateMyProfile(profileImageId: String): ApiState<UserInfo> {
        val request = UpdateMyProfileRequest(profileImageId)
        return memberRemoteDataSource.updateMyProfile(request).map { it.toDomain() }

    }

    override suspend fun updateMyLink(request: UpdateLinkRequest): ApiState<UserInfo> {
        val lists = request.links.map { LinkItemRequest(it.type, it.link) }
        val request = UpdateMyLinkRequest(lists)
        return memberRemoteDataSource.updateMyLink(request).map { it.toDomain() }
    }

    override suspend fun deleteUser(): ApiState<Unit> {
        val response = memberRemoteDataSource.deleteUser()
        if (response is ApiState.Success) {
            clearTokensUseCase()
        }
        return response
    }

}