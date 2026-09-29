package com.example.blankapp.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for AppUpdater.isNewer — the version comparison that decides
 * whether the self-updater shows "update available". A wrong result here
 * either nags users forever or silently withholds real updates.
 *
 * Regression context: every release up to v1.6.11 shipped
 * BuildConfig.VERSION_NAME = "1.6.7", so every installed build was told
 * an update was available forever. These tests pin the comparison logic.
 */
class AppUpdaterTest {
    @Test
    fun `older installed version sees an update`() {
        assertTrue(AppUpdater.isNewer("1.6.12", "1.6.7"))
        assertTrue(AppUpdater.isNewer("1.6.11", "1.6.7"))
    }

    @Test
    fun `same version is not an update`() {
        assertFalse(AppUpdater.isNewer("1.6.12", "1.6.12"))
    }

    @Test
    fun `newer installed version is not an update`() {
        assertFalse(AppUpdater.isNewer("1.6.11", "1.6.12"))
    }

    @Test
    fun `major version increase is an update`() {
        assertTrue(AppUpdater.isNewer("2.0.0", "1.9.9"))
    }

    @Test
    fun `different segment counts compare correctly`() {
        assertTrue(AppUpdater.isNewer("1.7", "1.6.12"))
        assertTrue(AppUpdater.isNewer("1.6.12", "1.6"))
        assertFalse(AppUpdater.isNewer("1.6", "1.6.12"))
    }

    @Test
    fun `patch bump within same minor is an update`() {
        assertTrue(AppUpdater.isNewer("1.6.13", "1.6.12"))
    }

    @Test
    fun `release tag prefix stripped input behaves like bare version`() {
        // AppUpdater strips 'v' before calling isNewer; verify plain values
        assertTrue(AppUpdater.isNewer("1.6.12", "1.6.7"))
        assertFalse(AppUpdater.isNewer("1.6.7", "1.6.12"))
    }
}
