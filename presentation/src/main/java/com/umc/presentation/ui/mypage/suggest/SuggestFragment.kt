package com.umc.presentation.ui.mypage.suggest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSuggestBinding
import com.umc.presentation.ui.mypage.MypageFragmentDirections
import com.umc.presentation.ui.mypage.adapter.SuggestionAdapter
import com.umc.presentation.ui.mypage.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SuggestFragment : BaseFragment<FragmentSuggestBinding, SuggestFragmentUiState, SuggestFragmentEvent, SuggestViewModel>(
    FragmentSuggestBinding::inflate,
){

    override val viewModel : SuggestViewModel by viewModels()

    private lateinit var suggestAdapter: SuggestionAdapter


    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //어댑터 정의
        suggestAdapter = SuggestionAdapter()
        binding.suggestRcv.apply {
            adapter = suggestAdapter
        }
    }


    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    suggestAdapter.submitList(state.tmpData)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    override fun handleEvent(event: SuggestFragmentEvent) {
        super.handleEvent(event)
        when(event) {
         is SuggestFragmentEvent.NavigateSuggestWrite -> {
             val action = SuggestFragmentDirections.actionSuggetstToSuggestWrite()
             findNavController().navigate(action)
         }
         is SuggestFragmentEvent.ClickBackPressed -> {
             requireActivity().onBackPressedDispatcher.onBackPressed()
         }
            else -> {}
        }
    }

}