package com.example.blankapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import com.example.blankapp.utils.InputSanitizer

/**
 * Supabase Data Repository
 * Handles all database operations using OkHttp REST API
 * RLS policies enforce access control at the database level
 */
object SupabaseRepository {

    private fun isUsingBackend(): Boolean = SupabaseConfig.isConfigured()
    private fun authToken(): String? = AuthRepository.getCurrentAuthToken()

    /**
     * Last error message from a failed backend call. ViewModels can read this
     * after an operation returns empty/partial data to distinguish
     * "genuinely no data" from "the network/server failed".
     */
    var lastError: String? = null
        private set

    private fun recordError(operation: String, e: Exception) {
        lastError = "$operation failed: ${e.message}"
        android.util.Log.e("SupabaseRepository", lastError, e)
    }

    // ============================================
    // STUDENTS
    // ============================================

    suspend fun getParentStudents(parentId: String): List<MockStudent> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext getMockStudentsForParent(parentId)

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "students",
                query = "parent_id=eq.$parentId&select=*,student_sports(*),collection_persons(*),medical_info(*)",
                authToken = authToken()
            ) ?: return@withContext getMockStudentsForParent(parentId)

            val arr = JSONArray(result)
            val students = mutableListOf<MockStudent>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                students.add(parseStudent(obj))
            }
            students
        } catch (e: Exception) {
            recordError("Backend query", e)
            getMockStudentsForParent(parentId)
        }
    }

    suspend fun getAllStudents(): List<MockStudent> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockStudents.toList()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "students",
                query = "select=*",
                authToken = authToken()
            ) ?: return@withContext mockStudents.toList()

            val arr = JSONArray(result)
            val students = mutableListOf<MockStudent>()
            for (i in 0 until arr.length()) {
                students.add(parseStudent(arr.getJSONObject(i)))
            }
            students
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockStudents.toList()
        }
    }

    suspend fun getStudent(studentId: String): MockStudent? = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockStudents.find { it.id == studentId }

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "students",
                query = "id=eq.$studentId&select=*",
                authToken = authToken()
            ) ?: return@withContext mockStudents.find { it.id == studentId }

            val arr = JSONArray(result)
            if (arr.length() > 0) parseStudent(arr.getJSONObject(0)) else null
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockStudents.find { it.id == studentId }
        }
    }

    // ============================================
    // APPLICATIONS
    // ============================================

    suspend fun getParentApplications(parentId: String): List<MockApplication> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext getMockApplicationsForParent(parentId)

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "applications",
                query = "parent_id=eq.$parentId&select=*",
                authToken = authToken()
            ) ?: return@withContext getMockApplicationsForParent(parentId)

            val arr = JSONArray(result)
            val apps = mutableListOf<MockApplication>()
            for (i in 0 until arr.length()) {
                apps.add(parseApplication(arr.getJSONObject(i)))
            }
            apps
        } catch (e: Exception) {
            recordError("Backend query", e)
            getMockApplicationsForParent(parentId)
        }
    }

    suspend fun getAllApplications(): List<MockApplication> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockApplications.toList()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "applications",
                query = "select=*",
                authToken = authToken()
            ) ?: return@withContext mockApplications.toList()

            val arr = JSONArray(result)
            val apps = mutableListOf<MockApplication>()
            for (i in 0 until arr.length()) {
                apps.add(parseApplication(arr.getJSONObject(i)))
            }
            apps
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockApplications.toList()
        }
    }

    // ============================================
    // INVOICES
    // ============================================

    suspend fun getParentInvoices(parentId: String): List<MockInvoice> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext getMockInvoicesForParent(parentId)

        try {
            // Get student IDs for this parent
            val studentsResult = SupabaseConfig.supabaseGet(
                table = "students",
                query = "parent_id=eq.$parentId&select=id",
                authToken = authToken()
            ) ?: return@withContext getMockInvoicesForParent(parentId)

            val studentsArr = JSONArray(studentsResult)
            if (studentsArr.length() == 0) return@withContext emptyList()

            val studentIds = (0 until studentsArr.length()).map {
                studentsArr.getJSONObject(it).getString("id")
            }

            // Single batched query: invoices WHERE student_id IN (...) instead of
            // one REST call per student (fixes the N+1 query pattern).
            val inList = studentIds.joinToString(",") { it }
            val result = SupabaseConfig.supabaseGet(
                table = "invoices",
                query = "student_id=in.($inList)&select=*",
                authToken = authToken()
            ) ?: return@withContext emptyList()

            val arr = JSONArray(result)
            val invoices = mutableListOf<MockInvoice>()
            for (i in 0 until arr.length()) {
                invoices.add(parseInvoice(arr.getJSONObject(i)))
            }
            invoices
        } catch (e: Exception) {
            recordError("Backend query", e)
            getMockInvoicesForParent(parentId)
        }
    }

    suspend fun getAllInvoices(): List<MockInvoice> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockInvoices.toList()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "invoices",
                query = "select=*",
                authToken = authToken()
            ) ?: return@withContext mockInvoices.toList()

            val arr = JSONArray(result)
            val invoices = mutableListOf<MockInvoice>()
            for (i in 0 until arr.length()) {
                invoices.add(parseInvoice(arr.getJSONObject(i)))
            }
            invoices
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockInvoices.toList()
        }
    }

    // ============================================
    // PAYMENTS
    // ============================================

    suspend fun getAllPayments(): List<MockPayment> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockPayments.toList()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "payments",
                query = "select=*",
                authToken = authToken()
            ) ?: return@withContext mockPayments.toList()

            val arr = JSONArray(result)
            val payments = mutableListOf<MockPayment>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                payments.add(
                    MockPayment(
                        id = obj.optString("id"),
                        invoiceId = obj.optString("invoice_id", ""),
                        parentId = obj.optString("parent_id", ""),
                        parentName = "",
                        amount = obj.optDouble("amount", 0.0),
                        paymentDate = obj.optString("payment_date", ""),
                        paymentMethod = if (obj.optString("payment_method") == "cash") PaymentMethod.CASH else PaymentMethod.EFT,
                        status = when (obj.optString("status")) {
                            "verified" -> PaymentStatus.VERIFIED
                            "rejected" -> PaymentStatus.REJECTED
                            else -> PaymentStatus.PENDING
                        },
                        proofUrl = obj.optString("proof_url", ""),
                        notes = obj.optString("admin_notes", "")
                    )
                )
            }
            payments
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockPayments.toList()
        }
    }

    suspend fun getStudentInvoices(studentId: String): List<MockInvoice> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockInvoices.filter { it.studentId == studentId }

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "invoices",
                query = "student_id=eq.$studentId&select=*",
                authToken = authToken()
            ) ?: return@withContext emptyList()

            val arr = JSONArray(result)
            val invoices = mutableListOf<MockInvoice>()
            for (i in 0 until arr.length()) {
                invoices.add(parseInvoice(arr.getJSONObject(i)))
            }
            invoices
        } catch (e: Exception) {
            recordError("Backend query", e)
            emptyList()
        }
    }

    suspend fun getStudentDocuments(studentId: String): List<MockDocument> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockDocuments.filter { it.studentId == studentId }

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "documents",
                query = "student_id=eq.$studentId&select=*",
                authToken = authToken()
            ) ?: return@withContext emptyList()

            val arr = JSONArray(result)
            val documents = mutableListOf<MockDocument>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                documents.add(
                    MockDocument(
                        id = obj.optString("id"),
                        studentId = obj.optString("student_id", ""),
                        parentId = obj.optString("parent_id", ""),
                        fileName = obj.optString("name", ""),
                        category = when (obj.optString("category")) {
                            "registration" -> DocumentCategory.REGISTRATION
                            "medical" -> DocumentCategory.MEDICAL
                            "id_document" -> DocumentCategory.ID_DOCUMENT
                            "report" -> DocumentCategory.REPORT
                            "photo" -> DocumentCategory.PHOTO
                            "proof_of_payment" -> DocumentCategory.PROOF_OF_PAYMENT
                            else -> DocumentCategory.OTHER
                        },
                        uploadDate = obj.optString("upload_date", ""),
                        fileSize = obj.optString("file_size", ""),
                        url = obj.optString("file_url", "")
                    )
                )
            }
            documents
        } catch (e: Exception) {
            recordError("Backend query", e)
            emptyList()
        }
    }

    // ============================================
    // PERMISSIONS
    // ============================================

    suspend fun getParentPermissions(parentId: String): List<MockPermission> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext getMockPermissionsForParent(parentId)

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "permissions",
                query = "parent_id=eq.$parentId&select=*",
                authToken = authToken()
            ) ?: return@withContext getMockPermissionsForParent(parentId)

            val arr = JSONArray(result)
            val permissions = mutableListOf<MockPermission>()
            for (i in 0 until arr.length()) {
                permissions.add(parsePermission(arr.getJSONObject(i)))
            }
            permissions
        } catch (e: Exception) {
            recordError("Backend query", e)
            getMockPermissionsForParent(parentId)
        }
    }

    suspend fun getAllPermissions(): List<MockPermission> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockPermissions.toList()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "permissions",
                query = "select=*",
                authToken = authToken()
            ) ?: return@withContext mockPermissions.toList()

            val arr = JSONArray(result)
            val permissions = mutableListOf<MockPermission>()
            for (i in 0 until arr.length()) {
                permissions.add(parsePermission(arr.getJSONObject(i)))
            }
            permissions
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockPermissions.toList()
        }
    }

    // ============================================
    // MESSAGES
    // ============================================

    suspend fun getUserMessages(userId: String): List<MockMessage> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext getMockMessagesForUser(userId)

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "messages",
                query = "select=*",
                authToken = authToken()
            ) ?: return@withContext getMockMessagesForUser(userId)

            val arr = JSONArray(result)
            val messages = mutableListOf<MockMessage>()
            for (i in 0 until arr.length()) {
                val msg = parseMessage(arr.getJSONObject(i))
                // RLS filters, but also filter client-side for 'ALL'
                if (msg.senderId == userId || msg.recipientId == userId || msg.recipientId == "ALL") {
                    messages.add(msg)
                }
            }
            messages
        } catch (e: Exception) {
            recordError("Backend query", e)
            getMockMessagesForUser(userId)
        }
    }

    // ============================================
    // NOTIFICATIONS
    // ============================================

    suspend fun getUserNotifications(userId: String): List<MockNotification> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockNotifications.toList()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "notifications",
                query = "user_id=eq.$userId&select=*",
                authToken = authToken()
            ) ?: return@withContext mockNotifications.toList()

            val arr = JSONArray(result)
            val notifications = mutableListOf<MockNotification>()
            for (i in 0 until arr.length()) {
                notifications.add(parseNotification(arr.getJSONObject(i)))
            }
            notifications
        } catch (e: Exception) {
            recordError("Backend query", e)
            mockNotifications.toList()
        }
    }

    // ============================================
    // NOTIFICATION PREFERENCES
    // ============================================

    /**
     * Load the signed-in user's notification preferences from the DB.
     * Returns defaults if no row exists yet.
     */
    suspend fun getNotificationPreferences(userId: String): NotificationPreference = withContext(Dispatchers.IO) {
        if (!isUsingBackend() || userId.isBlank()) return@withContext NotificationPreference()

        try {
            val result = SupabaseConfig.supabaseGet(
                table = "notification_preferences",
                query = "user_id=eq.$userId&select=*",
                authToken = authToken()
            ) ?: return@withContext NotificationPreference()

            val arr = JSONArray(result)
            if (arr.length() == 0) return@withContext NotificationPreference()
            val obj = arr.getJSONObject(0)
            NotificationPreference(
                paymentNotifications = obj.optBoolean("payment_notifications", true),
                documentNotifications = obj.optBoolean("document_notifications", true),
                applicationNotifications = obj.optBoolean("application_notifications", true),
                permissionNotifications = obj.optBoolean("permission_notifications", true),
                messageNotifications = obj.optBoolean("message_notifications", true),
                reminderNotifications = obj.optBoolean("reminder_notifications", true),
                announcementNotifications = obj.optBoolean("announcement_notifications", true)
            )
        } catch (e: Exception) {
            recordError("Backend query", e)
            NotificationPreference()
        }
    }

    /**
     * Persist preferences for the user. Updates the existing row, or inserts one
     * on first save (upsert pattern since PATCH on a missing row is a no-op).
     */
    suspend fun saveNotificationPreferences(userId: String, prefs: NotificationPreference): Boolean = withContext(Dispatchers.IO) {
        if (!isUsingBackend() || userId.isBlank()) return@withContext false

        try {
            val body = JSONObject().apply {
                put("payment_notifications", prefs.paymentNotifications)
                put("document_notifications", prefs.documentNotifications)
                put("application_notifications", prefs.applicationNotifications)
                put("permission_notifications", prefs.permissionNotifications)
                put("message_notifications", prefs.messageNotifications)
                put("reminder_notifications", prefs.reminderNotifications)
                put("announcement_notifications", prefs.announcementNotifications)
            }
            val updated = SupabaseConfig.supabasePatch(
                table = "notification_preferences",
                query = "user_id=eq.$userId",
                body = body.toString(),
                authToken = authToken()
            )
            if (updated != null && JSONArray(updated).length() > 0) {
                true
            } else {
                // No row yet — insert it (RLS restricts to own user_id)
                body.put("user_id", userId)
                val inserted = SupabaseConfig.supabasePost(
                    table = "notification_preferences",
                    body = body.toString(),
                    authToken = authToken()
                )
                inserted != null
            }
        } catch (e: Exception) {
            recordError("Backend query", e)
            false
        }
    }

    suspend fun sendMessage(message: MockMessage): Boolean = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext false

        try {
            val body = JSONObject().apply {
                put("thread_id", message.threadId)
                put("sender_id", message.senderId)
                put("sender_name", message.senderName)
                put("recipient_id", message.recipientId)
                put("recipient_name", message.recipientName)
                put("subject", message.subject)
                put("content", message.content)
                put("category", when (message.category) {
                    MessageCategory.APPLICATION -> "application"
                    MessageCategory.FINANCE -> "finance"
                    MessageCategory.PERMISSION -> "permission"
                    MessageCategory.ANNOUNCEMENT -> "announcement"
                    else -> "general"
                })
                put("is_read", message.isRead)
                if (message.parentMessageId != null) put("parent_message_id", message.parentMessageId)
            }
            val result = SupabaseConfig.supabasePost(
                table = "messages",
                body = body.toString(),
                authToken = authToken()
            )
            result != null
        } catch (e: Exception) {
            recordError("Backend query", e)
            false
        }
    }

    suspend fun getAllParents(): List<MockUser> = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext mockUsers.filter { it.role == UserRole.PARENT }

        try {
            // Only count parents who have at least one approved application or active student
            val result = SupabaseConfig.supabaseGet(
                table = "profiles",
                query = "role=eq.parent&select=id,full_name,email,phone,surname,id_number,employer,work_phone,created_at,updated_at&order=created_at.desc",
                authToken = authToken()
            ) ?: return@withContext emptyList()

            val arr = JSONArray(result)
            val parents = mutableListOf<MockUser>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val parentId = obj.optString("id")
                
                // Check if this parent has any approved applications
                val approvedApps = try {
                    val appsResult = SupabaseConfig.supabaseGet(
                        table = "applications",
                        query = "parent_id=eq.$parentId&status=eq.approved&select=id",
                        authToken = authToken()
                    )
                    if (appsResult != null && appsResult != "[]") {
                        JSONArray(appsResult).length() > 0
                    } else false
                } catch (e: Exception) { false }
                
                // Check if this parent has any active students
                val activeStudents = if (!approvedApps) {
                    try {
                        val studentsResult = SupabaseConfig.supabaseGet(
                            table = "students",
                            query = "parent_id=eq.$parentId&status=eq.active&select=id",
                            authToken = authToken()
                        )
                        if (studentsResult != null && studentsResult != "[]") {
                            JSONArray(studentsResult).length() > 0
                        } else false
                    } catch (e: Exception) { false }
                } else true
                
                // Only include parents with approved applications or active students
                if (approvedApps || activeStudents) {
                    parents.add(
                        MockUser(
                            id = parentId,
                            fullName = obj.optString("full_name", ""),
                            email = obj.optString("email", ""),
                            phone = obj.optString("phone", ""),
                            password = "",
                            role = UserRole.PARENT,
                            surname = obj.optString("surname", "")
                        )
                    )
                }
            }
            parents
        } catch (e: Exception) {
            recordError("Backend query", e)
            emptyList()
        }
    }

    suspend fun createPermission(permission: MockPermission): Boolean = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext false

        try {
            val body = JSONObject().apply {
                put("title", permission.title)
                put("description", permission.description)
                put("student_id", permission.studentId)
                put("parent_id", permission.parentId)
                put("created_by", permission.createdBy)
                put("category", when (permission.category) {
                    PermissionCategory.EXCURSION -> "excursion"
                    PermissionCategory.MEDICAL -> "medical"
                    PermissionCategory.PHOTO -> "photo"
                    PermissionCategory.SPORTS -> "sports"
                    else -> "general"
                })
                put("status", "pending")
                if (!permission.dueDate.isNullOrBlank()) put("due_date", permission.dueDate)
            }
            val result = SupabaseConfig.supabasePost(
                table = "permissions",
                body = body.toString(),
                authToken = authToken()
            )
            result != null
        } catch (e: Exception) {
            recordError("Backend query", e)
            false
        }
    }

    suspend fun respondToPermission(permissionId: String, granted: Boolean, notes: String?): Boolean = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext false

        try {
            val body = JSONObject().apply {
                put("status", "responded")
                put("response", if (granted) "granted" else "declined")
                put("response_notes", notes ?: "")
            }
            val result = SupabaseConfig.supabasePatch(
                table = "permissions",
                query = "id=eq.$permissionId",
                body = body.toString(),
                authToken = authToken()
            )
            result != null
        } catch (e: Exception) {
            recordError("Backend query", e)
            false
        }
    }

    // ============================================
    // CREATE APPLICATION
    // ============================================

    /**
     * Persist a registration as a real application row.
     * Returns the created [MockApplication] (with its DB id + status) on success,
     * or null on failure. When the backend is not configured it returns a local
     * mock record so the flow can continue in demo mode.
     */
    suspend fun createApplication(app: JSONObject, parentId: String): MockApplication? = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) {
            val mock = parseApplication(app).copy(
                id = "APP-${(1000..9999).random()}",
                parentId = parentId,
                status = ApplicationStatus.SUBMITTED
            )
            mockApplications.add(mock)
            return@withContext mock
        }

        try {
            val result = SupabaseConfig.supabasePost(
                table = "applications",
                body = app.toString(),
                authToken = authToken()
            ) ?: return@withContext null
            val arr = JSONArray(result)
            if (arr.length() > 0) {
                parseApplication(arr.getJSONObject(0))
            } else {
                null
            }
        } catch (e: Exception) {
            recordError("Backend query", e)
            null
        }
    }

    // ============================================
    // PROOF OF PAYMENT UPLOAD + APPLICATION STATUS
    // ============================================

    /**
     * Upload a proof-of-payment file to the 'proof-of-payment' storage bucket.
     * Path is scoped to the parent's auth uid so RLS keeps it private.
     * @return the stored object path (e.g. "proof-of-payment/<uid>/<appId>.png") or null on failure.
     */
    suspend fun uploadProofOfPayment(
        parentId: String,
        applicationId: String,
        bytes: ByteArray,
        contentType: String
    ): String? = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext null
        try {
            SupabaseConfig.supabaseStorageUpload(
                bucket = "proof-of-payment",
                path = "$parentId/$applicationId.${contentTypeToExt(contentType)}",
                bytes = bytes,
                contentType = contentType,
                authToken = authToken()
            )
        } catch (e: Exception) {
            recordError("POP upload", e)
            null
        }
    }

    /**
     * Persist an admin decision (status change) on an application, optionally
     * recording the verified proof-of-payment path.
     */
    suspend fun updateApplicationStatus(
        applicationId: String,
        status: String,
        paymentProofUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext false
        try {
            val body = JSONObject().apply {
                put("status", status)
                if (status == "payment_verified" || status == "approved") {
                    put("payment_verified_at", "now()")
                }
                if (status == "approved") {
                    put("approved_at", "now()")
                }
                if (status == "rejected") {
                    put("rejected_at", "now()")
                }
                if (paymentProofUrl != null) {
                    put("payment_proof_url", paymentProofUrl)
                }
            }
            val result = SupabaseConfig.supabasePatch(
                table = "applications",
                query = "id=eq.$applicationId",
                body = body.toString(),
                authToken = authToken()
            )
            result != null
        } catch (e: Exception) {
            recordError("Update application status", e)
            false
        }
    }

    // ============================================
    // ============================================
    // STUDENT CREATION FROM APPLICATION
    // ============================================

    /**
     * Create a student row from an approved application.
     * Called when admin approves an application.
     */
    suspend fun createStudentFromApplication(app: MockApplication): Boolean = withContext(Dispatchers.IO) {
        if (!isUsingBackend()) return@withContext false
        try {
            val body = JSONObject().apply {
                put("parent_id", app.parentId)
                put("first_name", app.studentFirstName)
                put("last_name", app.studentLastName)
                put("date_of_birth", app.studentDOB)
                put("grade", app.studentGrade)
                put("school", app.studentSchool)
                put("address", app.studentAddress)
                put("gender", app.studentGender)
                put("class_number", app.studentClassNumber)
                put("teacher_name", app.studentTeacherName)
                put("lsen", app.studentLsen)
                put("status", "active")
            }
            val result = SupabaseConfig.supabasePost(
                table = "students",
                body = body.toString(),
                authToken = authToken()
            )
            result != null
        } catch (e: Exception) {
            recordError("Create student from application", e)
            false
        }
    }
    // MOCK DATA HELPERS
    // ============================================

    private fun getMockStudentsForParent(parentId: String): List<MockStudent> {
        return mockStudents.filter { it.parentId == parentId }
    }

    private fun getMockApplicationsForParent(parentId: String): List<MockApplication> {
        return mockApplications.filter { it.parentId == parentId }
    }

    private fun getMockInvoicesForParent(parentId: String): List<MockInvoice> {
        val studentIds = mockStudents.filter { it.parentId == parentId }.map { it.id }
        return mockInvoices.filter { it.studentId in studentIds }
    }

    private fun getMockPermissionsForParent(parentId: String): List<MockPermission> {
        return mockPermissions.filter { it.parentId == parentId }
    }

    private fun getMockMessagesForUser(userId: String): List<MockMessage> {
        return mockMessages.filter {
            it.senderId == userId || it.recipientId == userId || it.recipientId == "ALL"
        }
    }

    /** Map a MIME type to a short file extension for storage object naming. */
    private fun contentTypeToExt(contentType: String): String {
        return when {
            contentType.contains("png") -> "png"
            contentType.contains("jpeg") || contentType.contains("jpg") -> "jpg"
            contentType.contains("pdf") -> "pdf"
            contentType.contains("webp") -> "webp"
            else -> "bin"
        }
    }

    // ============================================
    // JSON PARSERS
    // ============================================

    private fun parseStudent(obj: JSONObject): MockStudent {
        return MockStudent(
            id = obj.optString("id"),
            firstName = obj.optString("first_name"),
            lastName = obj.optString("last_name"),
            dateOfBirth = obj.optString("date_of_birth", ""),
            grade = obj.optInt("grade", 0),
            school = obj.optString("school", ""),
            address = obj.optString("address", ""),
            parentId = obj.optString("parent_id"),
            status = when (obj.optString("status")) {
                "active" -> StudentStatus.ACTIVE
                "pending" -> StudentStatus.PENDING
                "inactive" -> StudentStatus.INACTIVE
                else -> StudentStatus.PENDING
            },
            gender = obj.optString("gender", ""),
            classNr = obj.optString("class_number", ""),
            teacherName = obj.optString("teacher_name", ""),
            lsen = obj.optBoolean("lsen", false)
        )
    }

    private fun parseApplication(obj: JSONObject): MockApplication {
        return MockApplication(
            id = obj.optString("id"),
            parentId = obj.optString("parent_id"),
            parentName = obj.optString("parent_name", ""),
            childFirstName = obj.optString("student_first_name", ""),
            childLastName = obj.optString("student_last_name", ""),
            grade = obj.optInt("student_grade", 0),
            school = obj.optString("student_school", ""),
            submittedDate = obj.optString("submitted_at", ""),
            status = when (obj.optString("status")) {
                "submitted" -> ApplicationStatus.SUBMITTED
                "under_review" -> ApplicationStatus.UNDER_REVIEW
                "changes_required" -> ApplicationStatus.CHANGES_REQUIRED
                "payment_verified" -> ApplicationStatus.PAYMENT_VERIFIED
                "approved" -> ApplicationStatus.APPROVED
                "rejected" -> ApplicationStatus.REJECTED
                else -> ApplicationStatus.SUBMITTED
            },
            lastUpdated = obj.optString("updated_at", ""),
            notes = obj.optString("admin_notes", ""),
            registrationFeePaid = obj.optBoolean("payment_amount", false),
            documentsUploaded = obj.optBoolean("documents_uploaded", false),
            studentDOB = obj.optString("student_dob", ""),
            studentAddress = obj.optString("student_address", ""),
            studentGender = obj.optString("student_gender", ""),
            studentClassNumber = obj.optString("student_class_number", ""),
            studentTeacherName = obj.optString("student_teacher_name", ""),
            studentLsen = obj.optString("student_lsen", ""),
            paymentProofUrl = obj.optString("payment_proof_url").ifBlank { null }
        )
    }

    private fun parseInvoice(obj: JSONObject): MockInvoice {
        return MockInvoice(
            id = obj.optString("id"),
            studentId = obj.optString("student_id"),
            description = obj.optString("description"),
            amount = obj.optDouble("amount", 0.0),
            dueDate = obj.optString("due_date", ""),
            status = when (obj.optString("status")) {
                "paid" -> InvoiceStatus.PAID
                "pending" -> InvoiceStatus.PENDING
                "overdue" -> InvoiceStatus.OVERDUE
                else -> InvoiceStatus.PENDING
            },
            paidDate = obj.optString("paid_date", ""),
            category = when (obj.optString("category")) {
                "aftercare" -> InvoiceCategory.AFTERCARE
                "transport" -> InvoiceCategory.TRANSPORT
                "stationery" -> InvoiceCategory.STATIONERY
                "registration" -> InvoiceCategory.REGISTRATION
                else -> InvoiceCategory.AFTERCARE
            }
        )
    }

    private fun parsePermission(obj: JSONObject): MockPermission {        return MockPermission(
            id = obj.optString("id"),
            title = obj.optString("title"),
            description = obj.optString("description", ""),
            category = when (obj.optString("category")) {
                "excursion" -> PermissionCategory.EXCURSION
                "medical" -> PermissionCategory.MEDICAL
                "photo" -> PermissionCategory.PHOTO
                "sports" -> PermissionCategory.SPORTS
                else -> PermissionCategory.OTHER
            },
            createdBy = obj.optString("created_by"),
            createdDate = obj.optString("created_at", ""),
            expiryDate = obj.optString("expiry_date", ""),
            status = when (obj.optString("status")) {
                "pending" -> PermissionStatus.PENDING
                "approved" -> PermissionStatus.APPROVED
                "declined" -> PermissionStatus.DECLINED
                "expired" -> PermissionStatus.EXPIRED
                else -> PermissionStatus.PENDING
            },
            parentId = obj.optString("parent_id"),
            parentName = obj.optString("parent_name", ""),
            response = when (obj.optString("response", "")) {
                "granted" -> PermissionResponse.GRANTED
                "declined" -> PermissionResponse.DECLINED
                else -> null
            },
            responseDate = obj.optString("response_date", ""),
            notes = obj.optString("notes", "")
        )
    }

    private fun parseMessage(obj: JSONObject): MockMessage {
        return MockMessage(
            id = obj.optString("id"),
            threadId = obj.optString("thread_id", obj.optString("id")),
            senderId = obj.optString("sender_id"),
            senderName = obj.optString("sender_name", ""),
            recipientId = obj.optString("recipient_id"),
            recipientName = if (obj.optString("recipient_id") == "ALL") "All Parents" else obj.optString("recipient_name", ""),
            subject = obj.optString("subject"),
            content = obj.optString("content"),
            timestamp = obj.optString("created_at", ""),
            isRead = obj.optBoolean("is_read", false),
            category = when (obj.optString("category")) {
                "application" -> MessageCategory.APPLICATION
                "finance" -> MessageCategory.FINANCE
                "permission" -> MessageCategory.PERMISSION
                "announcement" -> MessageCategory.ANNOUNCEMENT
                else -> MessageCategory.GENERAL
            },
            parentMessageId = obj.optString("parent_message_id", "")
        )
    }

    private fun parseNotification(obj: JSONObject): MockNotification {
        return MockNotification(
            id = obj.optString("id"),
            userId = obj.optString("user_id"),
            title = obj.optString("title"),
            message = obj.optString("message"),
            timestamp = obj.optString("created_at", ""),
            isRead = obj.optBoolean("is_read", false),
            type = when (obj.optString("type")) {
                "payment" -> NotificationType.PAYMENT
                "document" -> NotificationType.DOCUMENT
                "application" -> NotificationType.APPLICATION
                "permission" -> NotificationType.PERMISSION
                "message" -> NotificationType.MESSAGE
                "reminder" -> NotificationType.REMINDER
                else -> NotificationType.GENERAL
            },
            relatedId = obj.optString("related_id", "")
        )
    }
}

