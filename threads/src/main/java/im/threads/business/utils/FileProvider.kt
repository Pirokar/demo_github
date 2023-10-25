package im.threads.business.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class FileProvider {
    private val authorityPostfix = ".im.threads.fileprovider"

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}$authorityPostfix",
            file
        )
    }
}
