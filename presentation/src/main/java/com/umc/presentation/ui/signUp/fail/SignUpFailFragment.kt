package com.umc.presentation.ui.signUp.fail

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UMypageDialog
import com.umc.presentation.component.UMypageDialogModel
import com.umc.presentation.databinding.FragmentSignUpFailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class SignUpFailFragment : BaseFragment<FragmentSignUpFailBinding, SignUpFailUiState, SignUpFailEvent, SignUpFailViewModel>(
    FragmentSignUpFailBinding::inflate,
) {
    override val viewModel: SignUpFailViewModel by viewModels()

    private val deleteUserDialogModel = UMypageDialogModel(
        title = "계정 삭제",
        content = "계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다. 정말 삭제하시겠습니까?",
        isTwoButton = true,
        positiveText = "삭제",
        negativeText = "취소"
    )

    override fun initView() {
        binding.apply {
            vm = viewModel
            // Add underline to the inquiry text
            val spannableString = SpannableString(textInquiry.text)
            spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textInquiry.text = spannableString

            // Add underline to the logout text
            val logoutSpannable = SpannableString(textLogout.text)
            logoutSpannable.setSpan(UnderlineSpan(), 0, logoutSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textLogout.text = logoutSpannable

            // Add underline to the delete user text
            val deleteUserSpannable = SpannableString(textDeleteUser.text)
            deleteUserSpannable.setSpan(UnderlineSpan(), 0, deleteUserSpannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textDeleteUser.text = deleteUserSpannable
        }

        lifecycleScope.launch {
            val googleToken = requestGoogleAccessToken()
            viewModel.setGoogleToken(googleToken)
        }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiEvent.collect {
                    handleEvent(it)
                }
            }

            launch {
                viewModel.uiState.collect {
                    // TODO 상태 관리
                }
            }
        }
    }

    override fun handleEvent(event: SignUpFailEvent) {
        when (event) {
            SignUpFailEvent.MoveToBack -> findNavController().popBackStack()
            SignUpFailEvent.MoveToHomePage -> {
                val intent = Intent(Intent.ACTION_VIEW, "https://umc.it.kr".toUri())
                startActivity(intent)
            }
            SignUpFailEvent.MoveToCode -> {
                val action = SignUpFailFragmentDirections.actionSignUpFailToCode()
                findNavController().navigate(action)
            }
            SignUpFailEvent.MoveToKakaoInquiry -> {
                val intent = Intent(Intent.ACTION_VIEW, "https://pf.kakao.com/_MDxhqX/chat".toUri())
                startActivity(intent)
            }
            SignUpFailEvent.MoveToLogin -> {
                findNavController().navigate(com.umc.presentation.R.id.action_global_to_login)
            }
            SignUpFailEvent.ShowDeleteUserDialog -> {
                val dialog = UMypageDialog(deleteUserDialogModel) {
                    viewModel.deleteUser()
                }
                dialog.show(parentFragmentManager, "SignUpFailDeleteUserDialog")
            }
        }
    }

    private suspend fun requestGoogleAccessToken(): String {
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(Scope(Scopes.PROFILE), Scope(Scopes.EMAIL))
            )
            .build()

        val authorizationResult = Identity.getAuthorizationClient(requireActivity())
            .authorize(authorizationRequest)
            .await()

        return authorizationResult.accessToken ?: ""
    }
}
