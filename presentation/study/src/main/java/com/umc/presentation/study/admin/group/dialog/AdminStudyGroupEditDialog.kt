package com.umc.presentation.study.admin.group.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold

@Composable
fun AdminStudyGroupEditDialog(
    groupName: String,
    selectedPart: String,
    canConfirm: Boolean,
    onGroupNameChanged: (String) -> Unit,
    onPartChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val parts = listOf("Plan", "Design", "Web", "Android", "iOS", "Spring", "Node.js")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(neutral000(), RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            UText(
                text = "그룹 정보 수정",
                style = SubheadlineBold,
                color = neutral900(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(8.dp))

            UText(
                text = "스터디 그룹의 기본 정보를 변경합니다.",
                style = Caption1,
                color = neutral500(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(22.dp))

            UText(
                text = "그룹 이름",
                style = Caption1Bold,
                color = neutral800()
            )

            Spacer(Modifier.height(8.dp))

            BasicTextField(
                value = groupName,
                onValueChange = onGroupNameChanged,
                textStyle = Body.copy(color = neutral800()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(neutral000(), RoundedCornerShape(8.dp))
                    .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (groupName.isBlank()) {
                            UText(
                                text = "예: React 실습 A팀",
                                style = Body,
                                color = neutral400()
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            UText(
                text = "소속 파트",
                style = Caption1Bold,
                color = neutral800()
            )

            Spacer(Modifier.height(8.dp))

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .onGloballyPositioned { coordinates ->
                            dropdownWidth = with(density) {
                                coordinates.size.width.toDp()
                            }
                        }
                        .background(neutral000(), RoundedCornerShape(8.dp))
                        .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UText(
                        text = selectedPart,
                        style = Body,
                        color = neutral800(),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_dropdown_down),
                        contentDescription = null,
                        tint = neutral500(),
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(dropdownWidth)
                        .background(neutral000())
                ) {
                    parts.forEach { part ->
                        DropdownMenuItem(
                            text = {
                                UText(
                                    text = part,
                                    style = Body,
                                    color = neutral800()
                                )
                            },
                            onClick = {
                                onPartChanged(part)
                                expanded = false
                            }
                        )
                    }
                }
            }

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
                    backgroundColor = neutral100(),
                    textColor = neutral700(),
                    textStyle = SubheadlineBold,
                    borderWidth = 0.dp,
                    cornerRadius = 8.dp,
                )

                UButton(
                    text = "수정 완료",
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    enabled = canConfirm,
                    backgroundColor = if (canConfirm) primary500() else neutral200(),
                    textColor = if (canConfirm) neutral000() else neutral400(),
                    textStyle = SubheadlineBold,
                    cornerRadius = 8.dp,
                )
            }
        }
    }
}