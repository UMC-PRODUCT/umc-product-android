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

    private enum class Mode { EMPTY, PICKING }
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
        binding.tvTitle.setText(R.string.study_leader_search_title)


        binding.btnConfirm.visibility = View.GONE
        binding.rvSelectedMembers.visibility = View.GONE

        adapter = MemberPickerAdapter(
            isMulti = false,
            onSinglePick = { picked ->
                onPicked(picked)
                dismiss()
            },
            onToggle = {},
            isChecked = { false }
        )

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PickLeaderBottomSheet.adapter
            itemAnimator = null
        }


        enterEmptyMode()


        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                latestFiltered = state.filteredChallengers.map { it.toMemberUiModel(schoolName) }

                if (mode == Mode.PICKING) {
                    adapter.submitList(latestFiltered)
                }

            }
        }


        binding.searchBar.setOnTextChangedListener { q ->
            vm.filterList(q)
            if (mode != Mode.PICKING) enterPickingMode()
        }


        binding.searchBar.setOnFocusChangedListener { hasFocus ->
            if (hasFocus && mode != Mode.PICKING) enterPickingMode()
        }

        binding.searchBar.setOnClickListener {
            if (mode != Mode.PICKING) enterPickingMode()
        }
    }

    private fun enterEmptyMode() {
        mode = Mode.EMPTY
        binding.rvList.visibility = View.GONE


        binding.emptySpace.visibility = View.VISIBLE
    }

    private fun enterPickingMode() {
        mode = Mode.PICKING
        binding.emptySpace.visibility = View.GONE

        binding.rvList.visibility = View.VISIBLE
        adapter.submitList(latestFiltered)
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}