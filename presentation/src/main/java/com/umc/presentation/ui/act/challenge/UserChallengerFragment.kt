package com.umc.presentation.ui.act.challenge

import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserChallengeBinding

class UserChallengerFragment : BaseFragment<FragmentUserChallengeBinding, Nothing, Nothing, Nothing>(
    FragmentUserChallengeBinding::inflate
) {
    override val viewModel: Nothing
        get() = throw IllegalStateException("ViewModel is not used in this Fragment.")

    override fun initView() {}
    override fun initStates() {}
}
