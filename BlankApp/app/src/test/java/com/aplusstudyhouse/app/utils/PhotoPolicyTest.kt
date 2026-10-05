package com.aplusstudyhouse.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rules that decide whether a child's photo is accepted and where it is stored.
 */
class PhotoPolicyTest {
    // ---------- MIME types ----------

    @Test
    fun `accepts the image types the bucket allows`() {
        assertTrue(PhotoPolicy.isAllowedMimeType("image/jpeg"))
        assertTrue(PhotoPolicy.isAllowedMimeType("image/png"))
        assertTrue(PhotoPolicy.isAllowedMimeType("image/webp"))
    }

    @Test
    fun `accepts common provider variants of jpeg`() {
        assertTrue(PhotoPolicy.isAllowedMimeType("image/jpg"))
        assertTrue(PhotoPolicy.isAllowedMimeType("IMAGE/JPEG"))
        assertTrue(PhotoPolicy.isAllowedMimeType(" image/png "))
    }

    @Test
    fun `rejects non-images and unknown types`() {
        assertFalse(PhotoPolicy.isAllowedMimeType("application/pdf"))
        assertFalse(PhotoPolicy.isAllowedMimeType("video/mp4"))
        assertFalse(PhotoPolicy.isAllowedMimeType(null))
        assertFalse(PhotoPolicy.isAllowedMimeType(""))
    }

    // ---------- Source validation ----------

    @Test
    fun `rejects an empty selection`() {
        assertEquals(PhotoPolicy.ERR_EMPTY, PhotoPolicy.validateSource("image/jpeg", 0L))
    }

    @Test
    fun `rejects a file that is not an image`() {
        assertEquals(PhotoPolicy.ERR_TYPE, PhotoPolicy.validateSource("application/pdf", 1_048_576L))
    }

    @Test
    fun `rejects an absurdly large camera file before decoding`() {
        val tooBig = PhotoPolicy.MAX_SOURCE_BYTES + 1
        assertEquals(PhotoPolicy.ERR_TOO_LARGE, PhotoPolicy.validateSource("image/jpeg", tooBig))
    }

    @Test
    fun `accepts a normal photo`() {
        assertNull(PhotoPolicy.validateSource("image/jpeg", 512_000L))
    }

    // ---------- Upload validation ----------

    @Test
    fun `rejects an empty upload`() {
        assertEquals(PhotoPolicy.ERR_EMPTY, PhotoPolicy.validateUpload(0))
    }

    @Test
    fun `rejects an upload over the bucket limit`() {
        assertEquals(PhotoPolicy.ERR_UPLOAD_TOO_LARGE, PhotoPolicy.validateUpload(PhotoPolicy.MAX_UPLOAD_BYTES + 1))
    }

    @Test
    fun `accepts an upload at the bucket limit`() {
        assertNull(PhotoPolicy.validateUpload(PhotoPolicy.MAX_UPLOAD_BYTES))
    }

    // ---------- Storage keys ----------

    @Test
    fun `stores photos under the parent folder`() {
        assertEquals(
            "parent-1/student-2.jpg",
            PhotoPolicy.objectPath("parent-1", "student-2")
        )
    }

    @Test
    fun `strips path separators from key segments`() {
        assertEquals(
            "etcpasswd/etcshadow.jpg",
            PhotoPolicy.objectPath("../../etc/passwd", "../../etc/shadow")
        )
    }

    @Test
    fun `falls back to a safe segment when nothing usable remains`() {
        assertEquals("unknown/unknown.jpg", PhotoPolicy.objectPath("***", "..."))
    }

    // ---------- Initials fallback ----------

    @Test
    fun `builds initials for the avatar fallback`() {
        assertEquals("JS", PhotoPolicy.initials("Jane", "Smith"))
        assertEquals("J", PhotoPolicy.initials("jane", ""))
        assertEquals("", PhotoPolicy.initials("", ""))
    }
}

/**
 * The two-pass decode relies on the subsample factor keeping memory bounded.
 */
class PhotoIoSamplingTest {
    @Test
    fun `does not subsample small images`() {
        assertEquals(1, PhotoIo.sampleSizeFor(800, 600))
    }

    @Test
    fun `subsamples a modern phone photo`() {
        assertEquals(2, PhotoIo.sampleSizeFor(4000, 3000))
    }

    @Test
    fun `subsamples a very large image hard`() {
        assertEquals(4, PhotoIo.sampleSizeFor(12000, 9000))
    }

    @Test
    fun `handles degenerate input`() {
        assertEquals(1, PhotoIo.sampleSizeFor(0, 0))
    }
}
