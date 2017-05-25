package im.threads.utils;

import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;

import im.threads.model.FileDescription;
import im.threads.model.FileUploadResponse;
import im.threads.retrofit.RetrofitService;
import im.threads.retrofit.ServiceGenerator;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by yuri on 03.08.2016.
 */
public class FilePoster {
    public static final String UPLOAD_FILE_URL = "https://datastore.threads.im/";
    private FileDescription fileDescription;
    private Context context;

    public FilePoster(FileDescription fileDescription, Context context) {
        this.fileDescription = fileDescription;
        this.context = context;
    }

//    public void post(final Callback<String, Exception> callback) {
//        if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
//            PushController
//                    .getInstance(context)
//                    .sendFileAsync(new File(fileDescription.getFilePath().replaceAll("file://", ""))
//                            , ""
//                            , TimeUnit.DAYS.toMillis(0)
//                            , new RequestProgressCallback() {
//                                @Override
//                                public void onProgress(double v) {
//
//                                }
//
//                                @Override
//                                public void onResult(String s) {
//                                    callback.onSuccess(s);
//                                }
//
//                                @Override
//                                public void onError(PushServerErrorException e) {
//                                    callback.onFail(e);
//                                }
//                            });
//        } else if (fileDescription.getFilePath() != null && !new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
//            if (fileDescription.getDownloadPath() != null) {
//                callback.onSuccess(fileDescription.getDownloadPath());
//            } else {
//                callback.onFail(new FileNotFoundException());
//            }
//        }else if (fileDescription.getFilePath() ==null && fileDescription.getDownloadPath() !=null){
//            callback.onSuccess(fileDescription.getDownloadPath());
//        }
//    }


    public void post(final Callback<String, Throwable> callback) {
        String token = PrefUtils.getClientID(context);
        if (!token.isEmpty()) {

            File file = null;

            if (fileDescription.getFilePath() != null) {
                file = new File(fileDescription.getFilePath().replaceAll("file://", ""));
            }

            if (file != null && file.exists() && file.isFile() && file.canRead()) {
                ServiceGenerator.setUrl(UPLOAD_FILE_URL);
                RetrofitService retrofitService = ServiceGenerator.getRetrofitService();
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
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                Call<FileUploadResponse> call = retrofitService.upload(body, token);

                call.enqueue(new retrofit2.Callback<FileUploadResponse>() {
                    @Override
                    public void onResponse(Call<FileUploadResponse> call, retrofit2.Response<FileUploadResponse> response) {
                        if (response.body() != null && response.body().getResult() != null && !response.body().getResult().isEmpty()) {
                            callback.onSuccess(response.body().getResult());
                        }
                    }

                    @Override
                    public void onFailure(Call<FileUploadResponse> call, Throwable t) {
                        callback.onFail(t);
                    }
                });

            } else if (fileDescription.getFilePath() != null && !new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
                if (fileDescription.getDownloadPath() != null) {
                    callback.onSuccess(fileDescription.getDownloadPath());
                } else {
                    callback.onFail(new FileNotFoundException());
                }
            } else if (fileDescription.getFilePath() == null && fileDescription.getDownloadPath() != null) {
                callback.onSuccess(fileDescription.getDownloadPath());
            }
        }

    }


}
