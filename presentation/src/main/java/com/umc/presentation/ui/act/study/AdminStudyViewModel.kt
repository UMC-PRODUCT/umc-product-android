package com.umc.presentation.ui.act.study

import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminStudyViewModel @Inject constructor() :
    BaseViewModel<AdminStudyState, AdminStudyEvent>(AdminStudyState)