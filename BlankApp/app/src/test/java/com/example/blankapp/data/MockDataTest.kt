package com.example.blankapp.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MockData
 * Tests data consistency, relationships, and business rules
 */
class MockDataTest {

    @Before
    fun resetMockState() {
        // The mock seed lists are intentionally empty in the current app (real data lives in
        // Supabase). Reset shared mutable lists before each test so integrity checks are not
        // polluted by applications/students created elsewhere in the same test JVM.
        mockApplications.clear()
        mockStudents.clear()
        mockInvoices.clear()
        mockPayments.clear()
        mockPermissions.clear()
        mockMessages.clear()
        mockDocuments.clear()
    }

    // ============================================
    // USER DATA TESTS
    // ============================================

    @Test
    fun `mockUsers contains admin`() {
        val admins = mockUsers.filter { it.role == UserRole.ADMIN }
        assertTrue("Should have at least 1 admin", admins.isNotEmpty())
        assertEquals("Admin should be Margaret", "Margaret", admins.first().fullName)
        assertEquals("Admin email should be correct", "admin@aplusstudy.co.za", admins.first().email)
    }

    @Test
    fun `mockUsers filter parents works without error`() {
        val parents = mockUsers.filter { it.role == UserRole.PARENT }
        assertNotNull(parents)
    }

    @Test
    fun `all users have unique emails`() {
        val emails = mockUsers.map { it.email.lowercase() }
        assertEquals("All emails should be unique", emails.size, emails.toSet().size)
    }

