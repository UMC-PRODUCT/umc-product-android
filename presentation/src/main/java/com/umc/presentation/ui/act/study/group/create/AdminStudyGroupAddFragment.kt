package com.umc.presentation.ui.act.study.group.create

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminStudyGroupAddBinding
import com.umc.presentation.ui.act.adapter.DropDownAdapter
import com.umc.presentation.ui.act.challenge.UserChallengerViewModel
import com.umc.presentation.ui.act.study.common.mapper.toMemberUiModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import com.umc.presentation.ui.act.study.common.picker.bottomsheet.PickLeaderBottomSheet
import com.umc.presentation.ui.act.study.common.picker.bottomsheet.PickMembersBottomSheet
import com.umc.presentation.ui.act.study.group.create.model.AdminStudyGroupAddEvent
import com.umc.presentation.ui.act.study.group.create.model.AdminStudyGroupAddState
import com.umc.presentation.ui.act.study.group.create.model.AdminStudyGroupAddViewModel
import com.umc.presentation.ui.act.util.toSummaryText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class AdminStudyGroupAddFragment :
    BaseFragment<
            FragmentAdminStudyGroupAddBinding,
            AdminStudyGroupAddState,
            AdminStudyGroupAddEvent,
            AdminStudyGroupAddViewModel
            >(FragmentAdminStudyGroupAddBinding::inflate) {

    override val viewModel: AdminStudyGroupAddViewModel by viewModels()


    private val challengerVm: UserChallengerViewModel by activityViewModels()

    private val parts = listOf("Web", "Android", "iOS", "Server", "Design", "Plan")

    private var cachedMembers: List<MemberUiModel> = emptyList()

    private var selectedPart: String = "Web"
    private var selectedLeader: MemberUiModel? = null
    private val selectedMembers = mutableListOf<MemberUiModel>()

    private var isPartDropdownOpen = false
    private lateinit var partDropDownAdapter: DropDownAdapter

    override fun initView() {
        binding.vm = viewModel

        binding.tvSelectedPart.text = selectedPart

        binding.btnBack.setOnClickListener { moveToStudyGroupTab() }
        binding.btnRegister.setOnClickListener { viewModel.submitCreateStudyGroup() }

        setupPartDropdown()
        setupLeaderPicker()
        setupMembersPicker()
        renderSelectedMembers()

        collectState()
        collectEvent()

        collectChallengersAsMembers()
    }

    private fun moveToStudyGroupTab() {
        val nav = findNavController()

        val actEntry = nav.getBackStackEntry(R.id.activityManagementFragment)

        actEntry.savedStateHandle["ACT_TARGET_TAB"] = 1
        actEntry.savedStateHandle["ADMIN_STUDY_TARGET_TAB"] = "GROUP"
        nav.popBackStack(R.id.activityManagementFragment, false)
    }

    private fun collectChallengersAsMembers() {
        viewLifecycleOwner.lifecycleScope.launch {
            challengerVm.uiState.collect { chState ->

                val schoolName = viewModel.uiState.value.run {
                    ""
                }

                cachedMembers = chState.allChallengers.map { it.toMemberUiModel(schoolName) }
            }
        }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.state = state
                state.leader?.let {
                    selectedLeader = it
                    binding.tvLeaderPlaceholder.text = it.name
                    binding.tvLeaderPlaceholder.setTextColor(resources.getColor(R.color.neutral800, null))
                }

                selectedMembers.clear()
                selectedMembers.addAll(state.selectedMembers)
                renderSelectedMembers()
            }
        }
    }

    private fun collectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    AdminStudyGroupAddEvent.ClickBack -> moveBackPressed()
                    AdminStudyGroupAddEvent.ClickPickLeader -> showPickLeader()
                    AdminStudyGroupAddEvent.ClickPickMembers -> showPickMembers()
                    AdminStudyGroupAddEvent.ClickRegister -> viewModel.submitCreateStudyGroup()

                    AdminStudyGroupAddEvent.CreateSuccess -> {
                        moveBackPressed()
                    }

                    is AdminStudyGroupAddEvent.ShowToast -> {
                        showToast(event.message)
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun moveBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun setupPartDropdown() {
        partDropDownAdapter = DropDownAdapter(object : DropDownAdapter.DropDownDelegate {
            override fun onClickItem(text: String) {
                selectedPart = text
                binding.tvSelectedPart.text = text
                viewModel.onPartChanged(text)
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
            showPickLeader()
        }
    }

    private fun setupMembersPicker() {
        binding.cardPickMembers.setOnClickListener {
            showPickMembers()
        }
    }

    private fun showPickLeader() {
        if (cachedMembers.isEmpty()) {
            showToast("멤버 목록을 불러오는 중입니다. 잠시 후 다시 시도해주세요.")
            return
        }

        PickLeaderBottomSheet(
            schoolName = "",
        ) { leader ->
            selectedLeader = leader
            viewModel.setLeader(leader)

            binding.tvLeaderPlaceholder.text = leader.name
            binding.tvLeaderPlaceholder.setTextColor(resources.getColor(R.color.neutral800, null))
        }.show(childFragmentManager, "PickLeader")
    }

    private fun showPickMembers() {
        if (cachedMembers.isEmpty()) {
            showToast("멤버 목록을 불러오는 중입니다. 잠시 후 다시 시도해주세요.")
            return
        }

        PickMembersBottomSheet(
            schoolName = "",
            preSelectedChallengerIds = selectedMembers.map { it.challengerId }.toSet()
        ) { picked ->
            selectedMembers.clear()
            selectedMembers.addAll(picked)

            viewModel.setMembers(selectedMembers)
            renderSelectedMembers()
        }.show(childFragmentManager, "PickMembers")
    }

    private fun renderSelectedMembers() {
        if (selectedMembers.isEmpty()) {
            binding.tvMembersPlaceholder.text = "스터디원을 선택하세요"
            binding.tvMembersPlaceholder.setTextColor(resources.getColor(R.color.neutral400, null))
        } else {
            binding.tvMembersPlaceholder.text =
                selectedMembers.toSummaryText(emptyText = "")
            binding.tvMembersPlaceholder.setTextColor(resources.getColor(R.color.neutral800, null))
        }
    }
}