package com.umc.presentation.ui.act.dialog

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseBottomSheetFragment
import com.umc.presentation.databinding.LayoutBottomSheetRewardEtcBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BottomSheetRewardEtcDialog()
    : BaseBottomSheetFragment<LayoutBottomSheetRewardEtcBinding, BottomSheetRewardEtcUiState, BottomSheetRewardEtcEvent, BottomSheetRewardEtcViewModel>(
        LayoutBottomSheetRewardEtcBinding::inflate
){


    override val viewModel: BottomSheetRewardEtcViewModel by viewModels()

    override fun initView() {
        binding.apply{
            vm = viewModel
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

    fun handleEvent(event: BottomSheetRewardEtcEvent) {
        when (event) {
            else -> {}
        }

    }


}