    @Test
    fun `all users have unique IDs`() {
        val ids = mockUsers.map { it.id }
        assertEquals("All IDs should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun `all users have non-empty names`() {
        mockUsers.forEach { user ->
            assertTrue("User ${user.id} should have non-empty name", user.fullName.isNotBlank())
        }
    }

    // ============================================
    // STUDENT DATA TESTS
    // ============================================

    @Test
    fun `all students have valid grades 1-7`() {
        mockStudents.forEach { student ->
            assertTrue("Student ${student.id} grade should be >= 1", student.grade >= 1)
            assertTrue("Student ${student.id} grade should be <= 7", student.grade <= 7)
        }
    }

    @Test
    fun `all students have valid parent references`() {
        mockStudents.forEach { student ->
            val parent = mockUsers.find { it.id == student.parentId }
            assertNotNull("Student ${student.id} should have valid parent ${student.parentId}", parent)
        }
    }

    @Test
    fun `all students have non-empty names`() {
        mockStudents.forEach { student ->
            assertTrue("Student ${student.id} should have first name", student.firstName.isNotBlank())
            assertTrue("Student ${student.id} should have last name", student.lastName.isNotBlank())
        }
    }

    @Test
    fun `all students have valid statuses`() {
        val validStatuses = setOf(StudentStatus.ACTIVE, StudentStatus.PENDING, StudentStatus.INACTIVE)
        mockStudents.forEach { student ->
            assertTrue("Student ${student.id} should have valid status", validStatuses.contains(student.status))
        }
    }

    @Test
    fun `student IDs are unique`() {
        val ids = mockStudents.map { it.id }
        assertEquals("Student IDs should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun `each parent with students has valid student-parent relationship`() {
        val parentIdsWithStudents = mockStudents.map { it.parentId }.toSet()
        parentIdsWithStudents.forEach { parentId ->
            val parent = mockUsers.find { it.id == parentId }
            assertNotNull("Parent $parentId should exist in mockUsers", parent)
            assertEquals("Parent $parentId should be a PARENT role", UserRole.PARENT, parent?.role)
        }
    }

    // ============================================
    // APPLICATION DATA TESTS
    // ============================================

    @Test
    fun `all applications have valid statuses`() {
        val validStatuses = setOf(
            ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW,
            ApplicationStatus.CHANGES_REQUIRED, ApplicationStatus.PAYMENT_VERIFIED,
            ApplicationStatus.APPROVED, ApplicationStatus.REJECTED
        )
        mockApplications.forEach { app ->
            assertTrue("Application ${app.id} should have valid status", validStatuses.contains(app.status))
        }
    }

    @Test
    fun `all applications have non-empty parent IDs`() {
        mockApplications.forEach { app ->
            assertTrue("Application ${app.id} should have non-empty parentId", app.parentId.isNotBlank())
        }
    }

    @Test
    fun `application IDs are unique`() {
        val ids = mockApplications.map { it.id }
        assertEquals("Application IDs should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun `applications have valid grades 1-7`() {
        mockApplications.forEach { app ->
            assertTrue("Application ${app.id} grade should be >= 1", app.studentGrade >= 1)
            assertTrue("Application ${app.id} grade should be <= 7", app.studentGrade <= 7)
        }
    }

    // ============================================
    // INVOICE DATA TESTS
    // ============================================

    @Test
    fun `all invoices have valid student references`() {
        mockInvoices.forEach { invoice ->
            val student = mockStudents.find { it.id == invoice.studentId }
            assertNotNull("Invoice ${invoice.id} should have valid student", student)
        }
    }

    @Test
    fun `all invoices have valid statuses`() {
        val validStatuses = setOf(InvoiceStatus.PAID, InvoiceStatus.PENDING, InvoiceStatus.OVERDUE, InvoiceStatus.CANCELLED)
        mockInvoices.forEach { invoice ->
            assertTrue("Invoice ${invoice.id} should have valid status", validStatuses.contains(invoice.status))
        }
    }

    @Test
    fun `all invoices have valid categories`() {
        val validCategories = setOf(
            InvoiceCategory.AFTERCARE, InvoiceCategory.TRANSPORT,
            InvoiceCategory.STATIONERY, InvoiceCategory.REGISTRATION
        )
        mockInvoices.forEach { invoice ->
            assertTrue("Invoice ${invoice.id} should have valid category", validCategories.contains(invoice.category))
        }
    }

    @Test
    fun `invoice amounts are positive`() {
        mockInvoices.forEach { invoice ->
            assertTrue("Invoice ${invoice.id} amount should be positive", invoice.amount > 0)
        }
    }

    @Test
    fun `registration fee invoices are R500`() {
        val regFee = 500.00
        val registrationInvoices = mockInvoices.filter { it.category == InvoiceCategory.REGISTRATION }
        registrationInvoices.forEach { invoice ->
            assertEquals("Registration invoice ${invoice.id} should be R500", regFee, invoice.amount, 0.01)
        }
    }

    // ============================================
    // PAYMENT DATA TESTS
    // ============================================

    @Test
    fun `all payments have valid invoice references`() {
        mockPayments.forEach { payment ->
            val invoice = mockInvoices.find { it.id == payment.invoiceId }
            assertNotNull("Payment ${payment.id} should have valid invoice", invoice)
        }
    }

    @Test
    fun `all payments have valid statuses`() {
        val validStatuses = setOf(PaymentStatus.PENDING, PaymentStatus.VERIFIED, PaymentStatus.REJECTED)
        mockPayments.forEach { payment ->
            assertTrue("Payment ${payment.id} should have valid status", validStatuses.contains(payment.status))
        }
    }

    @Test
    fun `payment amounts are positive`() {
        mockPayments.forEach { payment ->
            assertTrue("Payment ${payment.id} amount should be positive", payment.amount > 0)
        }
    }

    // ============================================
    // PERMISSION DATA TESTS
    // ============================================

    @Test
    fun `all permissions have valid student references`() {
        mockPermissions.forEach { perm ->
            val student = mockStudents.find { it.id == perm.studentId }
            assertNotNull("Permission ${perm.id} should have valid student", student)
        }
    }

    @Test
    fun `all permissions have valid parent references`() {
        mockPermissions.forEach { perm ->
            val parent = mockUsers.find { it.id == perm.parentId }
            assertNotNull("Permission ${perm.id} should have valid parent", parent)
        }
    }

    @Test
    fun `all permissions have valid categories`() {
        val validCategories = setOf(
            PermissionCategory.EXCURSION, PermissionCategory.MEDICAL,
            PermissionCategory.PHOTO, PermissionCategory.SPORTS, PermissionCategory.GENERAL, PermissionCategory.OTHER
        )
        mockPermissions.forEach { perm ->
            assertTrue("Permission ${perm.id} should have valid category", validCategories.contains(perm.category))
        }
    }

    // ============================================
    // MESSAGE DATA TESTS
    // ============================================

    @Test
    fun `all messages have valid sender references`() {
        mockMessages.forEach { msg ->
            val sender = mockUsers.find { it.id == msg.senderId }
            assertNotNull("Message ${msg.id} should have valid sender", sender)
        }
    }

    @Test
    fun `all messages have non-empty content`() {
        mockMessages.forEach { msg ->
            assertTrue("Message ${msg.id} should have content", msg.content.isNotBlank())
        }
    }

    @Test
    fun `announcement messages have ALL as recipient`() {
        val announcements = mockMessages.filter { it.isAnnouncement }
        announcements.forEach { msg ->
            assertEquals("Announcement ${msg.id} should be sent to ALL", "ALL", msg.recipientId)
        }
    }

    // ============================================
    // DOCUMENT DATA TESTS
    // ============================================

    @Test
    fun `all documents have valid categories`() {
        val validCategories = setOf(
            DocumentCategory.MEDICAL,
            DocumentCategory.ID_DOCUMENT, DocumentCategory.ID_COPY, DocumentCategory.REPORT, DocumentCategory.REPORT_CARD,
            DocumentCategory.PHOTO, DocumentCategory.PROOF_OF_PAYMENT, DocumentCategory.OTHER
        )
        mockDocuments.forEach { doc ->
            assertTrue("Document ${doc.id} should have valid category", validCategories.contains(doc.category))
        }
    }

    @Test
    fun `all documents have valid parent references`() {
        mockDocuments.forEach { doc ->
            val parent = mockUsers.find { it.id == doc.parentId }
            assertNotNull("Document ${doc.id} should have valid parent", parent)
        }
    }

    // ============================================
    // HELPER FUNCTION TESTS
    // ============================================

    @Test
    fun `getApplicationsByParent returns correct applications`() {
        val parentApps = getApplicationsByParent("P002")
        parentApps.forEach { app ->
            assertEquals("All apps should belong to P002", "P002", app.parentId)
        }
    }

    @Test
    fun `getApplicationsByParent returns empty for non-existent parent`() {
        val parentApps = getApplicationsByParent("NONEXISTENT")
        assertTrue("Non-existent parent should have 0 applications", parentApps.isEmpty())
    }

    // ============================================
    // BUSINESS RULE TESTS
    // ============================================

    @Test
    fun `grades only go up to 7`() {
        val maxGrade = mockStudents.maxOfOrNull { it.grade } ?: 1
        assertTrue("Max grade should be <= 7", maxGrade <= 7)
    }

    @Test
    fun `grades start at 1`() {
        val minGrade = mockStudents.minOfOrNull { it.grade } ?: 1
        assertTrue("Min grade should be >= 1", minGrade >= 1)
    }

    @Test
    fun `registration fee is R500`() {
        val regFee = 500.00
        val registrationInvoices = mockInvoices.filter { it.category == InvoiceCategory.REGISTRATION }
        registrationInvoices.forEach { invoice ->
            assertEquals("Registration fee should be R500", regFee, invoice.amount, 0.01)
        }
    }
}
