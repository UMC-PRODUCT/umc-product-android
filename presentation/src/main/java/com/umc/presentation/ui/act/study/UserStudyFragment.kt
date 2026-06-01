package com.umc.presentation.ui.act.study

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserStudyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserStudyFragment :
    BaseFragment<FragmentUserStudyBinding, UserStudyState, UserStudyEvent, UserStudyViewModel>(
        FragmentUserStudyBinding::inflate,
    ) {

    override val viewModel: UserStudyViewModel by viewModels()
    private lateinit var adapter: ActStudyAdapter

    override fun initView() {
        adapter = ActStudyAdapter()

        /* 기존 어댑터 세팅
        adapter = ActStudyAdapter(
            onToggle = {  },
            onLongApprove = { itemId -> viewModel.debugApprove(itemId) },
            onSubmitClick = { itemId, link -> viewModel.onSubmitClick(itemId, link) },
            onConfirmClick = { id -> viewModel.onConfirmClick(id) },
        )
        */

        binding.rvUserStudy.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserStudy.adapter = adapter
        binding.rvUserStudy.itemAnimator = null
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiState.collect { state ->
                binding.apply {
                    tvTitle.text = state.title

                    /* 기존 UI 바인딩
                    tvPart.text = "${state.part.label.uppercase()} PART CURRICULUM"
                    tvPercent.text = state.percentText
                    progress.progress = state.progress
                    tvSub.text = state.subText
                    */

                    val isEmpty = state.weeks.isEmpty()
                    cvCurriculum.isVisible = !isEmpty
                    rvUserStudy.isVisible = !isEmpty
                    layoutEmpty.isVisible = isEmpty
                }

                adapter.submitList(state.weeks)
            }
        }
    }
}