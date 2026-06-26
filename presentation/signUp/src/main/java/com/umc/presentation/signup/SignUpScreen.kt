package com.umc.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import com.umc.component.theme.primary700
import com.umc.component.theme.success500
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.school.SchoolInfo
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpRoute(
    viewModel: SignUpViewModel = hiltViewModel(),
    oAuthVerificationToken: String,
    navigateToBack: () -> Unit = {},
    navigateToPermission: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSchoolBottomSheet by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    var toastData by remember { mutableStateOf<UToastData?>(null) }
    var focusCodeField by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.setOAuthVerificationToken(oAuthVerificationToken)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SignUpEvent.MoveToBack -> navigateToBack()
                is SignUpEvent.MoveToPermissionEvent -> navigateToPermission()
                is SignUpEvent.ShowSchoolBottomSheet -> showSchoolBottomSheet = true
                is SignUpEvent.ShowVerifyToast ->
                    toastData = UToastData(AppStrings.SIGN_UP_EMAIL_VERIFY_SENT, UToastState.CHECK)
                is SignUpEvent.ShowVerifyCompleteToast ->
                    toastData = UToastData(AppStrings.SIGN_UP_EMAIL_VERIFY_COMPLETE, UToastState.CHECK)
                is SignUpEvent.ShowVerifyErrorToast ->
                    toastData = UToastData(AppStrings.SIGN_UP_EMAIL_VERIFY_ERROR, UToastState.ERROR)
                is SignUpEvent.FocusVerifyCodeField -> focusCodeField = true
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
            onEmailChanged = viewModel::onEmailChanged,
            onClickVerify = viewModel::onClickVerify,
            onCodeChanged = viewModel::onCodeChanged,
            onClickConfirm = viewModel::onClickConfirm,
            onClickNext = viewModel::register,
            focusCodeField = focusCodeField,
            onCodeFieldFocused = { focusCodeField = false },
        )

        if (showSchoolBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSchoolBottomSheet = false },
                sheetState = sheetState,
                containerColor = neutral000(),
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

        UToastHost(
            data = toastData,
            onDismiss = { toastData = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
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
            color = neutral800(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.SIGN_UP_SELECT_SCHOOL_HINT,
            style = UmcTypographyTokens.Subheadline,
            color = neutral600(),
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
            .background(neutral000())
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = school.schoolName,
            style = UmcTypographyTokens.Subheadline,
            color = if (isSelected) primary500() else neutral800(),
            modifier = Modifier.weight(1f),
        )

        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_white),
                contentDescription = null,
                tint = primary500(),
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
    onEmailChanged: (String) -> Unit = {},
    onClickVerify: () -> Unit = {},
    onCodeChanged: (String) -> Unit = {},
    onClickConfirm: () -> Unit = {},
    onClickNext: () -> Unit = {},
    focusCodeField: Boolean = false,
    onCodeFieldFocused: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val codeFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(focusCodeField) {
        if (focusCodeField) {
            codeFieldFocusRequester.requestFocus()
            onCodeFieldFocused()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .padding(12.dp)
                    .clickable { onClickBack() },
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = Color.Unspecified
            )

            UText(
                text = AppStrings.SIGN_UP,
                style = UmcTypographyTokens.Title1Bold,
                color = neutral800(),
                modifier = Modifier.padding(start = 24.dp, top = 16.dp)
            )

            UText(
                text = AppStrings.SIGN_UP_SUB_TITLE,
                style = UmcTypographyTokens.Body,
                color = neutral600(),
                modifier = Modifier.padding(start = 24.dp, top = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 48.dp, end = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    FieldLabel(text = AppStrings.NAME)
                    Spacer(modifier = Modifier.height(8.dp))
                    UTextField(
                        value = uiState.name,
                        onValueChange = onNameChanged,
                        placeholder = AppStrings.SIGN_UP_NAME_PLACEHOLDER,
                        textStyle = UmcTypographyTokens.Subheadline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                ) {
                    FieldLabel(text = AppStrings.NICKNAME)
                    Spacer(modifier = Modifier.height(8.dp))
                    UTextField(
                        value = uiState.nickname,
                        onValueChange = onNicknameChanged,
                        placeholder = AppStrings.SIGN_UP_NICKNAME_PLACEHOLDER,
                        textStyle = UmcTypographyTokens.Subheadline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel(
                text = AppStrings.SCHOOL,
                modifier = Modifier.padding(start = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
                    .clickable { onClickSchool() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                UText(
                    text = uiState.school.schoolName.ifEmpty { AppStrings.SIGN_UP_SELECT_SCHOOL_PLACEHOLDER },
                    style = UmcTypographyTokens.Subheadline,
                    color = if (uiState.school.schoolName.isEmpty()) neutral300() else neutral800(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel(
                text = AppStrings.EMAIL,
                modifier = Modifier.padding(start = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val isEmailError = uiState.verifyType == EmailVerifyType.ERROR

                UTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    placeholder = AppStrings.SIGN_UP_EMAIL_PLACEHOLDER,
                    textStyle = UmcTypographyTokens.Subheadline,
                    textColor = if (isEmailError) danger500() else neutral800(),
                    backgroundColor = if (isEmailError) danger100() else neutral000(),
                    strokeColor = if (isEmailError) danger500() else neutral300(),
                    focusStrokeColor = if (isEmailError) danger500() else primary500(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(16.dp))

                val verifyBgColor = if (
                    uiState.email.isEmpty()
                    || uiState.verifyType == EmailVerifyType.ERROR
                    || uiState.verifyType == EmailVerifyType.REQUEST
                    || uiState.verifyType == EmailVerifyType.VERIFY
                ) neutral300() else primary500()
                val isVerifyButtonEnabled = (uiState.email.isNotEmpty()
                        && uiState.verifyType != EmailVerifyType.ERROR)
                        || uiState.verifyType == EmailVerifyType.REQUEST
                        || uiState.verifyType == EmailVerifyType.VERIFY

                UButton(
                    text = AppStrings.SIGN_UP_VERIFY_REQUEST,
                    onClick = onClickVerify,
                    enabled = isVerifyButtonEnabled,
                    backgroundColor = verifyBgColor,
                    pressedColor = primary700(),
                    textColor = neutral000(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp),
                    textStyle = UmcTypographyTokens.Footnote,
                )
            }

            if (uiState.verifyType == EmailVerifyType.ERROR) {
                UText(
                    text = AppStrings.SIGN_UP_ERROR_EMAIL,
                    style = UmcTypographyTokens.Footnote,
                    color = danger500(),
                    modifier = Modifier.padding(start = 24.dp, top = 4.dp)
                )
            }

            if (uiState.verifyType == EmailVerifyType.REQUEST || uiState.verifyType == EmailVerifyType.VERIFY) {
                val isVerified = uiState.verifyType == EmailVerifyType.VERIFY
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UTextField(
                        value = uiState.code,
                        onValueChange = onCodeChanged,
                        enabled = !isVerified,
                        placeholder = AppStrings.SIGN_UP_VERIFY_CODE_PLACEHOLDER,
                        textStyle = UmcTypographyTokens.Subheadline,
                        textColor = if (isVerified) neutral600() else neutral800(),
                        backgroundColor = if (isVerified) neutral100() else neutral000(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(codeFieldFocusRequester),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    UButton(
                        text = AppStrings.SIGN_UP_VERIFY_CONFIRM,
                        onClick = onClickConfirm,
                        enabled = !isVerified,
                        backgroundColor = if (isVerified) neutral100() else neutral000(),
                        textColor = if (isVerified) neutral300() else success500(),
                        textStyle = UmcTypographyTokens.Footnote,
                        borderWidth = 1.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp),
                        borderColor = if (isVerified) neutral300() else success500(),
                    )
                }
            }
        }

        UButton(
            text = AppStrings.NEXT,
            onClick = onClickNext,
            enabled = uiState.enableNextButton,
            backgroundColor = if (uiState.enableNextButton) primary500() else neutral300(),
            pressedColor = primary700(),
            textColor = neutral000(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
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
            color = neutral800(),
        )
        UText(
            text = " *",
            style = UmcTypographyTokens.HeadlineBold,
            color = danger500(),
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
private fun SignUpScreenVerifyRequestPreview() {
    SignUpScreen(
        uiState = SignUpState(
            email = "test@univ.ac.kr",
            verifyType = EmailVerifyType.REQUEST
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenVerifyCompletePreview() {
    SignUpScreen(
        uiState = SignUpState(
            name = "홍길동",
            nickname = "길동이",
            email = "test@univ.ac.kr",
            verifyType = EmailVerifyType.VERIFY,
            school = SchoolInfo(schoolId = 1, schoolName = "서울대학교"),
        )
    )
}
