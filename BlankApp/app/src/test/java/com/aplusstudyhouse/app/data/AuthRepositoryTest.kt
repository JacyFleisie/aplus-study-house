package com.aplusstudyhouse.app.data

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AuthRepository (mock mode).
 *
 * The app now uses Supabase for real auth; in mock mode only the admin user is seeded.
 * These tests cover the auth/role-gating contract that must hold in BOTH modes:
 *  - admin credentials authenticate and yield the ADMIN role
 *  - wrong password / unknown email / empty inputs are rejected
 *  - createAccount mints a PARENT account
 *  - a logged-in session survives until signOut
 */
class AuthRepositoryTest {
    @Before
    fun setup() {
        // Force mock mode so tests run deterministically regardless of supabase.properties.
        SupabaseConfig.forceMockMode = true
        AuthRepository.signOut()
    }

    @After
    fun teardown() {
        AuthRepository.signOut()
    }

    // ============================================
    // ADMIN AUTH + ROLE GATING
    // ============================================

    @Test
    fun `admin login succeeds and yields ADMIN role`() =
        kotlinx.coroutines.runBlocking {
            val result = AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
            assertTrue("Admin login should succeed", result.success)
            assertEquals(UserRole.ADMIN, result.user?.role)
        }

    @Test
    fun `admin wrong password is rejected`() =
        kotlinx.coroutines.runBlocking {
            val result = AuthRepository.signIn("admin@aplusstudy.co.za", "WrongPassword")
            assertFalse("Admin login with wrong password must fail", result.success)
        }

    @Test
    fun `unknown email is rejected`() =
        kotlinx.coroutines.runBlocking {
            val result = AuthRepository.signIn("nobody@example.com", "Password1")
            assertFalse("Unknown email must fail", result.success)
            assertNull("No user returned on failure", result.user)
        }

    @Test
    fun `empty email or password is rejected`() =
        kotlinx.coroutines.runBlocking {
            assertFalse(AuthRepository.signIn("", "Password1").success)
            assertFalse(AuthRepository.signIn("admin@aplusstudy.co.za", "").success)
        }

    // ============================================
    // CREATE ACCOUNT (PARENT)
    // ============================================

    @Test
    fun `createAccount mints a PARENT account`() =
        kotlinx.coroutines.runBlocking {
            val result =
                AuthRepository.createAccount(
                    fullName = "Test Parent",
                    email = "parent${System.currentTimeMillis()}@example.com",
                    phone = "0821234567",
                    password = "Password1"
                )
            assertTrue("createAccount should succeed", result.success)
            assertEquals(UserRole.PARENT, result.user?.role)
        }

    @Test
    fun `duplicate account email is rejected`() =
        kotlinx.coroutines.runBlocking {
            val email = "duplicate${System.currentTimeMillis()}@example.com"
            val first = AuthRepository.createAccount("First Parent", email, "0821234567", "Password1")
            val second = AuthRepository.createAccount("Second Parent", email, "0821234567", "Password1")

            assertTrue(first.success)
            assertFalse(second.success)
            assertTrue(second.message.contains("already exists"))
        }

    @Test
    fun `demo sign in rejects wrong password and accepts seeded admin`() {
        val rejected = AuthRepository.demoSignIn("admin@aplusstudy.co.za", "wrong")
        assertFalse(rejected.success)

        val accepted = AuthRepository.demoSignIn("admin@aplusstudy.co.za", "Admin123")
        assertTrue(accepted.success)
        assertEquals(UserRole.ADMIN, accepted.user?.role)
    }

    @Test
    fun `reset password returns useful result for known and unknown users`() =
        kotlinx.coroutines.runBlocking {
            val known = AuthRepository.resetPassword("admin@aplusstudy.co.za")
            val unknown = AuthRepository.resetPassword("unknown${System.currentTimeMillis()}@example.com")

            assertTrue(known.success)
            assertFalse(unknown.success)
        }

    // ============================================
    // SESSION / ROLE STATE
    // ============================================

    @Test
    fun `signIn establishes a logged-in ADMIN session`() =
        kotlinx.coroutines.runBlocking {
            AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
            assertTrue("Should be logged in", AuthRepository.isLoggedIn())
            assertEquals(UserRole.ADMIN, AuthRepository.getCurrentUser()?.role)
        }

    @Test
    fun `signOut clears the session`() =
        kotlinx.coroutines.runBlocking {
            AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
            assertTrue(AuthRepository.isLoggedIn())
            AuthRepository.signOut()
            assertFalse("Should not be logged in after signOut", AuthRepository.isLoggedIn())
            assertNull("No current user after signOut", AuthRepository.getCurrentUser())
        }

    @Test
    fun `no session before login`() {
        assertFalse("Fresh state should not be logged in", AuthRepository.isLoggedIn())
        assertNull("Fresh state should have no current user", AuthRepository.getCurrentUser())
    }
}
