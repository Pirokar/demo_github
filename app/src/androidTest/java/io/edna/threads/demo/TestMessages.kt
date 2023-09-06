package io.edna.threads.demo

object TestMessages {
    const val scheduleWsMessage = "{\"action\":\"getMessages\"," +
        "\"data\":{\"messages\":[{\"messageId\":\"66f7c734-3a70-4d12-8fba-efda645c32ef\"," +
        "\"sentAt\":\"2023-08-24T12:19:11.855Z\",\"content\":{\"type\":\"SCHEDULE\"," +
        "\"clientId\":\"5555555\",\"providerIds\":[]," +
        "\"text\":\"{\\\"id\\\":3,\\\"active\\\":true," +
        "\\\"notification\\\":\\\"Война войной, а обед по рассписанию.\\\"," +
        "\\\"intervals\\\":[{\\\"id\\\":15,\\\"weekDay\\\":1,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399},{\\\"id\\\":16,\\\"weekDay\\\":2,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399},{\\\"id\\\":17,\\\"weekDay\\\":3,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399},{\\\"id\\\":18,\\\"weekDay\\\":4,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399},{\\\"id\\\":19,\\\"weekDay\\\":5,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399},{\\\"id\\\":20,\\\"weekDay\\\":6,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399},{\\\"id\\\":21,\\\"weekDay\\\":7,\\\"startTime\\\":0," +
        "\\\"endTime\\\":86399}],\\\"sendDuringInactive\\\":true,\\\"isVisibleOutOfHours\\\":true," +
        "\\\"serverTime\\\":\\\"2023-08-24T12:19:11.756Z\\\"}\"," +
        "\"content\":{\"id\":3,\"active\":true,\"notification\":\"Война войной, а обед по рассписанию.\"," +
        "\"intervals\":[{\"id\":15,\"weekDay\":1,\"startTime\":0,\"endTime\":86399},{\"id\":16,\"weekDay\":2," +
        "\"startTime\":0,\"endTime\":86399},{\"id\":17,\"weekDay\":3,\"startTime\":0,\"endTime\":86399}," +
        "{\"id\":18,\"weekDay\":4,\"startTime\":0,\"endTime\":86399},{\"id\":19,\"weekDay\":5,\"startTime\":0," +
        "\"endTime\":86399},{\"id\":20,\"weekDay\":6,\"startTime\":0,\"endTime\":86399},{\"id\":21,\"weekDay\":7," +
        "\"startTime\":0,\"endTime\":86399}],\"sendDuringInactive\":true,\"isVisibleOutOfHours\":true," +
        "\"serverTime\":\"2023-08-24T12:19:11.756Z\"},\"authorized\":true,\"massPushMessage\":false," +
        "\"externalClientId\":\"5555555\",\"aps/sound\":\"default\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"clientId\\\":\\\"5555555\\\",\\\"aps/content-available\\\":\\\"1\\\",\\\"origin\\\":\\\"threads\\\"}\"," +
        "\"important\":false,\"messageType\":\"NORMAL\"}]}}"

    const val attachmentSettingsWsMessage = "{\"action\":\"getMessages\",\"data\":{\"messages\":" +
        "[{\"messageId\":\"09fa852c-4675-42ca-b9d5-72f0bd009fae\",\"sentAt\":\"2023-08-24T12:19:11.857Z\"," +
        "\"content\":{\"type\":\"ATTACHMENT_SETTINGS\",\"clientId\":\"5555555\",\"providerIds\":[]," +
        "\"content\":{\"maxSize\":30,\"fileExtensions\":" +
        "[\"jpeg\",\"jpg\",\"png\",\"pdf\",\"doc\",\"docx\",\"rtf\",\"bmp\",\"tar.gz\",\"tar\",\"gz\",\"tgz\",\"mp4\",\"webp\",\"webm\",\"tgs\",\"ogg\"]}," +
        "\"authorized\":true,\"massPushMessage\":false,\"externalClientId\":\"5555555\",\"aps/sound\":\"default\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"clientId\\\":\\\"5555555\\\",\\\"aps/sound\\\":\\\"default\\\",\\\"origin\\\":\\\"threads\\\"}\"," +
        "\"important\":false,\"messageType\":\"NORMAL\"}]}}"

    const val registerDeviceWsAnswer = "{\"action\":\"registerDevice\",\"correlationId\":\"3e4983f4-e4aa-42dd-8351-a121c1664675\"," +
        "\"data\":{\"deviceAddress\":\"gcm_5555555_jzxn0m4r87pz9rp4hx6ugrnq3n64r3so\"}}"

    const val initChatWsAnswer = "{\"action\":\"sendMessage\",\"correlationId\":\"8112dcad-237f-44b2-af78-c350250ebe8f\"," +
        "\"data\":{\"messageId\":\"9426bc3c-1194-4499-98e6-2210df4a37e0\",\"sentAt\":\"2023-08-24T12:19:11.480Z\",\"status\":\"sent\"}}"

    const val clientInfoWsAnswer = "{\"action\":\"sendMessage\",\"correlationId\":\"8112dcad-237f-44b2-af78-c350250ebe8f\"," +
        "\"data\":{\"messageId\":\"9426bc3c-1194-4499-98e6-2210df4a37e0\",\"sentAt\":\"2023-08-24T12:19:11.480Z\",\"status\":\"sent\"}}"
}
