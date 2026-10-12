package com.aplusstudyhouse.app.data

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
    fun `unknown parent has no students, applications, invoices, or messages`() =
        kotlinx.coroutines.runBlocking {
            assertTrue("No students for unknown parent", SupabaseRepository.getParentStudents("NONEXISTENT").isEmpty())
            assertTrue(
                "No applications for unknown parent",
                SupabaseRepository.getParentApplications("NONEXISTENT").isEmpty()
            )
            assertTrue("No invoices for unknown parent", SupabaseRepository.getParentInvoices("NONEXISTENT").isEmpty())
            assertTrue("No messages for unknown parent", SupabaseRepository.getUserMessages("NONEXISTENT").isEmpty())
        }

    @Test
    fun `unknown student lookup returns null`() =
        kotlinx.coroutines.runBlocking {
            assertNull("Unknown student id returns null", SupabaseRepository.getStudent("NONEXISTENT"))
        }

    // ============================================
    // CREATE + ISOLATION ROUND-TRIP
    // ============================================

    @Test
    fun `createApplication is isolated to its parent and invisible to others`() =
        kotlinx.coroutines.runBlocking {
            val json =
                JSONObject().apply {
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
            assertTrue(
                "Global list reflects the new submission",
                SupabaseRepository.getAllApplications().any {
                    it.id == created?.id
                }
            )
        }

    @Test
    fun `students created for one parent stay isolated`() =
        kotlinx.coroutines.runBlocking {
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

    // ============================================
    // FEE CONFIG
    // ============================================

    @Test
    fun `monthly invoice body contains payable financial fields`() {
        val body = SupabaseRepository.buildMonthlyInvoiceBody("S001", 1700.0, "Jane Doe")

        assertEquals("S001", body.getString("student_id"))
        assertEquals(1700.0, body.getDouble("amount"), 0.001)
        assertEquals("Monthly School Fee", body.getString("description"))
        assertEquals("Jane Doe", body.getString("reference"))
        assertEquals(java.time.YearMonth.now().toString(), body.getString("billing_period"))
        assertEquals("pending", body.getString("status"))
        assertEquals("registration", body.getString("category"))
        assertEquals("now() + interval '30 days'", body.getString("due_date"))
        assertEquals("now()", body.getString("created_at"))
    }

    @Test
    fun `monthly invoice body preserves sibling fee and surname reference`() {
        val body = SupabaseRepository.buildMonthlyInvoiceBody("S002", 1650.0, "Doe, Jane", "2026-10")

        assertEquals(1650.0, body.getDouble("amount"), 0.001)
        assertEquals("2026-10", body.getString("billing_period"))
        assertEquals("Doe, Jane", body.getString("reference"))
    }

    @Test
    fun `getFeeConfig returns defaults in mock mode`() =
        kotlinx.coroutines.runBlocking {
            val fees = SupabaseRepository.getFeeConfig()
            assertEquals(
                "Registration fee default should match FeeConfig default",
                FeeConfig().registrationFee,
                fees.registrationFee,
                0.01
            )
            assertEquals(
                "Monthly first-child fee default should match FeeConfig default",
                FeeConfig().monthlyFeeFirstChild,
                fees.monthlyFeeFirstChild,
                0.01
            )
            assertEquals(
                "Monthly sibling fee default should match FeeConfig default",
                FeeConfig().monthlyFeeSibling,
                fees.monthlyFeeSibling,
                0.01
            )
            assertEquals(
                "Project fee default should match FeeConfig default",
                FeeConfig().projectFeeGrade6,
                fees.projectFeeGrade6,
                0.01
            )
            assertEquals(
                "Project fee quarter default should be Q3 in mock mode",
                FeeConfig().projectFeeQuarter,
                fees.projectFeeQuarter
            )
        }
}
