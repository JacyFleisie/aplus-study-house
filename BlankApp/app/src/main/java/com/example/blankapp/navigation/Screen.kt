package com.example.blankapp.navigation

/**
 * Type-safe navigation routes for the app.
 * Each route is a sealed class with proper parameter handling.
 */
sealed class Screen(val route: String) {

    // ============================================
    // PUBLIC ROUTES (No auth required)
    // ============================================

    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object CreateAccount : Screen("create_account")
    data object ForgotPassword : Screen("forgot_password")

    // ============================================
    // PARENT ROUTES (Require parent role)
    // ============================================

    data object ParentDashboard : Screen("parent_dashboard")
    data object RegistrationStart : Screen("registration_start")
    data object RegistrationStudentDetails : Screen("registration_student_details")
    data object RegistrationSportsActivities : Screen("registration_sports_activities")
    data object RegistrationCollection : Screen("registration_collection")
    data object RegistrationMedical : Screen("registration_medical")
    data object RegistrationParentDetails : Screen("registration_parent_details")
    data object RegistrationConsent : Screen("registration_consent")
    data object RegistrationPayment : Screen("registration_payment")
    data object RegistrationSubmit : Screen("registration_submit")
    data object ApplicationStatus : Screen("application_status")

    // Child Profile with parameter
    data class ChildProfile(val studentId: String) : Screen("child_profile/{studentId}") {
        companion object {
            const val ROUTE = "child_profile/{studentId}"
            fun createRoute(studentId: String) = "child_profile/$studentId"
        }
    }

    // Finance Payment with parameters
    data class FinancePayment(
        val invoiceId: String,
        val amount: Double,
        val description: String
    ) : Screen("finance_payment/{invoiceId}/{amount}/{description}") {
        companion object {
            const val ROUTE = "finance_payment/{invoiceId}/{amount}/{description}"
            fun createRoute(invoiceId: String, amount: Double, description: String): String {
                val encodedDescription = java.net.URLEncoder.encode(description, "UTF-8")
                return "finance_payment/$invoiceId/$amount/$encodedDescription"
            }
        }
    }

    // ============================================
    // ADMIN ROUTES (Require admin role)
    // ============================================

    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminFinancePayment : Screen("admin_finance_payment")
    data object CrashLogs : Screen("crash_logs")

    // Admin Student Profile with parameter
    data class AdminStudentProfile(val studentId: String) : Screen("admin_student_profile/{studentId}") {
        companion object {
            const val ROUTE = "admin_student_profile/{studentId}"
            fun createRoute(studentId: String) = "admin_student_profile/$studentId"
        }
    }

    // Admin Parent Profile with parameter
    data class AdminParentProfile(val parentId: String) : Screen("admin_parent_profile/{parentId}") {
        companion object {
            const val ROUTE = "admin_parent_profile/{parentId}"
            fun createRoute(parentId: String) = "admin_parent_profile/$parentId"
        }
    }

    // Application Review with parameter
    data class ApplicationReview(val applicationId: String) : Screen("application_review/{applicationId}") {
        companion object {
            const val ROUTE = "application_review/{applicationId}"
            fun createRoute(applicationId: String) = "application_review/$applicationId"
        }
    }
}
