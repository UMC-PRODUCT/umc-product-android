package com.umc.presentation.ui.community.top


import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentTopPostBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue


@AndroidEntryPoint
class TopPostFragment : BaseFragment<FragmentTopPostBinding, TopPostFragmentUiState, TopPostFragmentEvent, TopPostViewModel>(
    FragmentTopPostBinding::inflate
) {

    override val viewModel : TopPostViewModel by viewModels()


    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //뷰모델에서 불러오기
        viewModel.fetchTrophies(null, null, null)

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


    override fun handleEvent(event: TopPostFragmentEvent) {
        super.handleEvent(event)
    }

}

