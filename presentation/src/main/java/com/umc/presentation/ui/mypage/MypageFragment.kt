package com.umc.presentation.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMypageBinding
import com.umc.presentation.ui.home.HomeFragmentDirections
import kotlinx.coroutines.launch


class MypageFragment : BaseFragment<FragmentMypageBinding, MypageFragmentUiState, MypageFragmentEvent, MypageViewModel>(
    FragmentMypageBinding::inflate,
) {
    override val viewModel : MypageViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }


    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->

                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: MypageFragmentEvent) {
        super.handleEvent(event)
        when(event) {
            is MypageFragmentEvent.goGithub -> {}
            is MypageFragmentEvent.goBlog -> {}
            is MypageFragmentEvent.goLinkedin -> {}
            is MypageFragmentEvent.goEditProfile -> {
                val action = MypageFragmentDirections.actionMypageToProfile()
                findNavController().navigate(action)
            }
            is MypageFragmentEvent.goSuggetion -> {}
            is MypageFragmentEvent.goMypost -> {}
            is MypageFragmentEvent.goMyComment -> {}

            else -> {}
        }
    }



}