package com.umc.presentation.ui.act.study.common.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetMemberPickerBinding
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

class PickLeaderBottomSheet(
    private val allMembers: List<MemberUiModel>,
    private val onPicked: (MemberUiModel) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMemberPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MemberPickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMemberPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.setText(R.string.study_leader_search_title)
        binding.btnConfirm.visibility = View.GONE

        adapter = MemberPickerAdapter(
            isMulti = false,
            onSinglePick = { picked ->
                onPicked(picked)
                dismiss()
            },
            onToggle = {},
            isChecked = { false }
        )

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter


        binding.searchBar.setOnClickListener {
            showListIfNeeded()
        }

        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showListIfNeeded()
        }


        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showListIfNeeded()
        }
    }

    private fun showListIfNeeded() {
        if (binding.rvList.visibility != View.VISIBLE) {
            binding.rvList.visibility = View.VISIBLE
            adapter.submitList(allMembers)
        }
    }

    private fun submitFiltered(query: String) {
        val q = query.trim()
        val filtered = if (q.isEmpty()) allMembers
        else allMembers.filter { it.name.contains(q, ignoreCase = true) }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
