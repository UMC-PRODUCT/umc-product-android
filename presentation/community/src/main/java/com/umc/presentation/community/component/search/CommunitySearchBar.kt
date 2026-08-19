package com.umc.presentation.community.component.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950

/**
 * 커뮤니티 스레드 검색 화면에서 사용하는 검색바
 *
 * 검색어 입력, 검색 실행, 검색어 초기화 및
 * 검색 전/후 상태에 따른 취소·뒤로가기 기능을 제공합니다.
 */
@Composable
fun CommunitySearchBar(
    query: String,
    hasSearched: Boolean,
    focusRequester: FocusRequester,
    onQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
    onCancelClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 검색 완료 후에는 검색바 왼쪽에 뒤로가기 버튼 표시
        if (hasSearched) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = grey950(),
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // 검색어 입력 영역
        Surface(
            modifier = Modifier.weight(1f),
            color = grey000(),
            border = BorderStroke(
                width = 1.dp,
                color = grey200(),
            ),
            shape = RoundedCornerShape(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    textStyle = UmcTypographyTokens.Body.copy(
                        color = grey900(),
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(grey950()),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchClick()
                        },
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 15.5.dp,
                                ),
                        ) {
                            // 검색어가 없을 때 placeholder 표시
                            if (query.isBlank()) {
                                UText(
                                    text = "제목, 내용 검색",
                                    style = UmcTypographyTokens.Body,
                                    color = grey400(),
                                )
                            }

                            innerTextField()
                        }
                    },
                )

                // 입력된 검색어 전체 삭제 버튼
                if (query.isNotBlank()) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(24.dp),
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_community_clear_filled,
                            ),
                            contentDescription = "검색어 지우기",
                            tint = grey400(),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // 검색 전에는 검색 화면을 종료할 수 있는 취소 버튼 표시
        if (!hasSearched) {
            Spacer(modifier = Modifier.width(12.dp))

            UText(
                text = "취소",
                style = UmcTypographyTokens.CalloutBold,
                color = grey950(),
                modifier = Modifier.clickable(
                    onClick = onCancelClick,
                ),
            )
        }
    }
}