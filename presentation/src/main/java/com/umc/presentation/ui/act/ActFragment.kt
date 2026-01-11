package com.umc.presentation.ui.act

import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentActBinding
import com.umc.presentation.ui.act.adapter.ActViewPagerAdapter
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

        binding.switchAdmin.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAdminMode(isChecked)
        }
    }

    override fun initStates() {
        super.initStates()
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    setupViewPager(state.isAdmin)
                    // TODO: 그 외 다른 상태 관리
                }
            }
        }
    }

    private fun setupViewPager(isAdmin: Boolean) {
        if (currentAdapter?.isAdmin == isAdmin) return

        val adapter = ActViewPagerAdapter(this, isAdmin)
        currentAdapter = adapter
        binding.viewPager.adapter = adapter

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