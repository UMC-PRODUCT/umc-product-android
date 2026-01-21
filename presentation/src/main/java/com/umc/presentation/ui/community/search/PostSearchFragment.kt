package com.umc.presentation.ui.community.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPostSearchBinding
import com.umc.presentation.ui.community.detail.PostDetailFragmentEvent
import com.umc.presentation.ui.community.detail.PostDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostSearchFragment : BaseFragment<FragmentPostSearchBinding, PostSearchFragmentUiState, PostSearchFragmentEvent, PostSearchViewModel>(
    FragmentPostSearchBinding::inflate
) {
    override val viewModel: PostSearchViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }


    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
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

    override fun handleEvent(event: PostSearchFragmentEvent) {
        super.handleEvent(event)
    }
}