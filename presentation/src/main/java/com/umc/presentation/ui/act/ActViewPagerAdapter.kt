package com.umc.presentation.ui.act

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.umc.presentation.ui.act.challenge.AdminChallengeFragment
import com.umc.presentation.ui.act.challenge.UserChallengerFragment
import com.umc.presentation.ui.act.check.AdminCheckFragment
import com.umc.presentation.ui.act.check.UserCheckFragment
import com.umc.presentation.ui.act.study.AdminStudyFragment
import com.umc.presentation.ui.act.study.UserStudyFragment

class ActViewPagerAdapter(
    fragment: Fragment,
    val isAdmin: Boolean
) : FragmentStateAdapter(fragment) {

    companion object {
        private const val TAB_COUNT = 3

        // 탭 위치 상수
        const val TAB_CHECK = 0
        const val TAB_STUDY = 1
        const val TAB_CHALLENGE = 2
    }

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return if (isAdmin) {
            when (position) {
                TAB_CHECK -> AdminCheckFragment()
                TAB_STUDY -> AdminStudyFragment()
                TAB_CHALLENGE -> AdminChallengeFragment()
                else -> throw IllegalStateException("Invalid position: $position")
            }
        } else {
            when (position) {
                TAB_CHECK -> UserCheckFragment()
                TAB_STUDY -> UserStudyFragment()
                TAB_CHALLENGE -> UserChallengerFragment()
                else -> throw IllegalStateException("Invalid position: $position")
            }
        }
    }
}