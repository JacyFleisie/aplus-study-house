package com.aplusstudyhouse.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeConfigTest {
    @Test
    fun `fee config exposes safe school defaults`() {
        val fees = FeeConfig()

        assertEquals(500.0, fees.registrationFee, 0.001)
        assertEquals(1700.0, fees.monthlyFeeFirstChild, 0.001)
        assertEquals(1650.0, fees.monthlyFeeSibling, 0.001)
        assertEquals(380.0, fees.projectFeeGrade6, 0.001)
        assertEquals("Q3", fees.projectFeeQuarter)
    }

    @Test
    fun `registration payload uses supplied configurable fee`() {
        val draft = RegistrationDraft(studentName = "Ada Lovelace", grade = 6)

        val payload = draft.toApplicationJson("parent-1", registrationFee = 725.0)

        assertEquals("Ada", payload.getString("student_first_name"))
        assertEquals("Lovelace", payload.getString("student_last_name"))
        assertEquals(725.0, payload.getDouble("payment_amount"), 0.001)
        assertEquals("submitted", payload.getString("status"))
    }

    @Test
    fun `registration payload excludes blank photo path`() {
        val draft = RegistrationDraft(studentName = "Ada Lovelace", grade = 6)

        val payload = draft.toApplicationJson("parent-1")

        assertTrue(!payload.has("student_photo_path"))
    }
}
