package com.umc.presentation.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPlanDetailBinding

class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding, PlanDetailFragmentUiState, PlanDetailFragmentEvent, PlanDetailFragmentViewModel>(
    FragmentPlanDetailBinding::inflate,
) {
    override val viewModel: PlanDetailFragmentViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }

    }

    override fun initStates() {
        super.initStates()
    }


}