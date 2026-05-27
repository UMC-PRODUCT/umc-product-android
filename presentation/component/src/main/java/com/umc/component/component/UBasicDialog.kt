package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.umc.component.R
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.success100
import com.umc.component.theme.success500
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800

/**
 * 경고 / 반려 / 성공 등을 나타내는 AlertDialog입니다.
 * 중앙에 아이콘이랑 아래에 설명 및 확인/취소 버튼이 존재하는 형태입니다.
 *
 *
 * @param title: 다이얼로그 제목
 * @param content: 다이얼로그 내용 - nullable
 * @param negativeText: 취소 버튼의 텍스트 - 기본 취소
 * @param positiveText: 확인 버튼의 텍스트 - 기본 확인
 * @param type: 다이얼로그 타입 (UI 색상 반영) WARNING(경고), CANCEL(반려), SUCCESS(성공) - 기본 WARNING
 * @param onPositive: 확인 시 처리 로직
 * @param onNegative: 취소 시 처리 로직
 * @Param onDismissRequest: 닫기 시 처리 로직
 *
 */

enum class DialogType { WARNING, CANCEL, SUCCESS }

@Composable
fun UBasicDialog(
    title: String, //제목
    content: String? = null, //내용
    negativeText: String = "취소", //취소 버튼의 텍스트 
    positiveText: String = "확인", //확인 버튼의 텍스트
    type: DialogType = DialogType.WARNING,
    onPositive: () -> Unit,
    onNegative: () -> Unit,
    onDismissRequest: () -> Unit,
) {

    //type에 따라 시각적 요소(색상, 아이콘)를 결정
    val (iconRes, iconBgColor, iconTintColor, positiveColor) =
        when (type) {
        DialogType.WARNING -> {
            listOf(R.drawable.ic_check_failed, danger100(), danger500(), danger500())
        }
        DialogType.CANCEL -> {
            listOf(R.drawable.ic_check_failed, danger100(), danger500(), danger500())
        }
        DialogType.SUCCESS -> {
            listOf(R.drawable.ic_check_success, success100(), success500(), success500())
        }
    }

    //다이얼로그
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = neutral000()
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //닫기(X) 버튼
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismissRequest()},
                    contentAlignment = Alignment.CenterEnd) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_big),
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onNegative() }
                            .padding(12.dp),
                        tint = neutral800(),
                    )
                }

                //중앙 아이콘
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = iconBgColor as Color,
                            shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes as Int),
                        contentDescription = null,
                        tint = iconTintColor as Color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                //타이틀 & 내용
                Text(
                    text = title,
                    style = UmcTypographyTokens.Title3Bold,
                    color = neutral800())

                if (content != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = content!!,
                        style = UmcTypographyTokens.Subheadline,
                        color = neutral600()
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 하단 버튼
                Row(modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    //취소
                    UButton(
                        text = negativeText,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        backgroundColor = neutral000(),
                        borderColor = neutral300(),
                        borderWidth = 1.dp,
                        textColor = neutral800(),
                        onClick = onNegative
                    )

                    //확인
                    UButton(
                        text = positiveText,
                        modifier = Modifier.weight(1f).height(52.dp),
                        backgroundColor = neutral000(),
                        borderColor = positiveColor as Color,
                        borderWidth = 1.dp,
                        textColor = positiveColor as Color,
                        onClick = {
                            onPositive()

                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UBasicWarningDialogPreview() {
    UmcTheme(darkTheme = false) {
        UBasicDialog(
            title = "정말 삭제할까요?",
            content = "삭제한 내용은 다시 복구할 수 없습니다.",
            negativeText = "취소",
            positiveText = "삭제",
            type = DialogType.WARNING,
            onPositive = {},
            onNegative = {},
            onDismissRequest = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UBasicCancelDialogPreview() {
    UmcTheme(darkTheme = false) {
        UBasicDialog(
            title = "요청을 반려할까요?",
            content = "선택한 요청이 반려 처리됩니다.",
            negativeText = "취소",
            positiveText = "반려",
            type = DialogType.CANCEL,
            onPositive = {},
            onNegative = {},
            onDismissRequest = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UBasicSuccessDialogPreview() {
    UmcTheme(darkTheme = false) {
        UBasicDialog(
            title = "처리가 완료됐습니다.",
            content = "변경 사항이 정상적으로 반영되었습니다.",
            negativeText = "닫기",
            positiveText = "확인",
            type = DialogType.SUCCESS,
            onPositive = {},
            onNegative = {},
            onDismissRequest = {}
        )
    }
}
