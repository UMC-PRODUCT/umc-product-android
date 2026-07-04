package com.umc.presentation.study.admin.group.dialog

import androidx.compose.runtime.Composable

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
}