package com.umc.presentation.ui.mypage.mypost

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMypostBinding
import com.umc.presentation.ui.community.CommunityFragmentDirections
import com.umc.presentation.ui.community.adapter.ContentAdapter
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
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

    
    //각 아이템 클릭 시 로직
    override fun onItemClicked(item: ContentItem) {
        /**TODO. 이동 로직 작성하기**/
        val action = MypostFragmentDirections.actionMypostToPostDetail(
            postId = item.postId
        )
        findNavController().navigate(action)
    }

    override fun initView() {
        

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //텍스트 정의
        showType = args.showType
        binding.mypostTvTitle.text = when (showType) {
            "MYPOST" -> "내가 쓴 글"
            "MYCOMMENT" -> "댓글 단 글"
            "MYSCRAP" -> "스크랩"
            else -> showType
        }
        viewModel.initShowType(showType)

        //어댑터 정의 및 연결
        myContentAdapter = ContentAdapter(this)
        binding.mypostRcv.apply {
            adapter = myContentAdapter

            //무한 스크롤 로직
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // 로딩 중이 아닐 때 바닥에서 2번째 아이템 근처면 다음 데이터 로드
                    if (!viewModel.uiState.value.isPageLoading && lastVisibleItem >= totalItemCount - 2) {
                        viewModel.settingPost(isRefresh = false)
                    }
                }
            })
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