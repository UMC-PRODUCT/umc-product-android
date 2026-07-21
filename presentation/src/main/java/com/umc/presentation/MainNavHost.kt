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
import com.example.mypage.mycontent.MyContentRoute
import com.example.mypage.mypage.MypageRoute
import com.example.mypage.profile.ProfileRoute
import com.umc.failcode.SignUpFailRoute
import com.umc.failcode.code.SignUpFailCodeRoute
import com.umc.permission.PermissionRoute
import com.umc.presentation.home.home.HomeRoute
import com.umc.presentation.home.notification.NotificationRoute
import com.umc.presentation.home.schedule.add.ScheduleAddRoute
import com.umc.presentation.home.schedule.detail.ScheduleDetailRoute
import com.umc.presentation.login.LoginRoute
import com.umc.presentation.signup.SignUpRoute
import com.umc.presentation.splash.SplashRoute
import com.umc.presentation.study.admin.group.AdminStudyGroupRoute
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateRoute
import com.umc.presentation.study.admin.group.schedule.AdminStudyGroupScheduleRoute
import com.example.presentation.act.ActManageRoute
import com.umc.presentation.study.ActStudyRoute

@Composable
fun MainNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navHostController,
        startDestination = MainDestination.ActivityManagement,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MainDestination.Splash> {
            SplashRoute(
                navigateToLogin = { navHostController.navigate(MainDestination.Login) },
                navigateToMain = {
                    // TODO: 메인 화면 완성 후 연결
                },
                navigateToInputCode = {
                    // TODO: 코드 입력 화면 완성 후 연결
                }
            )
        }

        composable<MainDestination.Login> {
            LoginRoute(
                navigateToSignUp = { oAuthToken ->
                    navHostController.navigate(MainDestination.SignUp(oAuthToken))
                }
            )
        }

        composable<MainDestination.SignUp> { backStackEntry ->
            val destination = backStackEntry.toRoute<MainDestination.SignUp>()
            SignUpRoute(
                oAuthVerificationToken = destination.oAuthVerificationToken,
                navigateToBack = { navHostController.popBackStack() },
                navigateToPermission = { navHostController.navigate(MainDestination.Permission) },
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
        composable<MainDestination.Mypage>{
            MypageRoute(
                onNavigateToEditProfile = {
                    navHostController.navigate(MainDestination.MyProfile)
                },
                onNavigateToMyContent = {type ->
                    navHostController.navigate(MainDestination.MyContent(showType = type))
                                        },
                onNavigateToLogin = {}
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
            ProfileRoute()
        }



        /** 스터디 관리자 관련 정의 **/

        composable<MainDestination.AdminStudyGroup> {
            AdminStudyGroupRoute(
                onNavigateCreateGroup = {
                    navHostController.navigate(
                        MainDestination.AdminStudyGroupCreate
                    )
                },
                onNavigateAddSchedule = { groupId, groupTitle, groupPart ->
                    navHostController.navigate(
                        MainDestination.AdminStudyGroupSchedule(
                            groupId = groupId,
                            groupTitle = groupTitle,
                            groupPart = groupPart,
                        )
                    )
                },
                onOpenEditMembers = { item ->
                    // TODO 멤버 수정 화면 연결
                },
            )
        }

        composable<MainDestination.AdminStudyGroupCreate> {
            AdminStudyGroupCreateRoute(
                navigateBack = {
                    navHostController.popBackStack()
                },
            )
        }

        composable<MainDestination.AdminStudyGroupSchedule> { backStackEntry ->
            val destination =
                backStackEntry.toRoute<MainDestination.AdminStudyGroupSchedule>()

            AdminStudyGroupScheduleRoute(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
            )
        }

        composable<MainDestination.ActivityManagement> {
            ActManageRoute(
                studyContent = { isAdmin ->
                    ActStudyRoute(
                        isAdmin = isAdmin,
                        onNavigateCreateGroup = {
                            navHostController.navigate(
                                MainDestination.AdminStudyGroupCreate
                            )
                        },
                        onNavigateAddSchedule = { groupId, groupTitle, groupPart ->
                            navHostController.navigate(
                                MainDestination.AdminStudyGroupSchedule(
                                    groupId = groupId,
                                    groupTitle = groupTitle,
                                    groupPart = groupPart,
                                )
                            )
                        },
                        onOpenEditMembers = { item ->
                            // TODO 멤버 수정 화면 연결
                        },
                    )
                },
            )
        }

    }
}