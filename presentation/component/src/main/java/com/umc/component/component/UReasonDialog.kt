package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey50
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800

/**
 * 출석 승인 대기 목록에서 제출 사유를 확인하는 공용 다이얼로그입니다.
 *
 * 제목 아래에 선택적인 설명과 사유 본문을 표시하고 확인 버튼으로 닫습니다.
 *
 * @param title 다이얼로그 제목
 * @param subtitle 작성자 또는 보조 설명
 * @param content 제출된 사유 본문
 * @param onDismissRequest 다이얼로그 닫기 콜백
 * @param modifier 다이얼로그 컨테이너 Modifier
 * @param confirmText 확인 버튼 문구
 * @param onConfirm 확인 버튼 클릭 콜백
 */
@Composable
fun UReasonDialog(
    title: String,
    subtitle: String? = null,
    content: String? = null,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "확인",
    onConfirm: () -> Unit = onDismissRequest,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(grey000(), RoundedCornerShape(12.dp))
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
                    color = grey800(),
                )
            }

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                UText(
                    text = subtitle,
                    style = UmcTypographyTokens.Subheadline,
                    color = grey600(),
                )
            }


            if (!content.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(grey50())
                        .padding(vertical = 14.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    UText(
                        text = content,
                        style = UmcTypographyTokens.Subheadline,
                        color = grey800(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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

