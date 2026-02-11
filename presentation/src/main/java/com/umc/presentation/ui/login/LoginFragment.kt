package com.umc.presentation.ui.login

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding, UiState, LoginEvent, LoginViewModel>(
    FragmentLoginBinding::inflate,
) {
    override val viewModel: LoginViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            ULog.d("실패~!")
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
                val rawNonce = UUID.randomUUID().toString()
                val hashedNonce = hashNonce(rawNonce)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(com.umc.presentation.BuildConfig.GOOGLE_LOGIN_KEY)
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(true)
                    .build()

                // 4. 요청 객체 생성
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 5. API 호출 (비동기)
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireActivity()
                )


                // 6. 결과 처리
                handleSignIn(result)

            } catch (e: NoCredentialException) {
                ULog.d("사용 가능한 자격 증명이 없습니다.")
            } catch (e: GetCredentialException) {
                ULog.d("인증 실패: ${e.message}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        // 반환된 자격 증명이 Google ID 토큰인지 확인
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            try {
                // 토큰 파싱
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                val idToken = googleIdTokenCredential.idToken

                viewModel.login(idToken, LoginType.GOOGLE)

            } catch (e: GoogleIdTokenParsingException) {
                ULog.d("유효하지 않은 Google ID 토큰 $e")
            }
        } else {
            ULog.d("예상치 못한 자격 증명 유형")
        }
    }

    // 간단한 SHA-256 해시 함수 (Nonce용)
    private fun hashNonce(rawNonce: String): String {
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
