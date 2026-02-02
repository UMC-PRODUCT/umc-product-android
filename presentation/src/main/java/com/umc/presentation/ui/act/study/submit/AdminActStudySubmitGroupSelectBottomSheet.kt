package com.umc.presentation.ui.act.study.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.databinding.BottomSheetActAdminGroupSelectBinding

class AdminActStudySubmitGroupSelectBottomSheet(
    private val groups: List<String>,
    private val onSelect: (String) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetActAdminGroupSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetActAdminGroupSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = AdminActStudySubmitGroupSelectAdapter { name ->
            onSelect(name)
            dismiss()
        }
        binding.rvGroups.adapter = adapter
        adapter.submitList(groups)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
