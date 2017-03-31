package im.threads.utils;

import android.content.Context;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;

import im.threads.model.FileDescription;
import im.threads.model.FileUploadResponse;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by yuri on 03.08.2016.
 */
public class FilePoster {
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


//    public void post(final Callback<String, Throwable> callback) {
//        String token = PrefUtils.getClientID(context);
//        if (!token.isEmpty()) {
//
//            if (fileDescription.getFilePath() != null && new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
//                RetrofitService retrofitService = ServiceGenerator.getRetrofitService();
//                File file = new File(fileDescription.getFilePath().replaceAll("file://", ""));
//                Uri fileUri = Uri.fromFile(file);
//                String path = file.getPath();
//                String mimeType = null;
//
//                if (path.contains(".")) {
//                    String extension = file.getPath().substring(file.getPath().lastIndexOf(".") + 1);
//                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//                }
//
//                if(mimeType == null) {
//                    mimeType = "*/*";
//                }
//
//                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
//                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
//
//                Call<FileUploadResponse> call = retrofitService.upload(body, token);
//                call.enqueue(new retrofit2.Callback<FileUploadResponse>() {
//                    @Override
//                    public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
//                        if (response.body() != null && response.body().getResult() != null && !response.body().getResult().isEmpty()) {
//                            callback.onSuccess(response.body().getResult());
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<FileUploadResponse> call, Throwable t) {
//                        callback.onFail(t);
//                        Log.i("++++++++", t.toString());
//                    }
//                });
//            } else if (fileDescription.getFilePath() != null && !new File(fileDescription.getFilePath().replaceAll("file://", "")).exists()) {
//                if (fileDescription.getDownloadPath() != null) {
//                    callback.onSuccess(fileDescription.getDownloadPath());
//                } else {
//                    callback.onFail(new FileNotFoundException());
//                }
//            } else if (fileDescription.getFilePath() == null && fileDescription.getDownloadPath() != null){
//                callback.onSuccess(fileDescription.getDownloadPath());
//            }
//        }
//
//    }


    public void post(final Callback<String, Throwable> callback) {
        String url = "https://datastore.threads.im/files";
        String token = PrefUtils.getClientID(context);
        if (!token.isEmpty()) {

            File file = null;

            if (fileDescription.getFilePath() != null) {
                file = new File(fileDescription.getFilePath().replaceAll("file://", ""));
            }

            if (file != null && file.exists() && file.isFile() && file.canRead()) {

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new HttpLoggingInterceptor())
                        .build();

                String path = file.getPath();
                String mimeType = null;

                if (path.contains(".")) {
                    String extension = file.getPath().substring(path.lastIndexOf(".") + 1);
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }

                if (mimeType == null) {
                    mimeType = "*/*";
                }

                MediaType mediaType = MediaType.parse(mimeType);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), RequestBody.create(mediaType, file))
                        .build();

//                HttpUrl url = new HttpUrl.Builder()
//                        .scheme("https")
//                        .host("datastore.threads.im")
//                        .addPathSegment("files")
//                        .build();

                Request request = new Request.Builder()
                        .header("X-Client-Token", token)
                        .url(url)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Gson gson = new Gson();
                        FileUploadResponse fileUploadResponse = gson.fromJson(response.body().charStream(), FileUploadResponse.class);
                        callback.onSuccess(fileUploadResponse.getResult());
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
