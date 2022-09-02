package im.threads.business.transport;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import im.threads.business.utils.FileUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class InputStreamRequestBody extends RequestBody {
    private final MediaType contentType;
    private final ContentResolver contentResolver;
    private final Uri uri;

    public InputStreamRequestBody(MediaType contentType, ContentResolver contentResolver, Uri uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }
        this.contentType = contentType;
        this.contentResolver = contentResolver;
        this.uri = uri;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return FileUtils.getFileSize(uri);
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream != null) {
            try (Source source = Okio.source(inputStream)) {
                sink.writeAll(source);
            }
        }
    }
}

