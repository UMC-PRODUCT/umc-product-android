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
import com.umc.component.theme.green500
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import androidx.compose.ui.graphics.Color

/**
 * 활동 화면의 확인 및 승인/거절 흐름에 사용하는 공용 다이얼로그입니다.
 *
 * [isTwoButton]이 false이면 확인 버튼 하나를, true이면 취소/확인 버튼 두 개를 표시합니다.
 *
 * @param title 다이얼로그 제목
 * @param onDismissRequest 바깥 영역 터치 또는 뒤로 가기 콜백
 * @param modifier 다이얼로그 컨테이너 Modifier
 * @param content 기존 호출부에서 사용하는 설명
 * @param subtitle 활동 화면에서 사용하는 설명. 비어 있으면 [content]를 표시합니다.
 * @param isAccept 승인 다이얼로그 여부. true이면 승인 색상, false이면 거절/삭제 색상을 사용합니다.
 * @param isTwoButton 두 개 버튼 사용 여부
 * @param confirmText 단일 버튼 문구
 * @param onConfirm 단일 버튼 클릭 콜백
 * @param negativeText 왼쪽 버튼 문구
 * @param positiveText 오른쪽 버튼 문구
 * @param onNegative 왼쪽 버튼 클릭 콜백
 * @param onPositive 오른쪽 버튼 클릭 콜백
 * @param negativeBackgroundColor 왼쪽 버튼 배경색
 * @param negativeTextColor 왼쪽 버튼 텍스트 색상
 * @param negativeBorderColor 왼쪽 버튼 테두리 색상
 * @param positiveBackgroundColor 오른쪽 버튼 배경색
 * @param positiveTextColor 오른쪽 버튼 텍스트 색상
 * @param positiveBorderColor 오른쪽 버튼 테두리 색상
 */
@Composable
fun UDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: String = "",
    subtitle: String = "",
    isAccept: Boolean = false,
    isTwoButton: Boolean = false,
    confirmText: String = "확인",
    onConfirm: () -> Unit = onDismissRequest,
    negativeText: String = "취소",
    positiveText: String = "",
    onNegative: () -> Unit = onDismissRequest,
    onPositive: () -> Unit = {},

    // 이중 버튼 스타일 옵션
    negativeBackgroundColor: Color = grey000(),
    negativeTextColor: Color = grey800(),
    negativeBorderColor: Color = grey300(),
    positiveBackgroundColor: Color? = null,
    positiveTextColor: Color? = null,
    positiveBorderColor: Color? = null,
) {
    val description = subtitle.ifBlank { content }
    val actionColor = if (isAccept) green500() else red500()
    val resolvedPositiveBackgroundColor = positiveBackgroundColor ?: grey000()
    val resolvedPositiveTextColor = positiveTextColor ?: actionColor
    val resolvedPositiveBorderColor = positiveBorderColor ?: actionColor

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(grey000(), RoundedCornerShape(16.dp))
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            UText(
                text = title,
                style = UmcTypographyTokens.Title3Bold,
                color = grey800(),
            )

            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                UText(
                    text = description,
                    style = UmcTypographyTokens.Subheadline,
                    color = grey600(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isTwoButton) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    UButton(
                        text = negativeText,
                        onClick = onNegative,
                        modifier = Modifier.weight(1f),
                        backgroundColor = negativeBackgroundColor,
                        textColor = negativeTextColor,
                        textStyle = UmcTypographyTokens.SubheadlineBold,
                        borderWidth = 1.dp,
                        borderColor = negativeBorderColor,
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(vertical = 14.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    UButton(
                        text = positiveText,
                        onClick = onPositive,
                        modifier = Modifier.weight(1f),
                        backgroundColor = resolvedPositiveBackgroundColor,
                        textColor = resolvedPositiveTextColor,
                        textStyle = UmcTypographyTokens.SubheadlineBold,
                        borderWidth = 1.dp,
                        borderColor = resolvedPositiveBorderColor,
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(vertical = 14.dp),
                    )
                }
            } else {
                UButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = grey800(),
                    textColor = grey000(),
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
            .background(grey200()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(grey000(), RoundedCornerShape(16.dp))
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            UText(
                text = "회원가입에 실패했습니다.",
                style = UmcTypographyTokens.Title3Bold,
                color = grey800(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            UButton(
                text = "확인",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = grey800(),
                textColor = grey000(),
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
            .background(grey200()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(grey000(), RoundedCornerShape(16.dp))
                .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            UText(
                text = "정말 탈퇴하시겠습니까?",
                style = UmcTypographyTokens.Title3Bold,
                color = grey800(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            UText(
                text = "탈퇴 시 모든 데이터가 삭제됩니다.",
                style = UmcTypographyTokens.Subheadline,
                color = grey600(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                UButton(
                    text = "취소",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    backgroundColor = grey000(),
                    textColor = grey800(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    borderWidth = 1.dp,
                    borderColor = grey300(),
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(vertical = 14.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                UButton(
                    text = "탈퇴",
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    backgroundColor = grey000(),
                    textColor = red500(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    borderWidth = 1.dp,
                    borderColor = red500(),
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(vertical = 14.dp),
                )
            }
        }
    }
}
