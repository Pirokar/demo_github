package im.threads.business.transport;

import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.util.List;

import im.threads.business.models.MessageFromHistory;
import im.threads.business.rest.models.HistoryResponse;
import im.threads.business.rest.queries.BackendApi;
import im.threads.business.rest.queries.ThreadsApi;
import im.threads.business.utils.DateHelper;
import im.threads.business.config.BaseConfig;
import im.threads.internal.utils.AppInfoHelper;
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
        String token = BaseConfig.instance.transport.getToken();
        if (count == null) {
            count = BaseConfig.instance.historyLoadingCount;
        }
        if (!token.isEmpty()) {
            ThreadsApi threadsApi = BackendApi.get();
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
