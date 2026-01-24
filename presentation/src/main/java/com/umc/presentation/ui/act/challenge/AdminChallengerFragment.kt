package com.umc.presentation.ui.act.challenge

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminChallengerBinding

class AdminChallengerFragment : BaseFragment<FragmentAdminChallengerBinding, Nothing, Nothing, Nothing>(
    FragmentAdminChallengerBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}