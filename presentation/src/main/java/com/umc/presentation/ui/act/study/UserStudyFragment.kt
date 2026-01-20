package com.umc.presentation.ui.act.study

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserStudyBinding

class UserStudyFragment : BaseFragment<FragmentUserStudyBinding, UserStudyState, UserStudyEvent, UserStudyViewModel>(
    FragmentUserStudyBinding::inflate,
) {
    override val viewModel: UserStudyViewModel by viewModels()

    private lateinit var adapter: ActStudyAdapter

    override fun initView() {
        adapter = ActStudyAdapter(
            onToggle = { index -> viewModel.toggleExpand(index) },
            onLongApprove = { itemId -> viewModel.debugApprove(itemId) },
            onSubmitClick = { itemId, link -> viewModel.onSubmitClick(itemId, link) },
        )



        binding.rvUserStudy.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserStudy.adapter = adapter
        binding.state = viewModel.uiState.value   
        binding.lifecycleOwner = viewLifecycleOwner

    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiState.collect { state ->

                binding.tvPart.text = "WEB PART CURRICULUM"
                binding.tvTitle.text = state.title
                binding.tvPercent.text = state.percentText
                binding.progress.progress = state.progress
                binding.tvSub.text = state.subText


                adapter.submitList(state.items)
            }
        }
    }
}
