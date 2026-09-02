package com.example.blankapp.data

import kotlinx.serialization.Serializable

/**
 * Data structures for A+ Study House App
 * Mock data removed — app now uses Supabase for real data.
 * Only admin auto-login is retained for testing purposes.
 */

// ============================================
// USER DATA
// ============================================

data class MockUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: UserRole,
    val createdAt: String = "2024-01-15",
    val surname: String = "",
    val idNumber: String = "",
    val employer: String = "",
    val workPhone: String = ""
)

enum class UserRole {
    PARENT, ADMIN
}

// Admin user for testing only — not for production
val mockUsers = mutableListOf(
    MockUser(
        id = "A001",
        fullName = "Margaret",
        email = "admin@aplusstudy.co.za",
        phone = "0112345678",
        password = "Admin123",
        role = UserRole.ADMIN
    )
)

// ============================================
// STUDENT DATA (Grades 1-7 only)
// ============================================

data class MockStudent(
    val id: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val grade: Int,
    val school: String,
    val address: String,
    val parentId: String,
    val status: StudentStatus,
    val gender: String = "",
    val classNr: String = "",
    val teacherName: String = "",
    val lsen: Boolean = false,
    val sports: List<String> = emptyList(),
    val collectionPerson: String? = null,
    val collectionContact: String? = null,
    val vehicleReg1: String = "",
    val collectionPerson2: String? = null,
    val collectionContact2: String? = null,
    val vehicleReg2: String = "",
    val transportRequired: Boolean = false,
    val doctorName: String? = null,
    val doctorLocation: String? = null,
    val doctorContact: String? = null,
    val medicalPlan: String? = null,
    val medicalAidNumber: String? = null,
    val allergies: List<String> = emptyList(),
    val epilepsy: Boolean = false,
    val diabetic: Boolean = false,
    val asthma: Boolean = false,
    val noseBleeder: Boolean = false,
    val hasAllergies: Boolean = false,
    val photoConsent: Boolean = false,
    val parentSignature: String? = null
)

enum class StudentStatus {
    ACTIVE, PENDING, INACTIVE
}

val mockStudents = mutableListOf<MockStudent>()

// ============================================
// APPLICATION DATA
// ============================================

data class MockApplication(
    val id: String,
    val parentId: String,
    val parentName: String,
    val childFirstName: String,
    val childLastName: String,
    val grade: Int,
    val school: String,
    val submittedDate: String,
    val status: ApplicationStatus,
    val lastUpdated: String = "",
    val notes: String = "",
    val registrationFeePaid: Boolean = false,
    val documentsUploaded: Boolean = false,
    val studentFirstName: String = childFirstName,
    val studentLastName: String = childLastName,
    val studentGrade: Int = grade,
    val studentSchool: String = school,
    val studentDOB: String = "",
    val studentAddress: String = "",
    val studentGender: String = "",
    val studentClassNumber: String = "",
    val studentTeacherName: String = "",
    val studentLsen: String = "",
    val changesRequired: String? = if (notes.isNotBlank()) notes else null,
    val reviewedDate: String? = if (lastUpdated.isNotBlank()) lastUpdated else null,
    val paymentVerifiedDate: String? = if (lastUpdated.isNotBlank()) lastUpdated else null,
    val approvedDate: String? = if (lastUpdated.isNotBlank()) lastUpdated else null,
    val sports: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val collectionPerson: String = "",
    val collectionContact: String = "",
    val medicalInfo: String = "",
    val doctorName: String = "",
    val medicalAidName: String = "",
    val photoConsent: Boolean = false,
    val paymentAmount: Double = 450.0,
    val paymentProofUrl: String? = null
)

enum class ApplicationStatus {
    SUBMITTED, UNDER_REVIEW, CHANGES_REQUIRED, PAYMENT_VERIFIED, APPROVED, REJECTED
}

val mockApplications = mutableListOf<MockApplication>()

// ============================================
// REGISTRATION DRAFT
// Holds the data entered across the 9-step flow so the
// submit screen can persist a real application row (instead of
// discarding it). Each step's onContinue data is captured here.
// ============================================

