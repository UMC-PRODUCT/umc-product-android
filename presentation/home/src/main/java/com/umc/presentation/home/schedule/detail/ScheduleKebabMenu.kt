package com.umc.presentation.home.schedule.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey800

/**케밥 메뉴 창
 *
 * MenuItem들을 추가해서 사용 + HorizontalDivider
 * **/
@Composable
fun ScheduleKebabMenu(
    isVisible: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    //위에서 아래로 애니메이션 표시하기
    AnimatedVisibility(
        visible = isVisible && (canEdit || canDelete), //작성 권한이 있는 사람만 수정/삭제 가능
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .width(180.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            color = grey000()
        ) {
            Column(modifier = Modifier
                .padding(8.dp)
            ) {
                if(canEdit) {
                    MenuItem(
                        icon = R.drawable.ic_edit,
                        text = AppStrings.NOTICE_WRITE_VOTE_EDIT,
                        color = grey800(),
                        onClick = onEditClick
                    )

                }

                if(canEdit && canDelete) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(vertical = 4.dp),
                        color = grey200()
                    )
                }

                if(canDelete) {
                    MenuItem(
                        icon = R.drawable.ic_trash_can,
                        text = AppStrings.NOTICE_WRITE_VOTE_DELETE,
                        color = red500(),
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}


/**1개의 메뉴 컴포지블 함수**/
@Composable
fun MenuItem(icon: Int, text: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color)

        Spacer(modifier = Modifier
            .width(8.dp)
        )

        UText(
            text = text,
            style = UmcTypographyTokens.Subheadline,
            color = color,
            modifier = Modifier
                .weight(1f)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_next_small),
            contentDescription = null,
            tint = color)
    }
}