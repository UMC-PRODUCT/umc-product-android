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
import com.umc.presentation.component.UMypageDialog
import com.umc.presentation.component.UMypageDialogModel
import com.umc.presentation.databinding.FragmentMypageBinding
import com.umc.presentation.ui.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import coil.load
import android.provider.Settings

@AndroidEntryPoint
class MypageFragment : BaseFragment<FragmentMypageBinding, MypageFragmentUiState, MypageFragmentEvent, MypageViewModel>(
    FragmentMypageBinding::inflate,
) {
    override val viewModel : MypageViewModel by viewModels()

    private enum class OutLinkType(val label: String) {
        GITHUB("Github"),
        LINKEDIN("LinkedIn"),
        BLOG("Blog")
    }
    
    val logoutDialogModel = UMypageDialogModel(
        title = "로그아웃",
        content = "정말 로그아웃을 하시겠습니까?",
        isTwoButton = true,
        positiveText = "로그아웃",
        negativeText = "취소"
    )

    val deleteUserDialogModel = UMypageDialogModel(
        title = "계정 삭제",
        content = "계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다. 정말 삭제하시겠습니까?",
        isTwoButton = true,
        positiveText = "삭제",
        negativeText = "취소"
    )
    

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
                    //이미지 변화
                    binding.mypageCircleimvProfile.load(state.userInfo.profileImageLink) {
                        crossfade(true)
                        placeholder(R.drawable.ic_profile_default)
                        error(R.drawable.ic_profile_default)
                    }
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
                val url = viewModel.uiState.value.githubUrl

                handleOutLink(url, OutLinkType.GITHUB)


            }
            is MypageFragmentEvent.NavigateToBlog -> {
                val url = viewModel.uiState.value.blogUrl

                handleOutLink(url, OutLinkType.BLOG)


            }
            is MypageFragmentEvent.NavigateToLinkedin -> {
                val url = viewModel.uiState.value.linkedinUrl

                handleOutLink(url, OutLinkType.LINKEDIN)

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
                openPermissionPage()
            }

            is MypageFragmentEvent.NavigateToSettingLocation -> {
                openPermissionPage()
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
                //1. 다이얼로그로 체크
                val dialog = UMypageDialog(logoutDialogModel) {
                    /**TODO 로그아웃 로직 생성**/
                }

                dialog.show(parentFragmentManager, "MyPageDialog")

            }

            is MypageFragmentEvent.DeleteUser -> {
                //1. 다이얼로그로 체크
                val dialog = UMypageDialog(deleteUserDialogModel) {
                    /**TODO 회원 탈퇴 로직 생성**/
                }

                dialog.show(parentFragmentManager, "MyPageDialog")
                
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

    //outLink 3종 다이얼로그 포멧 만들기
    private fun setDialogContent(type : OutLinkType) : UMypageDialogModel {
        var name = ""
        if(type == OutLinkType.GITHUB){name = "Github를"}
        else if(type == OutLinkType.LINKEDIN){name = "LinkedIn을"}
        else if(type == OutLinkType.BLOG){name = "Blog를"}
        
        return UMypageDialogModel(
            title = "${name} 열 수 없어요.",
            content = "아직 등록된 링크가 없습니다. 프로필에서 링크를 추가해 주세요.",
            isTwoButton = false,
            confirmText = "확인"
        )
    
    }

    // url 여부와 타입에 따라 다이얼로그를 열 지 아니면, 실행할지 로직
    private fun handleOutLink(url: String, type: OutLinkType) {
        val model = setDialogContent(type)

        if(url == ""){
            val dialog = UMypageDialog(model){/**NO LOGIC**/}
            dialog.show(parentFragmentManager, "MyPageDialog")
        }
        else{
            openWebpage(url)
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

    //앱 권한 페이지로 이동(설정 페이지)
    private fun openPermissionPage() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                // 현재 패키지 정보를 URI 데이터로 삽입
                data = Uri.fromParts("package", requireContext().packageName, null)
                // 기존 화면 흐름과 분리하여 새로운 태스크로 실행
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {

        }

    }


    override fun onResume() {
        super.onResume()
        viewModel.getUserInfo()
    }



}