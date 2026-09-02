package com.example.mypage.mypage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.dialog.AddCodeDialog
import com.example.mypage.nearby.NearbyEvent
import com.example.mypage.nearby.NearbyUiState
import com.example.mypage.nearby.NearbyViewModel
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.user.UserApiClient
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.theme.red500
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.OutLinkType
import com.umc.domain.model.mypage.UserCard


/**
 * 앱 설정, 외부 링크 연결, 약관 및 계정 제어(로그아웃/탈퇴)를 담당하는 설정 화면 컴포저블
 *
 * 본래 마이페이지 탭의 메인 화면이었으나, 개편을 통해 MycardScreen이 마이페이지 최초 진입점 역할을 맡게 되면서
 * 본 화면은 내 카드 상단 탑바의 설정 아이콘을 클릭했을 때 진입하는 설정 전용 라우트로 기능이 변경되었습니다.
 *

 * [유의사항]
 * - 마이페이지 탭 클릭 시 최초 노출되는 화면은 MycardScreen이며, MypageScreen은 설정 버튼을 통해 진입하는 서브 화면 구조입니다.
 */

@Composable
fun MypageRoute(
    viewModel: MypageViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
    onNavigateToEditProfile: () -> Unit, //프로필 페이지 이동
    onNavigateToMyContent: (String) -> Unit, //내가 쓴 글 이동
    onNavigateToLogin: () -> Unit, //로그인 이동(로그아웃 or 탈퇴)
    onNavigateToBack: () -> Unit, //뒤로 가기
    onNavigateToQrCode: () -> Unit, /**qr 코드 이동**/
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddCodeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteUserDialog by remember { mutableStateOf(false) }

    //OutLink 3종(깃허브,링크드인,블로그)
    var selectedOutLinkType by remember { mutableStateOf<OutLinkType?>(null) }
    var showOutLinkDialog by remember { mutableStateOf(false) }


    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when(event){
                //깃허브 누를 때
                is MypageEvent.NavigateToGithub -> {
                    if (uiState.githubUrl.isBlank()) {
                        selectedOutLinkType = OutLinkType.GITHUB
                        showOutLinkDialog = true
                    } else {
                        openWebpage(context, uiState.githubUrl)
                    }
                }
                //블로그 누를 때
                is MypageEvent.NavigateToBlog -> {
                    if (uiState.blogUrl.isBlank()) {
                        selectedOutLinkType = OutLinkType.BLOG
                        showOutLinkDialog = true
                    } else {
                        openWebpage(context, uiState.blogUrl)
                    }
                }
                //링크드인 누를 때
                is MypageEvent.NavigateToLinkedin -> {
                    if (uiState.linkedinUrl.isBlank()) {
                        selectedOutLinkType = OutLinkType.LINKEDIN
                        showOutLinkDialog = true
                    } else {
                        openWebpage(context, uiState.linkedinUrl)
                    }
                }
                //프로필 누를 때 (이동)
                is MypageEvent.NavigateToEditProfile -> onNavigateToEditProfile()
                //챌린저 기록 추가 누를 때 (이동)
                is MypageEvent.NavigateToAddActivity -> {
                    showAddCodeDialog = true
                }
                //UMC 카카오톡 문의 누를 때
                is MypageEvent.NavigateToAssistUmc -> openKakaoChannel(context, event.channelId)
                //알림 설정 누를 때 + 위치 설정 누를 때
                is MypageEvent.NavigateToSettingNotice,
                is MypageEvent.NavigateToSettingLocation -> openPermissionPage(context)
                //개인정보처리 방침 누를 때
                is MypageEvent.NavigateToPersonalInformation -> openWebpage(context, event.privacyTerms)
                //이용약관 누를 때
                is MypageEvent.NavigateToUseManual -> openWebpage(context, event.manualTerms)
                //UMC 웹사이트 누를 때
                is MypageEvent.NavigateToWebstieUmc -> openWebpage(context, uiState.websiteUMC)
                //UMC 인스타그램 누를 때
                is MypageEvent.NavigateToInstagramUmc -> openWebpage(context, uiState.instagramUMC)
                //로그아웃 누를 때
                is MypageEvent.Logout -> {
                    showLogoutDialog = true
                }
                //회원탈퇴 누를 때
                is MypageEvent.DeleteUser -> {
                    showDeleteUserDialog = true
                }
                //온보드 누를 때
                is MypageEvent.MoveToOnBoardPage -> onNavigateToLogin()

                //로그아웃 시 이동
                is MypageEvent.Logout -> onNavigateToLogin()

                //챌린저 기록 추가 시 (다이얼로그)
                is MypageEvent.ConfirmAddCode -> {
                    Toast.makeText(context, "활동기록이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    showAddCodeDialog = false
                }
                //챌린저 기록 추가 실패 시(다이얼로그)
                is MypageEvent.FailAddCode -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    showAddCodeDialog = false

                }
                //뒤로 가기
                is MypageEvent.NavigateToBack -> onNavigateToBack()

                else -> {}
            }
        }
    }

    MypageScreen(
        uiState = uiState,
        onBackClick = viewModel::navigateToBack,
        onGithubClick = viewModel::navigateToGithub, //깃허브 이동
        onLinkedinClick = viewModel::navigateToLinkedin, //링크드인 이동
        onBlogClick = viewModel::navigateToBlog, //블로그 이동
        onAssistClick = viewModel::navigateToAssistUmc, //UMC 어시스트(카톡) 이동
        onNoticeSettingClick = viewModel::navigateToSettingNotice, //알림 설정 이동
        onLocationSettingClick = viewModel::navigateToSettingLocation, //위치 설정 이동
        onPrivacyClick = viewModel::navigateToPersonalInformation, //개인정보처리 방침 이동
        onTermsClick = viewModel::navigateToUseManual, //이용약관 이동
        onLogoutClick = viewModel::logout, //로그아웃
        onDeleteUserClick = viewModel::showDeleteUserDialog, //회원 탛퇴
        onWebsiteClick = viewModel::navigateToWebsiteUmc, //웹사이트 이동
        onInstagramClick = viewModel::navigateToInstagramUmc //인스타그램 이동

    )

    // 외부 링크 미등록 안내 다이얼로그 컴포저블
    if(showOutLinkDialog && selectedOutLinkType != null){
        val name = when(selectedOutLinkType) {
            OutLinkType.GITHUB -> "Github를"
            OutLinkType.LINKEDIN -> "LinkedIn을"
            OutLinkType.BLOG -> "Blog를"
            else -> ""
        }

        UDialog(
            title = "${name} 열 수 없어요.",
            content = "아직 등록된 링크가 없습니다. 프로필에서 링크를 추가해 주세요.",
            onDismissRequest = {
                showOutLinkDialog = false
                selectedOutLinkType = null
            },
            isTwoButton = false,
            confirmText = "확인"
        )

    }

    // 활동 코드 입력 다이얼로그 컴포저블 (현재 사용 X)
    if(showAddCodeDialog){
        AddCodeDialog(
            code = uiState.code,
            onCodeChanged = viewModel::onCodeChanged,
            onConfirmClick = viewModel::addChallengerCode,
            onDismissRequest = {showAddCodeDialog = false}
        )
    }

    // 로그아웃 확인 다이얼로그 컴포저블
    if(showLogoutDialog){


        UDialog(
            title = AppStrings.MYPAGE_LOGOUT_TITLE,
            content = AppStrings.MYPAGE_LOGOUT_CONTENT,
            isTwoButton = true,
            positiveText = "로그아웃",
            negativeText = "취소",
            onPositive = {
                /**TODO: 로그아웃 로직 연결*/
                showLogoutDialog = false
                viewModel.navigateToOnboard()
            },
            onNegative = {
                showLogoutDialog = false
            },
            onDismissRequest = {showLogoutDialog = false}
        )


    }

    // 회원 탈퇴 확인 다이얼로그 컴포저블
    if(showDeleteUserDialog){

        UDialog(
            title = AppStrings.MYPAGE_DELTE_USER_TITLE,
            content = AppStrings.MYPAGE_DELTE_USER_CONTENT,
            isTwoButton = true,
            positiveText = "계정 삭제",
            negativeText = "취소",
            onPositive = {
                /**TODO: 회원 탈퇴 로직 연결*/
                showDeleteUserDialog = false
                viewModel.deleteUser()
            },
            onNegative = {
                showDeleteUserDialog = false
            },
            onDismissRequest = {showDeleteUserDialog = false}
        )


    }


    }




