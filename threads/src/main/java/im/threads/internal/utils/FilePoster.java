package im.threads.internal.utils;

import android.accounts.NetworkErrorException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;

import im.threads.internal.Config;
import im.threads.internal.helpers.FileHelper;
import im.threads.internal.helpers.MediaHelper;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.retrofit.ApiGenerator;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * TODO THREADS-6288: this class needs refactoring, it contains one static method that does a lot of things making it untestable
 */
public final class FilePoster {

    private static final String TAG = "FilePoster ";
    private FilePoster() {
    }

    public static String post(FileDescription fileDescription) throws IOException, NetworkErrorException {
        ThreadsLogger.i(TAG, "post: " + fileDescription);
        String token = PrefUtils.getClientID();
        ThreadsLogger.i(TAG, "token = " + token);
        if (!token.isEmpty()) {
            File file = null;
            if (fileDescription.getFilePath() != null) {
                file = new File(fileDescription.getFilePath());
            }
            ThreadsLogger.i(TAG, "file: " + file);
            if (file != null) {
                ThreadsLogger.i(TAG, "file.exists = " + file.exists() + ", file.isFile = " + file.isFile() + ", file.canRead= "  + file.canRead());
            }
            if (file != null && file.exists() && file.isFile() && file.canRead()) {
                String response = sendFile(file, token);
                if (response != null) {
                    return response;
                }
            } else if (fileDescription.getFilePath() != null && !new File(fileDescription.getFilePath()).exists()) {
                if (fileDescription.getDownloadPath() != null) {
                    return fileDescription.getDownloadPath();
                } else {
                    throw new FileNotFoundException();
                }
            } else if (fileDescription.getFilePath() == null && fileDescription.getDownloadPath() != null) {
                return fileDescription.getDownloadPath();
            }
        }
        throw new NetworkErrorException();
    }

    private static String sendFile(File file, String token) throws IOException {
        ThreadsLogger.i(TAG, "sendFile: " + file);
        if (FileHelper.isThreadsImage(file)) {
            ThreadsLogger.i(TAG, "threads file: " + file);
            File downsizedImageFile = MediaHelper.downsizeImage(Config.instance.context, file, MediaHelper.PHOTO_RESIZE_MAX_SIDE);
            if (downsizedImageFile != null) {
                ThreadsLogger.i(TAG, "use downsizedImageFile ");
                file = downsizedImageFile;
            }
        }
        ThreadsLogger.i(TAG, "okHttpFileSend" + file);
        MultipartBody.Part part = MultipartBody.Part
                .createFormData("file", URLEncoder.encode(file.getName(), "utf-8"), RequestBody.create(MediaType.parse(FileUtils.getMimeType(file)), file));
        FileUploadResponse body = ApiGenerator.getThreadsApi().upload(part, token).execute().body();
        if (body != null) {
            return body.getResult();
        }
        return null;
    }

}
