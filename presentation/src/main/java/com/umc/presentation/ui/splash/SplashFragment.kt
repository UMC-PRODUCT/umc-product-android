package com.umc.presentation.ui.splash

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding, SplashUiState, SplashEvent, SplashViewModel>(
    FragmentSplashBinding::inflate,
) {
    override val viewModel: SplashViewModel by viewModels()

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
                    handleSplashEvent(it)
                }
            }

            launch {
                viewModel.uiState.collect {
                    // TODO 상태 관리
                }
            }
        }
    }

    private fun handleSplashEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.MoveToLoginEvent -> moveToLogin()
            SplashEvent.MoveToMainEvent -> moveToHome()
        }
    }

    private fun moveToHome() {
        val action = SplashFragmentDirections.actionSplashToHome()
        findNavController().navigate(action)
    }

    private fun moveToLogin() {
        val action = SplashFragmentDirections.actionSplashToLogin()
        findNavController().navigate(action)
    }
}
