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
 * 날짜와 시간을 선택 다이얼로그
 * 
 * 피그마 디자인 시스템에 맞춰 제작된 날짜/시간 입력 팝업 다이얼로그입니다.
 * 사용자가 입력한 사용자 지역(Local Time) 기준의 날짜/시간을
 * ISO 8601 형식의 UTC String(`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`)으로 변환하여 반환합니다.
 *
 * [주요 기능 및 동작 흐름]:
 * 1. 숫자만 전용으로 입력받아 컴포즈 상태(`dateInput`, `timeInput`)로 관리합니다.
 * 2. VisualTransformation을 통해 화면 상에는 `YYYY . MM . DD`, `HH:MM` 포맷으로 가공하여 보여줍니다.
 * 3. [Lenient = false] 설정이 적용된 Calendar 객체로 2월 30일, 13월 등 존재하지 않는 실시간 예외 날짜를 엄격히 검증합니다.
 * 4. [isAllday = true]일 경우 시간 입력 폼이 비활성화되며, [isStartTime] 값에 따라 00:00:00.000 또는 23:59:59.999로 자동 자동 보정됩니다.
 * 5. 최종 확정 시 사용자의 로컬 타임존 기준으로 Calendar를 설정한 후 UTC 타임존으로 파싱하여 반환합니다.
 *
 * @param onConfirm 확정 버튼 클릭 시 호출되는 콜백. UTC 변환 결과 문자열(`utcDateTime`)을 전달합니다.
 * @param onDismiss 취소 버튼 클릭 또는 다이얼로그 바깥 영역 클릭 시 호출되는 콜백.
 * @param isAllday 하루 종일 여부. `true` 설정 시 시간 입력 필드가 숨겨지고 날짜만 입력받습니다. (기본값: false)
 * @param isStartTime 하루 종일(`isAllday = true`)일 때, 시간의 시작(00:00:00.000)인지 종료(23:59:59.999)인지 지정합니다. (기본값: true)
 *
  **/


