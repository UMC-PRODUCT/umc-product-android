package com.umc.presentation.ui.mypage.mycomment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMycommentBinding
import com.umc.presentation.ui.mypage.MypageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class MycommentFragment : BaseFragment<FragmentMycommentBinding, MycommentFragmentUiState, MycommentFragmentEvent, MycommentViewModel>(
FragmentMycommentBinding::inflate,
) {

    override val viewModel : MycommentViewModel by viewModels()

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