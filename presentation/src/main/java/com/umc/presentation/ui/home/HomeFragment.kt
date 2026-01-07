package com.umc.presentation.ui.home

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeFragmentUiState, HomeFragmentEvent, HomeFragmentViewModel>(
    FragmentHomeBinding::inflate,
) {
    override val viewModel: HomeFragmentViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
    }

    override fun initStates() {
        super.initStates()
    }
}
