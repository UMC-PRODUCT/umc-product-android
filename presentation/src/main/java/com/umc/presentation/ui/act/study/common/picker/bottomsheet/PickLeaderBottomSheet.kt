package com.umc.presentation.ui.act.study.common.picker.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetMemberPickerBinding
import com.umc.presentation.ui.act.challenge.UserChallengerViewModel
import com.umc.presentation.ui.act.study.common.mapper.toMemberUiModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import com.umc.presentation.ui.act.study.common.picker.adapter.MemberPickerAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PickLeaderBottomSheet(
    private val schoolName: String,
    private val onPicked: (MemberUiModel) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMemberPickerBinding? = null
    private val binding get() = _binding!!

    private val vm: UserChallengerViewModel by activityViewModels()
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
        binding.rvSelectedMembers.visibility = View.GONE
        binding.rvList.visibility = View.GONE

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
        binding.rvList.itemAnimator = null

        // VM state 구독 -> MemberUiModel로 매핑
        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                adapter.submitList(state.filteredChallengers.map { it.toMemberUiModel(schoolName) })
            }
        }

        // 검색
        binding.searchBar.setOnTextChangedListener { q ->
            vm.filterList(q)
            showListIfNeeded()
        }

        binding.searchBar.setOnFocusChangedListener { hasFocus ->
            if (hasFocus) showListIfNeeded()
        }

        binding.searchBar.setOnClickListener { showListIfNeeded() }
    }

    private fun showListIfNeeded() {
        if (binding.rvList.visibility != View.VISIBLE) {
            binding.rvList.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}