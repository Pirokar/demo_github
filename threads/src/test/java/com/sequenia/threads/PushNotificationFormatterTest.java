package com.sequenia.threads;

import android.support.annotation.PluralsRes;

import com.sequenia.threads.controllers.PushNotificationFormatter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by yuri on 01.09.2016.
 */
public class PushNotificationFormatterTest {
    List<ChatItem> unreadMessages = new ArrayList<>();
    List<ChatItem> incomingPushes = new ArrayList<>();
    PushNotificationFormatter mPushNotificationFormatter;

    @Before
    public void setUp() throws Exception {
        mPushNotificationFormatter = new PushNotificationFormatter(
                "подключилась к диалогу"
                , "подключился к диалогу"
                , "покинула диалог"
                , "покинул диалог"
                , "Получен файл"
                , "(с файлом)"
                , "У вас есть непрочитанные сообщения"
                , "(с файлами)");
    }

    @Test
    public void testName() throws Exception {
        ConsultConnectionMessage connectedFemale = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, "Oper1", false, 0L, "", "", "", "");
        ConsultConnectionMessage connectedMale = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, "Oper1", true, 0L, "", "", "", "");
        incomingPushes.add(connectedFemale);
        List<String> out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);
        assertEquals(1, out.size());
        assertEquals("Oper1 подключилась к диалогу", out.get(0));//test that 1 connection message from female show right
        assertEquals(1, incomingPushes.size());
        assertEquals(connectedFemale, unreadMessages.get(0));

        incomingPushes.clear();
        unreadMessages.clear();
        ConsultPhrase onlyFile = new ConsultPhrase(mock(FileDescription.class), null, null, "", null, 0L, "", "", false, "");
        incomingPushes.add(onlyFile);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);
        assertEquals(1, out.size());
        assertEquals("Получен файл", out.get(0));//test that 1 message with only file works
        assertEquals(1, incomingPushes.size());
        assertEquals(onlyFile, unreadMessages.get(0));

        incomingPushes.clear();
        unreadMessages.clear();
        ConsultPhrase MessageWithFile = new ConsultPhrase(mock(FileDescription.class), null, null, "", "phrase", 0L, "", "", false, "");
        incomingPushes.add(MessageWithFile);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);
        assertEquals(1, out.size());
        assertEquals("phrase (с файлом)", out.get(0));//assert that 1 message  with file works
        assertEquals(1, incomingPushes.size());
        assertEquals(MessageWithFile, unreadMessages.get(0));

        incomingPushes.clear();
        unreadMessages.clear();
        incomingPushes.add(connectedMale);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);
        assertEquals(1, out.size());
        assertEquals("Oper1 подключился к диалогу", out.get(0));//test that 1 connection message from male shows right
        assertEquals(1, incomingPushes.size());
        assertEquals(connectedMale, unreadMessages.get(0));


        incomingPushes.clear();
        ConsultPhrase p1 = new ConsultPhrase(null, null, "Oper1", "", "first message from consult", 0L, "", "", false, "");
        incomingPushes.add(p1);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test that several messages shows right
        assertEquals(2, out.size());
        assertEquals("Oper1 подключился к диалогу", out.get(0));
        assertEquals("first message from consult", out.get(1));

        ConsultPhrase p2 = new ConsultPhrase(new FileDescription("", "", 0L, 0L), null, "Oper1", "", "second message from consult", 0L, "", "", false, "");
        incomingPushes.clear();
        incomingPushes.add(p2);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test that several messages shows right with file
        assertEquals(3, out.size());
        assertEquals("Oper1 подключился к диалогу", out.get(0));
        assertEquals("first message from consult", out.get(1));
        assertEquals("second message from consult (с файлом)", out.get(2));

        incomingPushes.clear();
        ConsultPhrase p3 = new ConsultPhrase(new FileDescription("", "", 0L, 0L), null, "Oper1", "", null, 0L, "", "", false, "");
        incomingPushes.add(p3);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test that several messages with files
        assertEquals(4, out.size());
        assertEquals("Oper1 подключился к диалогу", out.get(0));
        assertEquals("first message from consult", out.get(1));
        assertEquals("second message from consult (с файлом)", out.get(2));
        assertEquals("Получен файл", out.get(3));

        ConsultPhrase p4 = new ConsultPhrase(new FileDescription("", "", 0L, 0L), null, "Oper1", "", "fouth consult pharse", 0L, "", "", false, "");
        incomingPushes.clear();
        incomingPushes.add(p4);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test that several messages with files
        assertEquals(5, out.size());
        assertEquals("Oper1 подключился к диалогу", out.get(0));
        assertEquals("first message from consult", out.get(1));
        assertEquals("second message from consult (с файлом)", out.get(2));
        assertEquals("Получен файл", out.get(3));
        assertEquals("fouth consult pharse (с файлом)", out.get(4));

        incomingPushes.clear();
        ConsultPhrase p5 = new ConsultPhrase(new FileDescription("", "", 0L, 0L), null, "Oper1", "", "fifth consult pharse", 0L, "", "", false, "");
        incomingPushes.add(p5);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test 6 ьуыыфпуы works
        assertEquals(1, out.size());
        assertEquals("У вас есть непрочитанные сообщения (с файлами)", out.get(0));

        incomingPushes.clear();
        ConsultPhrase p6 = new ConsultPhrase(new FileDescription("", "", 0L, 0L), null, "Oper1", "", "fifth consult pharse", 0L, "", "", false, "");
        incomingPushes.add(p6);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test that 7 messages works
        assertEquals(1, out.size());
        assertEquals("У вас есть непрочитанные сообщения (с файлами)", out.get(0));

        incomingPushes.clear();
        unreadMessages.clear();
        incomingPushes.add(connectedFemale);
        incomingPushes.add(connectedFemale);
        incomingPushes.add(connectedFemale);
        incomingPushes.add(connectedFemale);
        incomingPushes.add(connectedFemale);
        incomingPushes.add(connectedFemale);
        out = mPushNotificationFormatter.format(unreadMessages, incomingPushes);//test that 7 messages works
        assertEquals(1, out.size());
        assertEquals("У вас есть непрочитанные сообщения", out.get(0));
    }
}
