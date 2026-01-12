package com.umc.presentation.ui.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMypageBinding


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
    }

}