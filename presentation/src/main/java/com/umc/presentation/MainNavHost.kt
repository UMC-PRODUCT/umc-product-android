package com.umc.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.mypage.mycard.MycardRoute
import com.umc.presentation.act.ActManageRoute
import com.umc.presentation.act.admin.challenger.AdminChallengerDetailRoute
import com.example.mypage.mycontent.MyContentRoute
import com.example.mypage.mypage.MypageRoute
import com.example.mypage.profile.ProfileRoute
import com.example.mypage.qrcode.QrCodeRoute
import com.example.mypage.receivedcard.ReceivedCardRoute
import com.umc.failcode.SignUpFailRoute
import com.umc.failcode.code.SignUpFailCodeRoute
import com.umc.permission.PermissionRoute
import com.umc.presentation.home.home.HomeRoute
import com.umc.presentation.home.notification.NotificationRoute
import com.umc.presentation.home.schedule.add.ScheduleAddRoute
import com.umc.presentation.home.schedule.detail.ScheduleDetailRoute
import com.umc.domain.model.enums.SignUpType
import com.umc.presentation.login.LoginRoute
import com.umc.presentation.login.emaillogin.EmailLoginRoute
import com.umc.presentation.signup.SignUpRoute
import com.umc.presentation.login.findpassword.FindPasswordRoute
import com.umc.presentation.notice.NoticeRoute
import com.umc.presentation.notice.adminnotice.AdminNoticeRoute
import com.umc.presentation.notice.search.NoticeSearchRoute
import com.umc.presentation.notice.detail.NoticeDetailRoute
import com.umc.presentation.notice.write.NoticeWriteRoute
import com.umc.presentation.signup.email.EmailSignUpRoute
import com.umc.presentation.signup.social.SocialSignUpRoute
import com.umc.presentation.splash.SplashRoute
import com.umc.presentation.community.CommunityRoute
import com.umc.presentation.community.chatting.CommunityChattingRoute
import com.umc.presentation.community.search.CommunitySearchRoute
import com.umc.presentation.community.create.CommunityCreateRoute
import com.umc.presentation.community.edit.CommunityEditRoute

private const val COMMUNITY_REFRESH_KEY = "community_refresh"
private const val NOTICE_REFRESH_KEY = "notice_refresh"
private const val COMMUNITY_THREAD_DEEP_LINK_BASE =
    "https://api.university.neordinary.com/community/threads"


/**
 * 공지 목록 화면에 새로고침이 필요함을 알린다.
 * 목록이 백스택 어디에 있든(작성 -> 목록, 상세 -> 목록) 찾아서 표시한다
 */
private fun NavHostController.notifyNoticeListRefresh() {
    runCatching { getBackStackEntry(MainDestination.Notice) }
        .getOrNull()
        ?.savedStateHandle
        ?.set(NOTICE_REFRESH_KEY, true)
}

