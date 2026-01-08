package com.umc.presentation.ui.signUp

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSignUpBinding
import kotlinx.coroutines.launch

class SignUpFragment : BaseFragment<FragmentSignUpBinding, SignUpState, SignUpEvent, SignUpViewModel>(
    FragmentSignUpBinding::inflate,
) {
    override val viewModel: SignUpViewModel by viewModels()

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
                    // TODO 이벤트 처리
                }
            }

            launch {
                viewModel.uiState.collect {
                }
            }
        }
    }
}
