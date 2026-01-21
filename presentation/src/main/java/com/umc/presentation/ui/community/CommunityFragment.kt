package com.umc.presentation.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.umc.domain.model.enums.CommunityType
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentCommunityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding, CommunityFragmentUiState, CommunityFragmentEvent, CommunityViewModel>(
    FragmentCommunityBinding::inflate
) {

    override val viewModel: CommunityViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //탭 초기화 및 설정
        setTabLayout()

        binding.communitySwitchRecruit.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRecruit(isChecked)
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

    override fun handleEvent(event: CommunityFragmentEvent) {
        super.handleEvent(event)
    }

    private fun setTabLayout(){
        binding.communityTabLayout.apply {
            removeAllTabs()

            val tabTitles = listOf(
                    getString(R.string.all),
                    getString(R.string.soft),
                    getString(R.string.hard),
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
                        0-> viewModel.setNowTab(CommunityType.ALL)
                        1-> viewModel.setNowTab(CommunityType.SOFT)
                        2-> viewModel.setNowTab(CommunityType.HARD)
                        3-> viewModel.setNowTab(CommunityType.TOP)
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