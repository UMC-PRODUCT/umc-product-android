package com.umc.presentation.ui.signUp.permission

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPermissionBinding
import com.umc.presentation.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class PermissionFragment :
    BaseFragment<FragmentPermissionBinding, PermissionUiState, PermissionEvent, PermissionViewModel>(
        FragmentPermissionBinding::inflate,
    ) {
    override val viewModel: PermissionViewModel by viewModels()

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

    override fun handleEvent(event: PermissionEvent) {
        when (event) {
            PermissionEvent.MoveToBack -> findNavController().popBackStack()
            PermissionEvent.MoveToMainEvent -> {}
        }
    }
}
