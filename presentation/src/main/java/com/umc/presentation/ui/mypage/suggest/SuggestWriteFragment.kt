package com.umc.presentation.ui.mypage.suggest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSuggestWriteBinding
import kotlinx.coroutines.launch
import kotlin.getValue


class SuggestWriteFragment : BaseFragment<FragmentSuggestWriteBinding, SuggestWriteFragmentUiState, SuggestWriteFragmentEvent, SuggestWriteViewModel>(
    FragmentSuggestWriteBinding::inflate,
) {

    override val viewModel : SuggestWriteViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //여기서 바뀔 때 호출
        binding.sugwriteSwitchAnomy.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAnomy(isChecked)
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

                }
            }
        }



    }
}