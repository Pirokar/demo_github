package im.threads.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by yuri on 29.07.2016.
 */
public abstract class FileDownloader {
    private static final String TAG = "FileDownloader ";
    private String path;
    private String fileName;
    private Context ctx;
    private boolean isStopped;

    public FileDownloader(String path, String filename, Context ctx) {
        this.path = path;
        this.ctx = ctx;
        this.fileName = filename;
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
                File outputFile;
                if (FileUtils.getExtensionFromPath(fileName) == FileUtils.PDF || FileUtils.getExtensionFromPath(fileName) == FileUtils.OTHER_DOC_FORMATS) {
                    outputFile = new File(ctx.getFilesDir(), fileName);
                } else {
                    outputFile = new File(ctx.getFilesDir(), fileName);
                }
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                List values = urlConnection.getHeaderFields().get("Content-Length");
                Long length = null;
                try {
                    if (values != null && !values.isEmpty()) {
                        length = Long.parseLong((String) values.get(0));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                int len1 = 0;
                long bytesReaded = 0;
                long lastReadTime = System.currentTimeMillis();
                final byte buffer[] = new byte[1024 * 8];
                while ((len1 = in.read(buffer)) > 0 && !isStopped) {
                    fileOutputStream.write(buffer, 0, len1);
                    bytesReaded += len1;
                    if (length != null && (System.currentTimeMillis() > (lastReadTime + 500))) {
                        int progress = (int) Math.floor((((double) bytesReaded) / ((double) length)) * 100.0);
                        lastReadTime = System.currentTimeMillis();
                        onProgress(progress);
                    }
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                if (!isStopped) onComplete(outputFile);
            } catch (Exception e) {
                Log.e(TAG, "1 "+e);
                e.printStackTrace();
                onFileDonwloaderError(e);
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, "2 "+e);
            e.printStackTrace();
            onFileDonwloaderError(e);
        }
    }

    public abstract void onProgress(double progress);

    public abstract void onComplete(File file);

    public abstract void onFileDonwloaderError(Exception e);
}
