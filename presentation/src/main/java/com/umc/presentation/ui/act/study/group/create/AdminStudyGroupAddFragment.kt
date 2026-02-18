package com.umc.presentation.ui.act.study.group.create

import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminStudyGroupAddBinding
import com.umc.presentation.ui.act.adapter.DropDownAdapter
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import com.umc.presentation.ui.act.study.common.picker.bottomsheet.PickLeaderBottomSheet
import com.umc.presentation.ui.act.study.common.picker.bottomsheet.PickMembersBottomSheet
import com.umc.presentation.ui.act.study.group.create.model.AdminStudyGroupAddEvent
import com.umc.presentation.ui.act.study.group.create.model.AdminStudyGroupAddState
import com.umc.presentation.ui.act.study.group.create.model.AdminStudyGroupAddViewModel
import com.umc.presentation.ui.act.util.toSummaryText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminStudyGroupAddFragment :
    BaseFragment<
            FragmentAdminStudyGroupAddBinding,
            AdminStudyGroupAddState,
            AdminStudyGroupAddEvent,
            AdminStudyGroupAddViewModel
            >(FragmentAdminStudyGroupAddBinding::inflate) {

    override val viewModel: AdminStudyGroupAddViewModel by viewModels()


    private val parts = listOf("Web", "Android", "iOS", "Server", "Design", "Plan")
    private val allMembers: List<MemberUiModel> = listOf(
        MemberUiModel(1, "홍길동", "Web", "16기", "학교"),
        MemberUiModel(2, "홍길동1", "Design", "16기", "학교"),
        MemberUiModel(3, "홍길동2", "PM", "16기", "학교"),
    )


    private var selectedPart: String = "Web"
    private var selectedLeader: MemberUiModel? = null
    private val selectedMembers = mutableListOf<MemberUiModel>()


    private var isPartDropdownOpen = false
    private lateinit var partDropDownAdapter: DropDownAdapter

    override fun initView() {
        binding.vm = viewModel

        binding.tvSelectedPart.text = selectedPart

        binding.btnBack.setOnClickListener { moveBackPressed() }

        setupPartDropdown()
        setupLeaderPicker()
        setupMembersPicker()


        renderSelectedMembers()
    }


    private fun moveBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun setupPartDropdown() {
        partDropDownAdapter = DropDownAdapter(object : DropDownAdapter.DropDownDelegate {
            override fun onClickItem(text: String) {
                selectedPart = text
                binding.tvSelectedPart.text = text


                togglePartDropdown(open = false)
            }
        })

        binding.rvPartDropdown.apply {
            adapter = partDropDownAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
        }
        partDropDownAdapter.submitList(parts)

        binding.cardPart.setOnClickListener {
            togglePartDropdown()
        }
    }

    private fun togglePartDropdown(open: Boolean? = null) {
        isPartDropdownOpen = open ?: !isPartDropdownOpen

        binding.cardPartDropdown.visibility =
            if (isPartDropdownOpen) View.VISIBLE else View.GONE

        binding.ivPartArrow.animate()
            .rotation(if (isPartDropdownOpen) 180f else 0f)
            .setDuration(120)
            .start()

        if (isPartDropdownOpen) {
            binding.cardPartDropdown.bringToFront()
            binding.cardPartDropdown.requestLayout()
            binding.cardPartDropdown.invalidate()
        }
    }

    private fun setupLeaderPicker() {
        binding.cardPickLeader.setOnClickListener {
            PickLeaderBottomSheet(allMembers) { leader ->
                selectedLeader = leader

                binding.tvLeaderPlaceholder.text = leader.name
                binding.tvLeaderPlaceholder.setTextColor(
                    resources.getColor(R.color.neutral800, null)
                )
            }.show(childFragmentManager, "PickLeader")
        }
    }


    private fun setupMembersPicker() {
        binding.cardPickMembers.setOnClickListener {
            PickMembersBottomSheet(
                allMembers = allMembers,
                preSelectedIds = selectedMembers.map { it.id }.toSet()
            ) { picked ->

                selectedMembers.clear()
                selectedMembers.addAll(picked)


                renderSelectedMembers()
            }.show(childFragmentManager, "PickMembers")
        }
    }

    private fun renderSelectedMembers() {
        if (selectedMembers.isEmpty()) {
            binding.tvMembersPlaceholder.text = "스터디원을 선택하세요"
            binding.tvMembersPlaceholder.setTextColor(
                resources.getColor(R.color.neutral400, null)
            )
        } else {
            binding.tvMembersPlaceholder.text =
                selectedMembers.toSummaryText(emptyText = "")
            binding.tvMembersPlaceholder.setTextColor(
                resources.getColor(R.color.neutral800, null)
            )
        }
    }


}
