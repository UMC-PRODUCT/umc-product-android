package com.umc.presentation.notice

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.umc.domain.model.enums.NoticeWriterRole
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.notice.NoticeTarget
import com.umc.presentation.notice.detail.NoticeDetailScreen
import com.umc.presentation.notice.detail.NoticeDetailUiState
import com.umc.presentation.notice.write.NoticeWriteScreen
import com.umc.presentation.notice.write.NoticeWriteUiState

// #402의 표시 변경만 한 본문에 모아 작성·상세 모드의 차이를 비교한다.
private val markdownPreviewContent = """
    # 제목
    ## 머리말
    ### 부머리말
    *가 **나** 다* · _user_name_
    **a `**b**` c**
    <u>밑줄</u> · ~~취소선~~
    > 연속 인용 1
    > 연속 인용 2

    > 분리 인용
    - 글머리
    – 대시
    1. 번호
    [신청서](https://umc.dev)**필수**
    https://umc.dev/a\(1\)
    <mark color="0.686,0.322,0.871,0.350">보라</mark> <mark color="1.000,0.176,0.333,0.350">분홍</mark> <mark color="1.000,0.584,0.000,0.350">주황</mark> <mark color="0.000,0.780,0.745,0.350">민트</mark> <mark color="0.000,0.478,1.000,0.350">파랑</mark>
    \# 리터럴 · \*\*별표\*\*
""".trimIndent()

@Preview(showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
internal fun NoticeMarkdownWritePreview() {
    NoticeWriteScreen(
        uiState = NoticeWriteUiState(
            writerRole = NoticeWriterRole.CENTRAL_STAFF,
            isEditMode = true,
            title = "마크다운 작성",
            content = TextFieldValue(markdownPreviewContent),
        ),
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
internal fun NoticeMarkdownDetailPreview() {
    NoticeDetailScreen(
        uiState = NoticeDetailUiState(
            detail = NoticeDetail(
                id = 1L,
                title = "마크다운 상세",
                content = markdownPreviewContent,
                targetInfo = NoticeTarget(targetGisuId = 1),
                createdAt = "2026-01-01T00:00:00",
            ),
            isLoading = false,
            authorName = "운영진",
            receiverText = "수신대상: 전체",
        ),
    )
}

@Preview(showBackground = true, widthDp = 400)
@Composable
internal fun NoticeMarkdownCardPreview() {
    NoticeCard(
        notice = NoticeSummary(
            id = 1L,
            title = "마크다운 평문 미리보기",
            content = "# **필독** [신청서](https://umc.dev)\n\\*\\*리터럴\\*\\* · <mark>강조</mark> · `코드`",
            shouldSendNotification = false,
            viewCount = 10,
            createdAt = "2026-01-01T00:00:00",
            targetInfo = NoticeTarget(targetGisuId = 1),
            authorChallengerId = 1L,
            authorNickname = "운영진",
            authorName = "운영진",
        ),
    )
}
