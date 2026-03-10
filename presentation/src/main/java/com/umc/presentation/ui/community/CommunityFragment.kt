package com.umc.presentation.ui.community

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentCommunityBinding
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
import com.umc.presentation.ui.community.top.TopPostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding, CommunityFragmentUiState, CommunityFragmentEvent, CommunityViewModel>(
    FragmentCommunityBinding::inflate
), ContentItemDelegate {

    override val viewModel: CommunityViewModel by viewModels()

    override fun onItemClicked(item: ContentItem) {
        /**TODO. 이동 로직 작성하기**/

        val action = CommunityFragmentDirections.actionCommunityToPostDetail(
            postId = item.postId
        )
        findNavController().navigate(action)
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //viewPager 설정
        setViewPager()



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

    override fun handleEvent(event: CommunityFragmentEvent) {
        super.handleEvent(event)

        when(event){
            is CommunityFragmentEvent.NavigateSearch -> {
                val action = CommunityFragmentDirections.actionCommunityToPostSearch()
                findNavController().navigate(action)
            }
        }
    }


    private fun setViewPager(){
        val tabTitles = listOf(getString(R.string.all),
            getString(R.string.question),
            "번개모임",
            getString(R.string.community_top)
        )

        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 4
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    //명예의 전당
                    3 -> TopPostFragment()
                    else -> {
                        //각 탭 이동
                        val type = when(position) {
                            0 -> ContentType.ALL
                            1 -> ContentType.QUESTION
                            else -> ContentType.LIGHTNING
                        }
                        CommunityListFragment.newInstance(type)
                    }
                }
            }
        }

        binding.communityViewPager.adapter = pagerAdapter

        //TabLayout과 ViewPager2를 연결하여 스와이프 동기화
        TabLayoutMediator(
            binding.communityTabLayout,
            binding.communityViewPager
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()


    }


}