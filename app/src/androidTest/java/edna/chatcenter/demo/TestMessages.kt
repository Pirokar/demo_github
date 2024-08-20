package edna.chatcenter.demo

object TestMessages {
    const val correlationId = "8112dcad-237f-44b2-af78-c350250ebe8f"

    const val registerDeviceWsAnswer = "{\"action\":\"registerDevice\",\"correlationId\":\"$correlationId\"," +
        "\"data\":{\"deviceAddress\":\"gcm_5555555_jzxn0m4r87pz9rp4hx6ugrnq3n64r3so\"}}"

    const val initChatWsAnswer = "{\"action\":\"sendMessage\",\"correlationId\":\"$correlationId\"," +
        "\"data\":{\"messageId\":\"9426bc3c-1194-4499-98e6-2210df4a37e0\",\"sentAt\":\"2023-08-24T12:19:11.480Z\",\"status\":\"sent\"}}"

    const val clientInfoWsAnswer = "{\"action\":\"sendMessage\",\"correlationId\":\"$correlationId\"," +
        "\"data\":{\"messageId\":\"9426bc3c-1194-4499-98e6-2210df4a37e0\",\"sentAt\":\"2023-08-24T12:19:11.480Z\",\"status\":\"sent\"}}"

    const val emptyHistoryMessage = "{\"messages\":[],\"agentInfo\":{\"action\":\"AGENT_LOOKUP\",\"actionDate\":\"2023-03-22T08:51:43.124Z\"," +
        "\"thread\":{\"id\":304,\"state\":\"UNASSIGNED\"},\"agent\":null},\"allowedToSendMessages\":true}"

    const val emptyNoThreadHistoryMessage = "{\"messages\":[],\"agentInfo\":{\"action\":\"AGENT_LOOKUP\",\"actionDate\":\"2023-03-22T08:51:43.124Z\"," +
        "\"state\":\"UNASSIGNED\"},\"agent\":null},\"allowedToSendMessages\":true}"

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

    const val threadIsClosed = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"8377f464-331a-46aa-89d1-7747cf26b7af\"," +
        "\"sentAt\":\"2099-10-10T14:25:30.317Z\",\"notification\":\"Диалог завершен. Будем рады проконсультировать вас снова!\"," +
        "\"content\":{\"type\":\"THREAD_CLOSED\",\"clientId\":\"12345\",\"threadId\":0,\"isPreRegister\":false,\"providerIds\":[]," +
        "\"uuid\":\"8377f464-331a-46aa-89d1-7747cf26b7af\",\"text\":\"Диалог завершен. Будем рады проконсультировать вас снова!\"," +
        "\"receivedDate\":\"2099-10-10T14:25:30.258Z\",\"display\":true,\"authorized\":true,\"massPushMessage\":false," +
        "\"externalClientId\":\"12345\",\"aps/sound\":\"default\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"clientId\\\":\\\"12345\\\",\\\"isPreRegister\\\":\\\"false\\\",\\\"aps/sound\\\":\\\"default\\\"," +
        "\\\"origin\\\":\\\"threads\\\"}\",\"important\":false,\"messageType\":\"NORMAL\"}]}}"

    const val clientIsBlocked = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"ea97c79c-cfb6-49d8-93ee-66b264178313\"," +
        "\"sentAt\":\"2023-10-11T07:36:38.459Z\",\"notification\":\"Вы заблокированы, дальнейшее общение с оператором ограничено\"," +
        "\"content\":{\"type\":\"CLIENT_BLOCKED\",\"clientId\":\"0\",\"isPreRegister\":false,\"providerIds\":[]," +
        "\"text\":\"Вы заблокированы, дальнейшее общение с оператором ограничено\",\"display\":true,\"authorized\":true,\"massPushMessage\":false," +
        "\"externalClientId\":\"12345\",\"aps/sound\":\"default\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"clientId\\\":\\\"12345\\\",\\\"isPreRegister\\\":\\\"false\\\",\\\"aps/sound\\\":\\\"default\\\"," +
        "\\\"origin\\\":\\\"threads\\\"}\",\"important\":true,\"messageType\":\"NORMAL\"}]}}"

