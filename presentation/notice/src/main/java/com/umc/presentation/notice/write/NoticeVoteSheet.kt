package com.umc.presentation.notice.write

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UDateTimePickerDialog
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.domain.model.notice.NoticeVoteForm
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * 투표 만들기/수정 바텀시트.
 * 시작/종료 일시는 UDateTimePickerDialog로 입력받아 UTC ISO 문자열로 보관한다.
 * [existingVote]가 있으면 수정 모드로 삭제하기/수정하기 버튼이 노출됨
 */
@Composable
fun NoticeVoteSheetContent(
    existingVote: NoticeVoteForm?,
    onComplete: (NoticeVoteForm) -> Unit = {},
    onDelete: () -> Unit = {},
) {
    var form by remember { mutableStateOf(existingVote ?: NoticeVoteForm()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val isEditMode = existingVote != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        // 헤더
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_vote),
                contentDescription = null,
                tint = indigo500(),
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            UText(
                text = AppStrings.NOTICE_WRITE_VOTE_TITLE,
                style = UmcTypographyTokens.Title3Bold,
                color = grey950(),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 투표 제목
        VoteInputField(
            text = form.title,
            placeholder = AppStrings.NOTICE_WRITE_VOTE_TITLE_PLACEHOLDER,
            onTextChanged = { form = form.copy(title = it) },
        )

        // 투표 항목. 기본 2개는 삭제 불가, 추가된 항목에만 휴지통 노출
        form.options.forEachIndexed { index, option ->
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                VoteInputField(
                    text = option,
                    placeholder = AppStrings.NOTICE_WRITE_VOTE_OPTION_PLACEHOLDER,
                    onTextChanged = { text ->
                        form = form.copy(
                            options = form.options.toMutableList().apply { set(index, text) },
                        )
                    },
                    modifier = Modifier.weight(1f),
                )

                if (index >= MIN_VOTE_OPTION_COUNT) {
                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash_can),
                        contentDescription = null,
                        tint = red500(),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                form = form.copy(
                                    options = form.options.toMutableList().apply { removeAt(index) },
                                )
                            },
                    )
                }
            }
        }

        if (form.options.size < NoticeWriteViewModel.MAX_VOTE_OPTION_COUNT) {
            Spacer(modifier = Modifier.height(16.dp))

            // 항목 추가하기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(grey100())
                    .clickable { form = form.copy(options = form.options + "") }
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus_circle),
                    contentDescription = null,
                    tint = grey900(),
                    modifier = Modifier.size(24.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                UText(
                    text = AppStrings.NOTICE_WRITE_VOTE_ADD,
                    style = UmcTypographyTokens.CalloutBold,
                    color = grey900(),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 익명 투표 / 복수 선택 허용
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VoteCheckOption(
                label = AppStrings.NOTICE_WRITE_ANONYMITY,
                isChecked = form.isAnonymous,
                onClick = { form = form.copy(isAnonymous = !form.isAnonymous) },
            )

            Spacer(modifier = Modifier.width(16.dp))

            VoteCheckOption(
                label = AppStrings.NOTICE_WRITE_SELECT_MULTIPLE,
                isChecked = form.allowMultipleChoice,
                onClick = { form = form.copy(allowMultipleChoice = !form.allowMultipleChoice) },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 시작/종료 일시
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, grey200(), RoundedCornerShape(12.dp)),
        ) {
            VoteDateRow(
                label = AppStrings.START,
                dateText = formatVoteDateTime(form.startsAt),
                onClick = { showStartPicker = true },
            )

            HorizontalDivider(thickness = 1.dp, color = grey200())

            VoteDateRow(
                label = AppStrings.END,
                dateText = formatVoteDateTime(form.endsAt),
                onClick = { showEndPicker = true },
            )
        }

        Spacer(modifier = Modifier.height(21.dp))

        if (isEditMode) {
            Row(modifier = Modifier.fillMaxWidth()) {
                VoteBottomButton(
                    text = AppStrings.NOTICE_WRITE_VOTE_DELETE,
                    backgroundColor = red100(),
                    textColor = red500(),
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(16.dp))

                VoteBottomButton(
                    text = AppStrings.NOTICE_WRITE_VOTE_EDIT,
                    backgroundColor = if (form.canSubmit) indigo500() else grey100(),
                    textColor = if (form.canSubmit) grey000() else grey400(),
                    onClick = { if (form.canSubmit) onComplete(form) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            VoteBottomButton(
                text = AppStrings.NOTICE_WRITE_VOTE_CREATE,
                backgroundColor = if (form.canSubmit) indigo500() else grey100(),
                textColor = if (form.canSubmit) grey000() else grey400(),
                onClick = { if (form.canSubmit) onComplete(form) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showStartPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                form = form.copy(startsAt = utcDateTime)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false },
        )
    }

    if (showEndPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                form = form.copy(endsAt = utcDateTime)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false },
        )
    }
}

private const val MIN_VOTE_OPTION_COUNT = 2

/** 투표 제목/항목 공용 입력 필드 */
@Composable
private fun VoteInputField(
    text: String,
    placeholder: String,
    onTextChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, grey200(), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChanged,
            textStyle = UmcTypographyTokens.Body.copy(color = grey950()),
            cursorBrush = SolidColor(grey950()),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (text.isEmpty()) {
                        UText(
                            text = placeholder,
                            style = UmcTypographyTokens.Body,
                            color = grey400(),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/** 익명/복수 선택 체크 옵션 (라벨 + 체크박스) */
@Composable
private fun VoteCheckOption(
    label: String,
    isChecked: Boolean,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() },
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.Subheadline,
            color = grey600(),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(
                id = if (isChecked) R.drawable.ic_check_box else R.drawable.ic_check_box_empty
            ),
            contentDescription = null,
            tint = if (isChecked) indigo500() else grey300(),
            modifier = Modifier.size(24.dp),
        )
    }
}

/** 시작/종료 일시 행. 날짜 미설정 시 chevron, 설정 시 indigo 칩으로 표시 */
@Composable
private fun VoteDateRow(
    label: String,
    dateText: String?,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.Callout,
            color = grey600(),
        )

        if (dateText == null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400(),
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(indigo100())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                UText(
                    text = dateText,
                    style = UmcTypographyTokens.SubheadlineBold,
                    color = indigo500(),
                )
            }
        }
    }
}

