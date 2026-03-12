package com.umc.presentation.ui.signUp.fail

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.base.UiState
import com.umc.presentation.databinding.FragmentSignUpFailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFailFragment : BaseFragment<FragmentSignUpFailBinding, UiState, SignUpFailEvent, SignUpFailViewModel>(
    FragmentSignUpFailBinding::inflate,
) {
    override val viewModel: SignUpFailViewModel by viewModels()

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
        }
    }
}
