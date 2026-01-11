package com.umc.presentation.ui.act.study

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminStudyBinding

class AdminStudyFragment : BaseFragment<FragmentAdminStudyBinding, Nothing, Nothing, Nothing>(
    FragmentAdminStudyBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}