/** 시트 하단 버튼 (생성하기/삭제하기/수정하기 공용) */
@Composable
private fun VoteBottomButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.HeadlineBold,
            color = textColor,
        )
    }
}

/**
 * 작성 화면 본문 아래에 표시되는 첨부 투표 요약 행.
 * "투표  익명, 복수 허용, 3개의 항목" + 우측 수정 칩
 */
@Composable
fun NoticeVoteRow(
    vote: NoticeVoteForm,
    onClickModify: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(grey100())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_vote),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        UText(
            text = AppStrings.NOTICE_WRITE_VOTE,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.width(8.dp))

        UText(
            text = voteConditionText(vote),
            style = UmcTypographyTokens.Subheadline,
            color = grey600(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(grey100())
                .clickable { onClickModify() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            UText(
                text = AppStrings.NOTICE_WRITE_VOTE_MODIFY,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey700(),
            )
        }
    }
}

/** "익명, 복수 허용, N개의 항목" 형태의 투표 조건 요약 */
private fun voteConditionText(vote: NoticeVoteForm): String {
    val anonymity = if (vote.isAnonymous) {
        AppStrings.NOTICE_WRITE_VOTE_ANONYMOUS
    } else {
        AppStrings.NOTICE_WRITE_VOTE_REAL_NAME
    }
    val multiple = if (vote.allowMultipleChoice) {
        AppStrings.NOTICE_WRITE_VOTE_MULTIPLE
    } else {
        AppStrings.NOTICE_WRITE_VOTE_SINGLE
    }
    val count = AppStrings.NOTICE_WRITE_VOTE_OPTION_COUNT.format(vote.validOptions.size)
    return "$anonymity, $multiple, $count"
}

/** UDateTimePickerDialog가 주는 UTC ISO 문자열을 "2025.01.01 ･ 오후 4:00" 로컬 표기로 변환 */
internal fun formatVoteDateTime(utcDateTime: String?): String? {
    if (utcDateTime.isNullOrBlank()) return null
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(utcDateTime) ?: return null
        SimpleDateFormat("yyyy.MM.dd ･ a h:mm", Locale.KOREAN).format(date)
    }.getOrNull()
}

@Preview(showBackground = true)
@Composable
private fun NoticeVoteSheetPreview() {
    Column {
        NoticeVoteSheetContent(
            existingVote = NoticeVoteForm(
                title = "회식 메뉴",
                options = listOf("치킨", "피자", ""),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeVoteRowPreview() {
    NoticeVoteRow(
        vote = NoticeVoteForm(
            title = "회식 메뉴",
            options = listOf("치킨", "피자", "족발"),
            isAnonymous = true,
            allowMultipleChoice = true,
        ),
    )
}
