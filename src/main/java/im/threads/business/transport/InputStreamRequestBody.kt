package im.threads.business.transport

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import im.threads.business.utils.FileUtils.getFileSize
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

class InputStreamRequestBody(contentType: MediaType?, contentResolver: ContentResolver, uri: Uri?) :
    RequestBody() {
    private val contentType: MediaType?
    private val contentResolver: ContentResolver
    private val uri: Uri

    init {
        if (uri == null) {
            throw NullPointerException("uri == null")
        }
        this.contentType = contentType
        this.contentResolver = contentResolver
        this.uri = uri
    }

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return getFileSize(uri)
    }

    @SuppressLint("Recycle")
    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.source()?.use { source -> sink.writeAll(source) }
    }
}
