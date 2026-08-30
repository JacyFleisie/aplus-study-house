package com.example.blankapp.data

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SupabaseRepository (mock mode).
 *
 * In mock mode the seed lists are intentionally empty (the app uses Supabase for real
 * data); these tests verify the repository's contract:
 *  - queries for an unknown parent return empty lists (no cross-tenant leakage)
 *  - createApplication persists into the parent's own scope and stays isolated
 *  - the global getters reflect what was created
 */
class SupabaseRepositoryTest {

    @Before
    fun setup() {
        // Force mock mode so tests run deterministically regardless of supabase.properties.
        SupabaseConfig.forceMockMode = true
        AuthRepository.signOut()
        mockApplications.clear()
        mockStudents.clear()
        mockInvoices.clear()
        mockPermissions.clear()
        mockMessages.clear()
    }

    // ============================================
    // ISOLATION (no cross-tenant data)
    // ============================================

    @Test
    fun `unknown parent has no students, applications, invoices, or messages`() = kotlinx.coroutines.runBlocking {
        assertTrue("No students for unknown parent", SupabaseRepository.getParentStudents("NONEXISTENT").isEmpty())
        assertTrue("No applications for unknown parent", SupabaseRepository.getParentApplications("NONEXISTENT").isEmpty())
        assertTrue("No invoices for unknown parent", SupabaseRepository.getParentInvoices("NONEXISTENT").isEmpty())
        assertTrue("No messages for unknown parent", SupabaseRepository.getUserMessages("NONEXISTENT").isEmpty())
    }

    @Test
    fun `unknown student lookup returns null`() = kotlinx.coroutines.runBlocking {
        assertNull("Unknown student id returns null", SupabaseRepository.getStudent("NONEXISTENT"))
    }

    // ============================================
    // CREATE + ISOLATION ROUND-TRIP
    // ============================================

    @Test
    fun `createApplication is isolated to its parent and invisible to others`() = kotlinx.coroutines.runBlocking {
        val json = JSONObject().apply {
            put("student_first_name", "Iso")
            put("student_last_name", "Late")
            put("grade", "Grade 3")
            put("school", "Iso Primary")
        }
        val created = SupabaseRepository.createApplication(json, "P001")
        assertNotNull("createApplication returns the persisted app", created)
        assertEquals("P001", created?.parentId)

        val p001Apps = SupabaseRepository.getParentApplications("P001")
        val p002Apps = SupabaseRepository.getParentApplications("P002")
        assertTrue("Owner sees their application", p001Apps.any { it.id == created?.id })
        assertTrue("Other parent must NOT see it", p002Apps.none { it.id == created?.id })
        assertTrue("Global list reflects the new submission", SupabaseRepository.getAllApplications().any { it.id == created?.id })
    }

    @Test
    fun `students created for one parent stay isolated`() = kotlinx.coroutines.runBlocking {
        mockStudents.add(
            MockStudent(
                id = "S_TEST", firstName = "Test", lastName = "Child",
                dateOfBirth = "2018-01-01", grade = 3, school = "Test School",
                address = "1 Test Rd", parentId = "P001", status = StudentStatus.ACTIVE
            )
        )
        val p001 = SupabaseRepository.getParentStudents("P001")
        val p002 = SupabaseRepository.getParentStudents("P002")
        assertTrue("P001 sees the student", p001.any { it.id == "S_TEST" })
        assertTrue("P002 sees no student", p002.isEmpty())
    }
}