    const val operatorTransfer = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"3eb7fe31-b1fe-4e09-b6f9-20594510166e\"," +
        "\"sentAt\":\"2023-10-11T08:28:24.998Z\",\"notification\":\"Для решения вопроса диалог переводится другому оператору\"," +
        "\"content\":{\"type\":\"THREAD_WILL_BE_REASSIGNED\",\"clientId\":\"12345\",\"threadId\":0,\"isPreRegister\":false,\"providerIds\":[]," +
        "\"uuid\":\"3eb7fe31-b1fe-4e09-b6f9-20594510166e\",\"operator\":{\"id\":4,\"name\":\"Оператор1 Петровна\"," +
        "\"photoUrl\":\"20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg\",\"gender\":\"FEMALE\",\"organizationUnit\":\"Skill Credit\"," +
        "\"role\":\"OPERATOR\"},\"text\":\"Для решения вопроса диалог переводится другому оператору\",\"receivedDate\":\"2023-10-11T08:28:24.967Z\"," +
        "\"display\":true,\"authorized\":true,\"massPushMessage\":false,\"externalClientId\":\"12345\",\"aps/sound\":\"default\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"organizationUnit\\\":\\\"Skill Credit\\\",\\\"clientId\\\":\\\"12345\\\"," +
        "\\\"operatorPhotoUrl\\\":\\\"20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg\\\",\\\"operatorName\\\":\\\"Оператор1 Петровна\\\"," +
        "\\\"isPreRegister\\\":\\\"false\\\",\\\"aps/sound\\\":\\\"default\\\",\\\"origin\\\":\\\"threads\\\"}\",\"important\":true," +
        "\"messageType\":\"NORMAL\"}]}}"

    const val operatorAssigned = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"a5976ab7-42a2-49f7-880f-0e380d00f1c6\"," +
        "\"sentAt\":\"2023-10-11T08:28:25.008Z\",\"notification\":\"Вам ответит Оператор0 Иванович\",\"content\":{\"type\":\"OPERATOR_JOINED\"," +
        "\"clientId\":\"12345\",\"threadId\":0,\"isPreRegister\":false,\"providerIds\":[],\"uuid\":\"a5976ab7-42a2-49f7-880f-0e380d00f1c6\"," +
        "\"operator\":{\"id\":3,\"name\":\"Оператор0 Иванович\",\"gender\":\"MALE\",\"organizationUnit\":\"Skill Credit\",\"role\":\"OPERATOR\"}," +
        "\"text\":\"Вам ответит Оператор0 Иванович\",\"receivedDate\":\"2023-10-11T08:28:24.975Z\",\"display\":true,\"authorized\":true," +
        "\"massPushMessage\":false,\"externalClientId\":\"12345\",\"aps/sound\":\"default\",\"origin\":\"threads\"}," +
        "\"attributes\":\"{\\\"organizationUnit\\\":\\\"Skill Credit\\\",\\\"clientId\\\":\\\"12345\\\"," +
        "\\\"operatorName\\\":\\\"Оператор0 Иванович\\\",\\\"isPreRegister\\\":\\\"false\\\",\\\"aps/sound\\\":\\\"default\\\"," +
        "\\\"origin\\\":\\\"threads\\\"}\",\"important\":true,\"messageType\":\"NORMAL\"}]}}"

    const val operatorWaiting = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"76f563a8-f4de-4437-a5fc-6090d35b0b62\"," +
        "\"sentAt\":\"2023-10-11T09:20:11.326Z\",\"notification\":\"Среднее время ожидания ответа составляет 2 минуты\"," +
        "\"content\":{\"type\":\"AVERAGE_WAIT_TIME\",\"clientId\":\"12345\",\"threadId\":0,\"isPreRegister\":false,\"providerIds\":[]," +
        "\"uuid\":\"76f563a8-f4de-4437-a5fc-6090d35b0b62\",\"text\":\"Среднее время ожидания ответа составляет 2 минуты\"," +
        "\"receivedDate\":\"2023-10-11T09:20:11.304Z\",\"display\":true,\"authorized\":true,\"massPushMessage\":false," +
        "\"externalClientId\":\"5522\",\"aps/sound\":\"default\",\"origin\":\"threads\"},\"attributes\":\"{\\\"clientId\\\":\\\"12345\\\"," +
        "\\\"isPreRegister\\\":\\\"false\\\",\\\"aps/sound\\\":\\\"default\\\",\\\"origin\\\":\\\"threads\\\"}\",\"important\":true," +
        "\"messageType\":\"NORMAL\"}]}}"

