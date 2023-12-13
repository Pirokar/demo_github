package im.threads.ui.styles.permissions

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PermissionDescriptionDialogStyleTest {

    @Test
    fun whenGetDefaultDialogStyleForStorage_thenCorrectDialogStyleIsReturned() {
        val dialogStyle = PermissionDescriptionDialogStyle.getDefaultDialogStyle(PermissionDescriptionType.STORAGE)
        assertEquals(im.threads.R.string.ecc_permission_description_access_to_files_title, dialogStyle.titleStyle.textResId)
        assertEquals(im.threads.R.string.ecc_permission_description_access_to_files_message, dialogStyle.messageStyle.textResId)
    }

    @Test
    fun whenGetDefaultDialogStyleForRecordAudio_thenCorrectDialogStyleIsReturned() {
        val dialogStyle = PermissionDescriptionDialogStyle.getDefaultDialogStyle(PermissionDescriptionType.RECORD_AUDIO)
        assertEquals(im.threads.R.string.ecc_permission_description_access_to_audio_recording_title, dialogStyle.titleStyle.textResId)
        assertEquals(im.threads.R.string.ecc_permission_description_access_to_audio_recording_message, dialogStyle.messageStyle.textResId)
    }

    @Test
    fun whenGetDefaultDialogStyleForCamera_thenCorrectDialogStyleIsReturned() {
        val dialogStyle = PermissionDescriptionDialogStyle.getDefaultDialogStyle(PermissionDescriptionType.CAMERA)
        assertEquals(im.threads.R.string.ecc_permission_description_access_to_camera_title, dialogStyle.titleStyle.textResId)
        assertEquals(im.threads.R.string.ecc_permission_description_access_to_camera_message, dialogStyle.messageStyle.textResId)
    }
}
