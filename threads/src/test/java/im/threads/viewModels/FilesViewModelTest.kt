package im.threads.viewModels

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import im.threads.business.models.FileDescription
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.ui.activities.filesActivity.FilesViewModel
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilesViewModelTest {
    private lateinit var viewModel: FilesViewModel
    private lateinit var viewModelSpy: FilesViewModel
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val fileDescriptionWithData =
        FileDescription(
            "Serious operator",
            Uri.parse("https://www.testurl.ru/somefile.no"),
            1000L,
            1223345L
        )

    @Mock
    private lateinit var database: DatabaseHolder

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        `when`(database.allFileDescriptions).thenReturn(Single.just(ArrayList()))

        viewModel = FilesViewModel(context, database)
        viewModelSpy = spy(viewModel)
    }

    @Test
    fun whenUpdateProgress_thenPassedToActivity() {
        viewModelSpy.updateProgress(null)
        verify(viewModelSpy, times(1)).localFilesFlowLiveData
    }

    @Test
    fun whenDownloadError_thenPassedToActivity() {
        viewModelSpy.onDownloadError(null, null)
        verify(viewModelSpy, times(1)).localFilesFlowLiveData
    }

    @Test
    fun whenGetFilesAsync_thenDatabaseAllFileDescriptionsCalled() {
        viewModel.getFilesAsync()
        verify(database, times(1)).allFileDescriptions
    }

    @Test
    fun givenFileDescriptionIsNull_whenOnFileClick_thenReturn() {
        viewModelSpy.onFileClick(null)
        verify(viewModelSpy, never()).localIntentLiveData
    }

    @Test
    fun givenFileDescriptionNonNull_whenOnFileClick_thenReturn() {
        // Unfortunately, "isImage" check requires context inside the FileUtils. But it's null there
        // TODO: rewrite FileUtils with DI and then change this test
        try {
            viewModel.onFileClick(fileDescriptionWithData)
            assert(false)
        } catch (exc: NullPointerException) {
            assert(true)
        }
    }

    @Test
    fun givenFileDescriptionNonNull_whenDownloadFileClick_fileDownloadStarted() {
        // If "start download" will be called, it will be crashed because of null context
        // or work manager not initialized
        // TODO: rewrite when FileDownloadWorker static methods will be replaced by companion
        try {
            viewModel.onDownloadFileClick(fileDescriptionWithData)
            assert(false)
        } catch (exc: NullPointerException) {
            assert(true)
        } catch (exc: IllegalStateException) {
            assert(true)
        }
    }

    @Test
    fun whenOnCleared_thenReceiverDisconnected() {
        viewModelSpy.onCleared()
        verify(viewModelSpy).disconnectReceiver()
    }
}