@Composable
fun UDateTimePickerDialog(
    onConfirm: (utcDateTime: String) -> Unit, // UTC로 변환된 결과 반환
    onDismiss: () -> Unit,
    isAllday: Boolean = false, //하루 종일일 경우의 true/false
    isStartTime: Boolean = true, //해당 시간이 시작 시간인지 아닌지 (기본 시작 시간 = 00:00 / 종료 시간 = 23:59)
) {
    //상태 관리 (순수 숫자만 저장하도록 처리)
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var isAm by remember { mutableStateOf(true) }

    //실시간 날짜 유혀성 검증 로직
    val isValidDateTime by remember(dateInput, timeInput) {
        derivedStateOf {
            // 1. 날짜 자릿수 8자리 길이 필수 검증
            if (dateInput.length != 8) return@derivedStateOf false

            // 2. 시간 자릿수 길이 검증 (하루 종일이 아닐 경우 4자리 필수: HHMM)
            if (!isAllday && timeInput.length != 4) return@derivedStateOf false

            try {
                // 3. 날짜 실존 여부 검증 (Calendar.isLenient = false 사용)
                val year = dateInput.substring(0, 4).toInt()
                val month = dateInput.substring(4, 6).toInt() - 1 // Calendar는 0 = 1월
                val day = dateInput.substring(6, 8).toInt()

                val testCal = Calendar.getInstance().apply {
                    isLenient = false //자동 범위 계산 끄기
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                testCal.time // 만약 잘못된 날짜(예: 88일)면 여기서 IllegalArgumentException 발생!

                if(!isAllday) {
                    // 4. 시간 범위 검증 (12시간제 입력 기준: 01~12시 / 00~59분)
                    val hour = timeInput.substring(0, 2).toInt()
                    val minute = timeInput.substring(2, 4).toInt()

                    val isHourValid = hour in 1..12
                    val isMinuteValid = minute in 0..59

                    if (!isHourValid || !isMinuteValid) return@derivedStateOf false
                }
                true

            } catch (e: Exception) {
                false // 날짜 파싱 실패 시 무조건 유효하지 않음
            }
        }
    }


    //확인 버튼 활성화 조건 (입력 자릿수 + 포맷)
    val isConfirmEnabled = isValidDateTime

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
                        ,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = indigo500(),
                        unfocusedBorderColor = grey200()
                    ),
                    //입력 시 자동으로 0000.00.00 형태로 바꿔주는 함수
                    visualTransformation = DateVisualTransformation()
                )


                // 2. 시간 입력 세션 (isAllday == false)일 떄만 생성
                if (!isAllday) {
                    Spacer(modifier = Modifier
                        .height(24.dp)
                    )
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
                        Spacer(
                            modifier = Modifier
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

                            // 시(Hour) 입력 시 12 초과 방지
                            if (digits.length >= 2) { //길이 2 이상 시(시간 입력)
                                val hh = digits.substring(0, 2).toIntOrNull() ?: 0
                                if (hh > 12) valid = false // 12시간제 검증
                            }

                            // 분(Minute) 입력 시 59 초과 방지
                            if (digits.length >= 4) { //길이 4 이상 시(분 입력)
                                val mm = digits.substring(2, 4).toIntOrNull() ?: 0
                                if (mm > 59) valid = false // 분 검증
                            }
                            if (valid) timeInput = digits
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = {
                            UText("12:00", color = grey400())
                        },
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
            }
        },
        confirmButton = {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)) {
                // 취소 버튼
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
                // 확인 버튼
                UButton(
                    text = AppStrings.CONFIRM,
                    onClick = {
                        if (isConfirmEnabled) {
                            //1. 날짜 데이터 추출(년 월 일)
                            val year = dateInput.substring(0, 4).toInt()
                            val month = dateInput.substring(4, 6).toInt() - 1 //Calendar.MONTH는 0부터 시작
                            val day = dateInput.substring(6, 8).toInt()

                            var hour: Int
                            var minute: Int
                            var second = 0
                            var millisecond = 0

                            // 2. 시간 계산 분기 처리
                            if(isAllday){
                                // 하루 종일인 경우: 시작시간(00:00:00.000)
                                if(isStartTime){
                                    hour = 0
                                    minute = 0
                                    second = 0
                                    millisecond = 0
                                }
                                // 하루 종일인 경우: 종료시간(23:59:59.999)
                                else{
                                    hour = 23
                                    minute = 59
                                    second = 59
                                    millisecond = 999
                                }
                            }

                            //직접 시간 입력인 경우
                            else{
                                //시간 데이터 추출 및 24시간제 변환
                                hour = timeInput.substring(0, 2).toInt()
                                minute = timeInput.substring(2, 4).toInt()

                                //오전오후 여부에 따라 시간 +-
                                if (isAm && hour == 12) hour = 0
                                if (!isAm && hour < 12) hour += 12
                            }

                            // 3. Calendar 객체 생성 후 UTC 변환
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, second)
                                set(Calendar.MILLISECOND, millisecond)
                            }

                            // 4. ISO 8601 표준 포맷을 이용해 UTC 시간 문자열로 최종 파싱
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

/**
 * 시간에서 오전/오후를 구분하는 토글 버튼
 *
 * @param text 표시할 텍스트 (오전/오후)
 * @param isSelected 현재 버튼의 선택 상태
 * @param onClick 클릭 이벤트 콜백
 * **/
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

/**
 * 숫자 날짜 입력을 시각적으로 "YYYY . MM . DD" 형태로 렌더링하는 포맷터
 *
 * [작동 방식]:
 * - 입력된 순수 텍스트(예: "20260902")의 특정 위치에 공백 및 점 기호(" . ")를 삽입합니다.
 * - [OffsetMapping]을 통해 UI상 텍스트 커서(Cursor) 위치와 실제 원본 텍스트 인덱스 간 1:1 위치를 상호 계산합니다.
 *
 */

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

/**
 * [숫자 시간 입력을 시각적으로 "HH:MM" 형태로 렌더링하는 포맷터]
 *
 * [작동 방식]:
 * - 입력된 순수 텍스트(예: "1230")의 2번째 글자 뒤에 콜론(":") 기호를 자동 삽입합니다.
 * - [OffsetMapping]을 통해 UI 커서 위치와 원본 텍스트 인덱스를 올바르게 대응시킵니다.
 */

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