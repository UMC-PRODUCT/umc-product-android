package com.umc.presentation.act.admin.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.umc.component.component.USelectSheetContent
import com.umc.component.component.UChip
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.white
import com.umc.domain.model.enums.UserPart
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import androidx.compose.ui.semantics.onClick as accessibilityClick
import com.umc.component.R as ComponentR

data class AdminChallengerFilterOption(val id: Long, val label: String)

/** 선택지와 고정 여부는 호출부에서 전달하며, 이 UI에서는 권한이나 서버 조회 범위를 판단하지 않는다. */
data class AdminChallengerFilterUi(
    val selectedOption: AdminChallengerFilterOption? = null,
    val options: ImmutableList<AdminChallengerFilterOption> = persistentListOf(),
    val isFixed: Boolean = false,
)

data class AdminChallengerFiltersUi(
    val gisu: AdminChallengerFilterUi = AdminChallengerFilterUi(),
    val chapter: AdminChallengerFilterUi = AdminChallengerFilterUi(),
    val school: AdminChallengerFilterUi = AdminChallengerFilterUi(),
)

enum class AdminChallengerFilterType { GISU, CHAPTER, SCHOOL }

@Composable
internal fun AdminChallengerFilterBar(
    filters: AdminChallengerFiltersUi,
    selectedPart: UserPart?,
    onPartFilterClick: () -> Unit,
    onFilterSelected: (AdminChallengerFilterType, AdminChallengerFilterOption?) -> Unit,
    modifier: Modifier = Modifier,
    partFilterEnabled: Boolean = true,
) {
    var openedFilter by rememberSaveable { mutableStateOf<AdminChallengerFilterType?>(null) }

    LaunchedEffect(filters) {
        val opened = openedFilter
        if (opened != null && filters.forType(opened).isFixed) openedFilter = null
    }

    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AdminChallengerFilterType.entries.forEach { type ->
            val filter = filters.forType(type)
            ChallengerFilterChip(
                label = filter.selectedOption?.label ?: type.label,
                isSelected = filter.selectedOption != null,
                isFixed = filter.isFixed,
                onClick = { openedFilter = type },
            )
        }
        ChallengerFilterChip(
            label = selectedPart?.label ?: AppStrings.COMMON_PART,
            isSelected = selectedPart != null,
            isFixed = !partFilterEnabled,
            onClick = onPartFilterClick,
        )
    }

    openedFilter?.let { type ->
        val filter = filters.forType(type)
        // 바텀시트가 열린 동안 호출부에서 고정 상태로 바뀌어도 선택 동작을 허용하지 않는다.
        if (!filter.isFixed) {
            AdminChallengerFilterBottomSheet(
                type = type,
                filter = filter,
                onDismissRequest = { openedFilter = null },
                onSelected = { option ->
                    onFilterSelected(type, option)
                    openedFilter = null
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengerFilterChip(
    label: String,
    isSelected: Boolean,
    isFixed: Boolean,
    onClick: () -> Unit,
) {
    val active = isSelected && !isFixed
    // UChip을 수정하지 않고 고정 칩의 클릭 접근성 정보와 터치 효과만 호출부에서 제거한다.
    val chipModifier = Modifier.clearAndSetSemantics {
        text = AnnotatedString(label)
        if (!isFixed) {
            role = Role.Button
            accessibilityClick {
                onClick()
                true
            }
        }
    }
    val rippleConfiguration = LocalRippleConfiguration.current
    CompositionLocalProvider(
        LocalRippleConfiguration provides if (isFixed) null else rippleConfiguration,
        // Surface의 기본 최소 높이 대신 필터 디자인의 38dp 높이를 사용한다.
        LocalMinimumInteractiveComponentSize provides 0.dp,
    ) {
        UChip(
            text = label,
            modifier = chipModifier,
            backgroundColor = if (active) grey800() else grey100(),
            textColor = if (active) white() else grey500(),
            textStyle = CalloutBold,
            minHeight = 38.dp,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = if (isFixed) 16.dp else 12.dp,
                top = 6.dp,
                bottom = 6.dp,
            ),
            nextIcon = if (isFixed) null else ComponentR.drawable.ic_dropdown_down,
            onClick = if (isFixed) null else onClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminChallengerFilterBottomSheet(
    type: AdminChallengerFilterType,
    filter: AdminChallengerFilterUi,
    onDismissRequest: () -> Unit,
    onSelected: (AdminChallengerFilterOption?) -> Unit,
) {
    var keyword by rememberSaveable(type) { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = white(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = grey600()) },
    ) {
        AdminChallengerFilterSheetContent(
            type = type,
            filter = filter,
            keyword = keyword,
            onKeywordChanged = { keyword = it },
            onSelected = onSelected,
            modifier = Modifier.imePadding(),
        )
    }
}

@Composable
internal fun AdminChallengerFilterSheetContent(
    type: AdminChallengerFilterType,
    filter: AdminChallengerFilterUi,
    keyword: String,
    onKeywordChanged: (String) -> Unit,
    onSelected: (AdminChallengerFilterOption?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val matchingOptions = filter.options.filter { it.label.contains(keyword.trim(), ignoreCase = true) }
    // 기수에는 전체 항목이 없다. 지부·학교의 null 선택은 해당 필터 해제를 뜻한다.
    val options = if (type != AdminChallengerFilterType.GISU && keyword.isBlank()) {
        listOf(null) + matchingOptions
    } else {
        matchingOptions
    }
    val selectedIndex = options.indexOfFirst { it?.id == filter.selectedOption?.id }.takeIf { it >= 0 }
    USelectSheetContent(
        title = type.title,
        items = options.map { it?.label ?: AppStrings.COMMON_ALL }.toImmutableList(),
        modifier = modifier.background(white()),
        selectedIndex = selectedIndex,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        onSelect = { onSelected(options[it]) },
        headerContent = {
            Spacer(Modifier.height(16.dp))
            if (type == AdminChallengerFilterType.SCHOOL) {
                UTextField(
                    value = keyword,
                    onValueChange = onKeywordChanged,
                    placeholder = AppStrings.CHALLENGER_SCHOOL_SEARCH_PLACEHOLDER,
                    textStyle = Body,
                    backgroundColor = grey100(),
                    strokeColor = grey100(),
                    focusStrokeColor = grey200(),
                    prevIcon = painterResource(ComponentR.drawable.ic_search),
                    prevIconTint = grey400(),
                    singleLine = true,
                )
                Spacer(Modifier.height(24.dp))
            }
        },
        emptyContent = {
            UText(
                text = AppStrings.EMPTY_SEARCH_RESULT,
                style = Body,
                color = grey600(),
                modifier = Modifier.padding(vertical = 16.dp),
            )
        },
    )
}

private fun AdminChallengerFiltersUi.forType(type: AdminChallengerFilterType): AdminChallengerFilterUi =
    when (type) {
        AdminChallengerFilterType.GISU -> gisu
        AdminChallengerFilterType.CHAPTER -> chapter
        AdminChallengerFilterType.SCHOOL -> school
    }

private val AdminChallengerFilterType.label: String
    get() = when (this) {
        AdminChallengerFilterType.GISU -> AppStrings.CHALLENGER_GISU_FILTER
        AdminChallengerFilterType.CHAPTER -> AppStrings.CHALLENGER_CHAPTER_FILTER
        AdminChallengerFilterType.SCHOOL -> AppStrings.COMMON_SCHOOL
    }

private val AdminChallengerFilterType.title: String
    get() = when (this) {
        AdminChallengerFilterType.GISU -> AppStrings.CHALLENGER_GISU_SELECT_TITLE
        AdminChallengerFilterType.CHAPTER -> AppStrings.CHALLENGER_CHAPTER_SELECT_TITLE
        AdminChallengerFilterType.SCHOOL -> AppStrings.CHALLENGER_SCHOOL_SELECT_TITLE
    }

@Preview(showBackground = true, widthDp = 412, name = "필터 선택형")
@PreviewScreenSizes
@Composable
private fun ChallengerSelectableFiltersPreview() {
    var filters by remember {
        mutableStateOf(
            AdminChallengerFiltersUi(
                gisu = AdminChallengerFilterUi(
                    selectedOption = AdminChallengerFilterOption(9, "9기"),
                    options = persistentListOf(
                        AdminChallengerFilterOption(9, "9기"),
                        AdminChallengerFilterOption(8, "8기"),
                    ),
                ),
                chapter = AdminChallengerFilterUi(
                    options = persistentListOf(AdminChallengerFilterOption(1, "중앙지부")),
                ),
                school = previewSchoolFilter,
            )
        )
    }
    UmcTheme {
        AdminChallengerFilterBar(
            filters = filters,
            selectedPart = null,
            onPartFilterClick = {},
            onFilterSelected = { type, option ->
                filters = when (type) {
                    AdminChallengerFilterType.GISU -> filters.copy(gisu = filters.gisu.copy(selectedOption = option))
                    AdminChallengerFilterType.CHAPTER -> filters.copy(chapter = filters.chapter.copy(selectedOption = option))
                    AdminChallengerFilterType.SCHOOL -> filters.copy(school = filters.school.copy(selectedOption = option))
                }
            },
            modifier = Modifier.background(white()).padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 412, name = "필터 고정형")
@Composable
private fun ChallengerFixedFiltersPreview() {
    UmcTheme {
        AdminChallengerFilterBar(
            filters = AdminChallengerFiltersUi(
                gisu = AdminChallengerFilterUi(AdminChallengerFilterOption(9, "9기"), isFixed = true),
                chapter = AdminChallengerFilterUi(AdminChallengerFilterOption(1, "중앙지부"), isFixed = true),
                school = AdminChallengerFilterUi(AdminChallengerFilterOption(1, "중앙대학교"), isFixed = true),
            ),
            selectedPart = null,
            onPartFilterClick = {},
            onFilterSelected = { _, _ -> },
            modifier = Modifier.background(white()).padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 560, name = "학교 선택")
@Composable
private fun ChallengerSchoolFilterPreview() {
    var keyword by rememberSaveable { mutableStateOf("") }
    UmcTheme {
        AdminChallengerFilterSheetContent(
            type = AdminChallengerFilterType.SCHOOL,
            filter = previewSchoolFilter,
            keyword = keyword,
            onKeywordChanged = { keyword = it },
            onSelected = {},
        )
    }
}

private val previewSchoolFilter = AdminChallengerFilterUi(
    options = persistentListOf(
        AdminChallengerFilterOption(1, "가톨릭대학교"),
        AdminChallengerFilterOption(2, "동국대학교"),
        AdminChallengerFilterOption(3, "숭실대학교"),
        AdminChallengerFilterOption(4, "서울여자대학교"),
        AdminChallengerFilterOption(5, "중앙대학교"),
    ),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 560, name = "학교 검색 결과 없음")
@Composable
private fun ChallengerSchoolFilterEmptyPreview() {
    UmcTheme {
        AdminChallengerFilterSheetContent(
            type = AdminChallengerFilterType.SCHOOL,
            filter = previewSchoolFilter,
            keyword = "검색 결과 없음",
            onKeywordChanged = {},
            onSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 840, name = "학교 선택 긴 목록")
@Composable
private fun ChallengerLongSchoolFilterPreview() {
    UmcTheme {
        AdminChallengerFilterBottomSheet(
            type = AdminChallengerFilterType.SCHOOL,
            filter = AdminChallengerFilterUi(
                options = (1L..30L).map {
                    AdminChallengerFilterOption(it, "대학교 $it")
                }.toImmutableList(),
            ),
            onDismissRequest = {},
            onSelected = {},
        )
    }
}

internal fun AdminChallengerUiState.filterUi(): AdminChallengerFiltersUi {
    val context = searchFilters
    val fixed = context?.isOrganizationFixed ?: true
    val chapterOptions = context?.organization?.chapterList.orEmpty().filter {
        !fixed || it.id.toLong() == context?.chapterId
    }.map {
        AdminChallengerFilterOption(it.id.toLong(), it.name)
    }.toImmutableList()
    val schoolOptions = context?.availableSchools.orEmpty().filter {
        !fixed || it.schoolId.toLong() == context?.schoolId
    }.map {
        AdminChallengerFilterOption(it.schoolId.toLong(), it.schoolName)
    }.toImmutableList()
    return AdminChallengerFiltersUi(
        gisu = AdminChallengerFilterUi(
            selectedOption = selectedGisu?.let { AdminChallengerFilterOption(it.gisuId.toLong(), "${it.generation}기") },
            options = searchableGisus.map { AdminChallengerFilterOption(it.gisuId.toLong(), "${it.generation}기") }.toImmutableList(),
            isFixed = searchableGisus.size <= 1,
        ),
        chapter = AdminChallengerFilterUi(chapterOptions.firstOrNull { it.id == context?.chapterId }, chapterOptions, fixed),
        school = AdminChallengerFilterUi(schoolOptions.firstOrNull { it.id == context?.schoolId }, schoolOptions, fixed),
    )
}

