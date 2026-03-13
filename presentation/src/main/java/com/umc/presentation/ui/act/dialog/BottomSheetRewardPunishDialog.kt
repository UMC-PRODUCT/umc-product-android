package com.umc.presentation.ui.act.dialog

import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umc.domain.model.enums.PunishCategory
import com.umc.presentation.base.BaseBottomSheetFragment
import com.umc.presentation.databinding.LayoutBottomSheetRewardPunishBinding
import com.umc.presentation.ui.act.adapter.RewardCategoryAdapter
import com.umc.presentation.ui.act.adapter.RewardCategoryDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottomSheetRewardPunishDialog(
    private val isReward: Boolean
)
    : BaseBottomSheetFragment<LayoutBottomSheetRewardPunishBinding, BottomSheetRewardPunishUiState, BottomSheetRewardPunishEvent, BottomSheetRewardPunishViewModel>(
        LayoutBottomSheetRewardPunishBinding::inflate
    )
{
    override val viewModel: BottomSheetRewardPunishViewModel by viewModels()

    private lateinit var rewardCategoryAdapter: RewardCategoryAdapter

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

            viewModel.setRewardMode(isReward)
        }

        rewardCategoryAdapter = RewardCategoryAdapter(object : RewardCategoryDelegate {
            override fun onClickCategory(item: PunishCategory) {
                viewModel.setPunishCategory(item)
            }
        })
        binding.rcvPunishTag.adapter = rewardCategoryAdapter
        rewardCategoryAdapter.submitList(PunishCategory.entries) //얘는 enum꺼 이용

    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    //선택 바뀔 때마다 호출
                    rewardCategoryAdapter.updateSelection(state.currentFilter)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    fun handleEvent(event: BottomSheetRewardPunishEvent) {
        when (event) {
            else -> {}
        }

    }

}