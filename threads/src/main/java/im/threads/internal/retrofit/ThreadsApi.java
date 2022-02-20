package im.threads.internal.retrofit;

import java.util.List;

import androidx.annotation.NonNull;
import im.threads.internal.Config;
import im.threads.internal.model.FileUploadResponse;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.SettingsResponse;
import im.threads.internal.opengraph.OGResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public final class ThreadsApi {

    private static final String SIGNATURE_STRING = "super-duper-signature-string:";

    @NonNull
    private final OldThreadsApi oldThreadsApi;

    @NonNull
    private final NewThreadsApi newThreadsApi;

    public ThreadsApi(@NonNull OldThreadsApi oldThreadsApi, @NonNull NewThreadsApi newThreadsApi) {
        this.oldThreadsApi = oldThreadsApi;
        this.newThreadsApi = newThreadsApi;
    }

    public Call<SettingsResponse> settings() {
        if (Config.instance.newChatCenterApi) {
            return newThreadsApi.settings();
        } else {
            return oldThreadsApi.settings();
        }
    }

    public Call<HistoryResponse> history(String token, String beforeDate, Integer count, String version) {
        if (Config.instance.newChatCenterApi) {
            return newThreadsApi.history(token, beforeDate, count, version);
        } else {
            return oldThreadsApi.history(token, beforeDate, count, version);
        }
    }

    public Call<Void> markMessageAsRead(List<String> ids) {
        if (Config.instance.newChatCenterApi) {
            return newThreadsApi.markMessageAsRead(ids);
        } else {
            return oldThreadsApi.markMessageAsRead(ids);
        }
    }

    public Call<OGResponse> openGraph(String url) {
        if (Config.instance.newChatCenterApi) {
            return newThreadsApi.openGraph(url);
        } else {
            return oldThreadsApi.openGraph(url);
        }
    }

    public Call<FileUploadResponse> upload(MultipartBody.Part file, RequestBody agent, String token) {
        if (Config.instance.newChatCenterApi) {
            return newThreadsApi.upload(file, agent, SIGNATURE_STRING + token);
        } else {
            return oldThreadsApi.upload(file, agent, SIGNATURE_STRING + token);
        }
    }
}
