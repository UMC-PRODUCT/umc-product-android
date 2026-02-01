package com.umc.presentation.ui.home.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.home.CategoryItem
import com.umc.presentation.R
import com.umc.presentation.databinding.LayoutBottomSheetCategorySelectPlanBinding
import com.umc.presentation.ui.home.PlanAddFragmentEvent
import com.umc.presentation.ui.home.PlanAddViewModel
import com.umc.presentation.ui.home.adapter.BottomSheetCategoryPlanAdapter
import com.umc.presentation.ui.home.adapter.CategoryPlanDelegate
import kotlinx.coroutines.launch

class BottomSheetCategoryPlanDialog(
    private val viewModel: PlanAddViewModel // 뷰모델 주입
) : BottomSheetDialogFragment(), CategoryPlanDelegate {

    private lateinit var binding: LayoutBottomSheetCategorySelectPlanBinding
    private lateinit var categoryAdapter: BottomSheetCategoryPlanAdapter

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutBottomSheetCategorySelectPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = BottomSheetCategoryPlanAdapter(this)
        binding.rcvCategories.adapter = categoryAdapter

        // 뷰모델의 categories 리스트 구독
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                categoryAdapter.submitList(state.categories)
            }
        }

        // '확인' 버튼 클릭 시 닫기 (데이터는 이미 실시간으로 반영됨)
        binding.btnConfirm.setOnClickListener { dismiss() }
    }

    override fun onCategoryToggled(item: CategoryItem) {
        val event = PlanAddFragmentEvent.SelectCategory(item)
        viewModel.handleEvent(event)
    }
}