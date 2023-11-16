package io.edna.threads.demo

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.kaspersky.kaspresso.internal.extensions.other.createFileIfNeeded
import im.threads.R
import im.threads.business.config.BaseConfig
import io.edna.threads.demo.appCode.activity.MainActivity
import org.junit.After
import org.junit.Rule
import java.io.File

open class BaseFilesTestCase : BaseTestCase() {
    protected val fileNamesToDelete = arrayListOf<String>()

    private val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    internal val activityRule = ActivityScenarioRule<MainActivity>(intent)

    init {
        applyDefaultUserToDemoApp()
        prepareWsMocks()
    }

    @Rule
    @JvmField
    val storageApiBelow29Rule = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    } else {
        null
    }

    @Rule
    @JvmField
    val storageApi29Rule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.ACCESS_MEDIA_LOCATION",
            "android.permission.RECORD_AUDIO"
        )
    } else {
        null
    }

    @After
    fun after() {
        if (fileNamesToDelete.isNotEmpty()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath?.let { downloadsPath ->
                fileNamesToDelete.forEach {
                    val toFile = File("$downloadsPath/$it")
                    if (toFile.exists()) {
                        toFile.delete()
                    }
                }
            }
        }
    }

    protected fun copyFileToDownloads(assetsPath: String): String? {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            copyToDownloadsApiBelow29(assetsPath)
        } else {
            copyToDownloadsApi29(assetsPath)
        }
    }

    @Suppress("DEPRECATION")
    private fun copyToDownloadsApiBelow29(filePathRelativeToAssets: String): String? {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath?.let {
            val fileName = filePathRelativeToAssets.split("/").last()
            val toFile = File("$it/$fileName")
            if (toFile.exists() && toFile.length() > 0) {
                return fileName
            } else if (toFile.exists()) {
                toFile.delete()
            }
            context.assets.open(filePathRelativeToAssets).toFile(toFile.createFileIfNeeded())
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            dm.addCompletedDownload(
                fileName,
                BaseConfig.getInstance().context.getString(R.string.ecc_media_description),
                true,
                getMimeType(fileName),
                toFile.path,
                toFile.length(),
                false
            )
            return fileName
        }

        return null
    }

    /**
     * Возвращает newFileName, если в процессе копирования файла
     * случилась ошибка UNIQUE constraint failed
     */
    private fun copyToDownloadsApi29(filePathRelativeToAssets: String, nameOfFile: String? = null): String? {
        try {
            val fileName = nameOfFile ?: getNameOfFile(filePathRelativeToAssets)
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath?.let {
                val toFile = File("$it/$fileName")
                if (toFile.exists() && toFile.length() > 0) {
                    return null
                } else if (toFile.exists()) {
                    toFile.delete()
                }
            }
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                fileName
            )
            values.put(
                MediaStore.MediaColumns.MIME_TYPE,
                getMimeType(fileName)
            )
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
            val uri: Uri? = context.contentResolver.insert(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                values
            )
            context.assets.open(filePathRelativeToAssets).copyToUri(uri!!, context)
            return fileName
        } catch (exc: android.database.sqlite.SQLiteConstraintException) {
            val name = getNameOfFile(filePathRelativeToAssets, true)
            copyToDownloadsApi29(filePathRelativeToAssets, name)
            return name
        }
    }

    private fun getNameOfFile(filePathRelativeToAssets: String, plusRandom: Boolean = false): String {
        val usualName = filePathRelativeToAssets.split("/").last()
        return if (plusRandom) {
            val nameParts = usualName.split(".")
            "${nameParts[0]}$userId.${nameParts[1]}"
        } else {
            usualName
        }
    }

    private fun getMimeType(fileName: String): String {
        return if (fileName.endsWith(".jpg")) {
            "image/jpeg"
        } else if (fileName.endsWith(".jpeg")) {
            "image/pjpeg"
        } else if (fileName.endsWith(".png")) {
            "image/png"
        } else if (fileName.endsWith(".gif")) {
            "image/gif"
        } else if (fileName.endsWith(".tiff")) {
            "image/tiff"
        } else if (fileName.endsWith(".ogg")) {
            "audio/ogg"
        } else if (fileName.endsWith(".aac")) {
            "image/aac"
        } else if (fileName.endsWith(".pdf")) {
            "application/pdf"
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            "application/msword"
        } else if (fileName.endsWith(".zip")) {
            "application/zip"
        } else if (fileName.endsWith(".gzip")) {
            "application/gzip"
        } else if (fileName.endsWith(".xml")) {
            "application/xml"
        } else {
            "*/*"
        }
    }
}
