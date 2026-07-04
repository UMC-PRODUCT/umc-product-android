package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold

@Composable
fun AdminStudyGroupSettingPopup(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(150.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(neutral000(), RoundedCornerShape(12.dp))
            .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp)
    ) {
        SettingPopupItem(
            text = "정보 수정",
            iconRes = R.drawable.ic_edit,
            textColor = neutral800(),
            iconTint = neutral700(),
            onClick = {
                onEditClick()
                onDismiss()
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(neutral100())
        )

        SettingPopupItem(
            text = "그룹 삭제",
            iconRes = R.drawable.ic_trash_can,
            textColor = danger500(),
            iconTint = danger500(),
            onClick = {
                onDeleteClick()
                onDismiss()
            }
        )
    }
}

@Composable
private fun SettingPopupItem(
    text: String,
    iconRes: Int,
    textColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )

        Spacer(Modifier.width(8.dp))

        UText(
            text = text,
            style = Caption1Bold,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_next),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(14.dp)
        )
    }
}