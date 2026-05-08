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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.success100
import com.umc.component.theme.success500

/**
 * UMC 공용 다이얼로그 컴포넌트.
 *
 * `isAccept` 값에 따라 상단 아이콘/강조 색상이 success 또는 danger 톤으로 변경되며,
 * 단일 버튼(`isTwoButton=false`)과 이중 버튼(`isTwoButton=true`) 모드를 지원한다.
 *
 * @param isAccept true면 승인(success), false면 반려/실패(danger) 스타일 적용
 * @param title 다이얼로그 제목 (필수)
 * @param subtitle 부제목/설명 텍스트. null 또는 blank면 표시하지 않음
 * @param onDismissRequest 배경 터치 등으로 다이얼로그가 닫힐 때 호출
 * @param modifier 외부 Column에 적용할 Modifier
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
    isAccept : Boolean = true,
    title: String,
    subtitle: String?= null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
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
                .background(neutral000(), RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if(isAccept)success100() else danger100()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if(isAccept)R.drawable.ic_check_success else R.drawable.ic_check_failed),
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                text = title,
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            if(!subtitle.isNullOrBlank()) {
                UText(
                    text = subtitle,
                    style = UmcTypographyTokens.Subheadline,
                    color = neutral600(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isTwoButton) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    UButton(
                        text = negativeText,
                        onClick = onNegative,
                        modifier = Modifier.weight(1f),
                        backgroundColor = neutral100(),
                        textColor = neutral700(),
                        textStyle = UmcTypographyTokens.SubheadlineBold,
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(vertical = 14.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    UButton(
                        text = positiveText,
                        onClick = onPositive,
                        modifier = Modifier.weight(1f),
                        backgroundColor = if(isAccept)success100() else danger100(),
                        textColor = if(isAccept) success500() else danger500(),
                        textStyle = UmcTypographyTokens.SubheadlineBold,
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

@Preview
@Composable
fun dialogAcceptAttendacnePreview() {
    UDialog(
        isAccept = true,
        title = "출석 요청을 승인하시겠습니까?",
        subtitle = "이 작업은 되돌릴 수 없습니다.",
        isTwoButton = true,
        positiveText = "승인하기",
        onDismissRequest = {},
    )
}

@Preview
@Composable
fun dialogRefuseAttendacnePreview() {
    UDialog(
        isAccept = false,
        title = "출석 요청을 반려하시겠습니까?",
        subtitle = "이 작업은 되돌릴 수 없습니다.",
        isTwoButton = true,
        positiveText = "반려하기",
        onDismissRequest = {},
    )
}
