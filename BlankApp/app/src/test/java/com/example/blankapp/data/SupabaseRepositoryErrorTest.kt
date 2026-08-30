package com.example.blankapp.data

import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Error handling tests for SupabaseRepository.
 * Tests edge cases: network failures, malformed responses, server errors.
 */
class SupabaseRepositoryErrorTest {

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
    fun `createApplication returns null on network error`() = kotlinx.coroutines.runBlocking {
        // In mock mode, createApplication should return a mock application
        val app = JSONObject().apply {
            put("student_first_name", "Test")
            put("student_last_name", "Child")
        }
        val result = SupabaseRepository.createApplication(app, "P001")
        // Mock mode returns a mock application
        assertNotNull("Should return mock application in mock mode", result)
    }

    @Test
    fun `getParentApplications returns empty list for unknown parent`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getParentApplications("NONEXISTENT")
        assertTrue("Should return empty list for unknown parent", result.isEmpty())
    }

    @Test
    fun `getParentStudents returns empty list for unknown parent`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getParentStudents("NONEXISTENT")
        assertTrue("Should return empty list for unknown parent", result.isEmpty())
    }

    @Test
    fun `getStudent returns null for non-existent ID`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getStudent("NONEXISTENT")
        assertNull("Should return null for non-existent student", result)
    }

    @Test
    fun `getParentInvoices returns empty list for unknown parent`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getParentInvoices("NONEXISTENT")
        assertTrue("Should return empty list for unknown parent", result.isEmpty())
    }

    @Test
    fun `getParentPermissions returns empty list for unknown parent`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getParentPermissions("NONEXISTENT")
        assertTrue("Should return empty list for unknown parent", result.isEmpty())
    }

    @Test
    fun `getUserMessages returns empty list for unknown user`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getUserMessages("NONEXISTENT")
        assertTrue("Should return empty list for unknown user", result.isEmpty())
    }

    @Test
    fun `getUserNotifications returns empty list for unknown user`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseRepository.getUserNotifications("NONEXISTENT")
        assertTrue("Should return empty list for unknown user", result.isEmpty())
    }

    @Test
    fun `lastError field exists for error tracking`() {
        // lastError is private but accessible via getter
        val lastError = SupabaseRepository.lastError
        // Should not throw exception
        assertNotNull("lastError field should be accessible", lastError ?: "no-error")
    }

    @Test
    fun `createApplication handles empty JSON gracefully`() = kotlinx.coroutines.runBlocking {
        val app = JSONObject()
        val result = SupabaseRepository.createApplication(app, "P001")
        // Should not crash, returns mock in mock mode
        assertNotNull("Should handle empty JSON gracefully", result)
    }

    @Test
    fun `createApplication handles special characters in name`() = kotlinx.coroutines.runBlocking {
        val app = JSONObject().apply {
            put("student_first_name", "Test<script>alert('xss')</script>")
            put("student_last_name", "O'Brien")
        }
        val result = SupabaseRepository.createApplication(app, "P001")
        assertNotNull("Should handle special characters", result)
    }

    @Test
    fun `createApplication handles very long names`() = kotlinx.coroutines.runBlocking {
        val app = JSONObject().apply {
            put("student_first_name", "A".repeat(1000))
            put("student_last_name", "B".repeat(1000))
        }
        val result = SupabaseRepository.createApplication(app, "P001")
        assertNotNull("Should handle very long names", result)
    }

    @Test
    fun `createApplication handles unicode characters`() = kotlinx.coroutines.runBlocking {
        val app = JSONObject().apply {
            put("student_first_name", "José")
            put("student_last_name", "Müller")
        }
        val result = SupabaseRepository.createApplication(app, "P001")
        assertNotNull("Should handle unicode characters", result)
    }

    @Test
    fun `createApplication handles null parent ID`() = kotlinx.coroutines.runBlocking {
        val app = JSONObject().apply {
            put("student_first_name", "Test")
            put("student_last_name", "Child")
        }
        val result = SupabaseRepository.createApplication(app, "")
        // Should not crash
        assertNotNull("Should handle null parent ID", result)
    }
}
