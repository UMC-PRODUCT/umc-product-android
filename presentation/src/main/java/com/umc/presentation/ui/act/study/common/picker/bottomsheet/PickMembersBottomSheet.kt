package com.umc.presentation.ui.act.study.common.picker.bottomsheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetMemberPickerBinding
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import com.umc.presentation.ui.act.study.common.picker.adapter.MemberPickerAdapter
import com.umc.presentation.ui.act.study.group.create.adater.SelectedMemberAdapter

class PickMembersBottomSheet(
    private val allMembers: List<MemberUiModel>,
    private val preSelectedIds: Set<Long>,
    private val onConfirmed: (List<MemberUiModel>) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMemberPickerBinding? = null
    private val binding get() = _binding!!

    private val selectedIds = linkedSetOf<Long>()

    private lateinit var listAdapter: MemberPickerAdapter
    private lateinit var selectedAdapter: SelectedMemberAdapter

    private enum class Mode { EMPTY, PICKING, CONFIRMED }
    private var mode: Mode = Mode.EMPTY

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


        selectedIds.clear()
        selectedIds.addAll(preSelectedIds)


        listAdapter = MemberPickerAdapter(
            isMulti = true,
            onSinglePick = {},
            onToggle = { member ->
                if (selectedIds.contains(member.id)) selectedIds.remove(member.id)
                else selectedIds.add(member.id)

                updateConfirmEnabled()
                if (mode == Mode.CONFIRMED) renderSelectedList()
            },
            isChecked = { member -> selectedIds.contains(member.id) }
        )

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
            itemAnimator = null
        }


        selectedAdapter = SelectedMemberAdapter(
            onDelete = { member ->
                selectedIds.remove(member.id)
                renderSelectedList()
                updateConfirmEnabled()
            }
        )

        binding.rvSelectedMembers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedAdapter
            itemAnimator = null
        }


        binding.searchBar.setOnClickListener { enterPickingMode(showAll = true) }
        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) enterPickingMode(showAll = true)
        }


        binding.searchBar.setOnFocusChangedListener { hasFocus ->
            if (hasFocus && mode != Mode.PICKING) enterPickingMode(showAll = true)
        }



        binding.btnConfirm.setOnClickListener {
            val picked = allMembers.filter { selectedIds.contains(it.id) }
            onConfirmed(picked)
            enterConfirmedMode()
            binding.searchBar.clearFocus()
        }


        if (selectedIds.isEmpty()) {
            enterEmptyMode()
        } else {

            enterConfirmedMode()
        }
    }

    private fun enterEmptyMode() {
        mode = Mode.EMPTY
        binding.btnConfirm.visibility = View.GONE
        binding.rvList.visibility = View.GONE
        binding.rvSelectedMembers.visibility = View.GONE

    }

    private fun enterPickingMode(showAll: Boolean) {
        mode = Mode.PICKING
        binding.btnConfirm.visibility = View.VISIBLE
        binding.rvList.visibility = View.VISIBLE
        binding.rvSelectedMembers.visibility = View.GONE

        if (showAll) listAdapter.submitList(allMembers)
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
        val picked = allMembers.filter { selectedIds.contains(it.id) }
        selectedAdapter.submitList(picked)

        if (picked.isEmpty()) enterEmptyMode()
    }

    private fun updateConfirmEnabled() {
        binding.btnConfirm.isEnabled = selectedIds.isNotEmpty()
    }

    private fun submitFiltered(query: String) {
        val q = query.trim()
        val filtered = if (q.isEmpty()) allMembers
        else allMembers.filter { it.name.contains(q, ignoreCase = true) }
        listAdapter.submitList(filtered)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener { dialog ->
                val d = dialog as BottomSheetDialog
                val bottomSheet =
                    d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return@setOnShowListener

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
