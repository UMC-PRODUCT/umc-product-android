package com.umc.presentation.ui.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMypageBinding
import com.umc.presentation.ui.home.HomeFragmentDirections
import kotlinx.coroutines.launch


class MypageFragment : BaseFragment<FragmentMypageBinding, MypageFragmentUiState, MypageFragmentEvent, MypageViewModel>(
    FragmentMypageBinding::inflate,
) {
    override val viewModel : MypageViewModel by viewModels()

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

    override fun handleEvent(event: MypageFragmentEvent) {
        super.handleEvent(event)
        when(event) {
            /**차후 링크는 실제 값으로 변경**/
            is MypageFragmentEvent.navigateToGithub -> {
                openWebpage(viewModel.uiState.value.tmpgithub)
            }
            is MypageFragmentEvent.navigateToBlog -> {
                openWebpage(viewModel.uiState.value.tmpblog)
            }
            is MypageFragmentEvent.navigateToLinkedin -> {
                openWebpage(viewModel.uiState.value.tmplinkedin)
            }
            is MypageFragmentEvent.navigateToEditProfile -> {
                val action = MypageFragmentDirections.actionMypageToProfile()
                findNavController().navigate(action)
            }

            is MypageFragmentEvent.navigateToSuggetion -> {
                val action = MypageFragmentDirections.actionMypageToSugget()
                findNavController().navigate(action)
            }
            is MypageFragmentEvent.navigateToMypost -> {
                val action = MypageFragmentDirections.actionMypageToMypost()
                findNavController().navigate(action)
            }
            is MypageFragmentEvent.navigateToMyComment -> {
                val action = MypageFragmentDirections.actionMypageToMycomment()
                findNavController().navigate(action)

            }

            else -> {}
        }
    }


    //웹페이지 이동
    private fun openWebpage(url: String) {
        try {
            val webpage: Uri = url.toUri()
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            startActivity(intent)
            //브라우저를 실행할 수 있는 앱이 있는지 확인
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // 브라우저조차 없는 특수한 상황
                val webIntent = Intent(Intent.ACTION_VIEW, webpage)
                startActivity(webIntent)
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }



}