@Serializable
data class RegistrationDraft(
    // Step 1 - Student details
    var studentName: String = "",
    var grade: Int = 0,
    var school: String = "",
    var dob: String = "",
    var address: String = "",
    var gender: String = "",
    var classNr: String = "",
    var teacherName: String = "",
    var lsen: Boolean = false,
    // Step 2 - Sports & activities
    var sports: List<String> = emptyList(),
    // Step 3 - Collection & transport
    var collectionPerson1: String = "",
    var contact1: String = "",
    var vehicleReg1: String = "",
    var collectionPerson2: String = "",
    var contact2: String = "",
    var vehicleReg2: String = "",
    var transportRequired: Boolean = false,
    // Step 4 - Medical information
    var doctorName: String = "",
    var doctorLocation: String = "",
    var doctorContact: String = "",
    var medicalPlan: String = "",
    var medicalAidNumber: String = "",
    var allergies: String = "",
    var epilepsy: Boolean = false,
    var diabetic: Boolean = false,
    var asthma: Boolean = false,
    var noseBleeder: Boolean = false,
    var hasAllergies: Boolean = false,
    // Step 5 - Parent details
    var motherName: String = "",
    var motherSurname: String = "",
    var motherId: String = "",
    var motherEmployer: String = "",
    var motherWorkPhone: String = "",
    var motherCell: String = "",
    var motherEmail: String = "",
    var fatherName: String = "",
    var fatherSurname: String = "",
    var fatherId: String = "",
    var fatherEmployer: String = "",
    var fatherWorkPhone: String = "",
    var fatherCell: String = "",
    var fatherEmail: String = "",
    // Step 6 - Consent & signature
    var photoConsent: Boolean = false,
    var parentSignature: String = "",
    // Step 7 - Payment
    var paymentMethod: String = "eft"
) {
    /** Copies all step fields from [other] into this draft (used to resume a saved draft). */
    fun copyFrom(other: RegistrationDraft) {
        studentName = other.studentName
        grade = other.grade
        school = other.school
        dob = other.dob
        address = other.address
        gender = other.gender
        classNr = other.classNr
        teacherName = other.teacherName
        lsen = other.lsen
        sports = other.sports
        collectionPerson1 = other.collectionPerson1
        contact1 = other.contact1
        vehicleReg1 = other.vehicleReg1
        collectionPerson2 = other.collectionPerson2
        contact2 = other.contact2
        vehicleReg2 = other.vehicleReg2
        transportRequired = other.transportRequired
        doctorName = other.doctorName
        doctorLocation = other.doctorLocation
        doctorContact = other.doctorContact
        medicalPlan = other.medicalPlan
        medicalAidNumber = other.medicalAidNumber
        allergies = other.allergies
        epilepsy = other.epilepsy
        diabetic = other.diabetic
        asthma = other.asthma
        noseBleeder = other.noseBleeder
        hasAllergies = other.hasAllergies
        motherName = other.motherName
        motherSurname = other.motherSurname
        motherId = other.motherId
        motherEmployer = other.motherEmployer
        motherWorkPhone = other.motherWorkPhone
        motherCell = other.motherCell
        motherEmail = other.motherEmail
        fatherName = other.fatherName
        fatherSurname = other.fatherSurname
        fatherId = other.fatherId
        fatherEmployer = other.fatherEmployer
        fatherWorkPhone = other.fatherWorkPhone
        fatherCell = other.fatherCell
        fatherEmail = other.fatherEmail
        photoConsent = other.photoConsent
        parentSignature = other.parentSignature
        paymentMethod = other.paymentMethod
    }
}

// ============================================
// INVOICE DATA
// ============================================

data class MockInvoice(
    val id: String,
    val studentId: String,
    val studentName: String = "",
    val description: String,
    val amount: Double,
    val dueDate: String,
    val status: InvoiceStatus,
    val paidDate: String? = null,
    val category: InvoiceCategory = InvoiceCategory.AFTERCARE,
    val reference: String = ""
)

