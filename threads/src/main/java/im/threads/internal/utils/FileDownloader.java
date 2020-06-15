package im.threads.internal.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FileDownloader {
    private static final String TAG = "FileDownloader ";
    private String path;
    private File outputFile;
    private boolean isStopped;

    private DownloadLister downloadLister;

    public FileDownloader(String path, String fileName, Context ctx, DownloadLister downloadLister) {
        this.path = path;
        outputFile = new File(getDownloadDir(ctx), fileName);
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
                List values = urlConnection.getHeaderFields().get("Content-Length");
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