    const val typingMessage = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"a6102344-5055-4bdc-8ddd-ac57e2875899\"," +
        "\"sentAt\":\"2023-10-11T10:50:25.318Z\",\"content\":{\"type\":\"TYPING\",\"clientId\":\"12345\",\"timestamp\":\"2023-10-11T10:50:25.303Z\"," +
        "\"operatorPhotoUrl\":\"20230703-e8c30f10-aa04-417f-bd97-99544fab26f2.jpg\",\"operatorName\":\"Оператор1 Петровна\",\"isPreRegister\":\"false\"," +
        "\"aps/content-available\":\"1\",\"origin\":\"threads\"},\"important\":false,\"messageType\":\"NORMAL\"}]}}"

    const val defaultConfigMock = "{\"settings\": {\"clientNotificationDisplayType\": \"ALL\",\"hideOnUrls\": []," +
        "\"registerAtFirstStart\": false,\"greetingConfigured\": true,\"messageRetryCount\": 3,\"retryIntervalMillis\": 500," +
        "\"retryMultiplier\": 1.0,\"typingMessagesIntervalSeconds\": 3,\"pingEnabled\": true,\"pingIntervalMillis\": 10000," +
        "\"pingRetries\": 3,\"sendClientInfoAfterMillis\": 5000}," +
        "\"schedule\": {\"content\": {\"type\": \"SCHEDULE\",\"content\": {\"id\": 3,\"active\": true,\"intervals\": [" +
        "{\"id\": 15,\"weekDay\": 1,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 16,\"weekDay\": 2,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 17,\"weekDay\": 3,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 18,\"weekDay\": 4,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 19,\"weekDay\": 5,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 20,\"weekDay\": 6,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 21,\"weekDay\": 7,\"startTime\": 0,\"endTime\": 86399}]," +
        "\"sendDuringInactive\": true,\"isVisibleOutOfHours\": true,\"serverTime\": \"2023-11-21T08:11:22.838Z\"}" +
        "}},\"attachmentSettings\": {\"content\": {\"type\": \"ATTACHMENT_SETTINGS\",\"content\": {" +
        "\"maxSize\": 1,\"fileExtensions\": [\"jpeg\",\"jpg\",\"png\",\"pdf\",\"doc\",\"docx\",\"rtf\",\"bmp\"," +
        "\"ogg\",\"txt\",\"oga\",\"mp3\"]}}}}"

    const val defaultConfigScheduleInactiveCanSendMock = "{\"settings\": {\"clientNotificationDisplayType\": \"ALL\",\"hideOnUrls\": []," +
        "\"registerAtFirstStart\": false,\"greetingConfigured\": true,\"messageRetryCount\": 3,\"retryIntervalMillis\": 500," +
        "\"retryMultiplier\": 1.0,\"typingMessagesIntervalSeconds\": 3,\"pingEnabled\": true,\"pingIntervalMillis\": 10000," +
        "\"pingRetries\": 3,\"sendClientInfoAfterMillis\": 5000}," +
        "\"schedule\": {\"content\": {\"type\": \"SCHEDULE\",\"content\": {\"id\": 3,\"active\": false, \"notification\":\"Война войной, а обед по расписанию.\"," +
        "\"intervals\": [{\"id\": 15,\"weekDay\": 1,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 16,\"weekDay\": 2,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 17,\"weekDay\": 3,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 18,\"weekDay\": 4,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 19,\"weekDay\": 5,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 20,\"weekDay\": 6,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 21,\"weekDay\": 7,\"startTime\": 0,\"endTime\": 86399}]," +
        "\"sendDuringInactive\": true,\"isVisibleOutOfHours\": true,\"serverTime\": \"2023-11-21T08:11:22.838Z\"}" +
        "}},\"attachmentSettings\": {\"content\": {\"type\": \"ATTACHMENT_SETTINGS\",\"content\": {" +
        "\"maxSize\": 1,\"fileExtensions\": [\"jpeg\",\"jpg\",\"png\",\"pdf\",\"doc\",\"docx\",\"rtf\",\"bmp\"," +
        "\"ogg\",\"txt\",\"oga\",\"mp3\"]}}}}"

