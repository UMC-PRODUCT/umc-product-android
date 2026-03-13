package com.umc.presentation.component


data class UCheckDialogModel(
    val title: String,
    val subtitle: String,
    val positiveText: String,
    val isWriteMode: Boolean,
    val reason: String? = null
)