package im.threads.internal.utils;

import android.accounts.NetworkErrorException;
import android.graphics.Bitmap;
import android.net.Uri;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import im.threads.internal.Config;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.retrofit.ApiGenerator;
import im.threads.internal.transport.InputStreamRequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * TODO THREADS-6288: this class needs refactoring, it contains one static method that does a lot of things making it untestable
 */
public final class FilePoster {

    private static final String TAG = "FilePoster ";
    private static int PHOTO_RESIZE_MAX_SIDE = 1600;

    private FilePoster() {
    }

    public static String post(FileDescription fileDescription) throws IOException, NetworkErrorException {
        ThreadsLogger.i(TAG, "post: " + fileDescription);
        String token = PrefUtils.getClientID();
        ThreadsLogger.i(TAG, "token = " + token);
        if (!token.isEmpty() || Config.instance.clientIdIgnoreEnabled) {
            if (fileDescription.getFileUri() != null) {
                return sendFile(fileDescription.getFileUri(), FileUtils.getMimeType(fileDescription.getFileUri()), token);
            }
            if (fileDescription.getDownloadPath() != null) {
                return fileDescription.getDownloadPath();
            }
        }
        throw new NetworkErrorException();
    }

    private static String sendFile(Uri uri, String mimeType, String token) throws IOException {
        ThreadsLogger.i(TAG, "sendFile: " + uri);
        MultipartBody.Part part = MultipartBody.Part
                .createFormData("file", URLEncoder.encode(FileUtils.getFileName(uri), "utf-8"), getFileRequestBody(uri, mimeType));
        FileUploadResponse body = ApiGenerator.getThreadsApi().upload(part, token).execute().body();
        if (body != null) {
            return body.getResult();
        }
        return null;
    }

    private static RequestBody getFileRequestBody(Uri uri, String mimeType) throws IOException {
        if (mimeType.equals("image/jpeg")) {
            return getJpegRequestBody(uri);
        }
        return new InputStreamRequestBody(MediaType.parse(mimeType), Config.instance.context.getContentResolver(), uri);
    }

    private static RequestBody getJpegRequestBody(Uri uri) throws IOException {
        ThreadsLogger.i(TAG, "sendFile: " + uri);
        File file = compressImage(uri);
        if (file == null) {
            throw new IOException("Unable to create compressed file");
        }
        return RequestBody.create(MediaType.parse("image/jpeg"), file);
    }


    private static File compressImage(Uri uri) throws IOException {
        Bitmap bitmap = Picasso.get()
                .load(uri)
                .resize(PHOTO_RESIZE_MAX_SIDE, PHOTO_RESIZE_MAX_SIDE)
                .centerInside()
                .onlyScaleDown()
                .get();
        File downsizedImageFile = new File(Config.instance.context.getCacheDir(), FileUtils.getFileName(uri));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        try (FileOutputStream fileOutputStream = new FileOutputStream(downsizedImageFile)) {
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            bitmap.recycle();
            return downsizedImageFile;
        } catch (IOException e) {
            ThreadsLogger.e(TAG, "downsizeImage", e);
            bitmap.recycle();
            downsizedImageFile.delete();
            return null;
        }
    }
}
