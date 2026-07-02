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
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.green100
import com.umc.component.theme.green500
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey300
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800

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
            listOf(R.drawable.ic_check_failed, red100(), red500(), red500())
        }
        DialogType.CANCEL -> {
            listOf(R.drawable.ic_check_failed, red100(), red500(), red500())
        }
        DialogType.SUCCESS -> {
            listOf(R.drawable.ic_check_success, green100(), green500(), green500())
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
            color = grey000()
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
                        tint = grey800(),
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
                    color = grey800())

                if (content != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = content!!,
                        style = UmcTypographyTokens.Subheadline,
                        color = grey600()
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
                        backgroundColor = grey000(),
                        borderColor = grey300(),
                        borderWidth = 1.dp,
                        textColor = grey800(),
                        onClick = onNegative
                    )

                    //확인
                    UButton(
                        text = positiveText,
                        modifier = Modifier.weight(1f).height(52.dp),
                        backgroundColor = grey000(),
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