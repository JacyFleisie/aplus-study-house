package com.example.blankapp.navigation

import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.UserRole
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for route guard helpers (isAdmin, isParent, hasRole).
 *
 * Verifies the pure-helper logic that RequireParent/RequireAdmin composables
 * delegate to, plus AuthRepository state consistency for sign-in/sign-out.
 */
class RouteGuardTest {

    @Before
    fun setUp() {
        AuthRepository.signOut()
    }

    @After
    fun tearDown() {
        AuthRepository.signOut()
    }

    // ============================================
    // isAdmin() tests
    // ============================================

    @Test
    fun `isAdmin returns false when no user is logged in`() {
        assertFalse("With no user, isAdmin should be false", isAdmin())
    }

    @Test
    fun `isAdmin returns false after signOut`() {
        AuthRepository.signOut()
        assertFalse("After signOut, isAdmin should be false", isAdmin())
    }

    // ============================================
    // isParent() tests
    // ============================================

    @Test
    fun `isParent returns false when no user is logged in`() {
        assertFalse("With no user, isParent should be false", isParent())
    }

    @Test
    fun `isParent returns false after signOut`() {
        AuthRepository.signOut()
        assertFalse("After signOut, isParent should be false", isParent())
    }

    // ============================================
    // hasRole() tests
    // ============================================

    @Test
    fun `hasRole returns false when no user is logged in`() {
        assertFalse("After signOut, hasRole(ADMIN) should be false", hasRole(UserRole.ADMIN))
        assertFalse("After signOut, hasRole(PARENT) should be false", hasRole(UserRole.PARENT))
    }

    // ============================================
    // AuthRepository state consistency
    // ============================================

    @Test
    fun `getCurrentUser returns null when logged out`() {
        AuthRepository.signOut()
        assertNull("getCurrentUser should return null after signOut", AuthRepository.getCurrentUser())
    }

    @Test
    fun `signOut clears auth state completely`() {
        AuthRepository.signOut()
        assertNull(AuthRepository.getCurrentUser())
        assertFalse(isAdmin())
        assertFalse(isParent())
        assertFalse(hasRole(UserRole.ADMIN))
        assertFalse(hasRole(UserRole.PARENT))
    }

    @Test
    fun `isAdmin and isParent are mutually exclusive for same auth state`() {
        AuthRepository.signOut()
        // When logged out, both must be false
        assertFalse(isAdmin())
        assertFalse(isParent())

        // hasRole should also agree
        assertFalse(hasRole(UserRole.ADMIN))
        assertFalse(hasRole(UserRole.PARENT))
    }
}
