package com.umc.presentation.ui.signUp.fail.code

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UMypageDialog
import com.umc.presentation.component.UMypageDialogModel
import com.umc.presentation.databinding.FragmentSignUpFailCodeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFailCodeFragment : BaseFragment<FragmentSignUpFailCodeBinding, SignUpFailCodeState, SignUpFailCodeEvent, SignUpFailCodeViewModel>(
    FragmentSignUpFailCodeBinding::inflate,
) {
    override val viewModel: SignUpFailCodeViewModel by viewModels()

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
                    // TODO 상태 관리
                }
            }
        }
    }

    override fun handleEvent(event: SignUpFailCodeEvent) {
        when (event) {
            SignUpFailCodeEvent.MoveToBack -> findNavController().popBackStack()
            SignUpFailCodeEvent.MoveToHome -> {
                val action = SignUpFailCodeFragmentDirections.actionSignUpFailCodeToHome()
                findNavController().navigate(action)
            }
            is SignUpFailCodeEvent.ShowErrorDialog -> {
                UMypageDialog(
                    model = UMypageDialogModel(
                        title = event.message,
                        content = "",
                        isTwoButton = false,
                        confirmText = "확인"
                    ),
                    onPositive = {}
                ).show(parentFragmentManager, null)
            }
        }
    }
}
