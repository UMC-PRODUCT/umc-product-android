package com.umc.presentation.ui.mypage.mypost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.domain.model.mypage.MyContentItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMypostBinding
import com.umc.presentation.ui.mypage.adapter.MyContentAdapter
import com.umc.presentation.ui.mypage.adapter.MyContentItemDelegate
import kotlinx.coroutines.launch
import kotlin.getValue


class MypostFragment : BaseFragment<FragmentMypostBinding, MypostFragmentUiState, MypostFragmentEvent, MypostViewModel>(FragmentMypostBinding::inflate,
), MyContentItemDelegate {

    override val viewModel : MypostViewModel by viewModels()

    private lateinit var myContentAdapter : MyContentAdapter

    override fun onItemClicked(item: MyContentItem) {
        /**TODO. 이동 로직 작성하기**/
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //어댑터 정의 및 연결
        myContentAdapter = MyContentAdapter(this)
        binding.mypostRcv.apply {
            adapter = myContentAdapter
        }

    }


    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    myContentAdapter.submitList(state.tmpData)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->

                }
            }
        }



    }

}