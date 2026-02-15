package com.umc.presentation.ui.mypage.mypost

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMypostBinding
import com.umc.presentation.ui.community.adapter.ContentAdapter
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
import com.umc.presentation.ui.home.PlanAddFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class MypostFragment : BaseFragment<FragmentMypostBinding, MypostFragmentUiState, MypostFragmentEvent, MypostViewModel>(FragmentMypostBinding::inflate,
), ContentItemDelegate {


    override val viewModel : MypostViewModel by viewModels()

    private lateinit var myContentAdapter : ContentAdapter

    private val args: MypostFragmentArgs by navArgs()
    /**
     * ShowType
     * MYPOST = 내가 쓴 글
     * MYCOMMENT = 댓글 단 글
     * MYSCRAP = 스크랩
     * **/
    private var showType : String = ""

    
    override fun onItemClicked(item: ContentItem) {
        /**TODO. 이동 로직 작성하기**/
    }

    override fun initView() {

        showType = args.showType


        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        if(showType == "MYPOST"){
            binding.mypostTvTitle.text = "내가 쓴 글"
            viewModel.settingPost(showType)
        }
        else if(showType == "MYCOMMENT"){
            binding.mypostTvTitle.text = "댓글 단 글"
            viewModel.settingPost(showType)
        }
        else if(showType == "MYSCRAP"){
            binding.mypostTvTitle.text = "스크랩"
            viewModel.settingPost(showType)
        }
        else{
            viewModel.settingPost("")
        }



        //어댑터 정의 및 연결
        myContentAdapter = ContentAdapter(this)
        binding.mypostRcv.apply {
            adapter = myContentAdapter
        }

    }



    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    myContentAdapter.submitList(state.nowContents)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    override fun handleEvent(event: MypostFragmentEvent) {
        super.handleEvent(event)

        when(event){
            is MypostFragmentEvent.ClickBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            is MypostFragmentEvent.ShowErrorToast -> {
                Toast.makeText(requireContext(), event.errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> {}

        }
    }

}