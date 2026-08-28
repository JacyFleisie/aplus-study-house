package com.example.blankapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.UserRole
import com.example.blankapp.screens.*
import com.example.blankapp.screens.admin.*
import com.example.blankapp.screens.auth.*
import com.example.blankapp.screens.parent.*
import com.example.blankapp.screens.parent.registration.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left, tween(300)
            )
        },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right, tween(300)
            )
        }
    ) {
        // ============================================
        // PUBLIC ROUTES (No auth required)
        // ============================================

        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    if (AuthRepository.isLoggedIn()) {
                        val user = AuthRepository.getCurrentUser()
                        when (user?.role) {
                            com.example.blankapp.data.UserRole.PARENT -> {
                                navController.navigate(Screen.ParentDashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            com.example.blankapp.data.UserRole.ADMIN -> {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            null -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { _, _, role ->
                    when (role) {
                        UserRole.PARENT -> {
                            navController.navigate(Screen.ParentDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        UserRole.ADMIN -> {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                },
                onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        // Create Account
        composable(Screen.CreateAccount.route) {
            CreateAccountScreen(
                onBackClick = { navController.popBackStack() },
                onCreateAccountClick = { _, _, _, _ ->
                    // After signup, go to registration flow
                    navController.navigate(Screen.RegistrationStart.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        // Forgot Password
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ============================================
        // PARENT ROUTES (Require parent role)
        // ============================================

        // Parent Dashboard
        composable(Screen.ParentDashboard.route) {
            RequireParent(navController = navController) {
                ParentDashboardScreen(
                    onLogout = {
                        AuthRepository.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateToRegistration = { navController.navigate(Screen.RegistrationStart.route) },
                    onNavigateToChildProfile = { studentId -> navController.navigate(Screen.ChildProfile.createRoute(studentId)) },
                    onNavigateToFinancePayment = { invoiceId, amount, description ->
                        navController.navigate(Screen.FinancePayment.createRoute(invoiceId, amount, description))
                    }
                )
            }
        }

        // Registration Flow
        composable(Screen.RegistrationStart.route) {
            RequireParent(navController = navController) {
                RegistrationStartScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onStartRegistration = { navController.navigate(Screen.RegistrationStudentDetails.route) },
                    onCheckStatus = { navController.navigate(Screen.ApplicationStatus.route) }
                )
            }
        }

        composable(Screen.RegistrationStudentDetails.route) {
            RequireParent(navController = navController) {
                StudentDetailsScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onContinue = { _, _, _, _, _, _, _, _, _ -> navController.navigate(Screen.RegistrationSportsActivities.route) }
                )
            }
        }

        composable(Screen.RegistrationSportsActivities.route) {
            RequireParent(navController = navController) {
                SportsActivitiesScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onContinue = { _ -> navController.navigate(Screen.RegistrationCollection.route) }
                )
            }
        }

        composable(Screen.RegistrationCollection.route) {
            RequireParent(navController = navController) {
                CollectionTransportScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onContinue = { _, _, _, _, _, _, _ -> navController.navigate(Screen.RegistrationMedical.route) }
                )
            }
        }

        composable(Screen.RegistrationMedical.route) {
            RequireParent(navController = navController) {
                MedicalInfoScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onContinue = { _, _, _, _, _, _, _, _, _, _, _ -> navController.navigate(Screen.RegistrationParentDetails.route) }
                )
            }
        }

        composable(Screen.RegistrationParentDetails.route) {
            RequireParent(navController = navController) {
                ParentDetailsScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onContinue = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> navController.navigate(Screen.RegistrationConsent.route) }
                )
            }
        }

        composable(Screen.RegistrationConsent.route) {
            RequireParent(navController = navController) {
                ConsentSignatureScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onContinue = { _, _ -> navController.navigate(Screen.RegistrationPayment.route) }
                )
            }
        }

        composable(Screen.RegistrationPayment.route) {
            RequireParent(navController = navController) {
                RegistrationPaymentScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onPaymentComplete = { },
                    onContinue = { navController.navigate(Screen.RegistrationSubmit.route) }
                )
            }
        }

        composable(Screen.RegistrationSubmit.route) {
            RequireParent(navController = navController) {
                RegistrationSubmitScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onSubmit = { },
                    onContinue = { navController.navigate(Screen.ApplicationStatus.route) }
                )
            }
        }

        composable(Screen.ApplicationStatus.route) {
            RequireParent(navController = navController) {
                ApplicationStatusScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Child Profile
        composable(
            route = Screen.ChildProfile.ROUTE,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequireParent(navController = navController) {
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
                ChildProfileScreen(studentId = studentId, onBackClick = { navController.popBackStack() })
            }
        }

        // Finance Payment
        composable(
            route = Screen.FinancePayment.ROUTE,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.FloatType },
                navArgument("description") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            RequireParent(navController = navController) {
                val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: ""
                val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
                val description = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("description") ?: "", "UTF-8"
                )
                FinancePaymentScreen(
                    invoiceId = invoiceId,
                    amount = amount,
                    description = description,
                    onBackClick = { navController.popBackStack() },
                    onPaymentComplete = { navController.popBackStack() }
                )
            }
        }

        // ============================================
        // ADMIN ROUTES (Require admin role)
        // ============================================

        // Admin Dashboard
        composable(Screen.AdminDashboard.route) {
            RequireAdmin(navController = navController) {
                AdminDashboardScreen(
                    onLogout = {
                        AuthRepository.signOut()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateToApplication = { applicationId ->
                        navController.navigate(Screen.ApplicationReview.createRoute(applicationId))
                    },
                    onNavigateToStudent = { studentId ->
                        navController.navigate(Screen.AdminStudentProfile.createRoute(studentId))
                    },
                    onNavigateToFinance = {
                        navController.navigate(Screen.AdminFinancePayment.route)
                    }
                )
            }
        }

        // Admin Finance Payment
        composable(Screen.AdminFinancePayment.route) {
            RequireAdmin(navController = navController) {
                AdminFinancePaymentScreen(onBack = { navController.popBackStack() })
            }
        }

        // Admin Student Profile
        composable(
            route = Screen.AdminStudentProfile.ROUTE,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequireAdmin(navController = navController) {
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
                AdminStudentProfileScreen(studentId = studentId, onBackClick = { navController.popBackStack() })
            }
        }

        // Admin Parent Profile
        composable(
            route = Screen.AdminParentProfile.ROUTE,
            arguments = listOf(navArgument("parentId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequireAdmin(navController = navController) {
                val parentId = backStackEntry.arguments?.getString("parentId") ?: ""
                AdminParentProfileScreen(parentId = parentId, onBackClick = { navController.popBackStack() })
            }
        }

        // Application Review (admin only)
        composable(
            route = Screen.ApplicationReview.ROUTE,
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequireAdmin(navController = navController) {
                val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
                ApplicationReviewScreen(
                    applicationId = applicationId,
                    onBackClick = { navController.popBackStack() },
                    onDecisionMade = { navController.popBackStack() }
                )
            }
        }

        // Crash Logs (admin only)
        composable(Screen.CrashLogs.route) {
            RequireAdmin(navController = navController) {
                CrashLogScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
