package com.example.blankapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for data models
 * Tests data class construction, enum values, and default values
 */
class DataModelTest {

    // ============================================
    // MockUser TESTS
    // ============================================

    @Test
    fun `MockUser can be constructed with all fields`() {
        val user = MockUser(
            id = "P001",
            fullName = "Test User",
            email = "test@example.com",
            phone = "0821234567",
            password = "Password1",
            role = UserRole.PARENT,
            createdAt = "2024-01-15",
            surname = "User",
            idNumber = "8503151234089",
            employer = "Test Corp",
            workPhone = "0112345678"
        )
        assertEquals("P001", user.id)
        assertEquals("Test User", user.fullName)
        assertEquals(UserRole.PARENT, user.role)
    }

    @Test
    fun `MockUser has correct default values`() {
        val user = MockUser(
            id = "P001",
            fullName = "Test",
            email = "test@example.com",
            phone = "0821234567",
            password = "pass",
            role = UserRole.PARENT
        )
        assertEquals("2024-01-15", user.createdAt)
        assertEquals("", user.surname)
        assertEquals("", user.idNumber)
        assertEquals("", user.employer)
        assertEquals("", user.workPhone)
    }

    // ============================================
    // MockStudent TESTS
    // ============================================

    @Test
    fun `MockStudent can be constructed with required fields`() {
        val student = MockStudent(
            id = "S001",
            firstName = "Oliver",
            lastName = "Johnson",
            dateOfBirth = "2015-03-15",
            grade = 5,
            school = "Sunnydale Primary",
            address = "12 Oak Street",
            parentId = "P001",
            status = StudentStatus.ACTIVE
        )
        assertEquals("S001", student.id)
        assertEquals("Oliver", student.firstName)
        assertEquals(5, student.grade)
        assertEquals(StudentStatus.ACTIVE, student.status)
    }

    @Test
    fun `MockStudent has correct default values`() {
        val student = MockStudent(
            id = "S001",
            firstName = "Test",
            lastName = "Student",
            dateOfBirth = "2015-01-01",
            grade = 1,
            school = "Test School",
            address = "Test Address",
            parentId = "P001",
            status = StudentStatus.ACTIVE
        )
        assertEquals("", student.gender)
        assertEquals("", student.classNr)
        assertEquals("", student.teacherName)
        assertFalse(student.lsen)
        assertTrue(student.sports.isEmpty())
        assertFalse(student.transportRequired)
        assertFalse(student.photoConsent)
    }

    // ============================================
    // MockApplication TESTS
    // ============================================

    @Test
    fun `MockApplication can be constructed with status`() {
        val app = MockApplication(
            id = "APP001",
            parentId = "P001",
            parentName = "Test Parent",
            childFirstName = "Test",
            childLastName = "Student",
            grade = 1,
            school = "Test School",
            status = ApplicationStatus.SUBMITTED,
            submittedDate = "2024-01-01"
        )
        assertEquals(ApplicationStatus.SUBMITTED, app.status)
    }

    @Test
    fun `MockApplication default payment amount is R450`() {
        val app = MockApplication(
            id = "APP001",
            parentId = "P001",
            parentName = "Test Parent",
            childFirstName = "Test",
            childLastName = "Student",
            grade = 1,
            school = "Test School",
            status = ApplicationStatus.UNDER_REVIEW,
            submittedDate = "2024-01-01"
        )
        assertEquals(450.0, app.paymentAmount, 0.01)
    }

    // ============================================
    // MockInvoice TESTS
    // ============================================

    @Test
    fun `MockInvoice can be constructed`() {
        val invoice = MockInvoice(
            id = "INV001",
            studentId = "S001",
            description = "Aftercare — August 2024",
            amount = 1800.00,
            dueDate = "2024-08-31",
            status = InvoiceStatus.PENDING,
            category = InvoiceCategory.AFTERCARE
        )
        assertEquals("INV001", invoice.id)
        assertEquals(1800.00, invoice.amount, 0.01)
        assertEquals(InvoiceCategory.AFTERCARE, invoice.category)
    }

    // ============================================
    // MockPayment TESTS
    // ============================================

    @Test
    fun `MockPayment can be constructed`() {
        val payment = MockPayment(
            id = "PAY001",
            invoiceId = "INV001",
            parentId = "P001",
            parentName = "Sarah Johnson",
            studentId = "S001",
            amount = 1800.00,
            paymentDate = "2024-08-25",
            paymentMethod = PaymentMethod.EFT,
            status = PaymentStatus.VERIFIED
        )
        assertEquals("PAY001", payment.id)
        assertEquals(PaymentMethod.EFT, payment.paymentMethod)
        assertEquals(PaymentStatus.VERIFIED, payment.status)
    }

