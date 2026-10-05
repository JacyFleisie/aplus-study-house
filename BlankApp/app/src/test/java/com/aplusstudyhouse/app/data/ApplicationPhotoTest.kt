package com.aplusstudyhouse.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A child's photo is mandatory, so the submit payload must carry the storage key
 * of the uploaded photo; the key is copied to students.photo_path when an admin
 * approves the application (see SupabaseRepository.createStudentFromApplication).
 */
class ApplicationPhotoTest {
    private val draft =
        RegistrationDraft(
            studentName = "Thandi Mokoena",
            grade = 3,
            school = "Riverside Primary",
            gender = "Female"
        )

    @Test
    fun `submit payload includes the stored photo key`() {
        val json = draft.toApplicationJson("parent-1", "parent-1/registration-17.jpg")
        assertEquals("parent-1/registration-17.jpg", json.optString("student_photo_path"))
    }

    @Test
    fun `submit payload omits the photo key when none was uploaded`() {
        val json = draft.toApplicationJson("parent-1")
        assertFalse(json.has("student_photo_path"))
    }

    @Test
    fun `submit payload omits a blank photo key`() {
        val json = draft.toApplicationJson("parent-1", "   ")
        assertFalse(json.has("student_photo_path"))
    }

    @Test
    fun `submit payload still carries the parent for RLS`() {
        val json = draft.toApplicationJson("parent-1", "parent-1/registration-17.jpg")
        assertTrue(json.optString("parent_id").isNotBlank())
    }
}
