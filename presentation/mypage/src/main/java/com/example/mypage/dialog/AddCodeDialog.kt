package com.example.mypage.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500


/**활동 기록 추가 시 활동 코드를 입력받는 다이얼로그입니다.**/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCodeDialog(
    code: String, //입력 코드
    onCodeChanged: (String) -> Unit, //상태 변경 요청 콜백
    onConfirmClick: () -> Unit, //버튼 클릭 콜백
    onDismissRequest: () -> Unit, //시트 닫기 요청 콜백
){
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = neutral000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = neutral600())
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            UText(
                text = AppStrings.MYPAGE_ADDCODE_TITLE,
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800(),
                modifier = Modifier.padding(top = 28.dp)
            )

            UText(
                text = AppStrings.MYPAGE_ADDCODE_CODE,
                style = UmcTypographyTokens.HeadlineBold,
                color = neutral800(),
                modifier = Modifier.padding(top = 40.dp)
            )

            OutlinedTextField(
                value = code,
                onValueChange = onCodeChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(52.dp),
                placeholder = { Text(
                    AppStrings.MYPAGE_ADDCODE_CONTENT,
                    color = neutral400()
                ) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = neutral300(),
                    focusedBorderColor = primary500()
                )
            )

            Spacer(modifier = Modifier
                .height(32.dp)
            )

            UButton(
                text = AppStrings.MYPAGE_ADDCODE_CONFIRM,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                backgroundColor = primary500(),
                textColor = neutral000(),
                textStyle = UmcTypographyTokens.SubheadlineBold,
                onClick = onConfirmClick
            )
        }
    }
}