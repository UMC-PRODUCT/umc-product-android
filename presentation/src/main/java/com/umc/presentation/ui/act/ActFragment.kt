package com.umc.presentation.ui.act

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentActBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActFragment : BaseFragment<FragmentActBinding, ActViewModel.ActivityManagementUiState, ActViewModel.ActivityManagementEvent, ActViewModel>(
    FragmentActBinding::inflate
) {
    override val viewModel: ActViewModel by viewModels()
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var currentAdapter: ActViewPagerAdapter? = null

    override fun initView() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.switchAdmin.setOnCheckedChangeListener { _, isChecked ->
            viewModel.switchAdminMode(isChecked)
        }
    }

    override fun initStates() {
        super.initStates()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isAdminMode.collect { isAdmin ->
                    setupViewPager(isAdmin)
                }
            }
        }
    }

    private fun setupViewPager(isAdmin: Boolean) {
        // 기존 어댑터가 있고 모드가 같으면 재생성하지 않음
        if (currentAdapter?.isAdmin == isAdmin) {
            return
        }

        // 어댑터 설정
        val adapter = ActViewPagerAdapter(this, isAdmin)
        currentAdapter = adapter
        binding.viewPager.adapter = adapter

        // 탭 레이아웃 연결
        tabLayoutMediator?.detach()
        tabLayoutMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (isAdmin) {
                when (position) {
                    0 -> getString(R.string.tab_attendance_admin)
                    1 -> getString(R.string.tab_study_admin)
                    2 -> getString(R.string.tab_challenge_admin)
                    else -> ""
                }
            } else {
                when (position) {
                    0 -> getString(R.string.tab_attendance_user)
                    1 -> getString(R.string.tab_study_user)
                    2 -> getString(R.string.tab_challenge_user)
                    else -> ""
                }
            }
        }.apply { attach() }
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        binding.viewPager.adapter = null
        currentAdapter = null
        super.onDestroyView()
    }
}