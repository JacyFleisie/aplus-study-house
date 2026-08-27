package com.example.blankapp.data

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Unit tests for AuthRepository
 * Tests mock authentication flows (when Supabase is not configured)
 */
class AuthRepositoryTest {

    @Before
    fun setup() {
        // Skip tests if Supabase is configured (use mock mode for unit tests)
        assumeTrue("Skipping mock auth tests when Supabase is configured", !SupabaseConfig.isConfigured())
        AuthRepository.signOut()
    }

    @After
    fun teardown() {
        AuthRepository.signOut()
    }

    // ============================================
    // SIGN IN TESTS
    // ============================================

    @Test
    fun `signIn with valid parent credentials returns success`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("sarah@example.com", "Password1")
        assertTrue("Should succeed", result.success)
        assertEquals("Login successful", result.message)
        assertNotNull("Should return user", result.user)
        assertEquals(UserRole.PARENT, result.user?.role)
        assertEquals("Sarah Johnson", result.user?.fullName)
    }

    @Test
    fun `signIn with valid admin credentials returns success`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
        assertTrue("Should succeed", result.success)
        assertEquals(UserRole.ADMIN, result.user?.role)
        assertEquals("Margaret", result.user?.fullName)
    }

    @Test
    fun `signIn with invalid email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("nonexistent@example.com", "Password1")
        assertFalse("Should fail", result.success)
        assertTrue("Should mention no account", result.message.contains("No account found"))
        assertNull("Should not return user", result.user)
    }

    @Test
    fun `signIn with wrong password returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("sarah@example.com", "WrongPassword")
        assertFalse("Should fail", result.success)
        assertTrue("Should mention incorrect password", result.message.contains("Incorrect password"))
    }

    @Test
    fun `signIn with empty email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("", "Password1")
        assertFalse("Should fail", result.success)
    }

    @Test
    fun `signIn with empty password returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("sarah@example.com", "")
        assertFalse("Should fail", result.success)
    }

    // ============================================
    // CREATE ACCOUNT TESTS
    // ============================================

    @Test
    fun `createAccount with valid data returns success`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.createAccount(
            fullName = "Test User",
            email = "test${System.currentTimeMillis()}@example.com",
            phone = "0821234567",
            password = "Password1"
        )
        assertTrue("Should succeed", result.success)
        assertEquals("Account created successfully! You can now log in.", result.message)
        assertNotNull("Should return user", result.user)
        assertEquals(UserRole.PARENT, result.user?.role)
    }

    @Test
    fun `createAccount with existing email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.createAccount(
            fullName = "Duplicate User",
            email = "sarah@example.com", // Already exists
            phone = "0821234567",
            password = "Password1"
        )
        assertFalse("Should fail", result.success)
        assertTrue("Should mention existing account", result.message.contains("already exists"))
    }

    // ============================================
    // SESSION TESTS
    // ============================================

    @Test
    fun `isLoggedIn returns false when not logged in`() {
        assertFalse("Should not be logged in", AuthRepository.isLoggedIn())
    }

    @Test
    fun `getCurrentUser returns null when not logged in`() {
        assertNull("Should return null", AuthRepository.getCurrentUser())
    }

    @Test
    fun `signIn sets current user`() = kotlinx.coroutines.runBlocking {
        AuthRepository.signIn("sarah@example.com", "Password1")
        assertTrue("Should be logged in", AuthRepository.isLoggedIn())
        assertNotNull("Should have current user", AuthRepository.getCurrentUser())
        assertEquals("Sarah Johnson", AuthRepository.getCurrentUser()?.fullName)
    }

    @Test
    fun `signOut clears current user`() = kotlinx.coroutines.runBlocking {
        AuthRepository.signIn("sarah@example.com", "Password1")
        assertTrue("Should be logged in", AuthRepository.isLoggedIn())

        AuthRepository.signOut()
        assertFalse("Should not be logged in after signout", AuthRepository.isLoggedIn())
        assertNull("Should have no current user", AuthRepository.getCurrentUser())
    }

    // ============================================
    // ROLE TESTS
    // ============================================

    @Test
    fun `parent user has PARENT role`() = kotlinx.coroutines.runBlocking {
        AuthRepository.signIn("sarah@example.com", "Password1")
        assertEquals(UserRole.PARENT, AuthRepository.getCurrentUser()?.role)
    }

    @Test
    fun `admin user has ADMIN role`() = kotlinx.coroutines.runBlocking {
        AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
        assertEquals(UserRole.ADMIN, AuthRepository.getCurrentUser()?.role)
    }

    @Test
    fun `all parent accounts have correct roles`() = kotlinx.coroutines.runBlocking {
        val parentEmails = listOf("sarah@example.com", "michael@example.com", "emma@example.com")
        for (email in parentEmails) {
            val result = AuthRepository.signIn(email, "Password1")
            assertTrue("Should succeed for $email", result.success)
            assertEquals("Should be PARENT for $email", UserRole.PARENT, result.user?.role)
            AuthRepository.signOut()
        }
    }
}
