package com.umc.presentation.study.admin.group.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold

/**
 * 스터디 그룹 삭제 확인 다이얼로그
 *
 * 관리자가 스터디 그룹 삭제를 요청했을 때
 * 실제 삭제 API를 호출하기 전에 한 번 더 확인하기 위한 다이얼로그입니다.
 *
 * 삭제 시 그룹 정보를 복구할 수 없다는 안내 문구를 표시하고,
 * 사용자는 취소 또는 삭제하기를 선택할 수 있습니다.
 *
 * @param onDelete 삭제하기 버튼 클릭 시 호출
 * @param onDismiss 취소 또는 다이얼로그 외부 클릭 시 호출
 */
@Composable
fun AdminStudyGroupDeleteDialog(
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = grey000(),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * 삭제 경고 아이콘 영역
                 *
                 * 삭제처럼 주의가 필요한 작업임을 나타내기 위해
                 * 빨간색 계열의 배경과 경고 아이콘을 표시합니다.
                 */
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = red100(),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_error_filled
                        ),
                        contentDescription = null,
                        tint = red500(),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                /**
                 * 삭제 확인 제목
                 */
                UText(
                    text = "그룹을 삭제하시겠습니까?",
                    style = SubheadlineBold,
                    color = grey900(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * 삭제 시 주의사항
                 *
                 * 삭제된 그룹은 복구할 수 없고
                 * 연결된 스터디 데이터 역시 삭제된다는 내용을 안내합니다.
                 */
                UText(
                    text = "삭제된 스터디 그룹 정보는 복구할 수 없으며,\n" +
                            "연결된 모든 스터디 데이터가 삭제됩니다.",
                    style = Caption1,
                    color = grey600(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                /**
                 * 하단 액션 버튼
                 *
                 * 왼쪽: 삭제 취소
                 * 오른쪽: 실제 그룹 삭제 진행
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 삭제 취소 버튼
                    UButton(
                        text = "취소",
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = grey000(),
                        textColor = grey700(),
                        textStyle = SubheadlineBold,
                        borderWidth = 1.dp,
                        borderColor = grey200(),
                        cornerRadius = 8.dp,
                    )

                    // 그룹 삭제 버튼
                    UButton(
                        text = "삭제하기",
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = red100(),
                        textColor = red500(),
                        textStyle = SubheadlineBold,
                        borderWidth = 0.dp,
                        cornerRadius = 8.dp,
                    )
                }
            }
        }
    }
}