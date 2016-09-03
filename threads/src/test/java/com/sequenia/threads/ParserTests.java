package com.sequenia.threads;

import com.advisa.client.api.InOutMessage;
import com.google.common.collect.Lists;
import com.mfms.push.api.DateTime;
import com.pushserver.android.PushMessage;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.utils.MessageFormatter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml"
        , packageName = "com.sequenia.threads"
        , resourceDir = "src/main/res/"
        , sdk = 21
        , constants = BuildConfig.class
)
public class ParserTests {
    private PushMessage connectionMessage;
    private PushMessage leftMessage;
    private PushMessage consultPushPhrase;

    @Before
    public void before() {
        connectionMessage = new PushMessage();
        connectionMessage.setMessageId(String.valueOf(400016246501L));
        connectionMessage.setSentAt(1472452091338L);
        connectionMessage.setSecured(false);
        connectionMessage.setMessageId("100500");
        connectionMessage.setShortMessage("Оператор Test Operator #0 присоединился к диалогу");
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_JOINED\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"}}");

        leftMessage = new PushMessage();
        leftMessage.setSentAt(1472452091338L);
        leftMessage.setSecured(false);
        leftMessage.setMessageId("100600");
        leftMessage.setShortMessage("Оператор Test Operator #0 присоединился к диалогу");
        leftMessage.setFullMessage("{\"type\":\"OPERATOR_JOINED\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"}}");

        consultPushPhrase = new PushMessage();
        consultPushPhrase.setShortMessage("wetwetwte");
        consultPushPhrase.setFullMessage("{\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"},\"text\":\"wetwetwte\",\"receivedDate\":\"2016-08-29T06:28:16Z\",\"attachments\":[],\"quotes\":[]}, sessionKey=fe2ae55b-a02d-420c-9d58-6aa0e765e74c}], readMessageId=[], nextSyncToken=NDAwMDE2MjQ2NTAxLzQwMDAxNjI0NjUwMS80MDAwMTU2ODUxMDE=, needYetAnotherRequest=false}");
        consultPushPhrase.setMessageId(String.valueOf(400016246901L));
        consultPushPhrase.setSentAt(1472452096537L);
    }

    @Test
    public void testIncomingConnectionPushMessages() throws Exception {
        ConsultConnectionMessage consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_JOINED, "Test Operator #0", false, 1472452091338L, null, null, "Оператор", "100500");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);

        connectionMessage.setShortMessage("");
        consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_JOINED, "Test Operator #0", false, 1472452091338L, null, null, "", "100500");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);

        connectionMessage.setShortMessage("Оператор Test Operator #0 присоединился к диалогу");
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_LEFT\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"MALE\"}}");
        consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_LEFT, "Test Operator #0", true, 1472452091338L, null, null, "Оператор", "100500");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_LEFT\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":\"ебаться-сраться, це ж я!\",\"photoUrl\":null,\"gender\":\"MALE\"}}");
        connectionMessage.setMessageId("100600");
        consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_LEFT, "Test Operator #0", true, 1472452091338L, null, "ебаться-сраться, це ж я!", "Оператор", "100600");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);

        consultPushPhrase.setMessageId("400017455901");
        consultPushPhrase.setSentAt(1472811701844L);
        consultPushPhrase.setFullMessage("{\"type\":\"ON_HOLD\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":\"Оператор0\",\"photoUrl\":null,\"gender\":\"FEMALE\"},\"text\":\"Оператор готовит ответ, ожидайте\"}, sessionKey=303006ac-fff5-47b2-9d2b-5157c317ce93}]");

        ConsultPhrase phrase = new ConsultPhrase(
                null
                , null
                , "Test Operator #0"
                , "400017455901"
                , "Оператор готовит ответ, ожидайте"
                , 1472811701844L
                , "1"
                , null
                , false
                , "Оператор0");
        assertEquals(phrase, MessageFormatter.format(consultPushPhrase));
        before();

    }

    @Test
    public void testIncomingConsultPhrase() throws Exception {
        ConsultPhrase consultPhrase = new ConsultPhrase(null, null, "Test Operator #0", "400016246901", "wetwetwte", 1472452096537L, "1", null, false, null);
        assertEquals(MessageFormatter.format(consultPushPhrase), consultPhrase);
    }

    @Test
    public void testConsultConnectionMessageReaction() throws Exception {
      /*  ChatController chatController = new ChatController();
       *//* Intent i = ChatActivity.IntentBuilder.getBuilder(RuntimeEnvironment.application, "79139055742")
                .setDefaultChatTitle("Контакт центр")
                .setWelcomeScreenAttrs(R.drawable.logo,"Привет","Пока",R.color.white_dark,15f,10f)
                .setClientName("Кириллов Кирилл Кириллович")
                .build();*//*
        ChatActivity chatActivity = mock(ChatActivity.class);
        try {
            chatController.bindActivity(chatActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        chatController.onConsultMessage(connectionMessage, chatActivity);
        ConsultConnectionMessage ccm = (ConsultConnectionMessage) MessageFormatter.format(connectionMessage);
        wait(120);
        Field f = chatActivity.getClass().getDeclaredField("connectedConsultId");
        f.setAccessible(true);
        String id = (String) f.get(chatActivity);
        assertEquals(id, ccm.getConsultId());
        assertEquals(ConsultInfo.getCurrentConsultId(chatActivity), ccm.getConsultId());
        assertEquals(ConsultInfo.getCurrentConsultName(chatActivity), ccm.getName());
        assertEquals(ConsultInfo.getCurrentConsultPhoto(chatActivity), ccm.getAvatarPath());
        assertEquals(ConsultInfo.getCurrentConsultTitle(chatActivity), ccm.getTitle());
        TextView consultTitleTextView = (TextView) chatActivity.findViewById(R.id.consult_title);
        TextView consultNameTextView = (TextView) chatActivity.findViewById(R.id.consult_name);
        assertEquals(consultTitleTextView.getText(), ccm.getTitle());
        assert consultTitleTextView.getVisibility() == View.VISIBLE;
        assertEquals(consultNameTextView.getText(), ccm.getName());
        assert consultNameTextView.getVisibility() == View.VISIBLE;
        chatController.onConsultMessage(leftMessage, chatActivity);
        wait(120);
        assertEquals(id, String.valueOf(-1));
        assertEquals(ConsultInfo.getCurrentConsultId(chatActivity), null);
        assertEquals(ConsultInfo.getCurrentConsultName(chatActivity), null);
        assertEquals(ConsultInfo.getCurrentConsultPhoto(chatActivity), null);
        assertEquals(ConsultInfo.getCurrentConsultTitle(chatActivity), null);
        assertEquals(consultTitleTextView.getText(), "");
        assert consultTitleTextView.getVisibility() == View.VISIBLE;
        assertEquals(consultNameTextView.getText(), PrefUtils.getDefaultTitle(chatActivity));
        assert consultNameTextView.getVisibility() == View.VISIBLE;*/
    }

    @Test
    public void testHistoryFromServer() throws Exception {
        ArrayList<InOutMessage> in = new ArrayList<>();
        ArrayList<ChatItem> out = new ArrayList<>();
        InOutMessage message = new InOutMessage(true, 3930401L, new DateTime(1472468221733L), "{\"text\":\"request\"}", true);
        UserPhrase userPhrase = new UserPhrase(String.valueOf(3930401L), "request", null, 1472468221733L, null);
        in.add(message);
        out.add(userPhrase);
        ArrayList<InOutMessage> list1 = new ArrayList();
        ArrayList<ChatItem> list2 = new ArrayList();
        assertEquals(MessageFormatter.format(Lists.newArrayList(message)), Lists.newArrayList(userPhrase));
        message = new InOutMessage(false
                , 400016333202L
                , new DateTime(1472468279845L)
                , "{\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"},\"text\":\"answer\",\"receivedDate\":\"2016-08-29T10:57:58Z\",\"attachments\":[],\"quotes\":[]}}"
                , true);
        ConsultPhrase consultPhrase = new ConsultPhrase(null, null, "Test Operator #0", "400016333202", "answer", 1472468279845L, "1", null, true, null);
        in.add(message);
        out.add(consultPhrase);
        list1.add(message);
        list2.add(consultPhrase);
        assertEquals(list2, MessageFormatter.format(list1));
        list1.clear();
        list2.clear();
        message = new InOutMessage(false
                , 400016333202L
                , new DateTime(1472468279845L)
                , "{\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":\"qwert\",\"photoUrl\":null,\"gender\":\"FEMALE\"},\"text\":\"answer\",\"receivedDate\":\"2016-08-29T10:57:58Z\",\"attachments\":[],\"quotes\":[]}}"
                , true);
        consultPhrase = new ConsultPhrase(null, null, "Test Operator #0", "400016333202", "answer", 1472468279845L, "1", null, false, "qwert");
        assertEquals(MessageFormatter.format(Lists.newArrayList(message)), Lists.newArrayList(consultPhrase));
        in.add(message);
        out.add(consultPhrase);

        message = new InOutMessage(false
                , 400016333202L
                , new DateTime(1472468279845L)
                , "{\"type\":\"OPERATOR_LEFT\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"}}}"
                , true);

        ConsultConnectionMessage consultConnectionMessage = new ConsultConnectionMessage("1", ConsultConnectionMessage.TYPE_LEFT, "Test Operator #0", false, 1472468279845L, null, null, null, String.valueOf(400016333202L));
        assertEquals(Lists.newArrayList(consultConnectionMessage), MessageFormatter.format(Lists.newArrayList(message)));
        in.add(message);
        out.add(consultConnectionMessage);

        message = new InOutMessage(false
                , 400016333202L
                , new DateTime(1472468279845L)
                , "{\"type\":\"OPERATOR_JOINED\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":\"qwerty\",\"photoUrl\":\"qwerty\",\"gender\":\"MALE\"}}}"
                , true);
        consultConnectionMessage = new ConsultConnectionMessage("1", ConsultConnectionMessage.TYPE_JOINED, "Test Operator #0", true, 1472468279845L, "qwerty", "qwerty", null, String.valueOf("400016333202"));
        assertEquals(Lists.newArrayList(consultConnectionMessage), MessageFormatter.format(Lists.newArrayList(message)));
        in.add(message);
        out.add(consultConnectionMessage);
        assertEquals(MessageFormatter.format(in), out);
    }
}
