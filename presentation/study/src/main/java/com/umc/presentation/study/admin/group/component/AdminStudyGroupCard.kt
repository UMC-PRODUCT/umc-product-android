package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.presentation.study.admin.group.AdminStudyGroupItemUiModel

@Composable
fun AdminStudyGroupCard(
    item: AdminStudyGroupItemUiModel,
    isSettingOpen: Boolean,
    onSettingClick: () -> Unit,
    onDismissSetting: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onAddMemberClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(neutral000(), RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        UText(
            text = item.title,
            style = HeadlineBold,
            color = neutral800(),
        )

        UText(
            text = "${item.createdAtText}  |  멤버 ${item.memberCount}명",
            style = Body,
            color = neutral600(),
            modifier = Modifier.padding(top = 4.dp),
        )

        UText(
            text = "담당 파트장 ${item.leaderName}",
            style = Body,
            color = neutral800(),
            modifier = Modifier.padding(top = 12.dp),
        )

        UText(
            text = "스터디원 ${item.members.joinToString { it.name }}",
            style = Body,
            color = neutral600(),
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}