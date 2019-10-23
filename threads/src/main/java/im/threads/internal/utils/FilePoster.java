package im.threads.internal.utils;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import im.threads.internal.helpers.FileHelper;
import im.threads.internal.helpers.MediaHelper;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.retrofit.ServiceGenerator;
import im.threads.internal.retrofit.ThreadsApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public final class FilePoster {
    private static final String UPLOAD_FILE_URL = "https://datastore.threads.im/";
    private FileDescription fileDescription;
    private Context context;

    FilePoster(FileDescription fileDescription, Context context) {
        this.fileDescription = fileDescription;
        this.context = context;
    }

    public void post(final Callback<String, Throwable> callback) {
        String token = PrefUtils.getClientID();
        if (!token.isEmpty()) {
            File file = null;
            if (fileDescription.getFilePath() != null) {
                file = new File(fileDescription.getFilePath());
            }
            if (file != null && file.exists() && file.isFile() && file.canRead()) {
                if (FileHelper.isThreadsImage(file)) {
                    File downsizedImageFile = MediaHelper.downsizeImage(context, file, MediaHelper.PHOTO_RESIZE_MAX_SIDE);
                    if (downsizedImageFile != null) {
                        file = downsizedImageFile;
                    }
                }
                ServiceGenerator.setUrl(UPLOAD_FILE_URL);
                ThreadsApi threadsApi = ServiceGenerator.getThreadsApi();
                String path = file.getPath();
                String mimeType = null;
                if (path.contains(".")) {
                    String extension = file.getPath().substring(file.getPath().lastIndexOf(".") + 1);
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
                if (mimeType == null) {
                    mimeType = "*/*";
                }
                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
                try {
                    MultipartBody.Part body = MultipartBody.Part.createFormData("file", URLEncoder.encode(file.getName(), "utf-8"), requestFile);
                    Call<FileUploadResponse> call = threadsApi.upload(body, token);
                    call.enqueue(new retrofit2.Callback<FileUploadResponse>() {
                        @Override
                        public void onResponse(Call<FileUploadResponse> call, retrofit2.Response<FileUploadResponse> response) {
                            if (response.body() != null && response.body().getResult() != null && !response.body().getResult().isEmpty()) {
                                callback.onSuccess(response.body().getResult());
                            }
                        }

                        @Override
                        public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                            callback.onError(t);
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    callback.onError(e);
                }

            } else if (fileDescription.getFilePath() != null && !new File(fileDescription.getFilePath()).exists()) {
                if (fileDescription.getDownloadPath() != null) {
                    callback.onSuccess(fileDescription.getDownloadPath());
                } else {
                    callback.onError(new FileNotFoundException());
                }
            } else if (fileDescription.getFilePath() == null && fileDescription.getDownloadPath() != null) {
                callback.onSuccess(fileDescription.getDownloadPath());
            }
        } else {
            callback.onError(new NetworkErrorException());
        }
    }
}