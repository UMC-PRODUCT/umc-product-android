package com.umc.presentation.ui.login

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentLoginBinding
import com.umc.presentation.util.ULog
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginUiState, LoginEvent, LoginViewModel>(
    FragmentLoginBinding::inflate,
) {
    override val viewModel: LoginViewModel by viewModels()

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            ULog.d("실패~!")
        } else if (token != null) {
            viewModel.kakaoLogin()
        }
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiEvent.collect {
                    handleLoginEvent(it)
                }
            }

            launch {
                viewModel.uiState.collect {
                    // TODO 상태 관리
                }
            }
        }
    }

    private fun handleLoginEvent(event: LoginEvent) {
        when(event) {
            LoginEvent.KakaoLoginEvent -> signInKakao()
            LoginEvent.MoveToSignUpEvent -> moveToSignUp()
            LoginEvent.MoveToMainEvent -> moveToHome()
        }
    }

    private fun moveToHome() {
        val action = LoginFragmentDirections.actionLoginToHome()
        findNavController().navigate(action)
    }

    private fun moveToSignUp() {
        val action = LoginFragmentDirections.actionLoginToSignUp()
        findNavController().navigate(action)
    }

    fun signInKakao() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireActivity())) {
            UserApiClient.instance.loginWithKakaoTalk(requireActivity()) { token, error ->
                if (error != null) {
                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(requireActivity(), callback = callback)
                } else if (token != null) {
                    // 로그인 성공
                    viewModel.kakaoLogin()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireActivity(), callback = callback)
        }
    }
}