    const val defaultConfigScheduleInactiveCannotSendMock = "{\"settings\": {\"clientNotificationDisplayType\": \"ALL\",\"hideOnUrls\": []," +
        "\"registerAtFirstStart\": false,\"greetingConfigured\": true,\"messageRetryCount\": 3,\"retryIntervalMillis\": 500," +
        "\"retryMultiplier\": 1.0,\"typingMessagesIntervalSeconds\": 3,\"pingEnabled\": true,\"pingIntervalMillis\": 10000," +
        "\"pingRetries\": 3,\"sendClientInfoAfterMillis\": 5000}," +
        "\"schedule\": {\"content\": {\"type\": \"SCHEDULE\",\"content\": {\"id\": 3,\"active\": false, \"notification\":\"Война войной, а обед по расписанию.\"," +
        "\"intervals\": [{\"id\": 15,\"weekDay\": 1,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 16,\"weekDay\": 2,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 17,\"weekDay\": 3,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 18,\"weekDay\": 4,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 19,\"weekDay\": 5,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 20,\"weekDay\": 6,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 21,\"weekDay\": 7,\"startTime\": 0,\"endTime\": 86399}]," +
        "\"sendDuringInactive\": false,\"isVisibleOutOfHours\": true,\"serverTime\": \"2023-11-21T08:11:22.838Z\"}" +
        "}},\"attachmentSettings\": {\"content\": {\"type\": \"ATTACHMENT_SETTINGS\",\"content\": {" +
        "\"maxSize\": 1,\"fileExtensions\": [\"jpeg\",\"jpg\",\"png\",\"pdf\",\"doc\",\"docx\",\"rtf\",\"bmp\"," +
        "\"ogg\",\"txt\",\"oga\",\"mp3\"]}}}}"

    const val defaultConfigNoSettingsMock = "{\"schedule\": {\"content\": {\"type\": \"SCHEDULE\",\"content\": {\"id\": 3,\"active\": true,\"intervals\": [" +
        "{\"id\": 15,\"weekDay\": 1,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 16,\"weekDay\": 2,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 17,\"weekDay\": 3,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 18,\"weekDay\": 4,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 19,\"weekDay\": 5,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 20,\"weekDay\": 6,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 21,\"weekDay\": 7,\"startTime\": 0,\"endTime\": 86399}]," +
        "\"sendDuringInactive\": true,\"isVisibleOutOfHours\": true,\"serverTime\": \"2023-11-21T08:11:22.838Z\"}" +
        "}},\"attachmentSettings\": {\"content\": {\"type\": \"ATTACHMENT_SETTINGS\",\"content\": {" +
        "\"maxSize\": 1,\"fileExtensions\": [\"jpeg\",\"jpg\",\"png\",\"pdf\",\"doc\",\"docx\",\"rtf\",\"bmp\"," +
        "\"ogg\",\"txt\",\"oga\",\"mp3\"]}}}}"

