package com.sequenia.threads;

import android.view.View;
import android.widget.TextView;

import com.advisa.client.api.InOutMessage;
import com.google.common.collect.Lists;
import com.mfms.push.api.DateTime;
import com.pushserver.android.PushMessage;
import com.sequenia.threads.activities.ChatActivity;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.utils.ConsultInfo;
import com.sequenia.threads.utils.MessageFormatter;
import com.sequenia.threads.utils.PrefUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml"
        , packageName = "com.sequenia.threads"
        , sdk = 21
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
        connectionMessage.setShortMessage("Оператор Test Operator #0 присоединился к диалогу");
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_JOINED\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"}}");

        leftMessage = new PushMessage();
        connectionMessage.setSentAt(1472452091338L);
        connectionMessage.setSecured(false);
        connectionMessage.setShortMessage("Оператор Test Operator #0 присоединился к диалогу");
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_JOINED\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"}}");

        consultPushPhrase = new PushMessage();
        consultPushPhrase.setShortMessage("wetwetwte");
        consultPushPhrase.setFullMessage("{\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"},\"text\":\"wetwetwte\",\"receivedDate\":\"2016-08-29T06:28:16Z\",\"attachments\":[],\"quotes\":[]}, sessionKey=fe2ae55b-a02d-420c-9d58-6aa0e765e74c}], readMessageId=[], nextSyncToken=NDAwMDE2MjQ2NTAxLzQwMDAxNjI0NjUwMS80MDAwMTU2ODUxMDE=, needYetAnotherRequest=false}");
        consultPushPhrase.setMessageId(String.valueOf(400016246901L));
        consultPushPhrase.setSentAt(1472452096537L);
    }

    @Test
    public void testIncomingConnectionPushMessages() throws Exception {
        ConsultConnectionMessage consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_JOINED, "Test Operator #0", false, 1472452091338L, null, null, "Оператор");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);

        connectionMessage.setShortMessage("");
        consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_JOINED, "Test Operator #0", false, 1472452091338L, null, null, "");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);

        connectionMessage.setShortMessage("Оператор Test Operator #0 присоединился к диалогу");
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_LEFT\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"MALE\"}}");
        consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_LEFT, "Test Operator #0", true, 1472452091338L, null, null, "Оператор");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);
        connectionMessage.setFullMessage("{\"type\":\"OPERATOR_LEFT\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":\"ебаться-сраться, це ж я!\",\"photoUrl\":null,\"gender\":\"MALE\"}}");
        consultConnectionMessage = new ConsultConnectionMessage(
                "1", ConsultConnectionMessage.TYPE_LEFT, "Test Operator #0", true, 1472452091338L, null, "ебаться-сраться, це ж я!", "Оператор");
        assertEquals(MessageFormatter.format(connectionMessage), consultConnectionMessage);
        before();
    }

    @Test
    public void testIncomingConsultPhrase() throws Exception {
        ConsultPhrase consultPhrase = new ConsultPhrase(null, null, "Test Operator #0", "400016246901", "wetwetwte", 1472452096537L, "1", null, false,null);
        assertEquals(MessageFormatter.format(consultPushPhrase), consultPhrase);
    }

    @Test
    public void testConsultConnectionMessageReaction() throws Exception {
       /* ChatController chatController = new ChatController();
        ChatActivity chatActivity = Robolectric.buildActivity(ChatActivity.class).create().resume().get();
        chatController.bindActivity(chatActivity);
        chatController.onConsultMessage(connectionMessage, chatActivity);
        ConsultConnectionMessage ccm = (ConsultConnectionMessage) MessageFormatter.format(connectionMessage);
        assertEquals(chatActivity.connectedConsultId, ccm.getConsultId());
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
        assertEquals(chatActivity.connectedConsultId, String.valueOf(-1));
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
        ConsultPhrase consultPhrase = new ConsultPhrase(null,null,"Test Operator #0","400016333202","answer",1472468279845L,"1",null,true,null);
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
        consultPhrase = new ConsultPhrase(null,null,"Test Operator #0","400016333202","answer",1472468279845L,"1",null,false,"qwert");
        assertEquals(MessageFormatter.format(Lists.newArrayList(message)), Lists.newArrayList(consultPhrase));
        in.add(message);
        out.add(consultPhrase);

        message = new InOutMessage(false
                , 400016333202L
                , new DateTime(1472468279845L)
                , "{\"type\":\"OPERATOR_LEFT\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":null,\"photoUrl\":null,\"gender\":\"FEMALE\"}}}"
                , true);
        ConsultConnectionMessage consultConnectionMessage = new ConsultConnectionMessage("1",ConsultConnectionMessage.TYPE_LEFT,"Test Operator #0",false,1472468279845L,null,null,null);
        assertEquals(Lists.newArrayList(consultConnectionMessage),MessageFormatter.format(Lists.newArrayList(message)));
        in.add(message);
        out.add(consultConnectionMessage);

        message = new InOutMessage(false
                , 400016333202L
                , new DateTime(1472468279845L)
                , "{\"type\":\"OPERATOR_JOINED\",\"operator\":{\"id\":1,\"name\":\"Test Operator #0\",\"status\":\"qwerty\",\"photoUrl\":\"qwerty\",\"gender\":\"MALE\"}}}"
                , true);
        consultConnectionMessage = new ConsultConnectionMessage("1",ConsultConnectionMessage.TYPE_JOINED,"Test Operator #0",true,1472468279845L,"qwerty","qwerty",null);
        assertEquals(Lists.newArrayList(consultConnectionMessage), MessageFormatter.format(Lists.newArrayList(message)));
        in.add(message);
        out.add(consultConnectionMessage);
        assertEquals(MessageFormatter.format(in),out);
    }
}
