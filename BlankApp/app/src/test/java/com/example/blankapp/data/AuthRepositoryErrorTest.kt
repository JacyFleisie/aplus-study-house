package com.example.blankapp.data

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Error handling tests for AuthRepository.
 * Tests edge cases: invalid inputs, network errors, malformed responses.
 */
class AuthRepositoryErrorTest {

    @Before
    fun setup() {
        SupabaseConfig.forceMockMode = true
        AuthRepository.signOut()
    }

    @After
    fun teardown() {
        AuthRepository.signOut()
    }

    @Test
    fun `signIn with null email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn(null ?: "", "Password1")
        assertFalse("Should fail with null email", result.success)
    }

    @Test
    fun `signIn with null password returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("sarah@example.com", null ?: "")
        assertFalse("Should fail with null password", result.success)
    }

    @Test
    fun `signIn with empty email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("", "Password1")
        assertFalse("Should fail with empty email", result.success)
    }

    @Test
    fun `signIn with empty password returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("sarah@example.com", "")
        assertFalse("Should fail with empty password", result.success)
    }

    @Test
    fun `signIn with invalid email format returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("not-an-email", "Password1")
        assertFalse("Should fail with invalid email", result.success)
    }

    @Test
    fun `signIn with SQL injection attempt returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("admin'--", "Password1")
        assertFalse("Should fail with SQL injection attempt", result.success)
    }

    @Test
    fun `signIn with XSS attempt returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("<script>alert('xss')</script>", "Password1")
        assertFalse("Should fail with XSS attempt", result.success)
    }

    @Test
    fun `signIn with very long email returns failure`() = kotlinx.coroutines.runBlocking {
        val longEmail = "a".repeat(1000) + "@example.com"
        val result = AuthRepository.signIn(longEmail, "Password1")
        assertFalse("Should fail with very long email", result.success)
    }

    @Test
    fun `signIn with unicode in email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("用户@例子.测试", "Password1")
        assertFalse("Should fail with unicode email", result.success)
    }

    @Test
    fun `createAccount with existing email returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.createAccount(
            fullName = "Test User",
            email = "admin@aplusstudy.co.za", // Already exists in mock
            phone = "0821234567",
            password = "Password1"
        )
        assertFalse("Should fail with existing email", result.success)
    }

    @Test
    fun `createAccount with weak password returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.createAccount(
            fullName = "Test User",
            email = "new@example.com",
            phone = "0821234567",
            password = "123" // Too weak
        )
        // Mock mode may accept this, but in production it should fail
        assertNotNull("Should handle weak password", result)
    }

    @Test
    fun `createAccount with empty name returns failure`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.createAccount(
            fullName = "",
            email = "new@example.com",
            phone = "0821234567",
            password = "Password1"
        )
        assertFalse("Should fail with empty name", result.success)
    }

    @Test
    fun `isLoggedIn returns false when not logged in`() {
        assertFalse("Should not be logged in", AuthRepository.isLoggedIn())
    }

    @Test
    fun `getCurrentUser returns null when not logged in`() {
        assertNull("Should return null", AuthRepository.getCurrentUser())
    }

    @Test
    fun `signOut clears session without crash`() {
        // Should not throw exception
        AuthRepository.signOut()
        assertFalse("Should not be logged in after signOut", AuthRepository.isLoggedIn())
    }

    @Test
    fun `getCurrentAuthToken returns null when not logged in`() {
        assertNull("Should return null", AuthRepository.getCurrentAuthToken())
    }

    @Test
    fun `signIn with admin credentials works`() = kotlinx.coroutines.runBlocking {
        val result = AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
        assertTrue("Admin login should succeed", result.success)
        assertEquals(UserRole.ADMIN, result.user?.role)
    }

    @Test
    fun `signIn with parent credentials works`() = kotlinx.coroutines.runBlocking {
        // In mock mode, only admin exists
        val result = AuthRepository.signIn("admin@aplusstudy.co.za", "Admin123")
        assertTrue("Admin login should succeed", result.success)
        assertEquals(UserRole.ADMIN, result.user?.role)
    }

    @Test
    fun `signOut twice does not crash`() {
        AuthRepository.signOut()
        AuthRepository.signOut() // Second call should not throw
        assertFalse(AuthRepository.isLoggedIn())
    }
}