/**
 * Build the JSON body for an applications insert from a [RegistrationDraft],
 * mapping the collected UI fields onto the real database columns.
 * `child_first_name`/`child_last_name` are included so the app's status screen
 * (which reads those columns) displays the name; the DB trigger keeps them in
 * sync with `student_first_name`/`student_last_name`.
 */
fun RegistrationDraft.toApplicationJson(parentId: String): JSONObject {
    val parts = studentName.trim().split(Regex("\\s+"), limit = 2)
    val first = InputSanitizer.sanitizeName(parts.firstOrNull() ?: "")
    val last = InputSanitizer.sanitizeName(parts.getOrNull(1) ?: "")
    return JSONObject().apply {
        put("parent_id", parentId)  // ← CRITICAL: RLS needs this
        put("student_first_name", first)
        put("student_last_name", last)
        put("child_first_name", first)
        put("child_last_name", last)
        put("student_grade", InputSanitizer.sanitizeGrade(grade) ?: grade)
        put("student_dob", InputSanitizer.sanitizeText(dob))
        put("student_school", InputSanitizer.sanitizeText(school))
        put("student_address", InputSanitizer.sanitizeText(address))
        put("student_gender", gender)
        put("student_class_number", InputSanitizer.sanitizeText(classNr))
        put("student_teacher_name", InputSanitizer.sanitizeName(teacherName))
        put("student_lsen", lsen)
        put("sports", sports.joinToString(","))
        put("collection_person_1", InputSanitizer.sanitizeName(collectionPerson1))
        put("collection_contact_1", InputSanitizer.sanitizePhone(contact1))
        put("collection_vehicle_1", InputSanitizer.sanitizeVehicleReg(vehicleReg1))
        put("collection_person_2", InputSanitizer.sanitizeName(collectionPerson2))
        put("collection_contact_2", InputSanitizer.sanitizePhone(contact2))
        put("collection_vehicle_2", InputSanitizer.sanitizeVehicleReg(vehicleReg2))
        put("transport_required", transportRequired)
        put("doctor_name", InputSanitizer.sanitizeName(doctorName))
        put("doctor_location", InputSanitizer.sanitizeText(doctorLocation))
        put("doctor_contact", InputSanitizer.sanitizePhone(doctorContact))
        put("medical_plan", InputSanitizer.sanitizeText(medicalPlan))
        put("medical_aid_number", InputSanitizer.sanitizeText(medicalAidNumber))
        put("allergies", InputSanitizer.sanitizeText(allergies))
        put("has_allergies", hasAllergies)
        put("epilepsy", epilepsy)
        put("diabetic", diabetic)
        put("asthma", asthma)
        put("nose_bleeder", noseBleeder)
        put("parent_mother_name", InputSanitizer.sanitizeName(motherName))
        put("parent_mother_surname", InputSanitizer.sanitizeName(motherSurname))
        put("parent_mother_id", InputSanitizer.sanitizeIdNumber(motherId))
        put("parent_mother_employer", InputSanitizer.sanitizeText(motherEmployer))
        put("parent_mother_work_phone", InputSanitizer.sanitizePhone(motherWorkPhone))
        put("parent_mother_cell", InputSanitizer.sanitizePhone(motherCell))
        put("parent_mother_email", InputSanitizer.sanitizeEmail(motherEmail))
        put("parent_father_name", InputSanitizer.sanitizeName(fatherName))
        put("parent_father_surname", InputSanitizer.sanitizeName(fatherSurname))
        put("parent_father_id", InputSanitizer.sanitizeIdNumber(fatherId))
        put("parent_father_employer", InputSanitizer.sanitizeText(fatherEmployer))
        put("parent_father_work_phone", InputSanitizer.sanitizePhone(fatherWorkPhone))
        put("parent_father_cell", InputSanitizer.sanitizePhone(fatherCell))
        put("parent_father_email", InputSanitizer.sanitizeEmail(fatherEmail))
        put("photo_consent", photoConsent)
        put("signature_data", InputSanitizer.sanitizeName(parentSignature))
        put("payment_method", paymentMethod)
        put("payment_amount", 450.00)
        put("status", "submitted")
    }
}
