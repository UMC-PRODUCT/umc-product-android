package com.umc.presentation.ui.act.challenge

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminChallengeBinding

class AdminChallengeFragment : BaseFragment<FragmentAdminChallengeBinding, Nothing, Nothing, Nothing>(
    FragmentAdminChallengeBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}