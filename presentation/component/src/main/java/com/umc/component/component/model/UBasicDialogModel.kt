package com.umc.component.component.model

import androidx.compose.ui.graphics.Color
import com.umc.component.R
import com.umc.component.theme.*

/**
 * 다이얼로그에 들어갈 데이터를 정리한 Data Model
 * 제목과 내용, 확인/취소 텍스르랑 어떤 타입인지만 작성해주세요.
 *
 * @param type: WARNING(경고), CANCEL(반려), SUCCESS(성공)
 * @param title: 다이얼로그 제목
 * @param content: 다이얼로그 내용
 * @param negativeText: 취소 버튼의 텍스트(정의 x시 '취소')
 * @param confirmText: 확인 버튼의 텍스트
 *
 * **/
sealed class UBasicDialogModel(
    val title: String, //제목
    val content: String?, //내용
    val negativeText: String, //취소 버튼의 텍스트
    val confirmText: String, //확인 버튼의 텍스트
    val type: DialogType
) {

    enum class DialogType { WARNING, CANCEL, SUCCESS }

    class Warning(
        title: String,
        content: String? = null,
        negativeText: String = "취소",
        positiveText: String
    ) : UBasicDialogModel(title, content, negativeText, positiveText, DialogType.WARNING)

    class Cancel(
        title: String,
        content: String? = null,
        negativeText: String = "취소",
        positiveText: String
    ) : UBasicDialogModel(title, content, negativeText, positiveText, DialogType.CANCEL)

    class Success(
        title: String,
        content: String? = null,
        negativeText: String = "취소",
        positiveText: String
    ) : UBasicDialogModel(title, content, negativeText, positiveText, DialogType.SUCCESS)
}