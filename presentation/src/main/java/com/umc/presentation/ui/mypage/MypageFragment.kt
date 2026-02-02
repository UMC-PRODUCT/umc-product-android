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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
            is MypageFragmentEvent.NavigateToGithub -> {
                openWebpage(viewModel.uiState.value.tmpgithub)
            }
            is MypageFragmentEvent.NavigateToBlog -> {
                openWebpage(viewModel.uiState.value.tmpblog)
            }
            is MypageFragmentEvent.NavigateToLinkedin -> {
                openWebpage(viewModel.uiState.value.tmplinkedin)
            }
            is MypageFragmentEvent.NavigateToEditProfile -> {
                val action = MypageFragmentDirections.actionMypageToProfile()
                findNavController().navigate(action)
            }

            is MypageFragmentEvent.NavigateToSuggetion -> {
                val action = MypageFragmentDirections.actionMypageToSugget()
                findNavController().navigate(action)
            }
            is MypageFragmentEvent.NavigateToMypost -> {
                val action = MypageFragmentDirections.actionMypageToMypost()
                findNavController().navigate(action)
            }
            is MypageFragmentEvent.NavigateToMyComment -> {
                val action = MypageFragmentDirections.actionMypageToMycomment()
                findNavController().navigate(action)

            }
            
            is MypageFragmentEvent.NavigateToScrap -> {
                /**TODO 스크랩 글 생성**/
            }


            is MypageFragmentEvent.NavigateToAssistUmc -> {
                /**TODO UMC 문의 이동 로직**/
            }


            is MypageFragmentEvent.NavigateToSettingNotice -> {
                /**TODO 알림 설정 이동 로직**/
            }

            is MypageFragmentEvent.NavigateToSettingLocation -> {
                /**TODO 위치 설정 이동 로직**/
            }

            is MypageFragmentEvent.NavigateToSocialSetting -> {
                /**TODO 소셜 연동 이동 로직**/
            }

            is MypageFragmentEvent.NavigateToPersonalInformation -> {
                /**TODO 개인정보처리 방침 이동 로직**/
            }

            is MypageFragmentEvent.NavigateToUseManual -> {
                /**TODO 이용약관 이동 로직**/

            }
            
            is MypageFragmentEvent.Logout -> {
                /**TODO logout 로직 생성**/
            }

            is MypageFragmentEvent.DeleteUser -> {
                /**TODO 회원 탈퇴 로직 생성**/
            }

            is MypageFragmentEvent.NavigateToWebstieUmc -> {
                openWebpage(viewModel.uiState.value.websiteUMC)
            }

            is MypageFragmentEvent.NavigateToInstagramUmc -> {
                openWebpage(viewModel.uiState.value.instagramUMC)
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