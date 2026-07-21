package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.presentation.study.admin.group.AdminStudyGroupMemberUiModel

@Composable
fun AdminStudyGroupMemberChip(
    member: AdminStudyGroupMemberUiModel,
) {
    Row(
        modifier = Modifier
            .background(grey000(), CircleShape)
            .border(1.dp, grey200(), CircleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(R.drawable.ic_profile_default),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp)
        )


        Spacer(Modifier.width(4.dp))

        UText(
            text = member.name,
            style = Caption1Bold,
            color = grey800()
        )
    }
}