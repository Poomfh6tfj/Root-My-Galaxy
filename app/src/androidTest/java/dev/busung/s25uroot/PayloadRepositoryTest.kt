package dev.busung.s25uroot

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayloadRepositoryTest {
    @Test
    fun signedManifestMatchesDeviceAndArtifactsDownload() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = PayloadRepository(context)
        val profile = repository.resolveTarget(DeviceSnapshot.current())
        assertEquals("pa3q-S938NKSUACZF1", profile.profileId)

        val payloads = repository.download(profile) { }
        assertEquals(profile.exploit.size, payloads.exploit.length())
        assertEquals(profile.kernelSu.artifact.size, payloads.kernelSu.length())
        assertTrue(payloads.exploit.canRead())
        assertTrue(payloads.kernelSu.canRead())
    }
}
