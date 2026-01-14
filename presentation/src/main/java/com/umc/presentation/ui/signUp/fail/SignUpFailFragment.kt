package com.umc.presentation.ui.signUp.fail

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
                //TODO 공식 홈페이지 url 이동
            }
        }
    }
}
