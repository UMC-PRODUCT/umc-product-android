package com.umc.presentation.ui.mypage.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentProfileBinding
import com.umc.presentation.ui.mypage.MypageViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileFragmentUiState, ProfileFragmentEvent, ProfileViewModel>(
    FragmentProfileBinding::inflate,
) {
    override val viewModel : ProfileViewModel by viewModels()

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

                }
            }
        }



    }

}