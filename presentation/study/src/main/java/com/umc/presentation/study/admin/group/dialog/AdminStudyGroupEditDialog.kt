package com.umc.presentation.study.admin.group.dialog

import com.umc.domain.model.enums.UserPart
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold

/**
 * 스터디 그룹 정보 수정 다이얼로그
 *
 * 현재 그룹의 이름과 파트를 표시하고,
 * 사용자가 수정한 값을 상위 화면으로 전달합니다.
 *
 * 이 Composable 자체에서는 실제 수정 API를 호출하지 않습니다.
 * 사용자의 입력과 선택만 처리하고,
 * 최종적으로 "수정 완료" 버튼을 누르면 onConfirm을 호출합니다.
 *
 * @param groupName 현재 입력된 그룹 이름
 * @param selectedPart 현재 선택된 그룹 파트
 * @param canConfirm 수정 완료 버튼 활성화 여부
 * @param onGroupNameChanged 그룹 이름 변경 시 호출
 * @param onPartChanged 파트 변경 시 호출
 * @param onConfirm 수정 완료 버튼 클릭 시 호출
 * @param onDismiss 다이얼로그를 닫을 때 호출
 */
@Composable
fun AdminStudyGroupEditDialog(
    groupName: String,
    selectedPart: String,
    canConfirm: Boolean,
    onGroupNameChanged: (String) -> Unit,
    onPartChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    /**
     * 파트 선택 DropdownMenu 표시 여부
     */
    var expanded by remember {
        mutableStateOf(false)
    }

    /**
     * 파트 선택 영역의 실제 너비
     *
     * DropdownMenu의 너비를 파트 선택 영역과
     * 동일하게 맞추기 위해 사용합니다.
     */
    var dropdownWidth by remember {
        mutableStateOf(0.dp)
    }

    /**
     * px 단위로 측정되는 Compose 좌표 값을
     * dp로 변환하기 위해 사용합니다.
     */
    val density = LocalDensity.current

    /**
     * 화면에서 선택 가능한 파트 목록
     *
     * 예전에는 라벨을 직접 나열해 Admin 이 빠지고 "Spring" 처럼 서버에 없는 값이 섞였습니다.
     */
    val parts = UserPart.selectable.map { it.label }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = grey000(),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp
                )
        ) {
            /**
             * 다이얼로그 제목
             */
            UText(
                text = "그룹 정보 수정",
                style = SubheadlineBold,
                color = grey900(),
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /**
             * 다이얼로그 설명
             */
            UText(
                text = "스터디 그룹의 기본 정보를 변경합니다.",
                style = Caption1,
                color = grey500(),
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            /**
             * 그룹 이름 입력 영역
             */
            UText(
                text = "그룹 이름",
                style = Caption1Bold,
                color = grey800()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            BasicTextField(
                value = groupName,
                onValueChange = onGroupNameChanged,
                textStyle = Body.copy(
                    color = grey800()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = grey000(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = grey300(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        /**
                         * 그룹 이름이 비어있는 경우
                         * 입력 예시를 placeholder로 표시합니다.
                         */
                        if (groupName.isBlank()) {
                            UText(
                                text = "예: React 실습 A팀",
                                style = Body,
                                color = grey400()
                            )
                        }

                        // 실제 BasicTextField 입력 영역
                        innerTextField()
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /**
             * 소속 파트 선택 영역
             */
            UText(
                text = "소속 파트",
                style = Caption1Bold,
                color = grey800()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /**
             * 파트 선택 영역과 DropdownMenu를
             * 같은 Box 안에 배치합니다.
             */
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)

                        /**
                         * 현재 파트 선택 Row의 너비 측정
                         *
                         * 측정된 너비를 dropdownWidth에 저장하여
                         * 아래 DropdownMenu와 너비를 동일하게 맞춥니다.
                         */
                        .onGloballyPositioned { coordinates ->
                            dropdownWidth = with(density) {
                                coordinates.size.width.toDp()
                            }
                        }
                        .background(
                            color = grey000(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = grey300(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            expanded = true
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /**
                     * 현재 선택된 파트
                     */
                    UText(
                        text = selectedPart,
                        style = Body,
                        color = grey800(),
                        modifier = Modifier.weight(1f)
                    )

                    /**
                     * 파트 선택 Dropdown 아이콘
                     */
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_dropdown_down
                        ),
                        contentDescription = null,
                        tint = grey500(),
                        modifier = Modifier.size(20.dp)
                    )
                }

                /**
                 * 파트 선택 DropdownMenu
                 *
                 * expanded가 true일 때 표시되며,
                 * 사용자가 파트를 선택하면 onPartChanged를 호출한 뒤
                 * DropdownMenu를 닫습니다.
                 */
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    },
                    modifier = Modifier
                        .width(dropdownWidth)
                        .background(grey000())
                ) {
                    parts.forEach { part ->
                        DropdownMenuItem(
                            text = {
                                UText(
                                    text = part,
                                    style = Body,
                                    color = grey800()
                                )
                            },
                            onClick = {
                                /**
                                 * 선택한 파트를 상위 상태로 전달
                                 */
                                onPartChanged(part)

                                /**
                                 * 선택 완료 후 DropdownMenu 닫기
                                 */
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            /**
             * 다이얼로그 하단 액션 버튼
             *
             * 왼쪽: 수정 취소
             * 오른쪽: 수정 완료
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 수정 취소
                UButton(
                    text = "취소",
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    backgroundColor = grey100(),
                    textColor = grey700(),
                    textStyle = SubheadlineBold,
                    borderWidth = 0.dp,
                    cornerRadius = 8.dp,
                )

                /**
                 * 수정 완료
                 *
                 * canConfirm이 false인 경우 버튼을 비활성화하고,
                 * 활성/비활성 상태에 따라 버튼 색상도 변경합니다.
                 */
                UButton(
                    text = "수정 완료",
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    enabled = canConfirm,
                    backgroundColor = if (canConfirm) {
                        indigo500()
                    } else {
                        grey200()
                    },
                    textColor = if (canConfirm) {
                        grey000()
                    } else {
                        grey400()
                    },
                    textStyle = SubheadlineBold,
                    cornerRadius = 8.dp,
                )
            }
        }
    }
}