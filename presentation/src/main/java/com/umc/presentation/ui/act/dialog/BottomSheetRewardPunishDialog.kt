package com.umc.presentation.ui.act.dialog

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umc.domain.model.enums.PunishCategory
import com.umc.domain.model.enums.RewardType
import com.umc.presentation.base.BaseBottomSheetFragment
import com.umc.presentation.databinding.LayoutBottomSheetRewardPunishBinding
import com.umc.presentation.ui.act.adapter.RewardCategoryAdapter
import com.umc.presentation.ui.act.adapter.RewardCategoryDelegate
import com.umc.presentation.ui.act.adapter.RewardSelectAdapter
import com.umc.presentation.ui.act.adapter.RewardSelectDelegate
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottomSheetRewardPunishDialog(
    private val challengerId: Long,
    private val isReward: Boolean,
    private val onConfirm:() -> Unit
)
    : BaseBottomSheetFragment<LayoutBottomSheetRewardPunishBinding, BottomSheetRewardPunishUiState, BottomSheetRewardPunishEvent, BottomSheetRewardPunishViewModel>(
        LayoutBottomSheetRewardPunishBinding::inflate
    )
{
    override val viewModel: BottomSheetRewardPunishViewModel by viewModels()

    private lateinit var rewardCategoryAdapter: RewardCategoryAdapter //벌점 카테고리 어댑터
    private lateinit var rewardSelectAdapter: RewardSelectAdapter

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
        
        //챌린저 정보 저장
        viewModel.setChallengerId(challengerId)

        //어댑터 설정
        rewardCategoryAdapter = RewardCategoryAdapter(object : RewardCategoryDelegate {
            override fun onClickCategory(item: PunishCategory) {
                viewModel.setPunishCategory(item)
            }
        })
        binding.rcvPunishTag.adapter = rewardCategoryAdapter
        rewardCategoryAdapter.submitList(PunishCategory.entries) //얘는 enum꺼 이용

        rewardSelectAdapter = RewardSelectAdapter(object : RewardSelectDelegate {
            override fun onClickReward(item: RewardType) {
                viewModel.setRewardType(item)
            }
        })
        binding.rcvRewardList.adapter = rewardSelectAdapter

        //상벌점에 따른 리스트 초기화 작업
        if(isReward){
            //상점
            val rewardList = RewardType.getBonusList()
            viewModel.setRewardList(rewardList)
        }
        else{
            //벌점
            val rewardList = RewardType.getPenaltyList()
            viewModel.setRewardList(rewardList)
        }


    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    //선택 바뀔 때마다 호출
                    //카테고리 누를 때 변화
                    rewardCategoryAdapter.updateSelection(state.currentFilter)
                    //리스트 변화 및 누를 때 호출
                    rewardSelectAdapter.submitList(state.displayList)
                    rewardSelectAdapter.updateSelection(state.selectedItem)

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
            is BottomSheetRewardPunishEvent.SendSuccess -> {
                UToast.createToast(
                    context = requireContext(),
                    message = "상/벌점이 등록되었습니다.",
                    state = UToast.State.CHECK
                ).show()
                onConfirm()
                dismiss()
            }

            is BottomSheetRewardPunishEvent.SendFail -> {
                UToast.createToast(
                    context = requireContext(),
                    message = event.message,
                    state = UToast.State.ERROR
                ).show()
            }
            else -> {}
        }

    }

}