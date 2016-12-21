package com.sequenia.threads.formatters;

import android.content.Context;

import com.sequenia.threads.BuildConfig;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.utils.Tuple;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by yuri on 14.09.2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml"
        , packageName = "com.sequenia.threads"
        , resourceDir = "src/main/res/"
        , sdk = 23
        , constants = BuildConfig.class
        , qualifiers = "ru"
)
public class NugatMessageFormatterTest {
    List<ChatItem> unreadMessages = new ArrayList<>();
    List<ChatItem> incomingPushes = new ArrayList<>();
    MarshmellowPushMessageFormatter mMarshmellowPushMessageFormatter;
    Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = RuntimeEnvironment.application;
    }

    @Test
    public void testGetFormattedMessageAsPushContents() throws Exception {
        String consultName = "Татьяна";
        ConsultConnectionMessage connectedFemale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, consultName, false, 0L, null, "", "", "");
        ConsultConnectionMessage connectedMale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, consultName, true, 0L, "", "", "", "");
        ConsultConnectionMessage disconnectedMale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_LEFT, consultName, true, 0L, "", "", "", "");
        FileDescription imageDescr = new FileDescription("", null, 0L, 0L);
        imageDescr.setIncomingName("image.png");
        FileDescription fileDescr = new FileDescription("", null, 0L, 0L);
        fileDescr.setIncomingName("doc.pdf");
        ConsultPhrase onlyImage = new ConsultPhrase(imageDescr, null, consultName, "", "", 0L, "", "", false, "", true);
        ConsultPhrase onlyFile = new ConsultPhrase(fileDescr, null, consultName, "", "", 0L, "", "", false, "", false);
        ConsultPhrase messageWithFile = new ConsultPhrase(null, new Quote("", "", fileDescr, 0L), consultName, "", "phrase", 0L, "", "", false, "", false);
        ConsultPhrase onlyMessageFromMale = new ConsultPhrase(null, null, consultName, "", "phrase", 0L, "", "", false, "", true);

        incomingPushes.add(connectedFemale);
        Tuple<Boolean, NugatMessageFormatter.PushContents> out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals("no response on connection message", false, out.first);
        assertEquals(new NugatMessageFormatter.PushContents(consultName, "Подключилась к диалогу", false, false, 0, 1, false, null, null), out.second);

        unreadMessages.clear();
        incomingPushes.clear();

        unreadMessages.add(onlyMessageFromMale);
        incomingPushes.add(connectedFemale);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("2 новых сообщения", "Подключилась к диалогу", false, false, 0, 2, false, null, null), out.second);

        unreadMessages.clear();
        incomingPushes.clear();

        ConsultPhrase withAvatar = new ConsultPhrase(null,null, consultName, "", "phrase", 0L, "", "avatarPath", false, "", false);
        incomingPushes.add(withAvatar);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents(consultName, "phrase", true, false, 0, 1, false,  "avatarPath", null), out.second);

        unreadMessages.clear();
        incomingPushes.clear();

        incomingPushes.add(onlyImage);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислал изображение", "Дотроньтесь для просмотра", false, true, 1, 1, false,  "", imageDescr.getDownloadPath()), out.second);

        unreadMessages.clear();
        incomingPushes.clear();
        ConsultPhrase imageWithText = new ConsultPhrase(imageDescr,null,consultName,"","phrase",0L,"","avatarPath",false,"",true);
        incomingPushes.add(imageWithText);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислал изображение", "phrase", true, true, 1, 1, false,  "avatarPath", imageDescr.getDownloadPath()), out.second);
        unreadMessages.clear();
        incomingPushes.clear();

        unreadMessages.add(onlyImage);
        unreadMessages.add(onlyImage);
        incomingPushes.add(onlyImage);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислал 3 изображения", "Дотроньтесь для просмотра", false, true,3, 3, false,  "", imageDescr.getDownloadPath()), out.second);

        unreadMessages.clear();
        incomingPushes.clear();
        unreadMessages.add(onlyImage);
        unreadMessages.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(imageWithText);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислал 4 изображения", "phrase", true, true,4, 4, false,  "avatarPath", imageDescr.getDownloadPath()), out.second);

        unreadMessages.clear();
        incomingPushes.clear();

        unreadMessages.add(onlyFile);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислала файл", "doc.pdf", false, false,0, 1, true,  "", null), out.second);

        unreadMessages.clear();
        incomingPushes.clear();
        incomingPushes.add(messageWithFile);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислала файл", "phrase", false, false,0, 1, true,  "", null), out.second);


        unreadMessages.clear();
        incomingPushes.clear();
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(true, out.first);
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислала 5 файлов", "Дотроньтесь чтобы скачать", false, false,0, 5, true,  "", null), out.second);

        unreadMessages.clear();
        incomingPushes.clear();
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        unreadMessages.add(onlyFile);
        incomingPushes.add(imageWithText);
        assertEquals(true, out.first);
        out = new NugatMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new NugatMessageFormatter.PushContents("Татьяна прислал 6 файлов", "phrase", true, true,1, 6, true,  "avatarPath", fileDescr.getDownloadPath()), out.second);
    }
}