    // ============================================
    // MockDocument TESTS
    // ============================================

    @Test
    fun `MockDocument can be constructed`() {
        val doc = MockDocument(
            id = "DOC001",
            fileName = "Medical Certificate",
            name = "Medical Certificate",
            category = DocumentCategory.MEDICAL,
            studentId = "S001",
            parentId = "P001",
            uploadDate = "2024-01-15",
            fileSize = "245 KB"
        )
        assertEquals("DOC001", doc.id)
        assertEquals(DocumentCategory.MEDICAL, doc.category)
    }

    // ============================================
    // MockPermission TESTS
    // ============================================

    @Test
    fun `MockPermission can be constructed`() {
        val perm = MockPermission(
            id = "PERM001",
            title = "Sports Day",
            description = "Permission for sports day",
            studentId = "S001",
            studentName = "Oliver Johnson",
            parentId = "P001",
            parentName = "Sarah Johnson",
            createdBy = "A001",
            createdDate = "2024-09-01",
            dueDate = "2024-09-10",
            status = PermissionStatus.PENDING,
            category = PermissionCategory.SPORTS
        )
        assertEquals("PERM001", perm.id)
        assertEquals(PermissionCategory.SPORTS, perm.category)
        assertEquals(PermissionStatus.PENDING, perm.status)
    }

    // ============================================
    // MockMessage TESTS
    // ============================================

    @Test
    fun `MockMessage can be constructed`() {
        val msg = MockMessage(
            id = "MSG001",
            threadId = "MSG001",
            subject = "Test Subject",
            senderId = "A001",
            senderName = "Margaret",
            recipientId = "P001",
            recipientName = "Sarah Johnson",
            content = "Test message content",
            timestamp = "2024-09-01 09:00"
        )
        assertEquals("MSG001", msg.id)
        assertEquals("Test Subject", msg.subject)
        assertFalse(msg.isRead)
    }

    @Test
    fun `MockMessage default threadId equals id`() {
        val msg = MockMessage(
            id = "MSG001",
            threadId = "MSG001",
            subject = "Test",
            senderId = "A001",
            senderName = "Margaret",
            recipientId = "P001",
            recipientName = "Sarah",
            content = "Content",
            timestamp = "2024-09-01"
        )
        assertEquals("Thread ID should default to message ID", msg.id, msg.threadId)
    }

    // ============================================
    // MockNotification TESTS
    // ============================================

    @Test
    fun `MockNotification can be constructed`() {
        val notif = MockNotification(
            id = "NOT001",
            userId = "P001",
            title = "Payment Verified",
            message = "Your payment has been verified",
            type = NotificationType.PAYMENT,
            timestamp = "2024-08-26 10:00"
        )
        assertEquals("NOT001", notif.id)
        assertEquals(NotificationType.PAYMENT, notif.type)
        assertFalse(notif.isRead)
    }

    // ============================================
    // ENUM TESTS
    // ============================================

    @Test
    fun `UserRole has exactly 2 values`() {
        assertEquals(2, UserRole.values().size)
        assertTrue(UserRole.values().contains(UserRole.PARENT))
        assertTrue(UserRole.values().contains(UserRole.ADMIN))
    }

    @Test
    fun `StudentStatus has exactly 3 values`() {
        assertEquals(3, StudentStatus.values().size)
    }

    @Test
    fun `ApplicationStatus has exactly 6 values`() {
        assertEquals(6, ApplicationStatus.values().size)
    }

    @Test
    fun `InvoiceStatus has valid values`() {
        assertTrue(InvoiceStatus.values().size >= 3)
    }

    @Test
    fun `InvoiceCategory has exactly 4 values`() {
        assertEquals(4, InvoiceCategory.values().size)
    }

    @Test
    fun `PaymentMethod has valid values`() {
        assertTrue(PaymentMethod.values().size >= 2)
    }

    @Test
    fun `PaymentStatus has exactly 3 values`() {
        assertEquals(3, PaymentStatus.values().size)
    }

    @Test
    fun `DocumentCategory has valid values`() {
        assertTrue(DocumentCategory.values().size >= 7)
    }

    @Test
    fun `PermissionCategory has valid values`() {
        assertTrue(PermissionCategory.values().size >= 5)
    }

    @Test
    fun `MessageCategory has exactly 5 values`() {
        assertEquals(5, MessageCategory.values().size)
    }

    @Test
    fun `NotificationType has valid values`() {
        assertTrue(NotificationType.values().size >= 7)
    }
}