/**
 * 외부 브라우저 인텐트를 호출하는 암시적 인텐트 연결 메서드
 */
private fun openWebpage(context: Context, url: String) {
    if (url.isBlank()) return

    try {
        val webpage: Uri = url.toUri()
        val intent = Intent(Intent.ACTION_VIEW, webpage).apply {
            //Activity 외부(Context)에서 시작할 경우 필요한 플래그
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        //브라우저를 실행할 수 있는 앱이 있는지 확인
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // 브라우저조차 없는 특수한 상황
            val webIntent = Intent(Intent.ACTION_VIEW, webpage)
            context.startActivity(webIntent)
        }
    }
    catch (e: Exception){
        e.printStackTrace()
    }
}

/**
 * 안드로이드 OS 세부 애플리케이션 설정 화면을 호출하는 메서드
 */
private fun openPermissionPage(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            // 현재 패키지 정보를 URI 데이터로 삽입
            data = Uri.fromParts("package", context.packageName, null)
            // 기존 화면 흐름과 분리하여 새로운 태스크로 실행
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {

    }

}

/**
 * 카카오톡 앱 채널 채팅창을 호출하거나 웹 링크로 라우팅하는 메서드
 */
private fun openKakaoChannel(context: Context, channelId: String){

    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        // 카카오톡이 설치되어 있다면 앱 내 채널 채팅창으로 이동
        TalkApiClient.instance.chatChannel(context, channelId) { error ->
            if(error != null){
                // 만약 에러 호출 시 웹 브라우저 시도
                openKakaoChannelIntent(context, channelId)
            }
        }
    } else {
        // 카카오톡이 없다면 웹 브라우저로 우회
        openKakaoChannelIntent(context, channelId)
    }
}

