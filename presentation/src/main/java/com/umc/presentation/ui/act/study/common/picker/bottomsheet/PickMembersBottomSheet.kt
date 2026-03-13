package com.umc.presentation.ui.act.study.common.picker.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetMemberPickerBinding
import com.umc.presentation.ui.act.study.common.picker.ChallengerPickerViewModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import com.umc.presentation.ui.act.study.common.picker.adapter.MemberPickerAdapter
import com.umc.presentation.ui.act.study.common.picker.adapter.buildMemberSectionRows
import com.umc.presentation.ui.act.study.group.create.adater.SelectedMemberAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PickMembersBottomSheet(
    private val schoolName: String,
    private val part: String?,
    private val preSelectedChallengerIds: Set<Long>,
    private val excludedChallengerId: Long? = null,
    private val onConfirmed: (List<MemberUiModel>) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMemberPickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChallengerPickerViewModel by viewModels()

    private val selectedChallengerIds = linkedSetOf<Long>()
    private val selectedMap = linkedMapOf<Long, MemberUiModel>()

    private var sent = false
    private var shouldApplyOnDismiss = false
    private lateinit var listAdapter: MemberPickerAdapter
    private lateinit var selectedAdapter: SelectedMemberAdapter

    private enum class Mode { EMPTY, PICKING, CONFIRMED }
    private var mode: Mode = Mode.EMPTY

    private var latestMembers: List<MemberUiModel> = emptyList()
    private var didOpenOnce = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetMemberPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.setText(R.string.study_member_add_placeholder)

        selectedChallengerIds.clear()
        selectedChallengerIds.addAll(
            preSelectedChallengerIds.filterNot { it == excludedChallengerId }
        )
        selectedMap.clear()

        val layoutManager = LinearLayoutManager(requireContext())
        listAdapter = MemberPickerAdapter(
            isMulti = true,
            onSinglePick = {},
            onToggle = { member ->
                val id = member.challengerId

                if (id == excludedChallengerId) return@MemberPickerAdapter

                if (selectedChallengerIds.contains(id)) {
                    selectedChallengerIds.remove(id)
                    selectedMap.remove(id)
                } else {
                    selectedChallengerIds.add(id)
                    selectedMap[id] = member
                }
                updateConfirmEnabled()

                if (mode == Mode.CONFIRMED) renderSelectedList()
            },
            isChecked = { member -> selectedChallengerIds.contains(member.challengerId) }
        )

        binding.rvList.apply {
            this.layoutManager = layoutManager
            adapter = listAdapter
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val last = layoutManager.findLastVisibleItemPosition()
                    if (layoutManager.itemCount - last <= 5) viewModel.loadNext()
                }
            })
        }

        selectedAdapter = SelectedMemberAdapter(
            onDelete = { member ->
                selectedChallengerIds.remove(member.challengerId)
                selectedMap.remove(member.challengerId)

                renderSelectedList()
                updateConfirmEnabled()
            }
        )

        binding.rvSelectedMembers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedMembers.adapter = selectedAdapter
        binding.rvSelectedMembers.itemAnimator = null

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                latestMembers = state.items.filterNot { it.challengerId == excludedChallengerId }

                for (m in latestMembers) {
                    if (selectedChallengerIds.contains(m.challengerId)) {
                        selectedMap.putIfAbsent(m.challengerId, m)
                    }
                }

                when (mode) {
                    Mode.PICKING -> listAdapter.submitList(buildMemberSectionRows(latestMembers))
                    Mode.CONFIRMED -> renderSelectedList()
                    Mode.EMPTY -> updateEmptyView()
                }
            }
        }

        binding.searchBar.setOnFocusChangedListener { hasFocus ->
            if (hasFocus) enterPickingMode()
        }
        binding.searchBar.setOnClickListener { enterPickingMode() }
        binding.searchBar.setOnTextChangedListener { q ->
            if (mode != Mode.PICKING) enterPickingMode()
            viewModel.onQueryChanged(q)
        }

        binding.btnConfirm.setOnClickListener {
            shouldApplyOnDismiss = true
            binding.searchBar.setText("")
            binding.root.requestFocus()
            enterConfirmedMode()
        }

        if (selectedChallengerIds.isEmpty()) enterEmptyMode() else enterConfirmedMode()
    }

    private fun enterPickingMode() {
        if (mode == Mode.PICKING) return
        mode = Mode.PICKING

        binding.btnConfirm.visibility = View.VISIBLE
        binding.btnConfirm.setText("완료")

        binding.rvList.visibility = View.VISIBLE
        binding.rvSelectedMembers.visibility = View.GONE
        binding.emptySpace.visibility = View.GONE

        updateConfirmEnabled()

        if (!didOpenOnce) {
            didOpenOnce = true
            viewModel.open(part = null, selectedSchoolId = null, selectedGisuId = null)
        } else {
            viewModel.onQueryChanged("")
        }

        listAdapter.submitList(buildMemberSectionRows(latestMembers))
    }

    private fun enterConfirmedMode() {
        mode = Mode.CONFIRMED

        binding.btnConfirm.visibility = View.GONE
        binding.rvList.visibility = View.GONE
        binding.rvSelectedMembers.visibility = View.VISIBLE
        binding.emptySpace.visibility = View.GONE

        renderSelectedList()
    }

    private fun enterEmptyMode() {
        mode = Mode.EMPTY

        binding.btnConfirm.visibility = View.GONE
        binding.rvList.visibility = View.GONE
        binding.rvSelectedMembers.visibility = View.GONE
        binding.emptySpace.visibility = View.VISIBLE
    }

    private fun renderSelectedList() {
        val picked = selectedMap.values
            .filterNot { it.challengerId == excludedChallengerId }

        selectedAdapter.submitList(picked)
        if (picked.isEmpty()) enterEmptyMode()
    }

    private fun updateConfirmEnabled() {
        binding.btnConfirm.isEnabled = selectedChallengerIds.isNotEmpty()
    }

    private fun updateEmptyView() {
        binding.emptySpace.visibility = if (mode == Mode.EMPTY) View.VISIBLE else View.GONE
    }

    private fun allPicked(): List<MemberUiModel> =
        selectedMap.values.filterNot { it.challengerId == excludedChallengerId }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setOnShowListener { dialog ->
                val d = dialog as BottomSheetDialog
                val bottomSheet =
                    d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        ?: return@setOnShowListener

                val screenHeight = resources.displayMetrics.heightPixels
                val targetHeight = (screenHeight * 0.75f).toInt()
                val topOffset = screenHeight - targetHeight

                bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                bottomSheet.requestLayout()

                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.isFitToContents = false
                behavior.expandedOffset = topOffset
                behavior.peekHeight = targetHeight
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!sent && shouldApplyOnDismiss) {
            sent = true
            onConfirmed(allPicked())
        }
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}