@Composable
fun MainNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,


        // 스플래시에서 저장된 토큰으로 자동 로그인 판정 후 홈/로그인/코드입력으로 분기
        startDestination = MainDestination.Splash,

        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MainDestination.Splash> {
            // 스플래시는 백스택에서 제거 (뒤로가기 시 스플래시로 돌아가지 않도록)
            SplashRoute(
                navigateToLogin = {
                    navHostController.navigate(MainDestination.Login) {
                        popUpTo(MainDestination.Splash) { inclusive = true }
                    }
                },
                navigateToMain = {
                    navHostController.navigate(MainDestination.Home) {
                        popUpTo(MainDestination.Splash) { inclusive = true }
                    }
                },
                navigateToInputCode = {
                    navHostController.navigate(MainDestination.SignUpFailCode) {
                        popUpTo(MainDestination.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<MainDestination.Login> {
            LoginRoute(
                navigateToMain = {
                    navHostController.navigate(MainDestination.Home) {
                        popUpTo(MainDestination.Login) { inclusive = true }
                    }
                },
                navigateToSignUp = { oAuthToken ->
                    // 소셜 로그인 후 미가입 회원 -> 이메일 인증 단계부터 진행
                    navHostController.navigate(MainDestination.SocialSignUp(oAuthToken))
                },
                navigateToEmailLogin = {
                    navHostController.navigate(MainDestination.EmailLogin)
                },
                navigateToInputCode = {
                    // 챌린저 ID가 없는 회원 -> 코드 입력 화면으로 이동
                    navHostController.navigate(MainDestination.SignUpFailCode)
                },
            )
        }

        composable<MainDestination.EmailLogin> {
            EmailLoginRoute(
                navigateToBack = { navHostController.popBackStack() },
                navigateToMain = {
                    navHostController.navigate(MainDestination.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navigateToFindPassword = {
                    navHostController.navigate(MainDestination.FindPassword)
                },
                navigateToInputCode = {
                    // 챌린저 ID가 없는 회원 -> 코드 입력 화면으로 이동
                    navHostController.navigate(MainDestination.SignUpFailCode)
                },
                navigateToSignUp = {
                    navHostController.navigate(MainDestination.EmailSignUp)
                },
            )
        }

        // 비밀번호 찾기 (이메일 인증 후 새 비밀번호 설정)
        composable<MainDestination.FindPassword> {
            FindPasswordRoute(
                navigateToBack = { navHostController.popBackStack() },
                navigateToLogin = { navHostController.popBackStack() },
            )
        }

        // 개인정보 입력 단계 (signUpType에 따라 소셜/이메일 회원가입 API 분기)
        composable<MainDestination.SignUp> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.SignUp>()
            SignUpRoute(
                signUpType = SignUpType.valueOf(destination.signUpType),
                oAuthVerificationToken = destination.oAuthVerificationToken,
                emailVerificationToken = destination.emailVerificationToken,
                rawPassword = destination.rawPassword,
                navigateToBack = { navHostController.popBackStack() },
                navigateToPermission = { navHostController.navigate(MainDestination.Permission) },
            )
        }

        // 소셜 회원가입 (이메일 인증)
        composable<MainDestination.SocialSignUp> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.SocialSignUp>()
            SocialSignUpRoute(
                oAuthVerificationToken = destination.oAuthVerificationToken,
                navigateToBack = { navHostController.popBackStack() },
                navigateToNext = { oAuthToken, emailToken ->
                    navHostController.navigate(
                        MainDestination.SignUp(
                            signUpType = SignUpType.SOCIAL.name,
                            oAuthVerificationToken = oAuthToken,
                            emailVerificationToken = emailToken,
                        )
                    )
                },
            )
        }

        // 이메일 회원가입 (이메일 인증 + 비밀번호 설정)
        composable<MainDestination.EmailSignUp> {
            EmailSignUpRoute(
                navigateToBack = { navHostController.popBackStack() },
                navigateToNext = { emailToken, rawPassword ->
                    navHostController.navigate(
                        MainDestination.SignUp(
                            signUpType = SignUpType.EMAIL.name,
                            emailVerificationToken = emailToken,
                            rawPassword = rawPassword,
                        )
                    )
                },
            )
        }

        composable<MainDestination.Permission> {
            PermissionRoute(
                navigateToBack = { navHostController.popBackStack() },
                navigateToMain = {
                    // 스플래시는 이미 스택에서 제거된 상태라 가입 스택 전체를 비우고 홈으로
                    navHostController.navigate(MainDestination.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navigateToFail = {
                    navHostController.navigate(MainDestination.SignUpFail) {
                        popUpTo(MainDestination.Permission) { inclusive = true }
                    }
                },
            )
        }

        composable<MainDestination.SignUpFail> {
            SignUpFailRoute(
                navigateToCode = { navHostController.navigate(MainDestination.SignUpFailCode) },
                navigateToLogin = {
                    navHostController.navigate(MainDestination.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable<MainDestination.SignUpFailCode> {
            SignUpFailCodeRoute(
                navigateToBack = { navHostController.popBackStack() },
                navigateToHome = {
                    navHostController.navigate(MainDestination.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        /**공지 탭에 대한 내용입니다.**/
        //공지 목록
        composable<MainDestination.Notice> { backStackEntry ->
            // 공지 작성·수정·삭제 후 돌아오면 목록을 다시 불러온다
            val shouldRefresh by backStackEntry
                .savedStateHandle
                .getStateFlow(NOTICE_REFRESH_KEY, false)
                .collectAsStateWithLifecycle()

            NoticeRoute(
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle[NOTICE_REFRESH_KEY] = false
                },
                navigateToSearch = { gisuId, noticeTab, chapterId, schoolId, part ->
                    navHostController.navigate(
                        MainDestination.NoticeSearch(gisuId, noticeTab, chapterId, schoolId, part)
                    )
                },
                navigateToAdminNotice = { gisuId ->
                    navHostController.navigate(MainDestination.AdminNotice(gisuId))
                },
                navigateToWrite = {
                    navHostController.navigate(MainDestination.NoticeWrite())
                },
                navigateToDetail = { noticeId ->
                    navHostController.navigate(MainDestination.NoticeDetail(noticeId))
                },
            )
        }

        //공지 작성 (권한별 카테고리/게시판 분류). noticeId가 있으면 수정 모드
        composable<MainDestination.NoticeWrite> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.NoticeWrite>()
            NoticeWriteRoute(
                editNoticeId = destination.noticeId,
                navigateToBack = { navHostController.popBackStack() },
                onSubmitSuccess = {
                    navHostController.notifyNoticeListRefresh()
                    navHostController.popBackStack()
                },
            )
        }

        //공지 상세
        composable<MainDestination.NoticeDetail> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.NoticeDetail>()
            NoticeDetailRoute(
                noticeId = destination.noticeId,
                navigateToBack = { navHostController.popBackStack() },
                navigateToEdit = { noticeId ->
                    navHostController.navigate(MainDestination.NoticeWrite(noticeId))
                },
            )
        }

        //운영진 공지 (권한별 탭 노출)
        composable<MainDestination.AdminNotice> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.AdminNotice>()
            AdminNoticeRoute(
                gisuId = destination.gisuId,
                navigateToBack = { navHostController.popBackStack() },
                navigateToSearch = { gisuId, noticeTab, schoolId ->
                    navHostController.navigate(
                        MainDestination.NoticeSearch(
                            gisuId = gisuId,
                            noticeTab = noticeTab,
                            schoolId = schoolId,
                        )
                    )
                },
                navigateToDetail = { noticeId ->
                    navHostController.navigate(MainDestination.NoticeDetail(noticeId))
                },
            )
        }

        //공지 검색
        composable<MainDestination.NoticeSearch> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.NoticeSearch>()
            NoticeSearchRoute(
                gisuId = destination.gisuId,
                noticeTab = destination.noticeTab,
                chapterId = destination.chapterId,
                schoolId = destination.schoolId,
                part = destination.part,
                navigateToBack = { navHostController.popBackStack() },
                navigateToDetail = { noticeId ->
                    navHostController.navigate(MainDestination.NoticeDetail(noticeId))
                },
            )
        }

        /**홈 화면 탭에 대한 내용입니다.**/
        composable<MainDestination.Act> {
            ActManageRoute(
                onNavigateToChallengerDetail = { challengerId ->
                    navHostController.navigate(
                        MainDestination.AdminChallengerDetail(challengerId)
                    )
                }
            )
        }

        composable<MainDestination.AdminChallengerDetail> { backStackEntry ->
            val destination =
                backStackEntry.toRoute<MainDestination.AdminChallengerDetail>()
            AdminChallengerDetailRoute(
                challengerId = destination.challengerId,
                onNavigateToBack = { navHostController.popBackStack() },
            )
        }



        //홈 화면
        composable<MainDestination.Home> {
            HomeRoute(
                onNavigateToNotice = {
                    navHostController.navigate(MainDestination.Notice)
                },
                onNavigateToScheduleAdd = {
                    navHostController.navigate(MainDestination.ScheduleAdd)
                },
                onNavigateToScheduleDetail = {
                    //여기서 인자를 던지면, savedStateHandle에서 받아채서 ViewModel에서 처리
                    navHostController.navigate(MainDestination.ScheduleDetail(scheduleId = it.id, plusDay = it.plusDay))
                },
                onNavigateToNotification = {
                    navHostController.navigate(MainDestination.Notification)
                },
                onNavigateToCardShare = {
                    navHostController.navigate(MainDestination.Mycard(openExchangeDialog = true))
                }
            )
        }
        //공지 화면
        composable<MainDestination.Notification>{
            NotificationRoute()
        }


        //일정 생성
        composable<MainDestination.ScheduleAdd> {
            ScheduleAddRoute(
                scheduleId = -1,
                onNavigateToBack = {navHostController.popBackStack()},
                onShowAttendanceDialog = { _, _ -> }
            )
        }
        //일정 수정
        composable<MainDestination.ScheduleEdit> {backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.ScheduleEdit>()

            ScheduleAddRoute(
                scheduleId = destination.scheduleId, // 실제 수정할 scheduleId
                onNavigateToBack = { navHostController.popBackStack() },
                onShowAttendanceDialog = { _, _ -> }
            )
        }
        //일정 상세
        composable<MainDestination.ScheduleDetail>{ data ->

            ScheduleDetailRoute(
                onBackClick = {navHostController.popBackStack()},
                onNavigateToAttendSchedule = {
                    /**TODO. 일정 출석 페이지로 이동하기**/
                },
                onNavigateToEditSchedule = { scheduleId ->
                    navHostController.navigate(MainDestination.ScheduleEdit(scheduleId = scheduleId))
                }
            )
        }

        /**마이페이지 관련 정의**/

        //신 마이페이지
        composable<MainDestination.Mycard>(
            deepLinks = listOf(
                navDeepLink {
                    // 스토어 URL 기반 딥링크 패턴 매핑
                    uriPattern = "umc://card?memberId={memberId}"
                },
                navDeepLink {
                    uriPattern = "https://api.university.neordinary.com/community/threads/card?memberId={memberId}"
                }
            )
        ) { backStackEntry ->
            // Type-Safe Navigation 파라미터 추출 (딥링크 포함)
            val mycardDestination = backStackEntry.toRoute<MainDestination.Mycard>()
            val targetMemberId = mycardDestination.memberId
            val openExchangeDialog = mycardDestination.openExchangeDialog


            MycardRoute(
                targetMemberId = targetMemberId,
                openExchangeDialog = openExchangeDialog,
                onNavigateToMypage = {
                    navHostController.navigate(MainDestination.Mypage)
                },
                onNavigateToMyqrCode = {
                    navHostController.navigate(MainDestination.Qrcode)
                },
                onNavigateToEditCard = {
                    navHostController.navigate(MainDestination.MyProfile)
                },
                onNavigateToReceivedCard = {
                    navHostController.navigate(MainDestination.ReceivedCard)
                }
            )
        }

        //구 마이페이지 -> 신 설정
        composable<MainDestination.Mypage>{
            MypageRoute(
                onNavigateToEditProfile = {
                    navHostController.navigate(MainDestination.MyProfile)
                },
                onNavigateToMyContent = {type ->
                    navHostController.navigate(MainDestination.MyContent(showType = type))
                                        },
                onNavigateToLogin = {},
                onNavigateToQrCode = {
                    navHostController.navigate(MainDestination.Qrcode)
                },
                onNavigateToBack = {
                    navHostController.popBackStack()
                }
            )

        }

        //내 활동
        composable<MainDestination.MyContent> {
            MyContentRoute(
                onNavigateToPostDetail = { id ->
                    /**TODO id를 줘서 커뮤니티 게시글 상세 페이지로 이동*/
                }
            )
        }

        //내 프로필
        composable<MainDestination.MyProfile> {
            ProfileRoute(
                onNavigateToBack = {
                    navHostController.popBackStack()
                }
            )
        }

        /**qr 코드**/
        composable<MainDestination.Qrcode> {
            QrCodeRoute(
                onNavigateToBack = {
                    navHostController.popBackStack()
                }
            )
        }

        //받은 명함
        composable<MainDestination.ReceivedCard> {
            ReceivedCardRoute (
                onNavigateToBack = {
                    navHostController.popBackStack()
                }
            )
        }

        /** 커뮤니티 화면 **/
        composable<MainDestination.Community> {
            CommunityRoute(
                onNavigateToThreadDetail = { threadId ->
                    navHostController.navigate(
                        MainDestination.CommunityChatting(
                            threadId = threadId,
                        )
                    )
                },
                onNavigateToSearch = {
                    navHostController.navigate(
                        MainDestination.CommunitySearch
                    )
                },
                onNavigateToCreateThread = {
                    navHostController.navigate(
                        MainDestination.CommunityCreate
                    )
                },
                onNavigateToEditThread = { threadId ->
                    navHostController.navigate(
                        MainDestination.CommunityEdit(
                            threadId = threadId,
                        )
                    )
                },
            )
        }

        /** 커뮤니티 스레드 만들기 화면 **/
        composable<MainDestination.CommunityCreate> {
            CommunityCreateRoute(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                onNavigateToEmojiPicker = {
                    // TODO: 이모지 선택 화면 또는 다이얼로그 연결
                },
                onCreateSuccess = {
                    navHostController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            COMMUNITY_REFRESH_KEY,
                            true,
                        )

                    navHostController.popBackStack()
                },
            )
        }

        /** 커뮤니티 스레드 수정 화면 **/
        composable<MainDestination.CommunityEdit> { backStackEntry ->
            val destination =
                backStackEntry.toRoute<MainDestination.CommunityEdit>()

            CommunityEditRoute(
                threadId = destination.threadId,
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                onNavigateToEmojiPicker = {
                    // TODO: 이모지 선택 화면 또는 다이얼로그 연결
                },
                onEditSuccess = {
                    navHostController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            COMMUNITY_REFRESH_KEY,
                            true,
                        )

                    navHostController.popBackStack()
                },
            )
        }

        /** 커뮤니티 검색 화면 **/
        composable<MainDestination.CommunitySearch> {
            CommunitySearchRoute(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                onNavigateToThreadDetail = { threadId ->
                    navHostController.navigate(
                        MainDestination.CommunityChatting(
                            threadId = threadId,
                        )
                    )
                },
            )
        }

        composable<MainDestination.CommunityChatting>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "$COMMUNITY_THREAD_DEEP_LINK_BASE?threadId={threadId}"
                }
            ),
        ) { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.CommunityChatting>()
            val shouldRefresh by backStackEntry
                .savedStateHandle
                .getStateFlow(COMMUNITY_REFRESH_KEY, false)
                .collectAsStateWithLifecycle()

            CommunityChattingRoute(
                onBack = {
                    navHostController.popBackStack()
                },
                onEditThread = {
                    navHostController.navigate(
                        MainDestination.CommunityEdit(destination.threadId)
                    )
                },
                onViewParticipantProfile = {},
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle[COMMUNITY_REFRESH_KEY] = false
                    navHostController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(COMMUNITY_REFRESH_KEY, true)
                },
                onThreadDeleted = {
                    navHostController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(COMMUNITY_REFRESH_KEY, true)
                    navHostController.popBackStack()
                },
            )
        }
    }
}
