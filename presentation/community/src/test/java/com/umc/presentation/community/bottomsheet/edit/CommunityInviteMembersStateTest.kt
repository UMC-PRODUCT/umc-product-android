package com.umc.presentation.community.bottomsheet.edit

import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Assert.*
import org.junit.Test

class CommunityInviteMembersStateTest {
    private fun member(id: Long) = CommunityChallengerUiModel(id, "회원$id", "", "", 11, "Android")

    @Test fun searchOnlyDisplaysMembersWhoHaveNotJoined() {
        val state = CommunityMemberBottomSheetState(
            isSearching = true,
            currentMembers = persistentListOf(member(1)),
            ownerMemberIds = persistentSetOf(2),
            searchResults = persistentListOf(member(1), member(2), member(3)),
        )
        assertEquals(listOf(3L), state.displayedMembers.map { it.memberId })
        assertEquals(listOf(1L), state.copy(isSearching = false).displayedMembers.map { it.memberId })
    }

    @Test fun missingAnExistingMemberFromSelectionCannotEnableADeletion() {
        val state = CommunityMemberBottomSheetState(
            canManage = true, isSearching = true,
            currentMembers = persistentListOf(member(1)),
        )
        assertFalse(state.hasMemberChanges)
        assertFalse(state.isConfirmEnabled)
        assertTrue(state.addedMembers.isEmpty())
    }

    @Test fun onlyNewSelectionsAreInvited() {
        val state = CommunityMemberBottomSheetState(
            canManage = true, isSearching = true,
            currentMembers = persistentListOf(member(1)),
            selectedMembers = persistentListOf(member(1), member(3)),
        )
        assertTrue(state.isConfirmEnabled)
        assertEquals(listOf(3L), state.addedMembers.map { it.memberId })
        assertTrue(state.copy(currentMembers = persistentListOf(member(1), member(3))).addedMembers.isEmpty())
    }
}