    const val defaultConfigNoAttachmentSettingsMock = "{\"settings\": {\"clientNotificationDisplayType\": \"ALL\",\"hideOnUrls\": []," +
        "\"registerAtFirstStart\": false,\"greetingConfigured\": true,\"messageRetryCount\": 3,\"retryIntervalMillis\": 500," +
        "\"retryMultiplier\": 1.0,\"typingMessagesIntervalSeconds\": 3,\"pingEnabled\": true,\"pingIntervalMillis\": 10000," +
        "\"pingRetries\": 3,\"sendClientInfoAfterMillis\": 5000}," +
        "\"schedule\": {\"content\": {\"type\": \"SCHEDULE\",\"content\": {\"id\": 3,\"active\": true, \"notification\":\"Война войной, а обед по расписанию.\"," +
        "\"intervals\": [{\"id\": 15,\"weekDay\": 1,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 16,\"weekDay\": 2,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 17,\"weekDay\": 3,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 18,\"weekDay\": 4,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 19,\"weekDay\": 5,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 20,\"weekDay\": 6,\"startTime\": 0,\"endTime\": 86399}," +
        "{\"id\": 21,\"weekDay\": 7,\"startTime\": 0,\"endTime\": 86399}]," +
        "\"sendDuringInactive\": true,\"isVisibleOutOfHours\": true,\"serverTime\": \"2023-11-21T08:11:22.838Z\"}" +
        "}}}"

    const val defaultConfigNoScheduleMock = "{\"settings\": {\"clientNotificationDisplayType\": \"ALL\",\"hideOnUrls\": []," +
        "\"registerAtFirstStart\": false,\"greetingConfigured\": true,\"messageRetryCount\": 3,\"retryIntervalMillis\": 500," +
        "\"retryMultiplier\": 1.0,\"typingMessagesIntervalSeconds\": 3,\"pingEnabled\": true,\"pingIntervalMillis\": 10000," +
        "\"pingRetries\": 3,\"sendClientInfoAfterMillis\": 5000}," +
        "\"attachmentSettings\": {\"content\": {\"type\": \"ATTACHMENT_SETTINGS\",\"content\": {" +
        "\"maxSize\": 1,\"fileExtensions\": [\"jpeg\",\"jpg\",\"png\",\"pdf\",\"doc\",\"docx\",\"rtf\",\"bmp\"," +
        "\"ogg\",\"txt\",\"oga\",\"mp3\"]}}}}"

    const val websocketErrorStringMock = "Unable to resolve host tg.mobile3.chc.dte: No address associated with hostname"

    const val buttonsSurveyMock = "{\"action\":\"getMessages\",\"data\":{\"messages\":[{\"messageId\":\"08cfb72f-b480-401f-bf3c-c8eb2b6815bc\"," +
        "\"sentAt\":\"2024-08-14T11:48:59.050Z\",\"notification\":\"New chat message received\",\"content\":{\"type\":\"BUTTON_SURVEY\"," +
        "\"uuid\":\"08cfb72f-b480-401f-bf3c-c8eb2b6815bc\",\"content\":{\"id\":2,\"uuid\":\"08cfb72f-b480-401f-bf3c-c8eb2b6815bc\"," +
        "\"sendingId\":86,\"hideAfter\":5,\"blockInput\":true,\"questions\":[{\"id\":2,\"sendingId\":86,\"text\":\"Доброжелательность\"," +
        "\"displayText\":\"Доброжелательность\",\"buttons\":[{\"text\":\"Text 1\",\"value\":1},{\"text\":\"Text 2\",\"value\":2}]}," +
        "{\"id\":3,\"sendingId\":86,\"text\":\"Недоброжелательность\",\"displayText\":\"Недоброжелательность\"," +
        "\"buttons\":[{\"text\":\"Text 3-1\",\"value\":1},{\"text\":\"Text 3-2\",\"value\":3},{\"text\":\"Text 3-3\",\"value\":5}," +
        "{\"text\":\"Text 3-4\",\"value\":7},{\"text\":\"Text 3-5\",\"value\":9},{\"text\":\"Text 3-6\",\"value\":11}," +
        "{\"text\":\"Text 3-7\",\"value\":13},{\"text\":\"Text 3-8\",\"value\":15},{\"text\":\"Text 3-9\",\"value\":16}," +
        "{\"text\":\"Text 3-10\",\"value\":18}]}]}},\"important\":true,\"messageType\":\"NORMAL\"}]},\"createdAt\":1723636139109}"
}
