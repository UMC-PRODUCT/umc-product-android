package com.umc.presentation.ui.community.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.presentation.R
import com.umc.presentation.databinding.LayoutBottomSheetCategoryBinding
import com.umc.presentation.ui.community.adapter.BottomSheetCategoryAdapter

//정의한 layout_bottom_sheet_category랑
//안의 recyclerview를 위한 item_bottom_sheet_category랑 BottomSheetCategoryAdapter랑 연결

class CategoryBottomSheetDialog (
    private val onSelected: (CommunityCategoryType) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: LayoutBottomSheetCategoryBinding


    //XML의 둥근 모서리를 보여주기 위해 투명 테마를 적용
    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutBottomSheetCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //어댑터 초기화 및 클릭 리스너 연결
        val categoryAdapter = BottomSheetCategoryAdapter { selectedCategory ->
            onSelected(selectedCategory) //카테고리 전달
            dismiss() //다이얼로그 닫기
        }

        //리사이클러뷰 설정
        binding.rvCategories.adapter = categoryAdapter

        //데이터 주입 (Enum의 모든 항목)
        categoryAdapter.submitList(CommunityCategoryType.entries)
    }

}