package im.threads.internal.utils;

import android.text.TextUtils;

import com.mfms.android.push_lite.PushController;
import com.mfms.android.push_lite.RequestCallback;
import com.mfms.android.push_lite.exception.PushServerErrorException;
import com.mfms.android.push_lite.repo.push.remote.api.InMessageSend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.threads.internal.formatters.IncomingMessageParser;
import im.threads.internal.Config;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.HistoryResponse;
import im.threads.internal.model.MessageFromHistory;
import im.threads.internal.retrofit.ServiceGenerator;
import im.threads.internal.retrofit.ThreadsApi;
import retrofit2.Call;
import retrofit2.Response;

public final class Transport {

    private static final String TAG = Transport.class.getSimpleName();
    private static Long lastLoadedTimestamp;

    /**
     * метод-обертка над методом mfms sendMessage
     */
    public static void sendMessageMFMSSync(String message, boolean isSystem) throws PushServerErrorException {
        getPushControllerInstance().sendMessage(message, isSystem);
    }

    /**
     * метод обертка над методом mfms sendMessageAsync
     *
     * @param message           сообщение для отправки
     * @param isSystem          системное сообщение
     * @param listener          слушатель успешной/неуспешной отправки
     * @param exceptionListener слушатель ошибки отсутствия DeviceAdress
     */
    public static void sendMessageMFMSAsync(String message,
                                            boolean isSystem,
                                            final RequestCallback<InMessageSend.Response, PushServerErrorException> listener,
                                            final ExceptionListener exceptionListener) {
        try {
            getPushControllerInstance().sendMessageAsync(
                    message, isSystem, new RequestCallback<InMessageSend.Response, PushServerErrorException>() {
                        @Override
                        public void onResult(InMessageSend.Response response) {
                            if (listener != null) {
                                listener.onResult(response);
                            }
                        }

                        @Override
                        public void onError(PushServerErrorException e) {
                            if (listener != null) {
                                listener.onError(e);
                            }
                        }
                    });
        } catch (Exception e) {
            ThreadsLogger.e(TAG, "sendMessageMFMSAsync", e);
            if (exceptionListener != null) {
                exceptionListener.onException(e);
            }
        }
    }

    /**
     * обращение к пуш контроллеру, если нет DeviceAddress,
     * то выкидывает PushServerErrorException
     */
    public static PushController getPushControllerInstance() throws PushServerErrorException {
        PushController controller = PushController.getInstance(Config.instance.context);
        String deviceAddress = controller.getDeviceAddress();
        if (deviceAddress != null && !deviceAddress.isEmpty()) {
            return controller;
        } else {
            throw new PushServerErrorException(PushServerErrorException.DEVICE_ADDRESS_INVALID);
        }
    }

    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param beforeTimestamp timestamp сообщения от которого грузить, null если с начала
     * @param count           количество сообщений для загрузки
     */
    public static HistoryResponse getHistorySync(Long beforeTimestamp, Integer count) throws Exception {

        String clientIdSignature = PrefUtils.getClientIdSignature();

        String token = (TextUtils.isEmpty(clientIdSignature) ? getPushControllerInstance().getDeviceAddress() : clientIdSignature)
                + ":" + PrefUtils.getClientID();
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
    public static HistoryResponse getHistorySync(Integer count, boolean fromBeginning) throws Exception {
        return getHistorySync(fromBeginning ? null : lastLoadedTimestamp, count);
    }

    public static List<ChatItem> getChatItemFromHistoryResponse(HistoryResponse response) {
        List<ChatItem> list = new ArrayList<>();
        if (response != null) {
            List<MessageFromHistory> responseList = response.getMessages();
            if (responseList != null) {
                list = IncomingMessageParser.formatNew(responseList);
                setupLastItemIdFromHistory(responseList);
            }
        }
        return list;
    }

    private static void setupLastItemIdFromHistory(List<MessageFromHistory> list) {
        if (list != null && !list.isEmpty()) {
            lastLoadedTimestamp = list.get(0).getTimeStamp();
        }
    }

    public interface ExceptionListener {
        void onException(Exception e);
    }
}