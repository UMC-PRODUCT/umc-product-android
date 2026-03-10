package com.umc.presentation.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.umc.presentation.R
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CommunityListFragment : BaseFragment<FragmentCommunityListBinding, CommunityListViewModel>(
    FragmentCommunityListBinding::inflate
) {

    override val viewModel: CommunityListViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }

    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->

                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: CommunityListFragmentEvent) {
        super.handleEvent(event)

    }

}