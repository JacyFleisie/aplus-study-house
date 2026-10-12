package com.aplusstudyhouse.app.updater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppUpdaterSignatureTest {
    private val context = mockk<Context>()
    private val packageManager = mockk<PackageManager>()

    private fun configurePackageManager(
        downloadedSignatures: Array<Signature>,
        installedSignatures: Array<Signature>,
    ) {
        val downloadedPackage = PackageInfo().apply {
            signingInfo = null
            signatures = downloadedSignatures
        }
        val installedPackage = PackageInfo().apply {
            signingInfo = null
            signatures = installedSignatures
        }

        every { context.packageManager } returns packageManager
        every { context.packageName } returns "com.aplusstudyhouse.app"
        every { packageManager.getPackageArchiveInfo(any(), any<Int>()) } returns downloadedPackage
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns installedPackage
    }

    @Test
    fun `matching certificate is accepted`() {
        val certificate = Signature(byteArrayOf(1, 2, 3))
        configurePackageManager(arrayOf(certificate), arrayOf(certificate))

        assertTrue(AppUpdater.verifyApkSignature(context, File("matching.apk")))
    }

    @Test
    fun `different certificate is rejected`() {
        val downloadedCertificate = mockk<Signature>()
        val installedCertificate = mockk<Signature>()
        every { downloadedCertificate.toByteArray() } returns byteArrayOf(1, 2, 3)
        every { installedCertificate.toByteArray() } returns byteArrayOf(4, 5, 6)
        configurePackageManager(
            downloadedSignatures = arrayOf(downloadedCertificate),
            installedSignatures = arrayOf(installedCertificate),
        )

        assertFalse(AppUpdater.verifyApkSignature(context, File("mismatched.apk")))
    }

    @Test
    fun `unsigned downloaded apk is rejected`() {
        configurePackageManager(
            downloadedSignatures = emptyArray(),
            installedSignatures = arrayOf(Signature(byteArrayOf(1, 2, 3))),
        )

        assertFalse(AppUpdater.verifyApkSignature(context, File("unsigned.apk")))
    }
}