enum class InvoiceStatus {
    PENDING, PAID, OVERDUE, CANCELLED
}

enum class InvoiceCategory {
    REGISTRATION, AFTERCARE, TRANSPORT, STATIONERY
}

val mockInvoices = mutableListOf<MockInvoice>()

// ============================================
// PAYMENT DATA
// ============================================

data class MockPayment(
    val id: String,
    val invoiceId: String,
    val parentId: String,
    val parentName: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val proofUrl: String? = null,
    val notes: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val description: String = "",
    val date: String = paymentDate,
    val reference: String = id,
    val type: String = paymentMethod.name
)

enum class PaymentMethod {
    EFT, CASH, CARD
}

enum class PaymentStatus {
    PENDING, VERIFIED, REJECTED
}

val mockPayments = mutableListOf<MockPayment>()

// ============================================
// DOCUMENT DATA
// ============================================

data class MockDocument(
    val id: String,
    val studentId: String,
    val parentId: String,
    val studentName: String = "",
    val fileName: String,
    val category: DocumentCategory,
    val uploadDate: String,
    val fileSize: String = "",
    val url: String = "",
    val type: String = "",
    val date: String = "",
    val name: String = fileName
)

enum class DocumentCategory {
    MEDICAL, ID_DOCUMENT, ID_COPY, REPORT, REPORT_CARD, PHOTO, PROOF_OF_PAYMENT, OTHER
}

val mockDocuments = mutableListOf<MockDocument>()

// ============================================
// PERMISSION DATA
// ============================================

data class MockPermission(
    val id: String,
    val title: String,
    val description: String,
    val category: PermissionCategory,
    val createdBy: String,
    val createdDate: String,
    val expiryDate: String? = null,
    val dueDate: String? = expiryDate,
    val status: PermissionStatus,
    val parentId: String,
    val parentName: String,
    val studentId: String = "",
    val studentName: String = "",
    val response: PermissionResponse? = null,
    val responseDate: String? = null,
    val responseNotes: String? = null,
    val notes: String = ""
)

enum class PermissionCategory {
    EXCURSION, MEDICAL, PHOTO, SPORTS, GENERAL, OTHER
}

enum class PermissionStatus {
    PENDING, APPROVED, DECLINED, EXPIRED, RESPONDED
}

enum class PermissionResponse {
    GRANTED, DECLINED
}

val mockPermissions = mutableListOf<MockPermission>()

// ============================================
// MESSAGE DATA
// ============================================

data class MockMessage(
    val id: String,
    val threadId: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val recipientName: String,
    val subject: String,
    val content: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val category: MessageCategory = MessageCategory.GENERAL,
    val parentMessageId: String? = null,
    val isAnnouncement: Boolean = false
)

enum class MessageCategory {
    APPLICATION, FINANCE, ANNOUNCEMENT, PERMISSION, GENERAL
}

val mockMessages = mutableListOf<MockMessage>()

// ============================================
// NOTIFICATION DATA
// ============================================

data class MockNotification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val type: NotificationType,
    val relatedId: String? = null
)

enum class NotificationType {
    PAYMENT, DOCUMENT, APPLICATION, PERMISSION, MESSAGE, REMINDER, GENERAL,
    PAYMENT_VERIFIED, CHANGES_REQUESTED, APPLICATION_UPDATE,
    PERMISSION_REQUEST, ANNOUNCEMENT, MESSAGE_RECEIVED
}

val mockNotifications = mutableListOf<MockNotification>()

// ============================================
// ACTIVITY LOG DATA
// ============================================

data class ActivityLogEntry(
    val id: String,
    val actorName: String,
    val action: String,
    val details: String,
    val timestamp: String,
    val type: ActivityType
)

enum class ActivityType {
    APPLICATION, PAYMENT, STUDENT, MESSAGE, SYSTEM
}

val mockActivityLog = mutableListOf<ActivityLogEntry>()

// ============================================
// NOTIFICATION PREFERENCES
// ============================================

