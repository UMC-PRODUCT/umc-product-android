package com.umc.presentation.ui.community.top


import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentTopPostBinding
import com.umc.presentation.ui.community.adapter.ShowCategoryAdapter
import com.umc.presentation.ui.community.adapter.TrophyDetailAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue


@AndroidEntryPoint
class TopPostFragment : BaseFragment<FragmentTopPostBinding, TopPostFragmentUiState, TopPostFragmentEvent, TopPostViewModel>(
    FragmentTopPostBinding::inflate
) {

    override val viewModel : TopPostViewModel by viewModels()

    //주차별 선택 어댑터
    private lateinit var weekAdapter : ShowCategoryAdapter

    //명예의전당 본문 어댑터
    private lateinit var trophyAdapter : TrophyDetailAdapter

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //주차 어댑터 설정
        setWeekAdapter()
        //본문 어댑터
        setTrophyAdapter()
        
        //뷰모델에서 불러오기(초기는 null)
        viewModel.fetchTrophies(1, null, null)
        

    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    weekAdapter.submitList(state.weekList)
                    trophyAdapter.submitList(state.uiTrophyList)
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

    //명예의전당 본문 recyclerview 초기화
    private fun setTrophyAdapter(){
        trophyAdapter = TrophyDetailAdapter { item ->
            //터치한 링크로 이동하는 로직
            openWebpage(item.url)
        }
        binding.topRcvCotent.apply {
            adapter = trophyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    //주차별 recyclerview 초기화
    private fun setWeekAdapter(){
        viewModel.initWeekList() //먼저 초기화

        weekAdapter = ShowCategoryAdapter { category ->
            viewModel.handleSelectWeek(category)
        }
        binding.topRcvWeek.apply {
            adapter = weekAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            itemAnimator = null
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

