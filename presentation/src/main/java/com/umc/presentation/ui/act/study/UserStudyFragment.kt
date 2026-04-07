package com.umc.presentation.ui.act.study

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserStudyBinding
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.isVisible

@AndroidEntryPoint
class UserStudyFragment :
    BaseFragment<FragmentUserStudyBinding, UserStudyState, UserStudyEvent, UserStudyViewModel>(
        FragmentUserStudyBinding::inflate,
    ) {

    override val viewModel: UserStudyViewModel by viewModels()
    private lateinit var adapter: ActStudyAdapter

    override fun initView() {
        adapter = ActStudyAdapter(
            //onToggle = { index -> viewModel.toggleExpand(index) },
            onToggle = {  },
            onLongApprove = { itemId -> viewModel.debugApprove(itemId) },
            onSubmitClick = { itemId, link -> viewModel.onSubmitClick(itemId, link) },
            onConfirmClick = { id -> viewModel.onConfirmClick(id) },
        )

        binding.rvUserStudy.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserStudy.adapter = adapter


        binding.rvUserStudy.itemAnimator = null

        binding.state = viewModel.uiState.value
        binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiState.collect { state ->

                binding.apply {
                    tvPart.text = "${state.part.label.uppercase()} PART CURRICULUM"
                    tvTitle.text = state.title
                    tvPercent.text = state.percentText
                    progress.progress = state.progress
                    tvSub.text = state.subText

                    val isEmpty = state.items.isNullOrEmpty()
                    binding.cvCurriculum.isVisible = !isEmpty
                    rvUserStudy.isVisible = !isEmpty
                    layoutEmpty.isVisible = isEmpty
                }

                adapter.submitList(state.items)
            }
        }
    }
}