data class NotificationPreference(
    val paymentNotifications: Boolean = true,
    val documentNotifications: Boolean = true,
    val applicationNotifications: Boolean = true,
    val permissionNotifications: Boolean = true,
    val messageNotifications: Boolean = true,
    val reminderNotifications: Boolean = true,
    val announcementNotifications: Boolean = true
)

// NotificationPreference is persisted per-user in the Supabase
// `notification_preferences` table (see SupabaseRepository).

// ============================================
// COLLECTION PERSON DATA
// ============================================

data class CollectionPerson(
    val name: String,
    val contact: String,
    val vehicleReg: String = "",
    val relationship: String = ""
)

// ============================================
// HELPER FUNCTIONS (return empty lists)
// ============================================

fun getStudentsByParent(parentId: String): List<MockStudent> {
    return mockStudents.filter { it.parentId == parentId }
}

fun getInvoicesByStudent(studentId: String): List<MockInvoice> {
    return mockInvoices.filter { it.studentId == studentId }
}

fun getDocumentsByParent(parentId: String): List<MockDocument> {
    return mockDocuments.filter { it.parentId == parentId }
}

fun getDocumentsByStudent(studentId: String): List<MockDocument> {
    return mockDocuments.filter { it.studentId == studentId }
}

fun getApplicationsByParent(parentId: String): List<MockApplication> {
    return mockApplications.filter { it.parentId == parentId }
}

fun getPermissionsByParent(parentId: String): List<MockPermission> {
    return mockPermissions.filter { it.parentId == parentId }
}

fun getMessagesByUser(userId: String): List<MockMessage> {
    return mockMessages.filter { it.recipientId == userId || it.senderId == userId || it.recipientId == "ALL" }
}

fun getThreadMessages(threadId: String): List<MockMessage> {
    return mockMessages.filter { it.threadId == threadId }.sortedBy { it.timestamp }
}

fun getFamilyBalance(parentId: String): Double {
    val students = getStudentsByParent(parentId)
    var totalBalance = 0.0
    students.forEach { student ->
        val invoices = getInvoicesByStudent(student.id)
        invoices.forEach { invoice ->
            if (invoice.status == InvoiceStatus.PENDING || invoice.status == InvoiceStatus.OVERDUE) {
                totalBalance += invoice.amount
            }
        }
    }
    return totalBalance
}

fun getStudentBalance(studentId: String): Double {
    val invoices = getInvoicesByStudent(studentId)
    var balance = 0.0
    invoices.forEach { invoice ->
        if (invoice.status == InvoiceStatus.PENDING || invoice.status == InvoiceStatus.OVERDUE) {
            balance += invoice.amount
        }
    }
    return balance
}

fun getStudentById(studentId: String): MockStudent? {
    return mockStudents.find { it.id == studentId }
}

fun getUserById(userId: String): MockUser? {
    return mockUsers.find { it.id == userId }
}

fun getOverdueCount(): Int {
    return mockInvoices.count { it.status == InvoiceStatus.OVERDUE }
}

fun getPendingApplicationCount(): Int {
    return mockApplications.count {
        it.status == ApplicationStatus.SUBMITTED || it.status == ApplicationStatus.UNDER_REVIEW
    }
}

fun getTotalCollected(): Double {
    return mockPayments.filter { it.status == PaymentStatus.VERIFIED }.sumOf { it.amount }
}

fun getTotalOutstanding(): Double {
    return mockInvoices
        .filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
        .sumOf { it.amount }
}

fun toUserFriendlyMessage(): String {
    return "An error occurred. Please try again."
}

fun Exception.toUserFriendlyMessage(): String {
    return when {
        this is java.net.UnknownHostException -> "No internet connection. Please check your network."
        this is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
        this is java.net.ConnectException -> "Unable to connect to server. Please try again later."
        this is javax.net.ssl.SSLException -> "Secure connection failed. Please try again."
        this is org.json.JSONException -> "Invalid data format. Please try again."
        this is kotlinx.coroutines.CancellationException -> throw this
        else -> "An unexpected error occurred. Please try again."
    }
}
