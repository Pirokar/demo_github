package im.threads.utils;

import android.content.Context;

import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.threads.formatters.IncomingMessageParser;
import im.threads.model.ChatItem;
import im.threads.model.ChatStyle;
import im.threads.model.HistoryResponse;
import im.threads.model.MessgeFromHistory;
import im.threads.retrofit.ServiceGenerator;
import im.threads.retrofit.ThreadsApi;
import retrofit2.Call;
import retrofit2.Response;


public final class Transport {

    private static Long lastLoadId;

    public interface ExceptionListener {
        void onException(Exception e);
    }

    /**
     * метод-обертка над методом mfms sendMessage
     */
    public static void sendMessageMFMSSync(Context ctx, String message, boolean isSystem) throws PushServerErrorException {
        getPushControllerInstance(ctx).sendMessage(message, isSystem);
    }

    /**
     * метод обертка над методом mfms sendMessageAsync
     *
     * @param message           сообщение для отправки
     * @param isSystem          системное сообщение
     * @param listener          слушатель успешной/неуспешной отправки
     * @param exceptionListener слушатель ошибки отсутствия DeviceAdress
     */
    public static void sendMessageMFMSAsync(Context ctx,
                                             String message,
                                             boolean isSystem,
                                             final RequestCallback<String, PushServerErrorException> listener,
                                             final ExceptionListener exceptionListener) {
        try {
            getPushControllerInstance(ctx).sendMessageAsync(
                    message, isSystem, new RequestCallback<String, PushServerErrorException>() {
                        @Override
                        public void onResult(String aVoid) {
                            if (listener != null) {
                                listener.onResult(aVoid);
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
            e.printStackTrace();
            if (exceptionListener != null) {
                exceptionListener.onException(e);
            }
        }
    }

    /**
     * обращение к пуш контроллеру, если нет DeviceAddress,
     * то выкидывает PushServerErrorException
     */
    public static PushController getPushControllerInstance(Context ctx) throws PushServerErrorException {
        PushController controller = PushController.getInstance(ctx);
        String deviceAddress = controller.getDeviceAddress();
        if (deviceAddress != null && !deviceAddress.isEmpty()) {
            return PushController.getInstance(ctx);
        } else {
            throw new PushServerErrorException(PushServerErrorException.DEVICE_ADDRESS_INVALID);
        }
    }
    /**
     * метод обертка для запроса истории сообщений
     * выполняется синхронно
     *
     * @param start id сообщения от которого грузить, null если с начала
     * @param count количество сообщений для загрузки
     */
    public static HistoryResponse getHistorySync(Context ctx, Long start, Long count) throws Exception {
        String token = getPushControllerInstance(ctx).getDeviceAddress() + ":" + PrefUtils.getClientID(ctx);
        String url = PrefUtils.getServerUrlMetaInfo(ctx);
        if (count == null) {
            count = getHistoryLoadingCount(ctx);
        }
        if (url != null && !url.isEmpty() && !token.isEmpty()) {
            ServiceGenerator.setUrl(url);
            ThreadsApi threadsApi = ServiceGenerator.getThreadsApi();
            Call<HistoryResponse> call = threadsApi.history(token, start, count, AppInfoHelper.getLibVersion());
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
     * @param count количество сообщений для загрузки
     * @param fromBeginning загружать ли историю с начала или с последнего полученного сообщения
     */
    public static HistoryResponse getHistorySync(Context ctx, Long count, boolean fromBeginning) throws Exception {
        return getHistorySync(ctx, fromBeginning ? null : lastLoadId, count);
    }

    public static long getHistoryLoadingCount(Context ctx) {
        return ChatStyle.getInstance().historyLoadingCount;
    }

    public static List<ChatItem> getChatItemFromHistoryResponse(HistoryResponse response) {
        List<ChatItem> list = new ArrayList<>();
        if (response != null) {
            List<MessgeFromHistory> responseList = response.getMessages();
            if (responseList != null) {
                list = IncomingMessageParser.formatNew(responseList);
                setupLastItemIdFromHistory(responseList);
            }
        }
        return list;
    }

    private static void setupLastItemIdFromHistory(List<MessgeFromHistory> list) {
        if (list != null) {
            for (MessgeFromHistory item : list) {
                if (lastLoadId == null) {
                    lastLoadId = item.getBackendId();
                } else if (item.getBackendId() != null && lastLoadId > item.getBackendId()) {
                    lastLoadId = item.getBackendId();
                }
            }
        }
    }
}
