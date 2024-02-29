package im.threads.business.utils

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import im.threads.business.core.ContextHolder
import im.threads.business.models.FileDescription
import im.threads.business.serviceLocator.core.startEdnaLocator
import im.threads.business.serviceLocator.coreSLModule
import im.threads.ui.serviceLocator.uiSLModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException

@RunWith(RobolectricTestRunner::class)
class FileUtilsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var fileDescription: FileDescription
    private lateinit var file: File
    private lateinit var uri: Uri

    @Before
    fun before() {
        ContextHolder.context = context
        startEdnaLocator { modules(coreSLModule, uiSLModule) }

        fileDescription = Mockito.mock(FileDescription::class.java)
        file = File.createTempFile("temp", null, context.cacheDir)
        uri = Uri.parse("content://com.example.provider/path/to/file.jpg")
    }

    @Test
    fun whenGenerateFileName_thenReturnsCorrectFileName() {
        Mockito.`when`(fileDescription.downloadPath).thenReturn("/path/to/download")
        Mockito.`when`(fileDescription.incomingName).thenReturn("file.jpg")
        val fileName = FileUtils.generateFileName(fileDescription)
        assert(fileName.length == 45 && fileName.endsWith("_file.jpg"))
    }

    @Test
    fun whenIsImageWithImageFile_thenReturnsTrue() {
        Mockito.`when`(fileDescription.mimeType).thenReturn("image/jpeg")
        assert(FileUtils.isImage(fileDescription))
    }

    @Test
    fun whenIsImageWithNonImageFile_thenReturnsFalse() {
        Mockito.`when`(fileDescription.mimeType).thenReturn("application/pdf")
        assertFalse(FileUtils.isImage(fileDescription))
    }

    @Test
    fun whenIsVoiceMessageWithVoiceFile_thenReturnsTrue() {
        Mockito.`when`(fileDescription.mimeType).thenReturn("audio/wav")
        assert(FileUtils.isVoiceMessage(fileDescription))
    }

    @Test
    fun whenIsVoiceMessageWithNonVoiceFile_thenReturnsFalse() {
        Mockito.`when`(fileDescription.mimeType).thenReturn("application/pdf")
        assertFalse(FileUtils.isVoiceMessage(fileDescription))
    }

    @Test
    fun whenGetMimeTypeWithKnownMimeType_thenReturnsCorrectMimeType() {
        Mockito.`when`(fileDescription.mimeType).thenReturn("image/jpeg")
        assert("image/jpeg" == FileUtils.getMimeType(fileDescription))
    }

    @Test
    fun whenGetExtensionFromPathWithKnownExtension_thenReturnsCorrectExtension() {
        assertEquals(FileExtensions.JPEG, FileUtils.getExtensionFromPath("file.jpg"))
    }

    @Test
    fun whenGetExtensionFromPathWithUnknownExtension_thenReturnsUnknownExtension() {
        assertEquals(FileExtensions.UNKNOWN, FileUtils.getExtensionFromPath("file.unknown"))
    }

    @Test
    fun whenSafeParseWithValidSource_thenReturnsCorrectUri() {
        val source = "https://www.example.com"
        val uri = FileUtils.safeParse(source)
        assertEquals(Uri.parse(source), uri)
    }

    @Test
    fun whenSafeParseWithNullSource_thenReturnsNull() {
        val uri = FileUtils.safeParse(null)
        assertNull(uri)
    }

    @Test
    fun whenCreateImageFile_thenReturnsFileInCorrectDirectory() {
        val imageFile = FileUtils.createImageFile(context)
        assert(imageFile?.path?.startsWith(context.filesDir.path) == true)
    }

    @Test
    fun whenGetExtensionFromMediaStoreWithValidUri_thenReturnsCorrectExtension() {
        val extension = FileUtils.getExtensionFromMediaStore(context, uri)
        assertEquals("jpg", extension)
    }

    @Test
    fun whenGetExtensionFromMediaStoreWithNullUri_thenReturnsNull() {
        val extension = FileUtils.getExtensionFromMediaStore(context, null)
        assertNull(extension)
    }

    @Test(expected = NullPointerException::class)
    fun whenGetFileNameFromMediaStoreWithNullUri_thenReturnsNull() {
        val fileName = FileUtils.getFileNameFromMediaStore(context, null)
        assertNull(fileName)
    }

    @Test
    fun whenSingleUri_thenSingleMessage() {
        val uris = listOf(uri)
        val result = FileUtils.getUpcomingUserMessagesFromSelection(uris, "inputText", "fileDescriptionText", null, null)
        assertEquals(1, result.size)
    }

    @Test
    fun whenMultipleUris_thenMultipleMessages() {
        val uris = listOf(uri, uri, uri)
        val result = FileUtils.getUpcomingUserMessagesFromSelection(uris, "inputText", "fileDescriptionText", null, null)
        assertEquals(3, result.size)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun whenNoUri_thenNoMessage() {
        val uris = emptyList<Uri>()
        FileUtils.getUpcomingUserMessagesFromSelection(uris, "inputText", "fileDescriptionText", null, null)
    }

    @Test
    fun whenSizeInTB_thenResultInTB() {
        val size = 2L * 1024 * 1024 * 1024 * 1024 // 2TB
        val result = size.toFileSize(context)
        assertEquals("2 TB", result)
    }

    @Test
    fun whenSizeInGB_thenResultInGB() {
        val size = 2L * 1024 * 1024 * 1024 // 2GB
        val result = size.toFileSize(context)
        assertEquals("2 GB", result)
    }

    @Test
    fun whenSizeInBytes_thenResultInBytes() {
        val size = 500L // 500 bytes
        val result = size.toFileSize(context)
        assertEquals("500 B", result)
    }
}
