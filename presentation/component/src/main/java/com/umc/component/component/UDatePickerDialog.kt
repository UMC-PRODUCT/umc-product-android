package com.umc.component.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.umc.component.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.umc.component.R


/**
 * 해당 Dialog는 Date와 Time 기능을 통합한 UDateTimePickerDialog로 대체 가능합니다.
 *
 *
 * **/
@Composable
fun UDatePickerDialog(
    initialDate: String? = null,
    onConfirm: (utcDate: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var dateInput by remember(initialDate) {
        mutableStateOf(
            initialDate
                ?.take(10)
                ?.replace("-", "")
                .orEmpty()
        )
    }

    val isConfirmEnabled = dateInput.length == 8

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = grey000(),
        shape = RoundedCornerShape(12.dp),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                UText(
                    text = "날짜를 선택해주세요.",
                    style = UmcTypographyTokens.Title3Bold,
                    color = grey900(),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = grey900(),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    UText(
                        text = "날짜",
                        style = UmcTypographyTokens.CalloutBold,
                        color = grey900()
                    )
                }

                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { newValue ->
                        dateInput = newValue
                            .filter { it.isDigit() }
                            .take(8)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    placeholder = {
                        UText(
                            text = "YYYY . MM . DD",
                            color = grey400()
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = indigo500(),
                        unfocusedBorderColor = grey200()
                    ),
                    visualTransformation = DateVisualTransformation()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UButton(
                    text = "취소",
                    onClick = onDismiss,
                    backgroundColor = grey100(),
                    textColor = grey700(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                UButton(
                    text = "확인",
                    onClick = {
                        if (isConfirmEnabled) {
                            val year = dateInput.substring(0, 4).toInt()
                            val month = dateInput.substring(4, 6).toInt() - 1
                            val day = dateInput.substring(6, 8).toInt()

                            val calendar = Calendar.getInstance(
                                TimeZone.getTimeZone("UTC")
                            ).apply {
                                set(year, month, day, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val formatter = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.getDefault()
                            ).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }

                            onConfirm(formatter.format(calendar.time))
                        }
                    },
                    enabled = isConfirmEnabled,
                    backgroundColor = if (isConfirmEnabled) {
                        indigo500()
                    } else {
                        grey200()
                    },
                    textColor = if (isConfirmEnabled) {
                        grey000()
                    } else {
                        grey400()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }
        }
    )
}