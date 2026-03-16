package com.umc.presentation.ui.act.dialog

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umc.presentation.base.BaseBottomSheetFragment
import com.umc.presentation.databinding.LayoutBottomSheetRewardEtcBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BottomSheetRewardEtcDialog(
    private val challengerId: Long,
    private val onConfirm:() -> Unit,
)
    : BaseBottomSheetFragment<LayoutBottomSheetRewardEtcBinding, BottomSheetRewardEtcUiState, BottomSheetRewardEtcEvent, BottomSheetRewardEtcViewModel>(
        LayoutBottomSheetRewardEtcBinding::inflate
){


    override val viewModel: BottomSheetRewardEtcViewModel by viewModels()

    override fun onStart() {
        super.onStart()


        // 다이얼로그의 내부 뷰(design_bottom_sheet)를 찾아 높이를 설정
        (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // 1. 레이아웃 파라미터의 높이를 화면 전체의 80%로 설정
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            bottomSheet.layoutParams = layoutParams

            // 2. 초기 상태를 확장 상태(EXPANDED)로 고정
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

        }


    }

    override fun initView() {
        binding.apply{
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        viewModel.setChallengerId(challengerId)

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
            is BottomSheetRewardEtcEvent.SendSuccess -> {
                Toast.makeText(requireContext(), "상/벌점이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                onConfirm()
                dismiss()
            }
            is BottomSheetRewardEtcEvent.SendFail -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }

    }


}