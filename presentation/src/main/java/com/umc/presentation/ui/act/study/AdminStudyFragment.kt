package com.umc.presentation.ui.act.study

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminStudyBinding
import com.umc.presentation.ui.act.study.group.AdminStudyGroupFragment
import com.umc.presentation.ui.act.study.submit.AdminActStudySubmitFragment
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController
import com.umc.presentation.R


@AndroidEntryPoint
class AdminStudyFragment :
    BaseFragment<FragmentAdminStudyBinding, AdminStudyState, AdminStudyEvent, AdminStudyViewModel>(
        FragmentAdminStudyBinding::inflate
    ) {

    override val viewModel: AdminStudyViewModel by viewModels()

    private var selectedTab: Tab = Tab.GROUP

    enum class Tab { SUBMIT, GROUP }

    override fun initView() {

        val actHandle = findNavController()
            .getBackStackEntry(R.id.activityManagementFragment)
            .savedStateHandle

        actHandle.getLiveData<String>("ADMIN_STUDY_TARGET_TAB")
            .observe(viewLifecycleOwner) { tab ->
                if (tab == "GROUP") showTab(Tab.GROUP)
                else showTab(Tab.GROUP)
                actHandle.remove<String>("ADMIN_STUDY_TARGET_TAB")
            }

        // 초기 탭
        if (childFragmentManager.findFragmentById(binding.fcvAdminStudyContainer.id) == null) {
            showTab(Tab.GROUP, initial = true)
        } else {
            // 이미 떠있는 경우에도 UI만 한번 맞춰주기
            //applyTabUi(selectedTab)
        }

//        binding.btnTabSubmit.setOnClickListener {
//            if (selectedTab != Tab.SUBMIT) showTab(Tab.SUBMIT)
//        }
//
//        binding.btnTabGroup.setOnClickListener {
//            if (selectedTab != Tab.GROUP) showTab(Tab.GROUP)
//        }
    }

    private fun showTab(tab: Tab, initial: Boolean = false) {
        selectedTab = tab

        val fragment = when (tab) {
            Tab.SUBMIT -> AdminActStudySubmitFragment()
            Tab.GROUP -> AdminStudyGroupFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(binding.fcvAdminStudyContainer.id, fragment)
            .commit()

        //applyTabUi(tab)
    }

//    private fun applyTabUi(tab: Tab) {
//
//        val active = com.umc.presentation.R.color.neutral900
//        val inactive = com.umc.presentation.R.color.neutral500
//
//        binding.btnTabSubmit.setTextColor(requireContext().getColor(if (tab == Tab.SUBMIT) active else inactive))
//        binding.btnTabGroup.setTextColor(requireContext().getColor(if (tab == Tab.GROUP) active else inactive))
//        val lpStart = binding.glIndicatorStart.layoutParams as ConstraintLayout.LayoutParams
//        val lpEnd = binding.glIndicatorEnd.layoutParams as ConstraintLayout.LayoutParams
//
//        if (tab == Tab.SUBMIT) {
//            lpStart.guidePercent = 0.0f
//            lpEnd.guidePercent = 0.5f
//        } else {
//            lpStart.guidePercent = 0.5f
//            lpEnd.guidePercent = 1.0f
//        }
//
//        binding.glIndicatorStart.layoutParams = lpStart
//        binding.glIndicatorEnd.layoutParams = lpEnd
//
//
//        binding.root.requestLayout()
//    }

    override fun initStates() {}
}
