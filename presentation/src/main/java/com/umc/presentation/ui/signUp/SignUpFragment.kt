package com.umc.presentation.ui.signUp

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment :
    BaseFragment<FragmentSignUpBinding, SignUpState, SignUpEvent, SignUpViewModel>(
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
        }
    }
}
