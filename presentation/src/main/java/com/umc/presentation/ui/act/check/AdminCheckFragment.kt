package com.umc.presentation.ui.act.check

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminCheckBinding

class AdminCheckFragment : BaseFragment<FragmentAdminCheckBinding, Nothing, Nothing, Nothing>(
    FragmentAdminCheckBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}
