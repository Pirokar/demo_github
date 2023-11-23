package im.threads.business.audio.audioConverter

import androidx.test.core.app.ApplicationProvider
import im.threads.business.audio.audioConverter.callback.IConvertCallback
import im.threads.business.audio.audioConverter.model.AudioFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AudioConverterTest {
    private lateinit var audioConverter: AudioConverter

    @Before
    fun before() {
        audioConverter = getNewAudioConverter()
    }

    @Test
    fun whenSetFile_thenFileExistsInObj() {
        val path = "testPath"
        audioConverter.setFile(File(path))
        assert(audioConverter.audioFile?.path?.endsWith(path) == true)
    }

    @Test
    fun whenSetFormat_thenFormatExistsInObj() {
        val format = AudioFormat.AAC
        audioConverter.setFormat(format)
        assert(audioConverter.format == format)
    }

    @Test
    fun whenSetCallback_thenCallbackExistsInObj() {
        val callback = object : IConvertCallback {
            override fun onSuccess(convertedFile: File) {}
            override fun onFailure(error: Exception) {}
        }
        audioConverter.setCallback(callback)
        assert(audioConverter.callback == callback)
    }

    private fun getNewAudioConverter() = AudioConverter.with(ApplicationProvider.getApplicationContext())
}
