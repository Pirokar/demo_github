package im.threads.business.config

import im.threads.ChatStyle
import im.threads.styles.permissions.PermissionDescriptionDialogStyle

interface UIConfig {
    fun getChatStyle(): ChatStyle
    fun setChatStyle(style: ChatStyle?)
    fun getStoragePermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle
    fun setStoragePermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?)
    fun getRecordAudioPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle
    fun setRecordAudioPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?)
    fun getCameraPermissionDescriptionDialogStyle(): PermissionDescriptionDialogStyle
    fun setCameraPermissionDescriptionDialogStyle(style: PermissionDescriptionDialogStyle?)
}
