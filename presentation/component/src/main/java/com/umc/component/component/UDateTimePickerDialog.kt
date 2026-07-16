package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** UDateTimePickerDialog
 * 피그마에 제시된 날짜와 시간을 입력받아, UTC String으로 제공해주는 다이얼로그
 * onConfirm: (utcDateTime: String) -> {}: 확인을 누를 시 처리 함수
 * 
 * 
 * **/


@Composable
fun UDateTimePickerDialog(
    onConfirm: (utcDateTime: String) -> Unit, // UTC로 변환된 결과 반환
    onDismiss: () -> Unit
) {
    //상태 관리 (순수 숫자만 저장하도록 처리)
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var isAm by remember { mutableStateOf(true) }

    //확인 버튼 활성화 조건 (날짜 8자리, 시간 4자리 모두 입력 시)
    val isConfirmEnabled = dateInput.length == 8 && timeInput.length == 4

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = grey000(),
        shape = RoundedCornerShape(12.dp),
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                UText(
                    text = AppStrings.DIALOG_DATETIME_TITLE,
                    style = UmcTypographyTokens.Title3Bold,
                    color = grey900(),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                //1. 날짜 입력 섹션
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = grey900(),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                    )
                    UText(
                        text = AppStrings.DIALOG_DATETIME_DATE,
                        style = UmcTypographyTokens.CalloutBold,
                        color = grey900()
                    )
                }

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { newValue ->
                        //숫자만 필터링하고 최대 8자리까지만 허용
                        val digits = newValue.filter { it.isDigit() }.take(8)
                        dateInput = digits
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { UText("YYYY . MM . DD", color = grey400()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = indigo500(),
                        unfocusedBorderColor = grey200()
                    ),
                    //입력 시 자동으로 0000.00.00 형태로 바꿔주는 함수
                    visualTransformation = DateVisualTransformation()
                )

                //2. 시간 입력 섹션
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clock),
                        contentDescription = null,
                        tint = grey900(),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                    )
                    UText(
                        text = AppStrings.DIALOG_DATETIME_TIME,
                        style = UmcTypographyTokens.CalloutBold,
                        color = grey900()
                    )
                }

                // 오전/오후 토글 버튼
                Row(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                ) {
                    AmPmToggleButton(
                        text = AppStrings.TIME_AM,
                        isSelected = isAm,
                        onClick = { isAm = true }
                    )
                    Spacer(modifier = Modifier
                        .width(8.dp)
                    )
                    AmPmToggleButton(
                        text = AppStrings.TIME_PM,
                        isSelected = !isAm,
                        onClick = { isAm = false }
                    )
                }

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { newValue ->
                        //숫자만 필터링 및 최대 4자리 허용, 간단한 시간 규칙 방어
                        val digits = newValue.filter { it.isDigit() }.take(4)
                        var valid = true
                        if (digits.length >= 2) { //길이 2 이상 시(시간 입력)
                            val hh = digits.substring(0, 2).toIntOrNull() ?: 0
                            if (hh > 12) valid = false // 12시간제 검증
                        }
                        if (digits.length >= 4) { //길이 4 이상 시(분 입력)
                            val mm = digits.substring(2, 4).toIntOrNull() ?: 0
                            if (mm > 59) valid = false // 분 검증
                        }
                        if (valid) timeInput = digits
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = {
                        UText("12:00", color = grey400()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = indigo500(),
                        unfocusedBorderColor = grey200()
                    ),
                    //분 입력 시 자동으로 :00 형태로 바꿔주는 함수
                    visualTransformation = TimeVisualTransformation()
                )
            }
        },
        confirmButton = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)) {
                UButton(
                    text = AppStrings.CANCEL,
                    onClick = onDismiss,
                    backgroundColor = grey100(),
                    textColor = grey700(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
                Spacer(modifier = Modifier
                    .width(8.dp)
                )
                UButton(
                    text = AppStrings.CONFIRM,
                    onClick = {
                        if (isConfirmEnabled) {
                            //1. 날짜 데이터 추출(년 월 일)
                            val year = dateInput.substring(0, 4).toInt()
                            val month = dateInput.substring(4, 6).toInt() - 1 //Calendar.MONTH는 0부터 시작
                            val day = dateInput.substring(6, 8).toInt()

                            //2. 시간 데이터 추출 및 24시간제 변환
                            var hour = timeInput.substring(0, 2).toInt()
                            val minute = timeInput.substring(2, 4).toInt()

                            //오전오후 여부에 따라 시간 +-
                            if (isAm && hour == 12) hour = 0
                            if (!isAm && hour < 12) hour += 12

                            //3. Calendar 객체 생성 후 UTC 변환
                            val cal = Calendar.getInstance().apply {
                                set(year, month, day, hour, minute, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }

                            val utcResult = sdf.format(cal.time)
                            onConfirm(utcResult)
                        }
                    },
                    backgroundColor = if (isConfirmEnabled) indigo500() else grey000(),
                    textColor = if (isConfirmEnabled) grey100() else grey300(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }
        }
    )
}

/**시간에서 오전/오후를 구분하는 토글 버튼**/
@Composable
fun AmPmToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) indigo100() else grey100())
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.CalloutBold,
            color = if (isSelected) indigo500() else grey400()
        )
    }
}

// -----------------------------------------------------------------
// 숫자 입력을 시각적으로 YYYY . MM . DD 형태로 바꿔주는 포맷터
// -----------------------------------------------------------------
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 || i == 5) out += " . "
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 6) return offset + 3
                if (offset <= 8) return offset + 6
                return 14
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 8) return offset - 3
                if (offset <= 14) return offset - 6
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// -----------------------------------------------------------------
// 숫자 입력을 시각적으로 HH:MM 형태로 바꿔주는 포맷터
// -----------------------------------------------------------------
class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += ":"
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                return 5
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}