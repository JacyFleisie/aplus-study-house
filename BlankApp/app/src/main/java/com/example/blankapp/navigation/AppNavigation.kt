package com.example.blankapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.RegistrationDraft
import com.example.blankapp.data.RegistrationDraftStore
import com.example.blankapp.data.SupabaseRepository
import com.example.blankapp.data.UserRole
import com.example.blankapp.data.toApplicationJson
import com.example.blankapp.screens.*
import com.example.blankapp.screens.admin.*
import com.example.blankapp.screens.auth.*
import com.example.blankapp.screens.parent.*
import com.example.blankapp.screens.parent.registration.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val registrationDraft = remember { RegistrationDraft() }
    var createdApplication by remember { mutableStateOf<com.example.blankapp.data.MockApplication?>(null) }
    val ctx = LocalContext.current

    // Persist the in-progress registration draft to disk so it survives restarts.
    val saveDraft: () -> Unit = {
        AuthRepository.getCurrentUser()?.id?.let { uid ->
            val copy = RegistrationDraft().apply { copyFrom(registrationDraft) }
            // Fire-and-forget persistence (never blocks the UI).
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                RegistrationDraftStore.save(ctx, uid, copy)
            }
        }
    }
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
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                var savedDraft by remember { mutableStateOf<RegistrationDraft?>(null) }
                var loadingDraft by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val uid = AuthRepository.getCurrentUser()?.id.orEmpty()
                    savedDraft = RegistrationDraftStore.load(ctx, uid)
                    loadingDraft = false
                }

                RegistrationStartScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onLogout = {
                        AuthRepository.signOut(ctx)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onStartRegistration = {
                        // Continue a saved draft (if any), then proceed to step 1.
                        val uid = AuthRepository.getCurrentUser()?.id.orEmpty()
                        val existing = savedDraft
                        if (existing != null) {
                            registrationDraft.apply { copyFrom(existing) }
                        } else {
                            registrationDraft.apply { copyFrom(RegistrationDraft()) }
                        }
                        navController.navigate(Screen.RegistrationStudentDetails.route)
                    },
                    onResumeDraft = {
                        // Parent tapped "Continue" to resume the saved draft.
                        val uid = AuthRepository.getCurrentUser()?.id.orEmpty()
                        val existing = savedDraft
                        if (existing != null) {
                            registrationDraft.apply { copyFrom(existing) }
                            navController.navigate(Screen.RegistrationStudentDetails.route)
                        }
                    },
                    hasSavedDraft = { loadingDraft || savedDraft != null },
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
                    onContinue = { studentName, grade, school, dob, address, gender, classNr, teacherName, lsen ->
                        registrationDraft.apply {
                            this.studentName = studentName
                            this.grade = grade
                            this.school = school
                            this.dob = dob
                            this.address = address
                            this.gender = gender
                            this.classNr = classNr
                            this.teacherName = teacherName
                            this.lsen = lsen
                        }
                        saveDraft()
                        navController.navigate(Screen.RegistrationSportsActivities.route)
                    },
                    draft = registrationDraft
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
                    onContinue = { sports ->
                        registrationDraft.sports = sports
                        saveDraft()
                        navController.navigate(Screen.RegistrationCollection.route)
                    },
                    draft = registrationDraft
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
                    onContinue = { collectionPerson1, contact1, vehicleReg1, collectionPerson2, contact2, vehicleReg2, transportRequired ->
                        registrationDraft.apply {
                            this.collectionPerson1 = collectionPerson1
                            this.contact1 = contact1
                            this.vehicleReg1 = vehicleReg1
                            this.collectionPerson2 = collectionPerson2
                            this.contact2 = contact2
                            this.vehicleReg2 = vehicleReg2
                            this.transportRequired = transportRequired
                        }
                        saveDraft()
                        navController.navigate(Screen.RegistrationMedical.route)
                    },
                    draft = registrationDraft
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
                    onContinue = { doctorName, doctorLocation, doctorContact, medicalPlan, medicalAidNumber, allergies, epilepsy, diabetic, asthma, noseBleeder, hasAllergies ->
                        registrationDraft.apply {
                            this.doctorName = doctorName
                            this.doctorLocation = doctorLocation
                            this.doctorContact = doctorContact
                            this.medicalPlan = medicalPlan
                            this.medicalAidNumber = medicalAidNumber
                            this.allergies = allergies
                            this.epilepsy = epilepsy
                            this.diabetic = diabetic
                            this.asthma = asthma
                            this.noseBleeder = noseBleeder
                            this.hasAllergies = hasAllergies
                        }
                        saveDraft()
                        navController.navigate(Screen.RegistrationParentDetails.route)
                    },
                    draft = registrationDraft
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
                    onContinue = { motherName, motherSurname, motherId, motherEmployer, motherWorkPhone, motherCell, motherEmail, fatherName, fatherSurname, fatherId, fatherEmployer, fatherWorkPhone, fatherCell, fatherEmail ->
                        registrationDraft.apply {
                            this.motherName = motherName
                            this.motherSurname = motherSurname
                            this.motherId = motherId
                            this.motherEmployer = motherEmployer
                            this.motherWorkPhone = motherWorkPhone
                            this.motherCell = motherCell
                            this.motherEmail = motherEmail
                            this.fatherName = fatherName
                            this.fatherSurname = fatherSurname
                            this.fatherId = fatherId
                            this.fatherEmployer = fatherEmployer
                            this.fatherWorkPhone = fatherWorkPhone
                            this.fatherCell = fatherCell
                            this.fatherEmail = fatherEmail
                        }
                        saveDraft()
                        navController.navigate(Screen.RegistrationConsent.route)
                    },
                    draft = registrationDraft
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
                    onContinue = { photoConsent, parentSignature ->
                        registrationDraft.apply {
                            this.photoConsent = photoConsent
                            this.parentSignature = parentSignature
                        }
                        saveDraft()
                        navController.navigate(Screen.RegistrationPayment.route)
                    },
                    draft = registrationDraft
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
                    onPaymentComplete = {
                        registrationDraft.paymentMethod = "eft"
                    },
                    onContinue = { navController.navigate(Screen.RegistrationSubmit.route) }
                )
            }
        }

        composable(Screen.RegistrationSubmit.route) {
            RequireParent(navController = navController) {
                val scope = rememberCoroutineScope()
                RegistrationSubmitScreen(
                    onBackClick = { navController.popBackStack() },
                    onExitFlow = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    onSubmit = { parentId ->
                        scope.launch {
                            val created = SupabaseRepository.createApplication(
                                registrationDraft.toApplicationJson(), parentId
                            )
                            createdApplication = created
                            // Successful submission: clear the persisted draft so
                            // RegistrationStart no longer offers "Continue".
                            if (created != null) {
                                val uid = AuthRepository.getCurrentUser()?.id.orEmpty()
                                RegistrationDraftStore.clear(ctx, uid)
                            }
                        }
                    },
                    onContinue = {
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.RegistrationStart.route) { inclusive = true }
                        }
                    },
                    registrationDraft = registrationDraft
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
