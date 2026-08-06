package com.umc.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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
import com.umc.presentation.signup.email.EmailSignUpRoute
import com.umc.presentation.signup.social.SocialSignUpRoute
import com.umc.presentation.splash.SplashRoute
import androidx.navigation.navDeepLink

@Composable
fun MainNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,

        startDestination = if (BuildConfig.DEBUG) {
            MainDestination.Login
        } else {
            MainDestination.Home
        },


        //startDestination = MainDestination.Mycard(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MainDestination.Splash> {
            SplashRoute(
                navigateToLogin = { navHostController.navigate(MainDestination.Login) },
                navigateToMain = {
                    navHostController.navigate(MainDestination.Home)
                },
                navigateToInputCode = {
                    navHostController.navigate(MainDestination.SignUpFailCode)
                }
            )
        }

        composable<MainDestination.Login> {
            LoginRoute(
                navigateToMain = {
                    navHostController.navigate(MainDestination.Act) {
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
                    navHostController.navigate(MainDestination.Act) {
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
                    navHostController.navigate(MainDestination.Home) {
                        popUpTo(MainDestination.Splash) { inclusive = true }
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
                    //navHostController.navigate(MainDestination.Notice)
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
                onShowAttendanceDialog = { _, _ -> }
            )
        }
        //일정 수정
        composable<MainDestination.ScheduleEdit> {
            ScheduleAddRoute(
                onShowAttendanceDialog = { _, _ -> }
            )
        }
        //일정 상세
        composable<MainDestination.ScheduleDetail>{ data ->

            ScheduleDetailRoute(

            )
        }

        /**마이페이지 관련 정의**/

        //신 마이페이지
        composable<MainDestination.Mycard>(
            deepLinks = listOf(
                navDeepLink {
                    // 스토어 URL 기반 딥링크 패턴 매핑
                    uriPattern = "umc://card?memberId={targetMemberId}"
                }
            )
        ) { backStackEntry ->
            // Type-Safe Navigation 파라미터 추출 (딥링크 포함)
            val mycardDestination = backStackEntry.toRoute<MainDestination.Mycard>()
            val targetMemberId = mycardDestination.targetMemberId


            MycardRoute(
                targetMemberId = targetMemberId,
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


    }
}
