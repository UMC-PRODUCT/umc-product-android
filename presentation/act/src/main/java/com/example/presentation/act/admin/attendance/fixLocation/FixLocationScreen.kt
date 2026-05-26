package com.example.presentation.act.admin.attendance.fixLocation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800

@Composable
fun FixLocationRoute() {
    FixLocationScreen()
}


@Composable
fun FixLocationScreen() {
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    val recentAddressTestData = listOf(
        "서울특별시 강남구 테헤란로 427",
        "서울특별시 송파구 올림픽로 300",
        "서울특별시 중구 세종대로 110",
        "경기도 성남시 분당구 판교역로 235",
        "인천광역시 연수구 센트럴로 123"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(700.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(neutral000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            UText(
                text = AppStrings.ACT_LOCATION_TITLE,
                style = Title3Bold,
                color = neutral800()
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(neutral100()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_gps),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        UText(
            text = AppStrings.ACT_LOCATION_UPDATE,
            style = Subheadline,
            color = neutral600(),
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(neutral100())
                .fillMaxWidth()
                .heightIn(52.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                modifier = Modifier.size(24.dp),
                contentDescription = null,
                tint = neutral400()

            )

            Spacer(Modifier.width(10.dp))

            BasicTextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                singleLine = true,
                textStyle = Body,
                cursorBrush = SolidColor(neutral800()),
                decorationBox = { innerTextField ->
                    if (searchKeyword.isBlank()) {
                        UText(
                            text = AppStrings.ACT_LOCATION_SEARCH_PLACEHOLDER,
                            style = Body,
                            color = neutral400()
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(Modifier.height(40.dp))

        UText(
            text = AppStrings.COMMUNITY_SEARCH_RECENT,
            style = HeadlineBold,
            color = neutral800()
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(recentAddressTestData) { address ->
                UText(
                    text = address,
                    style = Body,
                    color = neutral600(),
                )
                Spacer(Modifier.height(16.dp))
            }
        }

    }
}

@Composable
private fun DragHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(neutral600())
        )
    }
}

@Preview(showBackground = false)
@Composable
private fun FixLocationPreview() {
    UmcTheme(darkTheme = false) {
        FixLocationScreen()
    }
}
