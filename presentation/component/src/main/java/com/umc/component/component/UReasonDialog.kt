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
import com.umc.component.theme.neutral050
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.success100
import com.umc.component.theme.success500

/**
 * 출석 사유 확인용 단일 버튼 다이얼로그 컴포넌트.
 *
 * 제목을 중심으로 선택적으로 부제목과 사유 본문(content)을 표시하며,
 * 하단의 확인 버튼 1개로 닫힘/확인 동작을 처리한다.
 *
 * @param title 다이얼로그 제목 (필수)
 * @param subtitle 부제목/설명 텍스트. null 또는 blank면 표시하지 않음
 * @param content 사유 본문 텍스트. null 또는 blank면 표시하지 않음
 * @param onDismissRequest 배경 터치 등으로 다이얼로그가 닫힐 때 호출
 * @param modifier 외부 Column에 적용할 Modifier
 * @param confirmText 확인 버튼 텍스트. 기본값 "확인"
 * @param onConfirm 확인 버튼 클릭 시 호출. 기본값은 onDismissRequest
 */

@Composable
fun UReasonDialog(
    title: String,
    subtitle: String?= null,
    content : String?= null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "확인",
    onConfirm: () -> Unit = onDismissRequest,
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
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = title,
                    style = UmcTypographyTokens.Title3Bold,
                    color = neutral800(),
                )
            }

            if(!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                UText(
                    text = subtitle,
                    style = UmcTypographyTokens.Subheadline,
                    color = neutral600(),
                )
            }


            if(!content.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(neutral050())
                        .padding(vertical = 14.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    UText(
                        text = content,
                        style = UmcTypographyTokens.Subheadline,
                        color = neutral800(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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

@Preview(showBackground = false)
@Composable
fun dialogReasonPreview() {
    UReasonDialog(
        title = "출석 사유 확인",
        subtitle = "홍길동님이 작성하신 사유입니다.",
        content = "“지하철 연착으로 인해 5분 정도 늦을 것 같습니다. 죄송합니다!”",
        onDismissRequest = {},
    )
}

