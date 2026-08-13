package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.presentation.community.model.CommunityCategory

@Composable
fun CommunityTopBar(
    isFilterMenuExpanded: Boolean,
    selectedCategory: CommunityCategory,
    onFilterClick: () -> Unit,
    onFilterDismissRequest: () -> Unit,
    onCategorySelected: (CommunityCategory) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 8.dp)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = "커뮤니티",
            style = UmcTypographyTokens.Title2Bold,
            color = grey950(),
            modifier = Modifier.padding(start = 8.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_community_filter,
                    ),
                    contentDescription = "필터",
                    tint = grey950(),
                    modifier = Modifier.size(24.dp),
                )
            }

            CommunityFilterMenu(
                expanded = isFilterMenuExpanded,
                selectedCategory = selectedCategory,
                onDismissRequest = onFilterDismissRequest,
                onCategorySelected = onCategorySelected,
            )
        }

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_search,
                ),
                contentDescription = "검색",
                tint = grey950(),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}