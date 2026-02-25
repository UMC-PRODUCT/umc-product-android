package com.umc.presentation.ui.act.study.common.picker.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetMemberPickerBinding
import com.umc.presentation.ui.act.challenge.UserChallengerViewModel
import com.umc.presentation.ui.act.study.common.mapper.toMemberUiModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import com.umc.presentation.ui.act.study.common.picker.adapter.MemberPickerAdapter
import com.umc.presentation.ui.act.study.group.create.adater.SelectedMemberAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PickMembersBottomSheet(
    private val schoolName: String,
    private val preSelectedChallengerIds: Set<Long>,
    private val onConfirmed: (List<MemberUiModel>) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMemberPickerBinding? = null
    private val binding get() = _binding!!

    private val vm: UserChallengerViewModel by activityViewModels()

    // ✅ 여기선 challengerId로 선택 상태 관리 (memberId 불확실하니)
    private val selectedChallengerIds = linkedSetOf<Long>()

    private lateinit var listAdapter: MemberPickerAdapter
    private lateinit var selectedAdapter: SelectedMemberAdapter

    private enum class Mode { EMPTY, PICKING, CONFIRMED }
    private var mode: Mode = Mode.EMPTY

    private var latestFiltered: List<MemberUiModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMemberPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.setText(R.string.study_member_add_placeholder)

        selectedChallengerIds.clear()
        selectedChallengerIds.addAll(preSelectedChallengerIds)

        listAdapter = MemberPickerAdapter(
            isMulti = true,
            onSinglePick = {},
            onToggle = { member ->
                val id = member.challengerId
                if (selectedChallengerIds.contains(id)) selectedChallengerIds.remove(id)
                else selectedChallengerIds.add(id)

                updateConfirmEnabled()
                if (mode == Mode.CONFIRMED) renderSelectedList()
            },
            isChecked = { member -> selectedChallengerIds.contains(member.challengerId) }
        )

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
            itemAnimator = null
        }

        selectedAdapter = SelectedMemberAdapter(
            onDelete = { member ->
                selectedChallengerIds.remove(member.challengerId)
                renderSelectedList()
                updateConfirmEnabled()
            }
        )

        binding.rvSelectedMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedAdapter
            itemAnimator = null
        }

        // VM 구독
        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                latestFiltered = state.filteredChallengers.map { it.toMemberUiModel(schoolName) }

                if (mode == Mode.PICKING) listAdapter.submitList(latestFiltered)
                if (mode == Mode.CONFIRMED) renderSelectedList()
                if (mode == Mode.EMPTY && selectedChallengerIds.isNotEmpty()) enterConfirmedMode()
            }
        }

        // 검색바 -> PICKING 진입
        binding.searchBar.setOnTextChangedListener { q ->
            vm.filterList(q)
            if (mode != Mode.PICKING) enterPickingMode()
        }


        binding.searchBar.setOnClickListener {
            if (mode != Mode.PICKING) enterPickingMode()
        }

        binding.btnConfirm.setOnClickListener {
            val picked = allPicked()
            onConfirmed(picked)
            enterConfirmedMode()

            binding.root.requestFocus()
        }

        if (selectedChallengerIds.isEmpty()) enterEmptyMode() else enterConfirmedMode()
    }

    private fun allPicked(): List<MemberUiModel> {
        val all = vm.uiState.value.allChallengers.map { it.toMemberUiModel(schoolName) }
        return all.filter { selectedChallengerIds.contains(it.challengerId) }
    }

    private fun enterEmptyMode() {
        mode = Mode.EMPTY
        binding.btnConfirm.visibility = View.GONE
        binding.rvList.visibility = View.GONE
        binding.rvSelectedMembers.visibility = View.GONE
        updateConfirmEnabled()
    }

    private fun enterPickingMode() {
        mode = Mode.PICKING
        binding.btnConfirm.visibility = View.VISIBLE
        binding.rvList.visibility = View.VISIBLE
        binding.rvSelectedMembers.visibility = View.GONE

        listAdapter.submitList(latestFiltered)
        updateConfirmEnabled()
    }

    private fun enterConfirmedMode() {
        mode = Mode.CONFIRMED
        binding.btnConfirm.visibility = View.GONE
        binding.rvList.visibility = View.GONE
        binding.rvSelectedMembers.visibility = View.VISIBLE
        renderSelectedList()
    }

    private fun renderSelectedList() {
        val picked = allPicked()
        selectedAdapter.submitList(picked)
        if (picked.isEmpty()) enterEmptyMode()
    }

    private fun updateConfirmEnabled() {
        binding.btnConfirm.isEnabled = selectedChallengerIds.isNotEmpty()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener { dialog ->
                val d = dialog as BottomSheetDialog
                val bottomSheet =
                    d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        ?: return@setOnShowListener

                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.peekHeight = (resources.displayMetrics.heightPixels * 0.75f).toInt()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}