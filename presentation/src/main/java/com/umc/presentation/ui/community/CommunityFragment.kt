package com.umc.presentation.ui.community

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentCommunityBinding
import com.umc.presentation.ui.community.adapter.ContentAdapter
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding, CommunityFragmentUiState, CommunityFragmentEvent, CommunityViewModel>(
    FragmentCommunityBinding::inflate
), ContentItemDelegate {

    override val viewModel: CommunityViewModel by viewModels()

    private lateinit var myContentAdapter : ContentAdapter

    override fun onItemClicked(item: ContentItem) {
        /**TODO. 이동 로직 작성하기**/

        val action = CommunityFragmentDirections.actionCommunityToPostDetail()
        findNavController().navigate(action)
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //탭 초기화 및 설정
        setTabLayout()

        //게시글 default 필터링
        viewModel.filterContents()

        //스위치 로직
        /**
        binding.communitySwitchRecruit.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRecruit(isChecked)
        }
        **/

        //어댑터 정의 및 연결
        myContentAdapter = ContentAdapter(this)
        binding.communityRcv.apply {
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

    override fun handleEvent(event: CommunityFragmentEvent) {
        super.handleEvent(event)

        when(event){
            is CommunityFragmentEvent.NavigateWrite -> {
                val action = CommunityFragmentDirections.actionCommunityToPostWrite()
                findNavController().navigate(action)
            }
            is CommunityFragmentEvent.NavigateSearch -> {
                val action = CommunityFragmentDirections.actionCommunityToPostSearch()
                findNavController().navigate(action)
            }
        }
    }



    //탭 세팅 초기화
    private fun setTabLayout(){
        binding.communityTabLayout.apply {
            removeAllTabs()

            val tabTitles = listOf(
                    getString(R.string.all),
                    getString(R.string.question),
                    "번개모임",
                    getString(R.string.community_top),
                )

            tabTitles.forEach { title ->
                addTab(newTab().setText(title))
            }

            //각 탭을 터치했을 때, Uistate의 값을 update
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val position = tab?.position ?: return
                    
                    //만약 탭 추가나 변경 시 CommunityType 변경 후 적용
                    when(position){
                        0, 1, 2 -> {
                            //명예의 전당 탭 없애고 글쓰기 탭 보이기
                            binding.communityFragmentContainerWrite.visibility = View.VISIBLE
                            binding.communityFragmentContainerTop.visibility = View.GONE

                            //타입 빌딩
                            val type = when(position){
                                0 -> ContentType.ALL
                                1 -> ContentType.QUESTION
                                2 -> ContentType.LIGHTNING
                                else -> ContentType.ALL
                            }
                            //게시글 필터링
                            viewModel.setNowTab(type)
                        }
                        3-> {
                            //명예의 전당 탭 보이고 글쓰기 탭 없애기
                            binding.communityFragmentContainerWrite.visibility = View.GONE
                            binding.communityFragmentContainerTop.visibility = View.VISIBLE
                            
                            //일단 탭 변경
                            viewModel.setNowTab(ContentType.TOP)

                            /**TODO. 보이기 로직**/
                            //childFragmentManager.beginTransaction()
                            //    .replace(R.id.community_fragmentContainer_top, )
                            //    .commit()
                        }
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {

                }

                override fun onTabReselected(p0: TabLayout.Tab?) {

                }
            })
        }

    }


}