package com.umc.presentation.ui.login

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.umc.domain.model.enums.LoginType
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.base.UiState
import com.umc.presentation.databinding.FragmentLoginBinding
import com.umc.presentation.util.ULog
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding, UiState, LoginEvent, LoginViewModel>(
    FragmentLoginBinding::inflate,
) {
    override val viewModel: LoginViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            ULog.d("실패~! : $error")
        } else if (token != null) {
            viewModel.login(token = token.accessToken, loginType = LoginType.KAKAO)
        }
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
            credentialManager = CredentialManager.create(requireActivity())
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
        when (event) {
            LoginEvent.KakaoLoginEvent -> signInKakao()
            is LoginEvent.MoveToSignUpEvent -> moveToSignUp(event.oAuthToken)
            LoginEvent.MoveToMainEvent -> moveToHome()
            LoginEvent.GoogleLoginEvent -> signInGoogle()
            is LoginEvent.ShowErrorToast -> {
                UToast.createToast(
                    requireContext(),
                    event.message,
                    state = UToast.State.ERROR
                ).show()
            }
        }
    }

    private fun moveToHome() {
        val action = LoginFragmentDirections.actionLoginToHome()
        findNavController().navigate(action)
    }

    private fun moveToSignUp(token: String) {
        val action = LoginFragmentDirections.actionLoginToSignUp(token = token)
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
                    viewModel.login(token.accessToken, LoginType.KAKAO)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireActivity(), callback = callback)
        }
    }

    private fun signInGoogle() {
        lifecycleScope.launch {
            try {
                val googleSignInOption = GetSignInWithGoogleOption.Builder(
                    com.umc.presentation.BuildConfig.GOOGLE_LOGIN_KEY
                ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleSignInOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = requireActivity()
                )

                handleSignIn(result)

            } catch (e: GetCredentialException) {
                ULog.d("Google 로그인 실패: ${e.message}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        // 반환된 자격 증명이 Google ID 토큰인지 확인
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            try {
                // Access Token도 함께 요청
                lifecycleScope.launch {
                    try {
                        val accessToken = requestAccessToken()
                        ULog.d("Google Access Token: $accessToken")

                        // Access Token과 ID Token을 함께 전달
                        viewModel.login(
                            token = accessToken,
                            loginType = LoginType.GOOGLE
                        )
                    } catch (e: Exception) {
                        ULog.d("Access Token 획득 실패: ${e.message}")
                    }
                }

            } catch (e: GoogleIdTokenParsingException) {
                ULog.d("유효하지 않은 Google ID 토큰 $e")
            }
        } else {
            ULog.d("예상치 못한 자격 증명 유형")
        }
    }

    private suspend fun requestAccessToken(): String {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL))
            )
            .build()

        val authorizationResult = Identity.getAuthorizationClient(requireActivity())
            .authorize(authorizationRequest)
            .await()

        return authorizationResult.accessToken
            ?: throw IllegalStateException("Access Token을 받을 수 없습니다")
    }
}
