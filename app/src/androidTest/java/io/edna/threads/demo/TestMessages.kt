package io.edna.threads.demo

object TestMessages {
    const val correlationId = "8112dcad-237f-44b2-af78-c350250ebe8f"

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

    const val registerDeviceWsAnswer = "{\"action\":\"registerDevice\",\"correlationId\":\"$correlationId\"," +
        "\"data\":{\"deviceAddress\":\"gcm_5555555_jzxn0m4r87pz9rp4hx6ugrnq3n64r3so\"}}"

    const val initChatWsAnswer = "{\"action\":\"sendMessage\",\"correlationId\":\"$correlationId\"," +
        "\"data\":{\"messageId\":\"9426bc3c-1194-4499-98e6-2210df4a37e0\",\"sentAt\":\"2023-08-24T12:19:11.480Z\",\"status\":\"sent\"}}"

    const val clientInfoWsAnswer = "{\"action\":\"sendMessage\",\"correlationId\":\"$correlationId\"," +
        "\"data\":{\"messageId\":\"9426bc3c-1194-4499-98e6-2210df4a37e0\",\"sentAt\":\"2023-08-24T12:19:11.480Z\",\"status\":\"sent\"}}"

    const val emptyHistoryMessage = "{\"messages\":[],\"agentInfo\":{\"action\":\"AGENT_LOOKUP\",\"actionDate\":\"2023-03-22T08:51:43.124Z\"," +
        "\"thread\":{\"id\":304,\"state\":\"UNASSIGNED\"},\"agent\":null},\"allowedToSendMessages\":true}"

    const val operatorHelloMessage = "{\"action\":\"getMessages\",\"data\":{\"messages\":[" +
        "{\"messageId\":\"0238fafc-132e-4a7c-87e6-834d50c3a551\",\"sentAt\":\"2023-09-25T13:07:29.213Z\"," +
        "\"notification\":\"New chat message received\",\"content\":{\"type\":\"MESSAGE\",\"clientId\":\"545\"," +
        "\"threadId\":386,\"isPreRegister\":false,\"providerIds\":[],\"uuid\":\"0238fafc-132e-4a7c-87e6-834d50c3a551\"," +
        "\"operator\":{\"id\":4,\"name\":\"Оператор1 Петровна\",\"photoUrl\":\"20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg\"," +
        "\"gender\":\"FEMALE\",\"organizationUnit\":\"Skill Credit\",\"role\":\"OPERATOR\"},\"text\":\"привет!\"," +
        "\"receivedDate\":\"2023-09-25T13:07:29.213Z\",\"attachments\":[],\"quotes\":[],\"quickReplies\":[]," +
        "\"read\":false,\"settings\":{\"blockInput\":false,\"masked\":false},\"authorized\":true," +
        "\"massPushMessage\":false,\"externalClientId\":\"545\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"organizationUnit\\\":\\\"Skill Credit\\\",\\\"clientId\\\":\\\"545\\\"," +
        "\\\"operatorPhotoUrl\\\":\\\"20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg\\\"," +
        "\\\"operatorName\\\":\\\"Оператор1 Петровна\\\",\\\"isPreRegister\\\":\\\"false\\\"," +
        "\\\"aps/sound\\\":\\\"default\\\",\\\"origin\\\":\\\"threads\\\"}\",\"important\":true,\"messageType\":\"NORMAL\"}]}}"

    const val operatorImageMessage = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"9cedfc0d-c3d9-495d-ae27-252ff40f5408\"," +
        "\"sentAt\":\"2099-09-29T10:37:09.120Z\",\"notification\":\"New chat message received\",\"content\":{\"type\":\"MESSAGE\",\"clientId\":\"12345\"," +
        "\"threadId\":141,\"isPreRegister\":false,\"providerIds\":[],\"uuid\":\"9cedfc0d-c3d9-495d-ae27-252ff40f5408\"," +
        "\"operator\":{\"id\":4,\"name\":\"Оператор1 Петровна\",\"photoUrl\":\"20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg\"," +
        "\"gender\":\"FEMALE\",\"organizationUnit\":\"Skill Credit\",\"role\":\"OPERATOR\"},\"text\":\"\"," +
        "\"receivedDate\":\"2099-09-29T10:37:09.120Z\",\"attachments\":[{\"id\":273,\"result\":\"file:///android_asset/test_images/test_image6.jpg\"," +
        "\"optional\":{\"size\":95576,\"name\":\"test_image6.jpg\",\"type\":\"image/jpeg\"},\"state\":\"READY\",\"fileId\":\"test_image6.jpg\"," +
        "\"url\":\"file:///android_asset/test_images/test_image6.jpg\",\"name\":\"test_image6.jpg\",\"type\":\"image/jpeg\",\"size\":95576}]," +
        "\"quotes\":[],\"quickReplies\":[],\"read\":false,\"settings\":{\"blockInput\":false,\"masked\":false},\"authorized\":true," +
        "\"massPushMessage\":false,\"externalClientId\":\"12345\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"organizationUnit\\\":\\\"Skill Credit\\\",\\\"clientId\\\":\\\"12345\\\"," +
        "\\\"operatorPhotoUrl\\\":\\\"file:///android_asset/test_images/test_image6.jpg\\\",\\\"operatorName\\\":\\\"Оператор1 Петровна\\\"," +
        "\\\"isPreRegister\\\":\\\"false\\\",\\\"aps/sound\\\":\\\"default\\\",\\\"origin\\\":\\\"threads\\\"}\"," +
        "\"important\":true,\"messageType\":\"NORMAL\"}]}}"

    const val searchEdnHttpMock = "{\"total\":2,\"pages\":1,\"content\":[{\"type\":\"MESSAGE\",\"threadId\":304,\"providerIds\":[]," +
        "\"uuid\":\"eda36c57-7286-4360-bb42-0cf3d7932113\",\"text\":\"Добро пожаловать в наш чат! А кто такие Edna?\"," +
        "\"receivedDate\":\"2023-10-06T07:53:32.023Z\",\"attachments\":[],\"quotes\":[],\"quickReplies\":[],\"read\":false," +
        "\"settings\":{\"blockInput\":false,\"masked\":false},\"authorized\":true,\"massPushMessage\":false,\"origin\":\"threads\"}," +
        "{\"type\":\"MESSAGE\",\"threadId\":304,\"providerIds\":[],\"uuid\":\"110c8a46-8c38-45df-9949-5f12b7922f01\"," +
        "\"operator\":{\"id\":9,\"name\":\"Оператор5 Фёдоровна\",\"alias\":null,\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"," +
        "\"organizationUnit\":\"Skill Debet\",\"role\":\"OPERATOR\"}," +
        "\"text\":\"Edna – современное решение для построения диалога с клиентом\",\"receivedDate\":\"2023-10-06T07:32:44.260Z\"," +
        "\"attachments\":[],\"quotes\":[],\"quickReplies\":[],\"read\":true,\"settings\":{\"blockInput\":false,\"masked\":false}," +
        "\"authorized\":true,\"massPushMessage\":false,\"origin\":\"threads\"}],\"pageable\":{\"sort\":{\"empty\":false,\"unsorted\":false," +
        "\"sorted\":true},\"offset\":0,\"pageSize\":20,\"pageNumber\":0,\"unpaged\":false,\"paged\":true}}"
}
