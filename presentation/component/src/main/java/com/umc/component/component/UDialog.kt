package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800

/**
 * UMC 공용 다이얼로그 컴포넌트. UMypageDialog(XML)의 Compose 마이그레이션 버전.
 *
 * 단일 버튼(isTwoButton=false)과 이중 버튼(isTwoButton=true) 두 가지 모드를 지원.
 *
 * @param title 다이얼로그 제목 (필수)
 * @param onDismissRequest 배경 터치 등으로 다이얼로그가 닫힐 때 호출
 * @param modifier 외부 Column에 적용할 Modifier
 * @param content 부제목/설명 텍스트. 비어있으면 표시하지 않음
 * @param isTwoButton true이면 부정+긍정 2버튼 모드, false이면 단일 확인 버튼 모드
 * @param confirmText 단일 버튼 모드의 버튼 텍스트. 기본값 "확인"
 * @param onConfirm 단일 버튼 클릭 시 호출. 기본값은 onDismissRequest
 * @param negativeText 이중 버튼 모드의 취소 버튼 텍스트. 기본값 "취소"
 * @param positiveText 이중 버튼 모드의 긍정 버튼 텍스트
 * @param onNegative 취소 버튼 클릭 시 호출. 기본값은 onDismissRequest
 * @param onPositive 긍정 버튼 클릭 시 호출
 */

@Composable
fun UDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: String = "",
    isTwoButton: Boolean = false,
    confirmText: String = "확인",
    onConfirm: () -> Unit = onDismissRequest,
    negativeText: String = "취소",
    positiveText: String = "",
    onNegative: () -> Unit = onDismissRequest,
    onPositive: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(neutral000(), RoundedCornerShape(16.dp))
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            UText(
                text = title,
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800(),
            )

            if (content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                UText(
                    text = content,
                    style = UmcTypographyTokens.Subheadline,
                    color = neutral600(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isTwoButton) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    UButton(
                        text = negativeText,
                        onClick = onNegative,
                        modifier = Modifier.weight(1f),
                        backgroundColor = neutral000(),
                        textColor = neutral800(),
                        textStyle = UmcTypographyTokens.SubheadlineBold,
                        borderWidth = 1.dp,
                        borderColor = neutral300(),
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(vertical = 14.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UButton(
                        text = positiveText,
                        onClick = onPositive,
                        modifier = Modifier.weight(1f),
                        backgroundColor = neutral000(),
                        textColor = danger500(),
                        textStyle = UmcTypographyTokens.SubheadlineBold,
                        borderWidth = 1.dp,
                        borderColor = danger500(),
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(vertical = 14.dp),
                    )
                }
            } else {
                UButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = neutral800(),
                    textColor = neutral000(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(vertical = 14.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UDialogSingleButtonPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral200()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(neutral000(), RoundedCornerShape(16.dp))
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            UText(
                text = "회원가입에 실패했습니다.",
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            UButton(
                text = "확인",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = neutral800(),
                textColor = neutral000(),
                textStyle = UmcTypographyTokens.SubheadlineBold,
                cornerRadius = 8.dp,
                contentPadding = PaddingValues(vertical = 14.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UDialogTwoButtonPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral200()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(neutral000(), RoundedCornerShape(16.dp))
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            UText(
                text = "정말 탈퇴하시겠습니까?",
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            UText(
                text = "탈퇴 시 모든 데이터가 삭제됩니다.",
                style = UmcTypographyTokens.Subheadline,
                color = neutral600(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                UButton(
                    text = "취소",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    backgroundColor = neutral000(),
                    textColor = neutral800(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    borderWidth = 1.dp,
                    borderColor = neutral300(),
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(vertical = 14.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                UButton(
                    text = "탈퇴",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    backgroundColor = neutral000(),
                    textColor = danger500(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    borderWidth = 1.dp,
                    borderColor = danger500(),
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(vertical = 14.dp),
                )
            }
        }
    }
}