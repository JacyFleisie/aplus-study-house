package com.aplusstudyhouse.app.utils

import com.aplusstudyhouse.app.data.ApplicationStatus
import com.aplusstudyhouse.app.data.MockApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The photo sweep must delete only objects nobody can ever display again:
 * a rejected application's photo that no student row points at.
 */
class PhotoCleanupTest {
    private fun application(
        id: String,
        status: ApplicationStatus,
        photoPath: String
    ) = MockApplication(
        id = id,
        parentId = "parent-1",
        parentName = "Parent",
        childFirstName = "Child",
        childLastName = id,
        grade = 1,
        school = "School",
        submittedDate = "2026-01-01",
        status = status,
        studentPhotoPath = photoPath
    )

    @Test
    fun `removes the photo of a rejected application`() {
        val orphans =
            PhotoCleanup.orphanPhotoPaths(
                listOf(application("a1", ApplicationStatus.REJECTED, "parent-1/registration-1.jpg")),
                emptySet()
            )
        assertEquals(listOf("parent-1/registration-1.jpg"), orphans)
    }

    @Test
    fun `keeps photos of applications still under review`() {
        val apps =
            listOf(
                application("a1", ApplicationStatus.SUBMITTED, "parent-1/registration-1.jpg"),
                application("a2", ApplicationStatus.UNDER_REVIEW, "parent-1/registration-2.jpg"),
                application("a3", ApplicationStatus.CHANGES_REQUIRED, "parent-1/registration-3.jpg"),
                application("a4", ApplicationStatus.APPROVED, "parent-1/registration-4.jpg"),
                application("a5", ApplicationStatus.PAYMENT_VERIFIED, "parent-1/registration-5.jpg")
            )
        assertEquals(emptyList<String>(), PhotoCleanup.orphanPhotoPaths(apps, emptySet()))
    }

    @Test
    fun `never deletes a photo a student still uses`() {
        // attachApplicationPhoto falls back to the application key when the copy
        // to the student's own key fails, so the same path can be both.
        val apps = listOf(application("a1", ApplicationStatus.REJECTED, "parent-1/registration-1.jpg"))
        val orphans = PhotoCleanup.orphanPhotoPaths(apps, setOf("parent-1/registration-1.jpg"))
        assertEquals(emptyList<String>(), orphans)
    }

    @Test
    fun `ignores applications without a photo`() {
        val apps =
            listOf(
                application("a1", ApplicationStatus.REJECTED, ""),
                application("a2", ApplicationStatus.REJECTED, "   ")
            )
        assertEquals(emptyList<String>(), PhotoCleanup.orphanPhotoPaths(apps, emptySet()))
    }

    @Test
    fun `handles the same photo referenced twice`() {
        val apps =
            listOf(
                application("a1", ApplicationStatus.REJECTED, "parent-1/registration-1.jpg"),
                application("a2", ApplicationStatus.REJECTED, "parent-1/registration-1.jpg")
            )
        assertEquals(1, PhotoCleanup.orphanPhotoPaths(apps, emptySet()).size)
    }

    @Test
    fun `sweep runs at most every six hours`() {
        val now = 1_000_000_000_000L
        assertTrue("first run is always due", PhotoCleanup.isDue(0L, now))
        assertFalse("just ran", PhotoCleanup.isDue(now, now))
        assertFalse("ran an hour ago", PhotoCleanup.isDue(now - 60 * 60 * 1000L, now))
        assertTrue("ran six hours ago", PhotoCleanup.isDue(now - PhotoCleanup.INTERVAL_MS, now))
    }
}
