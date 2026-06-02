package com.umc.component.component

import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.umc.component.component.UText
import com.umc.component.theme.*

/**
 * 피그마에 나온 커스텀 timePicker 다이얼로그
 * 
 * **/
@Composable
fun UTimePickerDialog(
    initialHour: Int, //초기 보여줄 시간
    initialMinute: Int, //초기 보여줄 분
    onConfirm: (hour: Int, minute: Int) -> Unit, //확인 시 로직
    onDismiss: () -> Unit
) {
    //내부 입력 상태 관리를 위한 State (두 자리 포맷 유지)
    var hourInput by remember { mutableStateOf(String.format("%02d", initialHour)) }
    var minuteInput by remember { mutableStateOf(String.format("%02d", initialMinute)) }

    //다이얼로그 형태로 보여주기
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = neutral000()),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )

                //[시간 : 분] 입력 영역 Row (영역 맞추기 위해 if)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //시간 입력 칸
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        TimeInputField(
                            value = hourInput,
                            onValueChange = { newValue ->
                                // 숫자만 필터링 및 최대 2글자 제한
                                val filtered = newValue.filter { it.isDigit() }.take(2)
                                if (filtered.isEmpty() || filtered.toInt() in 0..23) {
                                    hourInput = filtered
                                }
                            },
                        )
                    }

                    //중간 콜론 (:)
                    Text(
                        text = ":",
                        style = UmcTypographyTokens.Title1Bold, // 디자인에 맞게 큰 폰트 적용
                        fontSize = 57.sp,
                        color = neutral800(),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        //분 입력 칸
                        TimeInputField(
                            value = minuteInput,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }.take(2)
                                if (filtered.isEmpty() || filtered.toInt() in 0..59) {
                                    minuteInput = filtered
                                }
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )

                //시간 / 분 텍스트
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        UText(
                            text = "시간",
                            style = UmcTypographyTokens.Caption1,
                            color = neutral800(),
                            modifier = Modifier.width(100.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    Text(
                        text = ":",
                        style = UmcTypographyTokens.Title1Bold,
                        fontSize = 4.sp,
                        color = Color.Transparent, //눈에는 안 보이게 투명 처리하여 공간만 확보
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        UText(
                            text = "분",
                            style = UmcTypographyTokens.Caption1,
                            color = neutral800(),
                            modifier = Modifier.width(100.dp),
                            textAlign = TextAlign.Start
                        )
                    }
                }


                Spacer(modifier = Modifier
                    .height(24.dp)
                )

                //[취소 / 확인] 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UText(
                        text = AppStrings.CANCEL,
                        color = primary500(),
                        style = UmcTypographyTokens.Subheadline,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier
                        .width(8.dp)
                    )

                    UText(
                        text = AppStrings.CONFIRM,
                        color = primary500(),
                        style = UmcTypographyTokens.Subheadline,
                        modifier = Modifier
                            .clickable {
                                //빈 값 처리 방지용 디폴트 0 대입
                                val finalHour = hourInput.ifEmpty { "0" }.toInt()
                                val finalMinute = minuteInput.ifEmpty { "0" }.toInt()
                                onConfirm(finalHour, finalMinute)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

/**
 * 포커스 상태에 따라 테두리 두께(8.dp), 배경색, 텍스트 색상이 스위칭되는 단일 입력창 컴포넌트
 */
@Composable
private fun TimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    //포커싱 여부에 따른 색깔
    val containerColor = if (isFocused) primary100() else neutral200()
    val textColor = if (isFocused) primary500() else neutral800()

    //포커스 시 8.dp 굵은 보더, 언포커스 시 보더 없음 처리
    val modifierWithBorder = if (isFocused) {
        Modifier.border(4.dp, primary500(), RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(100.dp)
                .height(72.dp)
                .then(modifierWithBorder)
                .background(containerColor, RoundedCornerShape(8.dp))
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = TextStyle(
                color = textColor,
                fontSize = 45.sp,
                fontFamily = UmcTypographyTokens.Title1Bold.fontFamily,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(primary500()), //텍스트 커서 색상도 테마 일치
            //가운데 정렬을 위한 컨테이너 래핑
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    innerTextField()
                }
            }
        )
    }
}