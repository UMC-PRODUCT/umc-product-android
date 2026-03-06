package com.umc.presentation.ui.signUp

import android.os.Build
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSignUpBinding
import com.umc.presentation.ui.signUp.bottomSheet.SchoolSelectBottomSheet
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment :
    BaseFragment<FragmentSignUpBinding, SignUpState, SignUpEvent, SignUpViewModel>(
        FragmentSignUpBinding::inflate,
    ) {
    override val viewModel: SignUpViewModel by viewModels()

    private val args: SignUpFragmentArgs by navArgs()

    override fun initView() {
        binding.apply {
            vm = viewModel
            viewModel.setOAuthVerificationToken(args.token)
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
                    if (it.verifyType == EmailVerifyType.ERROR) binding.textFieldEmail.clearFocus()
                }
            }
        }
    }

    override fun handleEvent(event: SignUpEvent) {
        when (event) {
            SignUpEvent.MoveToBack -> findNavController().popBackStack()
            SignUpEvent.MoveToPermissionEvent -> navigatePermission()
            SignUpEvent.ShowSchoolBottomSheet -> showSchoolSelectBottomSheet()
            SignUpEvent.ShowVerifyToast -> {
                UToast.createToast(
                    requireContext(),
                    R.string.sign_up_email_verify_sent,
                    state = UToast.State.CHECK
                ).show()
            }
            SignUpEvent.ShowVerifyCompleteToast -> {
                UToast.createToast(
                    requireContext(),
                    R.string.sign_up_email_verify_complete,
                    state = UToast.State.CHECK
                ).show()
            }
            SignUpEvent.ShowVerifyErrorToast -> {
                UToast.createToast(
                    requireContext(),
                    R.string.sign_up_email_verify_error,
                    state = UToast.State.ERROR
                ).show()
            }
            SignUpEvent.FocusVerifyCodeField -> {
                binding.textFieldVerifyCode.requestFocus()
            }
        }
    }

    private fun showSchoolSelectBottomSheet() {

        val bottomSheet = SchoolSelectBottomSheet.newInstance(viewModel.uiState.value.schoolList)

        setFragmentResultListener(SchoolSelectBottomSheet.SCHOOL_SELECT) { _, bundle ->
            val selectedSchool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(SchoolSelectBottomSheet.BUNDLE_KEY_SELECT, SchoolInfo::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getSerializable(SchoolSelectBottomSheet.BUNDLE_KEY_SELECT) as? SchoolInfo
            }
            selectedSchool?.let { viewModel.updateSelectSchool(school = selectedSchool) }
        }

        bottomSheet.show(parentFragmentManager, "SchoolSelectBottomSheet")
    }

    private fun navigatePermission() {
        val action = SignUpFragmentDirections.actionSignUpToPermission()
        findNavController().navigate(action)
    }
}
