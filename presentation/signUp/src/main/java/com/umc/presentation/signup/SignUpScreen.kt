package com.umc.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo700
import com.umc.component.theme.red500
import com.umc.domain.model.enums.SignUpType
import com.umc.domain.model.school.SchoolInfo
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpRoute(
    viewModel: SignUpViewModel = hiltViewModel(),
    signUpType: SignUpType = SignUpType.SOCIAL,
    oAuthVerificationToken: String = "",
    emailVerificationToken: String = "",
    rawPassword: String = "",
    navigateToBack: () -> Unit = {},
    navigateToPermission: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSchoolBottomSheet by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.setArguments(
            signUpType = signUpType,
            oAuthVerificationToken = oAuthVerificationToken,
            emailVerificationToken = emailVerificationToken,
            rawPassword = rawPassword,
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SignUpEvent.MoveToBack -> navigateToBack()
                is SignUpEvent.MoveToPermissionEvent -> navigateToPermission()
                is SignUpEvent.ShowSchoolBottomSheet -> showSchoolBottomSheet = true
                is SignUpEvent.ShowRegisterErrorDialog -> errorDialogMessage = event.message
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SignUpScreen(
            uiState = uiState,
            onClickBack = viewModel::onClickBack,
            onNameChanged = viewModel::onNameChanged,
            onNicknameChanged = viewModel::onNicknameChanged,
            onClickSchool = viewModel::onClickSchool,
            onClickNext = viewModel::register,
        )

        if (showSchoolBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSchoolBottomSheet = false },
                sheetState = sheetState,
                containerColor = grey000(),
            ) {
                SchoolSelectBottomSheetContent(
                    schoolList = uiState.schoolList,
                    selectedSchoolId = uiState.school.schoolId.takeIf { it != -1 }?.toLong(),
                    onSelectSchool = { school ->
                        viewModel.updateSelectSchool(school)
                        showSchoolBottomSheet = false
                    },
                )
            }
        }

        errorDialogMessage?.let { message ->
            UDialog(
                title = message,
                onDismissRequest = { errorDialogMessage = null },
                confirmText = AppStrings.CONFIRM,
                onConfirm = {
                    errorDialogMessage = null
                    navigateToBack()
                },
            )
        }
    }
}

@Composable
private fun SchoolSelectBottomSheetContent(
    schoolList: List<SchoolInfo>,
    selectedSchoolId: Long?,
    onSelectSchool: (SchoolInfo) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        UText(
            text = AppStrings.SIGN_UP_SELECT_SCHOOL_PLACEHOLDER,
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.SIGN_UP_SELECT_SCHOOL_HINT,
            style = UmcTypographyTokens.Subheadline,
            color = grey600(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(367.dp)
        ) {
            items(
                items = schoolList,
                key = { it.schoolId },
            ) { school ->
                val isSelected = school.schoolId.toLong() == selectedSchoolId
                SchoolItem(
                    school = school,
                    isSelected = isSelected,
                    onClick = { onSelectSchool(school) },
                )
            }
        }
    }
}

@Composable
private fun SchoolItem(
    school: SchoolInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(grey000())
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = school.schoolName,
            style = UmcTypographyTokens.Subheadline,
            color = if (isSelected) indigo500() else grey800(),
            modifier = Modifier.weight(1f),
        )

        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_white),
                contentDescription = null,
                tint = indigo500(),
            )
        }
    }
}

@Composable
fun SignUpScreen(
    uiState: SignUpState = SignUpState(),
    onClickBack: () -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    onNicknameChanged: (String) -> Unit = {},
    onClickSchool: () -> Unit = {},
    onClickNext: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 4.dp, top = 8.dp)
                .padding(12.dp)
                .clickable { onClickBack() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.SIGN_UP,
            style = UmcTypographyTokens.Title1Bold,
            color = grey950(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = AppStrings.SIGN_UP_SUB_TITLE,
            style = UmcTypographyTokens.Headline,
            color = grey600(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(text = AppStrings.NAME)

                    Spacer(modifier = Modifier.height(8.dp))

                    UTextField(
                        value = uiState.name,
                        onValueChange = onNameChanged,
                        placeholder = AppStrings.SIGN_UP_NAME_PLACEHOLDER,
                        textStyle = UmcTypographyTokens.Headline,
                        textColor = grey950(),
                        strokeColor = grey200(),
                        focusStrokeColor = grey900(),
                        cornerRadius = 10.dp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(text = AppStrings.NICKNAME)

                    Spacer(modifier = Modifier.height(8.dp))

                    UTextField(
                        value = uiState.nickname,
                        onValueChange = onNicknameChanged,
                        placeholder = AppStrings.SIGN_UP_NICKNAME_PLACEHOLDER,
                        textStyle = UmcTypographyTokens.Headline,
                        textColor = grey950(),
                        strokeColor = grey200(),
                        focusStrokeColor = grey900(),
                        cornerRadius = 10.dp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel(text = AppStrings.SCHOOL)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, grey200(), RoundedCornerShape(10.dp))
                    .clickable { onClickSchool() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UText(
                    text = uiState.school.schoolName.ifEmpty { AppStrings.SIGN_UP_SELECT_SCHOOL_PLACEHOLDER },
                    style = UmcTypographyTokens.Headline,
                    color = if (uiState.school.schoolName.isEmpty()) grey400() else grey950(),
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    tint = grey400(),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            UButton(
                text = AppStrings.NEXT,
                onClick = onClickNext,
                enabled = uiState.enableNextButton,
                backgroundColor = if (uiState.enableNextButton) indigo500() else grey100(),
                pressedColor = indigo700(),
                textColor = if (uiState.enableNextButton) grey000() else grey300(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )
        UText(
            text = " *",
            style = UmcTypographyTokens.HeadlineBold,
            color = red500(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    SignUpScreen()
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenCompletePreview() {
    SignUpScreen(
        uiState = SignUpState(
            name = "홍길동",
            nickname = "길동이",
            school = SchoolInfo(schoolId = 1, schoolName = "가나다대학교"),
        )
    )
}
