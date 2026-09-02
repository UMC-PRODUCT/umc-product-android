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
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo500


/**
 * 마이페이지 활동 기록 추가 시 개별 활동 코드를 입력받는 바텀시트 다이얼로그 컴포저블
 *
 * 유저가 발급받은 코드 문자열을 텍스트 필드(UTextField)로 입력받고,
 * 확인 버튼 클릭 콜백(onConfirmClick)을 통해 상위 뷰모델로 코드를 전달하는 레이아웃 구조를 제공합니다.
 *
 * [주의]
 * 커뮤니티 개편으로 현재 사용되지는 않지만, 추후 사용 가능성이 있기에 남겨둡니다.
 * **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCodeDialog(
    code: String, //입력 코드
    onCodeChanged: (String) -> Unit, //상태 변경 요청 콜백
    onConfirmClick: () -> Unit, //버튼 클릭 콜백
    onDismissRequest: () -> Unit, //시트 닫기 요청 콜백
){
    // 바텀시트 초기 확장 상태를 완전 확장 모드로 지정하는 시트 스태이트
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = grey600())
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
                color = grey800(),
                modifier = Modifier.padding(top = 28.dp)
            )

            UText(
                text = AppStrings.MYPAGE_ADDCODE_CODE,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey800(),
                modifier = Modifier.padding(top = 40.dp)
            )

            UTextField(
                value = code,
                onValueChange = onCodeChanged,
                placeholder = AppStrings.MYPAGE_ADDCODE_CONTENT,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)

            )


            Spacer(modifier = Modifier
                .height(32.dp)
            )

            UButton(
                text = AppStrings.MYPAGE_ADDCODE_CONFIRM,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                backgroundColor = indigo500(),
                textColor = grey000(),
                textStyle = UmcTypographyTokens.SubheadlineBold,
                onClick = onConfirmClick
            )
        }
    }
}