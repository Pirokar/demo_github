package im.threads.business.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class DeviceInfoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var deviceInfo: DeviceInfo

    @Before
    fun before() {
        deviceInfo = DeviceInfo()
    }

    @Test
    fun whenGetOsVersion_thenReturnsCorrectOsVersion() {
        assert(Build.VERSION.RELEASE == deviceInfo.osVersion)
    }

    @Test
    fun whenGetDeviceName_thenReturnsCorrectDeviceName() {
        assert("${Build.MANUFACTURER} ${Build.MODEL}" == deviceInfo.deviceName)
    }

    @Test
    fun whenGetLocale_thenReturnsCorrectLocale() {
        val configuration = Configuration()
        configuration.setLocale(Locale.US)
        context.resources.configuration.setLocale(Locale.US)
        assert("en-US" == deviceInfo.getLocale(context))
    }
}
