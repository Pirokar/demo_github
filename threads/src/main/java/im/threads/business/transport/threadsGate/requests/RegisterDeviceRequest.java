package im.threads.business.transport.threadsGate.requests;

import im.threads.business.transport.threadsGate.Action;

public final class RegisterDeviceRequest extends BaseRequest<RegisterDeviceRequest.Data> {

    public RegisterDeviceRequest(String correlationId, Data data) {
        super(Action.REGISTER_DEVICE, correlationId, data);
    }

    public static final class Data {

        private final String appPackage;

        private final String appVersion;

        private final String providerUid;

        private final String pnsPushAddress;

        private final String deviceUid;

        private final String osName;

        private final String osVersion;

        private final String locale;

        private final String timeZone;

        private final String deviceName;

        private final String deviceModel;

        private final String deviceAddress;

        private final String clientId;

        public Data(String appPackage,
                    String appVersion,
                    String providerUid,
                    String pnsPushAddress,
                    String deviceUid,
                    String osName,
                    String osVersion,
                    String locale,
                    String timeZone,
                    String deviceName,
                    String deviceModel,
                    String deviceAddress,
                    String clientId) {
            this.appPackage = appPackage;
            this.appVersion = appVersion;
            this.providerUid = providerUid;
            this.pnsPushAddress = pnsPushAddress;
            this.deviceUid = deviceUid;
            this.osName = osName;
            this.osVersion = osVersion;
            this.locale = locale;
            this.timeZone = timeZone;
            this.deviceName = deviceName;
            this.deviceModel = deviceModel;
            this.deviceAddress = deviceAddress;
            this.clientId = clientId;
        }
    }
}
