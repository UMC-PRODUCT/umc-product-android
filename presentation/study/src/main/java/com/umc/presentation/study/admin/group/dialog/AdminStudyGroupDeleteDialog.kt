package com.umc.presentation.study.admin.group.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold

@Composable
fun AdminStudyGroupDeleteDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(grey000(), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null,
                tint = grey500(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clickable { onDismiss() }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(red100(), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error_filled),
                        contentDescription = null,
                        tint = red500(),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.height(22.dp))

                UText(
                    text = "그룹을 삭제하시겠습니까?",
                    style = SubheadlineBold,
                    color = grey900()
                )

                Spacer(Modifier.height(8.dp))

                UText(
                    text = "삭제된 스터디 그룹 정보는 복구할 수 없으며,\n연결된 모든 스터디 데이터가 삭제됩니다.",
                    style = Caption1,
                    color = grey600()
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = grey000(),
                        textColor = grey700(),
                        textStyle = SubheadlineBold,
                        borderWidth = 1.dp,
                        borderColor = grey200(),
                        cornerRadius = 8.dp,
                    )

                    UButton(
                        text = "삭제하기",
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = red100(),
                        textColor = red500(),
                        textStyle = SubheadlineBold,
                        borderWidth = 0.dp,
                        cornerRadius = 8.dp,
                    )
                }
            }
        }
    }
}