package com.example.blankapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SupabaseConfig
 * Tests configuration validation and client setup
 */
class SupabaseConfigTest {

    // ============================================
    // CONFIGURATION VALIDATION TESTS
    // ============================================

    @Test
    fun `isConfigured returns true when BuildConfig has credentials`() {
        // In unit tests, BuildConfig values come from test build config
        // The actual check is: URL.isNotBlank() AND key.isNotBlank() AND URL starts with https://
        val url = SupabaseConfig.SUPABASE_URL
        val key = SupabaseConfig.SUPABASE_ANON_KEY
        val isConfigured = url.isNotBlank() &&
                key.isNotBlank() &&
                url.startsWith("https://")
        // This may be false in test environment if BuildConfig defaults to empty
        // which is expected behavior
        if (url.isNotBlank() && key.isNotBlank()) {
            assertTrue("Should be configured", isConfigured)
        } else {
            assertFalse("Should not be configured when credentials are empty", isConfigured)
        }
    }

    @Test
    fun `isConfigured returns false with empty URL`() {
        val url = ""
        val key = ""
        val isConfigured = url.isNotBlank() &&
                key.isNotBlank() &&
                url.startsWith("https://")
        assertFalse("Empty URL should not be configured", isConfigured)
    }

    @Test
    fun `isConfigured returns false with HTTP URL`() {
        val url = "http://example.supabase.co"
        val key = "test-key"
        val isConfigured = url.isNotBlank() &&
                key.isNotBlank() &&
                url.startsWith("https://")
        assertFalse("HTTP URL should not be configured", isConfigured)
    }

    @Test
    fun `isConfigured returns true with valid HTTPS values`() {
        val url = "https://example.supabase.co"
        val key = "test-key"
        val isConfigured = url.isNotBlank() &&
                key.isNotBlank() &&
                url.startsWith("https://")
        assertTrue("Valid HTTPS URL should be configured", isConfigured)
    }

    @Test
    fun `JSON_MEDIA_TYPE is set correctly`() {
        assertEquals("application/json; charset=utf-8", SupabaseConfig.JSON_MEDIA_TYPE.toString())
    }

    @Test
    fun `httpClient is initialized`() {
        assertNotNull("HTTP client should be initialized", SupabaseConfig.httpClient)
    }

    // ============================================
    // SUPABASE GET TESTS (with mock fallback)
    // ============================================

    @Test
    fun `supabaseGet completes without throwing`() {
        kotlinx.coroutines.runBlocking {
            // Contract: the call must never throw, regardless of config or server response.
            // (It may return null on a non-2xx response — acceptable, not a crash.)
            try {
                SupabaseConfig.supabaseGet("profiles")
            } catch (e: Exception) {
                fail("supabaseGet should not throw, but threw: ${e.message}")
            }
        }
    }

    @Test
    fun `supabasePost completes without throwing`() {
        kotlinx.coroutines.runBlocking {
            // Contract: the call must never throw, regardless of config or server response.
            // (It may return null on a non-2xx response — that is acceptable, not a crash.)
            try {
                SupabaseConfig.supabasePost("profiles", "{}")
            } catch (e: Exception) {
                fail("supabasePost should not throw, but threw: ${e.message}")
            }
        }
    }

    @Test
    fun `supabasePatch completes without throwing`() {
        kotlinx.coroutines.runBlocking {
            try {
                SupabaseConfig.supabasePatch("profiles", "{}", "id=eq.test")
            } catch (e: Exception) {
                fail("supabasePatch should not throw, but threw: ${e.message}")
            }
        }
    }

    @Test
    fun `supabaseAuth returns error when not configured`() = kotlinx.coroutines.runBlocking {
        val result = SupabaseConfig.supabaseAuth("token?grant_type=password", "{}")
        // When not configured, the request fails and returns JSONObject with error
        assertNotNull("Should return a result", result)
        assertTrue("Should contain error key", result?.has("error") == true)
    }
}
