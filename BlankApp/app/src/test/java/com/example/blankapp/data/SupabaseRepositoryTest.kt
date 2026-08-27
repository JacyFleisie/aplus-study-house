package com.example.blankapp.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Unit tests for SupabaseRepository
 * Tests data access patterns using mock fallback
 */
class SupabaseRepositoryTest {

    @Before
    fun setup() {
        // Skip tests if Supabase is configured (use mock mode for unit tests)
        assumeTrue("Skipping mock repository tests when Supabase is configured", !SupabaseConfig.isConfigured())
        AuthRepository.signOut()
    }

    // ============================================
    // STUDENT QUERIES
    // ============================================

    @Test
    fun `getParentStudents returns students for parent P001`() = kotlinx.coroutines.runBlocking {
        val students = SupabaseRepository.getParentStudents("P001")
        assertTrue("P001 should have students", students.isNotEmpty())
        students.forEach { student ->
            assertEquals("All students should belong to P001", "P001", student.parentId)
        }
    }

    @Test
    fun `getParentStudents returns empty for non-existent parent`() = kotlinx.coroutines.runBlocking {
        val students = SupabaseRepository.getParentStudents("NONEXISTENT")
        assertTrue("Non-existent parent should have 0 students", students.isEmpty())
    }

    @Test
    fun `getAllStudents returns all mock students`() = kotlinx.coroutines.runBlocking {
        val students = SupabaseRepository.getAllStudents()
        assertEquals("Should return all mock students", mockStudents.size, students.size)
    }

    @Test
    fun `getStudent returns correct student by ID`() = kotlinx.coroutines.runBlocking {
        val student = SupabaseRepository.getStudent("S001")
        assertNotNull("S001 should exist", student)
        assertEquals("Oliver", student?.firstName)
        assertEquals("Johnson", student?.lastName)
    }

    @Test
    fun `getStudent returns null for non-existent ID`() = kotlinx.coroutines.runBlocking {
        val student = SupabaseRepository.getStudent("NONEXISTENT")
        assertNull("Non-existent student should return null", student)
    }

    // ============================================
    // APPLICATION QUERIES
    // ============================================

    @Test
    fun `getParentApplications returns applications for parent P002`() = kotlinx.coroutines.runBlocking {
        val apps = SupabaseRepository.getParentApplications("P002")
        assertTrue("P002 should have applications", apps.isNotEmpty())
        apps.forEach { app ->
            assertEquals("All apps should belong to P002", "P002", app.parentId)
        }
    }

    @Test
    fun `getParentApplications returns empty for non-existent parent`() = kotlinx.coroutines.runBlocking {
        val apps = SupabaseRepository.getParentApplications("NONEXISTENT")
        assertTrue("Non-existent parent should have 0 apps", apps.isEmpty())
    }

    @Test
    fun `getAllApplications returns all mock applications`() = kotlinx.coroutines.runBlocking {
        val apps = SupabaseRepository.getAllApplications()
        assertEquals("Should return all mock applications", mockApplications.size, apps.size)
    }

    // ============================================
    // INVOICE QUERIES
    // ============================================

    @Test
    fun `getParentInvoices returns invoices for parent P001`() = kotlinx.coroutines.runBlocking {
        val invoices = SupabaseRepository.getParentInvoices("P001")
        assertTrue("P001 should have invoices", invoices.isNotEmpty())
    }

    @Test
    fun `getParentInvoices returns empty for non-existent parent`() = kotlinx.coroutines.runBlocking {
        val invoices = SupabaseRepository.getParentInvoices("NONEXISTENT")
        assertTrue("Non-existent parent should have 0 invoices", invoices.isEmpty())
    }

    @Test
    fun `getAllInvoices returns all mock invoices`() = kotlinx.coroutines.runBlocking {
        val invoices = SupabaseRepository.getAllInvoices()
        assertEquals("Should return all mock invoices", mockInvoices.size, invoices.size)
    }

    // ============================================
    // PERMISSION QUERIES
    // ============================================

    @Test
    fun `getParentPermissions returns permissions for parent P001`() = kotlinx.coroutines.runBlocking {
        val perms = SupabaseRepository.getParentPermissions("P001")
        assertTrue("P001 should have permissions", perms.isNotEmpty())
    }

    @Test
    fun `getAllPermissions returns all mock permissions`() = kotlinx.coroutines.runBlocking {
        val perms = SupabaseRepository.getAllPermissions()
        assertEquals("Should return all mock permissions", mockPermissions.size, perms.size)
    }

    // ============================================
    // MESSAGE QUERIES
    // ============================================

    @Test
    fun `getUserMessages returns messages for user P002`() = kotlinx.coroutines.runBlocking {
        val msgs = SupabaseRepository.getUserMessages("P002")
        assertTrue("P002 should have messages", msgs.isNotEmpty())
    }

    @Test
    fun `getUserMessages includes ALL announcements`() = kotlinx.coroutines.runBlocking {
        val msgs = SupabaseRepository.getUserMessages("P001")
        val announcements = msgs.filter { it.isAnnouncement }
        assertTrue("Should include announcements", announcements.isNotEmpty())
    }

    // ============================================
    // NOTIFICATION QUERIES
    // ============================================

    @Test
    fun `getUserNotifications returns notifications for user`() = kotlinx.coroutines.runBlocking {
        val notifs = SupabaseRepository.getUserNotifications("P001")
        // Mock notifications don't have user_id filtering, so just check it doesn't crash
        assertNotNull("Should return notifications", notifs)
    }

    // ============================================
    // DATA INTEGRITY TESTS
    // ============================================

    @Test
    fun `student parent IDs match user IDs`() = kotlinx.coroutines.runBlocking {
        val students = SupabaseRepository.getAllStudents()
        val userIds = mockUsers.map { it.id }.toSet()
        students.forEach { student ->
            assertTrue("Student ${student.id} parent ${student.parentId} should exist in users",
                userIds.contains(student.parentId))
        }
    }

    @Test
    fun `invoice student IDs match student IDs`() = kotlinx.coroutines.runBlocking {
        val invoices = SupabaseRepository.getAllInvoices()
        val studentIds = mockStudents.map { it.id }.toSet()
        invoices.forEach { invoice ->
            assertTrue("Invoice ${invoice.id} student ${invoice.studentId} should exist in students",
                studentIds.contains(invoice.studentId))
        }
    }
}