/**
 * 카카오 채널 웹 인텐트를 실행하는 메서드
 */
fun openKakaoChannelIntent(context: Context, channelId: String){
    val url = "https://pf.kakao.com/$channelId/chat"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

@Composable
fun MypageScreen(
    uiState: MypageUiState,
    onBackClick: () -> Unit,
    onGithubClick: () -> Unit,
    onLinkedinClick: () -> Unit,
    onBlogClick: () -> Unit,
    onAssistClick: () -> Unit,
    onNoticeSettingClick: () -> Unit,
    onLocationSettingClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteUserClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onInstagramClick: () -> Unit,
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey100())
    ) {

        MypageTopBar(
            onBackClick = {
                onBackClick()
            }
        )

        //중첩 스크롤 대비 LazyColumn 뼈대
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(grey100())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {


            item {
                //외부 링크 3종 섹션
                MypageSectionTitle(
                    text = AppStrings.MYPAGE_OUT_LINK
                )
                MypageListCard {
                    MypageListItem(
                        R.drawable.ic_github_link,
                        AppStrings.GITHUB,
                        onClick = onGithubClick
                    )
                    MypageListItem(
                        R.drawable.ic_linkedin_link,
                        AppStrings.LINKEDIN,
                        onClick = onLinkedinClick
                    )
                    MypageListItem(
                        R.drawable.ic_blog_link,
                        AppStrings.BLOG,
                        onClick = onBlogClick
                    )
                }
            }

            item {
                //지원 섹션
                MypageSectionTitle(
                    text = AppStrings.ASSIST
                )
                MypageListCard {
                    MypageListItem(
                        R.drawable.ic_inquire_umc,
                        AppStrings.MYPAGE_INQUIRE_UMC_KAKAO,
                        onClick = onAssistClick
                    )
                }
            }

            item {
                //설정 섹션
                MypageSectionTitle(
                    text = AppStrings.SETTING
                )
                MypageListCard {
                    MypageListItem(
                        R.drawable.ic_bottom_nav_notice,
                        AppStrings.MYPAGE_SETTING_NOTICE,
                        onClick = onNoticeSettingClick
                    )
                    MypageListItem(
                        R.drawable.ic_location,
                        AppStrings.MYPAGE_SETTING_LOCATION,
                        onClick = onLocationSettingClick
                    )
                }

            }

            //소셜 연동 섹션(조건부)
            if (uiState.isSocialCardVisible) {
                item {
                    MypageSectionTitle(
                        text = AppStrings.MYPAGE_SOCIAL
                    )
                    MypageSocialLinkCard(
                        targetPlatform = uiState.targetPlatform,
                        onClick = { /** TODO. VIewModel에서 로그인 화면으로 이동하기 **/ }
                    )
                }
            }

            item {
                //법률 섹션
                MypageSectionTitle(
                    text = AppStrings.LAW
                )
                MypageListCard {
                    MypageListItem(
                        R.drawable.ic_hand_question,
                        AppStrings.MYPAGE_PERSONAL_INFORMATION,
                        onClick = onPrivacyClick
                    )
                    MypageListItem(
                        R.drawable.ic_agreement,
                        AppStrings.MYPAGE_USE_MANUAL,
                        onClick = onTermsClick
                    )
                }
            }

            item {
                //하단 퇄퇴/로그아웃 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //회원 탈퇴
                    UButton(
                        text = AppStrings.DELETE_USER,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        backgroundColor = grey000(),
                        textColor = red500(),
                        textStyle = UmcTypographyTokens.Body,
                        cornerRadius = 12.dp,
                        onClick = onDeleteUserClick
                    )
                    //로그아웃
                    UButton(
                        text = AppStrings.LOGOUT,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        backgroundColor = grey000(),
                        textColor = grey800(),
                        textStyle = UmcTypographyTokens.Body,
                        cornerRadius = 12.dp,
                        onClick = onLogoutClick
                    )
                }
            }

            item {
                //UMC 외부 채널
                MypageSectionTitle(
                    text = AppStrings.MYPAGE_UMC_OUT_CHANNEL
                )
                UMCChannelButtons(
                    onWebsiteClick = onWebsiteClick,
                    onInstagramClick = onInstagramClick
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(64.dp)
                )
            }

        }
    }
}

