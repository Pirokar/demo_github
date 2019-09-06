package im.threads.internal.transport;

import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.List;

import im.threads.internal.Config;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.MessageFromHistory;
import im.threads.internal.retrofit.ServiceGenerator;
import im.threads.internal.retrofit.ThreadsApi;
import im.threads.internal.utils.AppInfoHelper;
import im.threads.internal.utils.DateHelper;
import im.threads.internal.utils.PrefUtils;
import retrofit2.Call;
import retrofit2.Response;

public final class HistoryLoader {

    private static Long lastLoadedTimestamp;

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param beforeTimestamp timestamp сообщения от которого грузить, null если с начала
     * @param count           количество сообщений для загрузки
     */
    @WorkerThread
    public static HistoryResponse getHistorySync(Long beforeTimestamp, Integer count) throws Exception {
        String token = Config.instance.transport.getToken();
        String url = PrefUtils.getServerUrlMetaInfo();
        if (count == null) {
            count = Config.instance.historyLoadingCount;
        }
        if (url != null && !url.isEmpty() && !token.isEmpty()) {
            ServiceGenerator.setUrl(url);
            ThreadsApi threadsApi = ServiceGenerator.getThreadsApi();
            String beforeDate = beforeTimestamp == null ? null : DateHelper.getMessageDateStringFromTimestamp(beforeTimestamp);
            Call<HistoryResponse> call = threadsApi.history(token, beforeDate, count, AppInfoHelper.getLibVersion());
            Response<HistoryResponse> response = call.execute();
            return response.body();
        } else {
            throw new IOException();
        }
    }

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param count         количество сообщений для загрузки
     * @param fromBeginning загружать ли историю с начала или с последнего полученного сообщения
     */
    @WorkerThread
    public static HistoryResponse getHistorySync(Integer count, boolean fromBeginning) throws Exception {
        return getHistorySync(fromBeginning ? null : lastLoadedTimestamp, count);
    }

    static void setupLastItemIdFromHistory(List<MessageFromHistory> list) {
        if (list != null && !list.isEmpty()) {
            lastLoadedTimestamp = list.get(0).getTimeStamp();
        }
    }
}
