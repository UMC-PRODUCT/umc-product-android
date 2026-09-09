package com.umc.presentation.study.admin.group.create

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.usecase.organization.CreateStudyGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * 스터디 그룹 생성 화면의 ViewModel
 *
 * 그룹 생성 화면에서 발생하는 사용자 액션을 처리하고,
 * 화면 상태를 관리하며,
 * 최종적으로 스터디 그룹 생성 API를 호출합니다.
 *
 * 주요 역할
 * - 그룹 이름 입력값 관리
 * - 파트 선택 상태 관리
 * - 담당 파트장 선택 상태 관리
 * - 스터디원 선택 상태 관리
 * - 각 바텀시트 열기 / 닫기
 * - 그룹 생성 요청 객체 생성
 * - 그룹 생성 API 호출
 * - 생성 성공 / 실패 이벤트 전달
 */
@HiltViewModel
class AdminStudyGroupCreateViewModel @Inject constructor(
    private val createStudyGroupUseCase: CreateStudyGroupUseCase,
) : BaseViewModel<
        AdminStudyGroupCreateState,
        AdminStudyGroupCreateEvent,
        >(
    AdminStudyGroupCreateState()
) {

    /**
     * 그룹 생성 화면에서 발생한 사용자 액션 처리
     *
     * UI에서는 직접 상태를 변경하지 않고,
     * AdminStudyGroupCreateAction을 통해
     * ViewModel에 사용자 행동을 전달합니다.
     */
    fun onAction(
        action: AdminStudyGroupCreateAction,
    ) {
        when (action) {

            /**
             * 그룹 이름 변경
             *
             * 사용자가 TextField에 입력한 값을
             * 현재 화면 상태에 저장합니다.
             */
            is AdminStudyGroupCreateAction.OnGroupNameChanged -> {
                updateState {
                    copy(
                        groupName = action.value
                    )
                }
            }

            /**
             * 파트 선택 영역 클릭
             *
             * 파트 선택 바텀시트를 표시합니다.
             */
            AdminStudyGroupCreateAction.OnPartClick -> {
                updateState {
                    copy(
                        showPartBottomSheet = true
                    )
                }
            }

            /**
             * 담당 파트장 선택 영역 클릭
             *
             * 담당 파트장 검색/선택 바텀시트를 표시합니다.
             */
            AdminStudyGroupCreateAction.OnPartLeaderClick -> {
                updateState {
                    copy(
                        showPartLeaderBottomSheet = true
                    )
                }
            }

            /**
             * 스터디원 선택 영역 클릭
             *
             * 스터디원 검색/선택 바텀시트를 표시합니다.
             */
            AdminStudyGroupCreateAction.OnMemberClick -> {
                updateState {
                    copy(
                        showMemberBottomSheet = true
                    )
                }
            }

            /**
             * 등록 버튼 클릭
             *
             * 현재 입력 및 선택 상태를 확인한 뒤
             * 스터디 그룹 생성 API를 호출합니다.
             */
            AdminStudyGroupCreateAction.OnRegisterClick -> {
                createStudyGroup()
            }

            /**
             * 뒤로가기 클릭
             *
             * Route에 NavigateBack 이벤트를 전달합니다.
             */
            AdminStudyGroupCreateAction.OnBackClick -> {
                emitEvent(
                    AdminStudyGroupCreateEvent.NavigateBack
                )
            }
        }
    }

    /**
     * 그룹을 생성할 기수 ID 설정
     *
     * Route 등 외부에서 전달받은 기수 ID를
     * 현재 화면 상태에 저장합니다.
     *
     * @param gisuId 그룹을 생성할 기수 ID
     */
    fun setGisuId(
        gisuId: Long,
    ) {
        updateState {
            copy(
                gisuId = gisuId
            )
        }
    }

    /**
     * 스터디 그룹 생성 요청
     *
     * 현재 화면에서 입력 및 선택된 정보를 이용하여
     * CreateStudyGroupRequest를 생성한 뒤
     * CreateStudyGroupUseCase를 호출합니다.
     *
     * API 요청 정보
     * - name: 그룹 이름
     * - gisuId: 기수 ID
     * - part: 선택한 파트
     * - mentorIds: 담당 파트장 ID 목록
     * - memberIds: 스터디원 ID 목록
     */
    private fun createStudyGroup() {
        val state = uiState.value

        /**
         * 필수 입력값이 모두 충족되지 않은 경우
         * API 요청을 실행하지 않습니다.
         */
        if (!state.isRegisterEnabled) {
            android.util.Log.d(
                "StudyGroupCreate",
                "등록 불가: state=$state"
            )
            return
        }

        /**
         * 기수 ID 확인
         *
         * 그룹 생성 API에서 필수로 사용하는 값이므로
         * null인 경우 요청하지 않습니다.
         */
        val gisuId = state.gisuId ?: run {
            android.util.Log.d(
                "StudyGroupCreate",
                "등록 실패: gisuId가 null입니다."
            )
            return
        }

        /**
         * 선택한 파트 확인
         *
         * isRegisterEnabled에서도 확인하지만
         * nullable 값을 안전하게 사용하기 위해
         * 다시 확인합니다.
         */
        val selectedPart = state.selectedPart ?: run {
            android.util.Log.d(
                "StudyGroupCreate",
                "등록 실패: selectedPart가 null입니다."
            )
            return
        }

        /**
         * 그룹 생성 API Request 생성
         *
         * UI에서 가지고 있는 MemberUiModel 전체를 보내는 것이 아니라
         * 서버에서 필요한 각 사용자의 id만 추출하여 전달합니다.
         */
        val request = CreateStudyGroupRequest(
            name = state.groupName.trim(),
            gisuId = gisuId,
            part = selectedPart.serverValue,

            // 담당 파트장 ID 목록
            mentorIds = state.selectedPartLeaders.map {
                it.id
            },

            // 스터디원 ID 목록
            memberIds = state.selectedMembers.map {
                it.id
            },
        )

        android.util.Log.d(
            "StudyGroupCreate",
            "등록 요청: $request"
        )

        /**
         * 스터디 그룹 생성 API 호출
         */
        viewModelScope.launch {

            // API 요청 시작
            updateState {
                copy(
                    isLoading = true
                )
            }

            when (
                val result =
                    createStudyGroupUseCase(request)
            ) {

                /**
                 * 그룹 생성 성공
                 */
                is ApiState.Success -> {
                    android.util.Log.d(
                        "StudyGroupCreate",
                        "등록 성공"
                    )

                    // 로딩 상태 종료
                    updateState {
                        copy(
                            isLoading = false
                        )
                    }

                    /**
                     * Route에 생성 성공 이벤트 전달
                     *
                     * Route에서는 이 이벤트를 받아
                     * 이전 화면으로 이동합니다.
                     */
                    emitEvent(
                        AdminStudyGroupCreateEvent.RegisterSuccess
                    )
                }

                /**
                 * 그룹 생성 실패
                 */
                is ApiState.Fail -> {
                    android.util.Log.d(
                        "StudyGroupCreate",
                        "등록 실패: ${result.failState.message}"
                    )

                    // 로딩 상태 종료
                    updateState {
                        copy(
                            isLoading = false
                        )
                    }

                    /**
                     * 서버에서 전달받은 실패 메시지를
                     * UI 이벤트로 전달합니다.
                     */
                    emitEvent(
                        AdminStudyGroupCreateEvent.RegisterFailure(
                            message = result.failState.message
                        )
                    )
                }
            }
        }
    }

    /**
     * 현재 열려 있는 선택 바텀시트를 닫습니다.
     *
     * 파트 / 파트장 / 스터디원 바텀시트를
     * 모두 false로 변경하여 어떤 바텀시트가 열려 있더라도
     * 공통으로 닫을 수 있도록 처리합니다.
     */
    fun dismissBottomSheet() {
        updateState {
            copy(
                showPartBottomSheet = false,
                showPartLeaderBottomSheet = false,
                showMemberBottomSheet = false,
            )
        }
    }

    /**
     * 파트 선택 완료
     *
     * 선택한 파트를 저장하고
     * 파트 선택 바텀시트를 닫습니다.
     *
     * 파트가 변경되면 기존에 선택했던
     * 담당 파트장과 스터디원도 초기화합니다.
     *
     * 이유:
     * 파트장 및 스터디원 검색 결과가
     * 선택된 파트에 영향을 받을 수 있기 때문입니다.
     *
     * @param part 새로 선택한 파트
     */
    fun selectPart(
        part: UserPart,
    ) {
        updateState {
            copy(
                selectedPart = part,

                // 기존 담당 파트장 선택 초기화
                selectedPartLeaders = persistentListOf(),

                // 기존 스터디원 선택 초기화
                selectedMembers = persistentListOf(),

                // 파트 선택 바텀시트 닫기
                showPartBottomSheet = false,
            )
        }
    }

    /**
     * 담당 파트장 선택 완료
     *
     * 바텀시트에서 선택한 담당 파트장 목록을
     * 현재 화면 상태에 저장하고 바텀시트를 닫습니다.
     *
     * @param leaders 선택된 담당 파트장 목록
     */
    fun selectPartLeaders(
        leaders: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        updateState {
            copy(
                selectedPartLeaders = leaders.toImmutableList(),
                showPartLeaderBottomSheet = false,
            )
        }
    }

    /**
     * 스터디원 선택 완료
     *
     * 바텀시트에서 선택한 스터디원 목록을
     * 현재 화면 상태에 저장하고 바텀시트를 닫습니다.
     *
     * @param members 선택된 스터디원 목록
     */
    fun selectMembers(
        members: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        updateState {
            copy(
                selectedMembers = members.toImmutableList(),
                showMemberBottomSheet = false,
            )
        }
    }
}