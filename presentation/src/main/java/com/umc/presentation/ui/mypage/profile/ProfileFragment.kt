package com.umc.presentation.ui.mypage.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentProfileBinding
import com.umc.presentation.ui.mypage.MypageViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileFragmentUiState, ProfileFragmentEvent, ProfileViewModel>(
    FragmentProfileBinding::inflate,
) {
    override val viewModel : ProfileViewModel by viewModels()

    //이미지 picker 세팅
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // 갤러리에서 사진을 선택하고 돌아왔을 때 실행
        if (uri != null) {
            // 뷰모델에 선택된 URI 전달
            viewModel.updateProfileImage(uri)
        }
    }
    
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
                    state.profileImageUri?.let {
                        binding.profileCircleimvProfile.setImageURI(it)
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

    override fun handleEvent(event: ProfileFragmentEvent) {
        super.handleEvent(event)

        when(event) {
            //갤러리 열어
            is ProfileFragmentEvent.ClickProfileImage -> {
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            //완료 로직
            is ProfileFragmentEvent.ClickComplete -> {
                /**TODO 서버에서 로직을 가져와야 해요!**/
                val nowGithub = binding.profileTextfieldGithub.getText()
                val nowLinkedin = binding.profileTextfieldLinkedin.getText()
                val nowBlog = binding.profileTextfieldBlog.getText()
                val nowProfileImage = viewModel.uiState.value.profileImageUri
                Log.d("log_mypage", "github : $nowGithub")
                Log.d("log_mypage", "image : $nowProfileImage}")


                //뒤로 가기
                requireActivity().onBackPressedDispatcher.onBackPressed()

            }

            //그냥 뒤로 가기
            is ProfileFragmentEvent.ClickBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }


            else -> {}
        }

    }

}