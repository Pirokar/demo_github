package im.threads.internal.utils;

import android.accounts.NetworkErrorException;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import im.threads.internal.Config;
import im.threads.internal.image_loading.CoilImageLoader;
import im.threads.internal.image_loading.ImageLoader;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.retrofit.DatastoreApiGenerator;
import im.threads.internal.transport.InputStreamRequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * TODO THREADS-6288: this class needs refactoring, it contains one static method that does a lot of things making it untestable
 */
public final class FilePoster {

    private static final String TAG = "FilePoster ";
    private static int PHOTO_RESIZE_MAX_SIDE = 1600;
    private static ImageLoader imageLoader = new CoilImageLoader();

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
        RequestBody agent = RequestBody.create(token, MediaType.parse("text/plain"));
        Response<FileUploadResponse> response = DatastoreApiGenerator.getApi().upload(part, agent, token).execute();
        if (response.isSuccessful()) {
            FileUploadResponse body = response.body();
            if (body != null) {
                return body.getResult();
            }
        }
        ThreadsLogger.e(TAG, "response = " + response.toString());
        throw new IOException(response.toString());
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }
    }

    private static boolean isJpeg(Uri uri) {
        try (InputStream iStream = Config.instance.context.getContentResolver().openInputStream(uri)) {
            byte[] inputData = getBytes(iStream);
            //JPEG(JFIF) header: FF D8 FF
            return inputData[0] == -1 && inputData[1] == -40 && inputData[2] == -1;
        } catch (IOException e) {
            return false;
        }
    }

    private static RequestBody getFileRequestBody(Uri uri, String mimeType) throws IOException {
        if (isJpeg(uri)) {
            RequestBody jpegRequestBody = getJpegRequestBody(uri);
            if (jpegRequestBody != null) {
                return jpegRequestBody;
            }
        }
        return new InputStreamRequestBody(MediaType.parse(mimeType), Config.instance.context.getContentResolver(), uri);
    }

    private static RequestBody getJpegRequestBody(Uri uri) throws IOException {
        ThreadsLogger.i(TAG, "sendFile: " + uri);
        File file = compressImage(uri);
        if (file == null) {
            return null;
        }
        return RequestBody.create(MediaType.parse("image/jpeg"), file);
    }


    private static File compressImage(Uri uri) throws IOException {
        Bitmap bitmap = imageLoader.getBitmap(Config.instance.context, uri.toString());
        bitmap = getResizedBitmap(bitmap, PHOTO_RESIZE_MAX_SIDE, PHOTO_RESIZE_MAX_SIDE);
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

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();

        return resizedBitmap;
    }
}
