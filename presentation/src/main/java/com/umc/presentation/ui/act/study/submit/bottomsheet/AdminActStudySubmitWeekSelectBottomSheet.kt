package com.umc.presentation.ui.act.study.submit.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.databinding.BottomSheetActWeekSelectBinding
import com.umc.presentation.ui.act.study.submit.adapter.AdminActStudySubmitWeekSelectAdapter

class AdminActStudySubmitWeekSelectBottomSheet(
    private val weeks: List<Int>,
    private val onSelect: (Int) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetActWeekSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetActWeekSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = AdminActStudySubmitWeekSelectAdapter { week ->
            onSelect(week)
            dismiss()
        }
        binding.rvWeeks.adapter = adapter
        adapter.submitList(weeks)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
