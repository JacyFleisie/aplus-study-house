package com.aplusstudyhouse.app.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM tests for the pure staleness logic of [UpdateCheckCache].
 * (The SharedPreferences-backed load/save need a device; they are
 * exercised on-device via the startup check.)
 */
class UpdateCheckCacheTest {
    private val sixHours = UpdateCheckCache.MAX_AGE_MS
    private val now = 1_000_000_000L

    @Test
    fun `zero timestamp is stale`() {
        assertTrue(UpdateCheckCache.isStale(0L, now))
    }

    @Test
    fun `negative timestamp is stale`() {
        assertTrue(UpdateCheckCache.isStale(-1L, now))
    }

    @Test
    fun `recent check is not stale`() {
        assertFalse(UpdateCheckCache.isStale(now - 60_000L, now))
    }

    @Test
    fun `check just under max age is not stale`() {
        assertFalse(UpdateCheckCache.isStale(now - sixHours + 1, now))
    }

    @Test
    fun `check at exactly max age is stale`() {
        assertTrue(UpdateCheckCache.isStale(now - sixHours, now))
    }

    @Test
    fun `check older than max age is stale`() {
        assertTrue(UpdateCheckCache.isStale(now - sixHours - 1, now))
    }

    @Test
    fun `future timestamp is not stale`() {
        assertFalse(UpdateCheckCache.isStale(now + 1, now))
    }
}
