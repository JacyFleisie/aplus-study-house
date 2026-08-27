package com.example.blankapp.navigation

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for navigation route definitions
 * Tests that routes are properly defined and parameterized
 */
class RouteTest {

    // ============================================
    // STATIC ROUTE TESTS
    // ============================================

    @Test
    fun `splash route is defined`() {
        assertEquals("splash", Screen.Splash.route)
    }

    @Test
    fun `login route is defined`() {
        assertEquals("login", Screen.Login.route)
    }

    @Test
    fun `create account route is defined`() {
        assertEquals("create_account", Screen.CreateAccount.route)
    }

    @Test
    fun `forgot password route is defined`() {
        assertEquals("forgot_password", Screen.ForgotPassword.route)
    }

    @Test
    fun `parent dashboard route is defined`() {
        assertEquals("parent_dashboard", Screen.ParentDashboard.route)
    }

    @Test
    fun `admin dashboard route is defined`() {
        assertEquals("admin_dashboard", Screen.AdminDashboard.route)
    }

    // ============================================
    // REGISTRATION ROUTE TESTS
    // ============================================

    @Test
    fun `registration start route is defined`() {
        assertEquals("registration_start", Screen.RegistrationStart.route)
    }

    @Test
    fun `all 10 registration step routes are defined`() {
        val registrationRoutes = listOf(
            Screen.RegistrationStart.route,
            Screen.RegistrationStudentDetails.route,
            Screen.RegistrationSportsActivities.route,
            Screen.RegistrationCollection.route,
            Screen.RegistrationMedical.route,
            Screen.RegistrationParentDetails.route,
            Screen.RegistrationConsent.route,
            Screen.RegistrationPayment.route,
            Screen.RegistrationSubmit.route,
            Screen.ApplicationStatus.route
        )
        assertEquals("Should have 10 registration routes", 10, registrationRoutes.size)
        registrationRoutes.forEach { route ->
            assertTrue("Route '$route' should not be blank", route.isNotBlank())
        }
    }

    // ============================================
    // PARAMETERIZED ROUTE TESTS
    // ============================================

    @Test
    fun `childProfile generates correct route`() {
        val route = Screen.ChildProfile.createRoute("S001")
        assertEquals("child_profile/S001", route)
    }

    @Test
    fun `childProfile handles special characters`() {
        val route = Screen.ChildProfile.createRoute("S-123")
        assertEquals("child_profile/S-123", route)
    }

    @Test
    fun `financePayment generates correct route`() {
        val route = Screen.FinancePayment.createRoute("INV001", 1800.0, "Aftercare")
        assertTrue("Route should start with finance_payment/", route.startsWith("finance_payment/"))
        assertTrue("Route should contain invoice ID", route.contains("INV001"))
        assertTrue("Route should contain amount", route.contains("1800.0"))
    }

    @Test
    fun `applicationReview generates correct route`() {
        val route = Screen.ApplicationReview.createRoute("APP001")
        assertEquals("application_review/APP001", route)
    }

    @Test
    fun `adminStudentProfile generates correct route`() {
        val route = Screen.AdminStudentProfile.createRoute("S001")
        assertEquals("admin_student_profile/S001", route)
    }

    @Test
    fun `adminParentProfile generates correct route`() {
        val route = Screen.AdminParentProfile.createRoute("P001")
        assertEquals("admin_parent_profile/P001", route)
    }

    // ============================================
    // ROUTE PATTERN TESTS
    // ============================================

    @Test
    fun `child profile route pattern matches`() {
        val route = Screen.ChildProfile.ROUTE
        assertTrue("Should contain {studentId}", route.contains("{studentId}"))
    }

    @Test
    fun `finance payment route pattern matches`() {
        val route = Screen.FinancePayment.ROUTE
        assertTrue("Should contain {invoiceId}", route.contains("{invoiceId}"))
        assertTrue("Should contain {amount}", route.contains("{amount}"))
        assertTrue("Should contain {description}", route.contains("{description}"))
    }

    @Test
    fun `application review route pattern matches`() {
        val route = Screen.ApplicationReview.ROUTE
        assertTrue("Should contain {applicationId}", route.contains("{applicationId}"))
    }

    @Test
    fun `admin student profile route pattern matches`() {
        val route = Screen.AdminStudentProfile.ROUTE
        assertTrue("Should contain {studentId}", route.contains("{studentId}"))
    }

    @Test
    fun `admin parent profile route pattern matches`() {
        val route = Screen.AdminParentProfile.ROUTE
        assertTrue("Should contain {parentId}", route.contains("{parentId}"))
    }

    // ============================================
    // ROUTE UNIQUENESS TESTS
    // ============================================

    @Test
    fun `all static routes are unique`() {
        val routes = listOf(
            Screen.Splash.route, Screen.Login.route, Screen.CreateAccount.route,
            Screen.ForgotPassword.route, Screen.ParentDashboard.route,
            Screen.AdminDashboard.route, Screen.RegistrationStart.route,
            Screen.RegistrationStudentDetails.route,
            Screen.RegistrationSportsActivities.route,
            Screen.RegistrationCollection.route,
            Screen.RegistrationMedical.route,
            Screen.RegistrationParentDetails.route,
            Screen.RegistrationConsent.route,
            Screen.RegistrationPayment.route,
            Screen.RegistrationSubmit.route,
            Screen.ApplicationStatus.route,
            Screen.AdminFinancePayment.route,
            Screen.CrashLogs.route
        )
        assertEquals("All routes should be unique", routes.size, routes.toSet().size)
    }

    // ============================================
    // TYPE SAFETY TESTS
    // ============================================

    @Test
    fun `Screen classes are data objects`() {
        // Verify that Screen objects are properly defined
        assertNotNull(Screen.Splash)
        assertNotNull(Screen.Login)
        assertNotNull(Screen.ParentDashboard)
        assertNotNull(Screen.AdminDashboard)
    }

    @Test
    fun `Screen data classes have companion objects`() {
        // Verify that parameterized routes have companion objects with createRoute
        assertNotNull(Screen.ChildProfile.Companion)
        assertNotNull(Screen.FinancePayment.Companion)
        assertNotNull(Screen.ApplicationReview.Companion)
        assertNotNull(Screen.AdminStudentProfile.Companion)
        assertNotNull(Screen.AdminParentProfile.Companion)
    }
}
