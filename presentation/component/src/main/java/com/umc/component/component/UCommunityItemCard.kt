package com.umc.component.component

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.accent100
import com.umc.component.theme.accent200
import com.umc.component.theme.accent500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.domain.model.community.ContentItem
import com.umc.component.R
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral400
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

/**커뮤니티 & 마이페이지(내 활동)에서 게시글 미리보기 카드 UI입니다.
 *
 * 재사용해야 해서, Component에 정의합니다.
 *
 * TODO: 게시글 태그에 따라 UBUTTON 색깔 변경
 * **/

@Composable
fun UCommunityItemCard(
    item: ContentItem,
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = neutral000()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 카테고리 버튼
            UButton(
                text = item.category.label,
                backgroundColor = accent100(), // accent100 대응
                borderColor = accent200(), // accent200 대응
                borderWidth = 1.dp,
                textColor = accent500(), // accent600 대응
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 4.dp,
                onClick = {}, // 클릭 비활성화 (아이템 전체 클릭임)
                modifier = Modifier
                    .height(24.dp)
            )

            // 제목
            UText(
                text = item.title,
                style = UmcTypographyTokens.HeadlineBold,
                color = neutral800(),
                modifier = Modifier.padding(top = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 유저 정보 및 작성 시간
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val userText = if (item.username.isEmpty()) "알 수 없음"
                    else "${item.userNickName}(${item.username})"

                    UText(
                        text = userText,
                        style = UmcTypographyTokens.Footnote,
                        color = neutral600()
                    )
                    UText(
                        text = " | ",
                        style = UmcTypographyTokens.Footnote,
                        color = neutral600()
                    )
                    UText(
                        text = item.writeTime,
                        style = UmcTypographyTokens.Footnote,
                        color = neutral600()
                    )
                }

                // 좋아요/댓글 통계
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_thumb_up_filled),
                        contentDescription = "Likes",
                        modifier = Modifier
                            .size(12.dp),
                        tint = neutral400()
                    )
                    UText(
                        text = item.likes.toString(),
                        style = UmcTypographyTokens.Footnote,
                        color = neutral600(),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "Comments",
                        modifier = Modifier.size(16.dp),
                        tint = neutral400()
                    )
                    UText(
                        text = item.comments.toString(),
                        style = UmcTypographyTokens.Footnote,
                        color = neutral600(),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun UCommunityItemCardPreview() {

    val dummyItem = ContentItem(
        postId = 100L,
        title = "이번 주 안드로이드 스터디 과제 공지드립니다.",
        category = CommunityCategoryType.FREE,
        username = "parkyusu",
        userNickName = "어헛차",
        writeTime = "1시간 전",
        likes = 15,
        comments = 8,
        content = "본문 내용입니다. 마이페이지 리스트에서는 본문이 보이지 않지만 데이터 모델에는 포함되어 있습니다.",
        userPart = UserPart.ANDROID,
        isLiked = false,
        isScrapped = true,
        scraps = 3
    )


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        UCommunityItemCard(
            item = dummyItem,
            onClick = {
                /* 클릭 시 상세 페이지 이동 로직 테스트 */
            }
        )
    }
}