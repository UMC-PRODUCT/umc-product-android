package com.umc.data.repository.member

import com.umc.domain.model.mypage.UserReportReason
import com.umc.data.request.member.ReportMemberRequest

import com.umc.data.dataSource.local.AppDataStore
import com.umc.domain.model.base.FailState
import com.umc.data.dataSource.remote.member.MemberRemoteDataSource
import com.umc.data.request.member.BusinessCardContactRequest
import com.umc.data.request.member.BusinessCardLinkRequest
import com.umc.data.request.member.DeleteUserRequest
import com.umc.data.request.member.EditBusinessCardRequest as EditBusinessCardRequestBody
import com.umc.data.request.member.LinkItemRequest
import com.umc.data.request.member.UpdateMyLinkRequest
import com.umc.data.request.member.UpdateMyProfileRequest
import com.umc.data.response.JwtLoginResponse.Companion.toModel
import com.umc.data.response.member.MemberResponse.Companion.toDomain
import com.umc.domain.model.member.MemberBlockChange
import com.umc.domain.model.member.MemberBlockChanges
import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.model.request.member.EditBusinessCardRequest
import com.umc.domain.model.request.member.RegisterEmailRequest
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.model.request.member.UpdateLinkRequest
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.usecase.appDataStore.ClearTokensUseCase
import com.umc.domain.usecase.appDataStore.UpdateUserInfoUseCase
import javax.inject.Inject
import java.io.IOException
import kotlinx.coroutines.CancellationException

/**여기서 API의 response -> model의 data class로 변경**/

class MemberRepositoryImpl @Inject constructor(
    private val appDataStore: AppDataStore,
    private val memberRemoteDataSource: MemberRemoteDataSource,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val clearTokensUseCase: ClearTokensUseCase,
) : MemberRepository {

    //내 정보 가져오기 + dataStore에 저장하기
    override suspend fun getMyProfile(): ApiState<UserInfo> {
        return try {
            val session = appDataStore.profileSession()
            val response = memberRemoteDataSource.getMyProfile().map { it.toDomain() }

            //여기서 체크 후 dataStore에 저장하는 로직 정의
            if (response is ApiState.Success) {
                try {
                    appDataStore.saveUserInfo(response.data, session)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: IllegalStateException) {
                    return ApiState.Fail(FailState(code = "ACCOUNT_CHANGED", message = "계정이 변경됐어요. 다시 확인해주세요."))
                }
            }

            response
        } catch (_: IOException) {
            ApiState.Fail(FailState(code = "LOCAL_ERROR", message = "내 정보를 저장하거나 불러오지 못했어요. 다시 시도해주세요."))
        }
    }

    override suspend fun getMemberProfile(id: Long): ApiState<UserInfo> {
        return memberRemoteDataSource.getMemberProfile(id).map { it.toDomain() }
    }

    override suspend fun registerOAuth(request: RegisterRequest): ApiState<JwtToken> {
        return memberRemoteDataSource.registerOAuth(request).map { it.toModel() }
    }

    override suspend fun registerEmail(request: RegisterEmailRequest): ApiState<JwtToken> {
        return memberRemoteDataSource.registerEmail(request).map { it.toModel() }
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

    override suspend fun deleteUser(kakaoAccessToken: String, googleAccessToken: String): ApiState<Unit> {
        return try {
            val session = appDataStore.profileSession()
            val request = DeleteUserRequest(kakaoAccessToken, googleAccessToken)
            val response = memberRemoteDataSource.deleteUser(request)
            if (response is ApiState.Success) {
                try {
                    appDataStore.purgeCurrentCardBook(session)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: IllegalStateException) {
                    return ApiState.Fail(FailState(code = "ACCOUNT_CHANGED", message = "계정이 변경됐어요."))
                }
            }
            response
        } catch (_: IOException) {
            ApiState.Fail(FailState(code = "LOCAL_ERROR", message = "기기 데이터를 정리하지 못했어요. 다시 확인해주세요."))
        }
    }

    override suspend fun reportMember(memberId: Long, reasons: Set<UserReportReason>, detail: String): ApiState<Unit> =
        memberRemoteDataSource.reportMember(memberId, ReportMemberRequest(reasons.map { it.name }, detail)).map { Unit }

    override suspend fun blockMember(memberId: Long): ApiState<Boolean> {
        val session = appDataStore.profileSession()
        val result = memberRemoteDataSource.blockMember(memberId).map { it.isBlocked }
        if (result is ApiState.Success && session.ownerId > 0 && appDataStore.profileSession() == session) {
            MemberBlockChanges.publish(MemberBlockChange(session.ownerId, memberId, result.data))
        }
        return result
    }

    override suspend fun unblockMember(memberId: Long): ApiState<Boolean> {
        val session = appDataStore.profileSession()
        val result = memberRemoteDataSource.unblockMember(memberId).map { it.isBlocked }
        if (result is ApiState.Success && session.ownerId > 0 && appDataStore.profileSession() == session) {
            MemberBlockChanges.publish(MemberBlockChange(session.ownerId, memberId, result.data))
        }
        return result
    }

    override suspend fun getBlockedMemberIds(): ApiState<List<Long>> {
        return memberRemoteDataSource.getBlockedMembers().map { response ->
            response.members.orEmpty().map { it.memberId }
        }
    }

    override suspend fun getMyBusinessCard(): ApiState<BusinessCard> {
        return memberRemoteDataSource.getMyBusinessCard().map { it.toDomain() }
    }

    override suspend fun getMyBusinessCardEdit(): ApiState<BusinessCard> {
        return memberRemoteDataSource.getMyBusinessCardEdit().map { it.toDomain() }
    }

    override suspend fun editMyBusinessCard(request: EditBusinessCardRequest): ApiState<BusinessCard> {
        // 빈 값은 null 로 보내 서버에서 지우게 한다
        val body = EditBusinessCardRequestBody(
            profileImageId = request.profileImageId,
            contact = BusinessCardContactRequest(
                email = request.contact.email.trim().ifBlank { null },
                isPublic = request.contact.isPublic
            ),
            links = request.links.map {
                BusinessCardLinkRequest(type = it.type.label, link = it.link.trim().ifBlank { null }, isPublic = it.isPublic)
            }
        )
        return memberRemoteDataSource.editMyBusinessCard(body).map { it.toDomain() }
    }
}
