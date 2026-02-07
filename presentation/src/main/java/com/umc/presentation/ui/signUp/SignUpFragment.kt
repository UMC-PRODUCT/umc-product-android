package com.umc.presentation.ui.signUp

import android.os.Build
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.umc.domain.model.signUp.School
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSignUpBinding
import com.umc.presentation.ui.signUp.bottomSheet.SchoolSelectBottomSheet
import com.umc.presentation.util.ULog
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
                }
            }
        }
    }

    override fun handleEvent(event: SignUpEvent) {
        when (event) {
            SignUpEvent.MoveToBack -> findNavController().popBackStack()
            SignUpEvent.MoveToLoginEvent -> {}
            SignUpEvent.MoveToMainEvent -> {}
            SignUpEvent.ShowSchoolBottomSheet -> showSchoolSelectBottomSheet()
        }
    }

    private fun showSchoolSelectBottomSheet() {
        val schoolList = arrayListOf(
            School(1, "서울고등학교"),
            School(2, "경기고등학교"),
            School(3, "핀업고등학교")
        )

        val bottomSheet = SchoolSelectBottomSheet.newInstance(schoolList)

        setFragmentResultListener(SchoolSelectBottomSheet.SCHOOL_SELECT) { _, bundle ->
            val selectedSchool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(SchoolSelectBottomSheet.BUNDLE_KEY_SELECT, School::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getSerializable(SchoolSelectBottomSheet.BUNDLE_KEY_SELECT) as? School
            }
            selectedSchool?.schoolName?.let { ULog.d(it) }
        }

        bottomSheet.show(parentFragmentManager, "SchoolSelectBottomSheet")
    }
}
