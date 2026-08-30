package com.example.blankapp.data

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Regression tests for the registration -> submission flow.
 *
 * These lock in the critical behaviour that was previously broken:
 *  - a submitted application is actually PERSISTED (not silently dropped with a fake ID)
 *  - a submitted application belongs to the submitting parent and is isolated from other families
 *
 * Run in mock mode (no Supabase configured) so they need no network.
 */
class ApplicationSubmitTest {

    @Before
    fun setup() {
        // Force mock mode so tests run deterministically regardless of supabase.properties.
        SupabaseConfig.forceMockMode = true
        AuthRepository.signOut()
        // start clean so counts are deterministic
        mockApplications.clear()
    }

    private fun sampleApplicationJson(): JSONObject = JSONObject().apply {
        put("student_first_name", "TestChild")
        put("student_last_name", "Submit")
        put("grade", "Grade 7")
        put("school", "Test Primary")
    }

    @Test
    fun `createApplication persists a submitted application for the parent`() = kotlinx.coroutines.runBlocking {
        val before = SupabaseRepository.getParentApplications("P001").size
        val created = SupabaseRepository.createApplication(sampleApplicationJson(), "P001")
        assertNotNull("createApplication should return the persisted application", created)
        assertEquals("Parent should be P001", "P001", created?.parentId)
        assertEquals("Status should be SUBMITTED", ApplicationStatus.SUBMITTED, created?.status)
        val after = SupabaseRepository.getParentApplications("P001").size
        assertEquals("P001 application count should increase by 1", before + 1, after)
    }

    @Test
    fun `submitted application is isolated from other parents`() = kotlinx.coroutines.runBlocking {
        val created = SupabaseRepository.createApplication(sampleApplicationJson(), "P001")
        assertNotNull(created)
        val p001Apps = SupabaseRepository.getParentApplications("P001")
        val p002Apps = SupabaseRepository.getParentApplications("P002")
        assertTrue("P001 should now see the application", p001Apps.any { it.id == created?.id })
        assertTrue("P002 must NOT see P001's application", p002Apps.none { it.id == created?.id })
    }

    @Test
    fun `parent cannot read another family's application by id`() = kotlinx.coroutines.runBlocking {
        val created = SupabaseRepository.createApplication(sampleApplicationJson(), "P001")
        assertNotNull(created)
        // A different parent's query must never include the submitted application.
        val otherParentApps = SupabaseRepository.getParentApplications("P999")
        assertTrue(
            "An unrelated parent must not see P001's submitted application",
            otherParentApps.none { it.id == created?.id }
        )
    }

    @Test
    fun `getAllApplications includes the newly submitted application`() = kotlinx.coroutines.runBlocking {
        val created = SupabaseRepository.createApplication(sampleApplicationJson(), "P001")
        assertNotNull(created)
        val all = SupabaseRepository.getAllApplications()
        assertTrue("Global application list should contain the new submission", all.any { it.id == created?.id })
    }
}
