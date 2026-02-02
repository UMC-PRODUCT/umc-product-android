package com.umc.presentation.ui.act.study.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.databinding.BottomSheetActAdminSheetWeekSelectBinding

class AdminActStudySubmitWeekSelectBottomSheet(
    private val weeks: List<Int>,
    private val onSelect: (Int) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetActAdminSheetWeekSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetActAdminSheetWeekSelectBinding.inflate(inflater, container, false)
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
