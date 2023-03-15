package io.edna.threads.demo.appCode.business.mockJsonProvider

class SamplesJsonProvider {
    fun getTextChatJson() = "{\"messages\":[{\"attachments\":[],\"display\":false,\"quickReplies\":[],\"quotes\":[],\"read\":true," +
        "\"receivedDate\":\"2023-03-14T13:11:54.577Z\",\"settings\":{\"blockInput\":false},\"simple\":false,\"text\":\"Hhh\"," +
        "\"threadId\":247,\"type\":\"MESSAGE\",\"uuid\":\"74a00ac4-142a-4984-973e-e09d47eea32f\"},{\"attachments\":[]," +
        "\"display\":false,\"quickReplies\":[],\"quotes\":[],\"read\":true,\"receivedDate\":\"2023-03-14T13:12:01.758Z\"," +
        "\"settings\":{\"blockInput\":false},\"simple\":false,\"text\":\"Ii\",\"threadId\":247,\"type\":\"MESSAGE\"," +
        "\"uuid\":\"451509d2-b128-4554-b01c-0ef3ef39cce4\"},{\"attachments\":[],\"display\":false," +
        "\"operator\":{\"gender\":\"MALE\",\"id\":3,\"name\":\"Оператор0 Иванович\",\"role\":\"OPERATOR\"},\"quickReplies\":[]," +
        "\"quotes\":[],\"read\":true,\"receivedDate\":\"2023-03-14T13:12:50.786Z\",\"settings\":{\"blockInput\":false}," +
        "\"simple\":false,\"text\":\"Привет, детка\",\"threadId\":247,\"type\":\"MESSAGE\"," +
        "\"uuid\":\"cbae7a3c-d4e5-4b0a-86bb-63326328be37\"}]}"

    fun getConnectionErrorJson() = ""

    fun getVoicesChatJson() = ""

    fun getImagesChatJson() = ""

    fun getSystemChatJson() = ""

    fun getChatBotJson() = ""
}
