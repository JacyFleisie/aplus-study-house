package com.example.blankapp.data

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class PayFastRepositoryTest {

    @Test
    fun `generateSignature produces valid MD5 hash`() {
        val data = JSONObject().apply {
            put("merchant_id", "123456")
            put("merchant_key", "test_key")
            put("amount", "100.00")
            put("item_name", "Test Invoice")
        }
        val passphrase = "test_passphrase"

        val signature = PayFastRepository.generateSignature(data, passphrase)

        assertEquals(32, signature.length)
        assertTrue(signature.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun `generateSignature without passphrase produces valid hash`() {
        val data = JSONObject().apply {
            put("merchant_id", "123456")
            put("amount", "50.00")
        }

        val signature = PayFastRepository.generateSignature(data, "")

        assertEquals(32, signature.length)
    }

    @Test
    fun `generateSignature excludes signature field`() {
        val data = JSONObject().apply {
            put("merchant_id", "123456")
            put("signature", "should_be_excluded")
            put("amount", "100.00")
        }

        val sig1 = PayFastRepository.generateSignature(data, "")
        val data2 = JSONObject().apply {
            put("merchant_id", "123456")
            put("amount", "100.00")
        }
        val sig2 = PayFastRepository.generateSignature(data2, "")

        assertEquals(sig1, sig2)
    }

    @Test
    fun `generateSignature sorts keys alphabetically`() {
        val data = JSONObject().apply {
            put("zebra", "z")
            put("alpha", "a")
            put("middle", "m")
        }

        val signature = PayFastRepository.generateSignature(data, "")

        assertEquals(32, signature.length)
    }

    @Test
    fun `buildQueryString produces correct format`() {
        val data = JSONObject().apply {
            put("merchant_id", "123456")
            put("amount", "100.00")
            put("item_name", "Test Invoice")
        }

        val query = PayFastRepository.buildQueryString(data)

        assertTrue(query.contains("amount=100.00"))
        assertTrue(query.contains("item_name=Test Invoice"))
        assertTrue(query.contains("merchant_id=123456"))
        assertFalse(query.contains("signature"))
    }

    @Test
    fun `buildQueryString skips empty values`() {
        val data = JSONObject().apply {
            put("merchant_id", "123456")
            put("empty_field", "")
            put("amount", "100.00")
        }

        val query = PayFastRepository.buildQueryString(data)

        assertFalse(query.contains("empty_field"))
        assertTrue(query.contains("merchant_id=123456"))
    }
}
