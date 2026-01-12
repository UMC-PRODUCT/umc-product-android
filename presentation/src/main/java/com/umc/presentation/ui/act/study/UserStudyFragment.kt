package com.umc.presentation.ui.act.study

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserStudyBinding

class UserStudyFragment : BaseFragment<FragmentUserStudyBinding, Nothing, Nothing, Nothing>(
    FragmentUserStudyBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}
