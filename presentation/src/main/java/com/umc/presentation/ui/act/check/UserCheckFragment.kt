package com.umc.presentation.ui.act.check

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserCheckBinding

class UserCheckFragment : BaseFragment<FragmentUserCheckBinding, Nothing, Nothing, Nothing>(
    FragmentUserCheckBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}