/**
 * 설정 상단 탑바 컴포저블
 */
@Composable
fun MypageTopBar(
    onBackClick: () -> Unit //뒤로 가기
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey000())
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        //뒤로 가기 버튼
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .clickable { onBackClick() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.SETTING,
            style = UmcTypographyTokens.Title2Bold,
            modifier = Modifier
                .padding(horizontal = 6.dp)

        )

        
    }
}



/**
 * 섹션 헤더 타이틀 컴포저블
 */
@Composable
fun MypageSectionTitle(text: String) {
    UText(
        text = text,
        style = UmcTypographyTokens.HeadlineBold,
        color = grey800(),
        modifier = Modifier
            .padding(top = 32.dp)
    )
}

/**
 * 리스트 항목을 둘러싸는 라운드 카드 컴포저블
 */
@Composable
fun MypageListCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey000()),
        elevation = CardDefaults.cardElevation(0.dp),
        content = content
    )
}

/**
 * 설정 개별 메뉴 행 컴포저블
 */
@Composable
fun MypageListItem(
    iconRes: Int,
    text: String,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
            tint = grey800()
        )
        UText(
            text = text,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            style = UmcTypographyTokens.Body,
            color = grey800()
        )
        if (showArrow) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400()
            )
        }
    }
}

/**
 * 소셜 로그인 플랫폼 표시 배지 컴포저블
 */
@Composable
fun SocialBadge(platform: LoginType) {
    val bgColor = if (platform == LoginType.KAKAO) Color(0xFFFEE500) else grey100()

    Surface(
        modifier = Modifier
            .padding(end = 4.dp)
            .padding(start = 4.dp),
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        UText(
            text = if (platform == LoginType.KAKAO) "카카오" else "구글",
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp),
            style = UmcTypographyTokens.Caption1Bold,
            color = grey700()
        )
    }
}
/**
 * 미연동 소셜 계정 연동 안내 카드 컴포저블
 * (카카오 /구글 로그인 여부에 따라 보여주기 다름)
 */
@Composable
fun MypageSocialLinkCard(
    targetPlatform: LoginType,
    onClick: () -> Unit
) {
    MypageListCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = if (targetPlatform == LoginType.KAKAO) R.drawable.ic_kakao else R.drawable.ic_google
                ),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            UText(
                text = if (targetPlatform == LoginType.KAKAO) "카카오" else "구글",
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                style = UmcTypographyTokens.Body,
                color = grey800()
            )
            UText(
                text = "연동하기",
                style = UmcTypographyTokens.Subheadline,
                color = grey800(),
                modifier = Modifier
                    .padding(end = 8.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400()
            )
        }
    }
}

/**
 * UMC 외부 채널(웹사이트 및 인스타그램) 연결 버튼 그룹 컴포저블
 */
@Composable
fun UMCChannelButtons(
    onWebsiteClick: () -> Unit,
    onInstagramClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_out_website_umc),
            contentDescription = "Website",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable { onWebsiteClick() }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_out_instagram_umc),
            contentDescription = "Instagram",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable { onInstagramClick() }
        )
    }
}