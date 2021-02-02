package im.threads.internal.utils;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FileDownloader {
    private static final String TAG = "FileDownloader ";
    private final String path;
    private final File outputFile;
    private final DownloadLister downloadLister;

    private boolean isStopped;

    public FileDownloader(@NonNull String path, @NonNull Context ctx, @Nullable DownloadLister downloadLister) {
        this.path = path;
        String filename = Uri.parse(path).getLastPathSegment();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(path.getBytes());
            filename = new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            ThreadsLogger.e(TAG, "constructor", e);
        }
        this.outputFile = new File(getDownloadDir(ctx), filename);
        this.downloadLister = downloadLister;
    }

    public void stop() {
        isStopped = true;
    }

    public void download() {
        try {
            URL url = new URL(this.path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);

                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                List<String> values = urlConnection.getHeaderFields().get("Content-Length");
                Long length = null;
                try {
                    if (values != null && !values.isEmpty()) {
                        length = Long.parseLong((String) values.get(0));
                    }
                } catch (NumberFormatException e) {
                    ThreadsLogger.e(TAG, "download", e);
                }
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                int len1;
                long bytesReaded = 0;
                long lastReadTime = System.currentTimeMillis();
                final byte[] buffer = new byte[1024 * 8];
                while ((len1 = in.read(buffer)) > 0 && !isStopped) {
                    fileOutputStream.write(buffer, 0, len1);
                    bytesReaded += len1;
                    if (length != null && (System.currentTimeMillis() > (lastReadTime + 500))) {
                        int progress = (int) Math.floor((((double) bytesReaded) / ((double) length)) * 100.0);
                        lastReadTime = System.currentTimeMillis();
                        if (downloadLister != null) {
                            downloadLister.onProgress(progress);
                        }
                    }
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                if (!isStopped) {
                    if (downloadLister != null) {
                        downloadLister.onComplete(outputFile);
                    }
                }
            } catch (Exception e) {
                ThreadsLogger.e(TAG, "1 ", e);
                if (downloadLister != null) {
                    downloadLister.onFileDownloadError(e);
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            ThreadsLogger.e(TAG, "2 ", e);
            if (downloadLister != null) {
                downloadLister.onFileDownloadError(e);
            }
        }
    }

    public static File getDownloadDir(Context ctx) {
        return ctx.getFilesDir();
    }

    public interface DownloadLister {
        void onProgress(double progress);

        void onComplete(File file);

        void onFileDownloadError(Exception e);

    }
}
