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

class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding, PlanDetailFragmentUiState, PlanDetailFragmentEvent, PlanDetailViewModel>(
    FragmentPlanDetailBinding::inflate,
) {
    override val viewModel: PlanDetailViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }

    }

    override fun initStates() {
        super.initStates()
    }


    private fun handleMoveEvent(event: PlanDetailFragmentEvent){
        when (event){
            is PlanDetailFragmentEvent.TouchConfirmAttention -> clickConfirmAttention()

            else -> {}
        }
    }


    //해당 탭을 닫고 이동하는 로직
    /**TODO 로직 작성 필요**/
    private fun clickConfirmAttention(){

    }


}