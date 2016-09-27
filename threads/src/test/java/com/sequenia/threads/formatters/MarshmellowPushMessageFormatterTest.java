package com.sequenia.threads.formatters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.sequenia.threads.BuildConfig;
import com.sequenia.threads.formatters.MarshmellowPushMessageFormatter;
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
import static org.mockito.Mockito.mock;

/**
 * Created by yuri on 12.09.2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml"
        , packageName = "com.sequenia.threads"
        , resourceDir = "src/main/res/"
        , sdk = 23
        , constants = BuildConfig.class
        , qualifiers = "ru"
)
public class MarshmellowPushMessageFormatterTest {
    List<ChatItem> unreadMessages = new ArrayList<>();
    List<ChatItem> incomingPushes = new ArrayList<>();
    MarshmellowPushMessageFormatter mMarshmellowPushMessageFormatter;
    Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = RuntimeEnvironment.application;
    }

    @Test
    public void testGetFormattedImage() throws Exception {
        String consultName = "Татьяна";
        ConsultConnectionMessage connectedFemale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, consultName, false, 0L, "", "", "", "");
        ConsultConnectionMessage connectedMale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, consultName, true, 0L, "", "", "", "");
        ConsultConnectionMessage disconnectedMale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_LEFT, consultName, true, 0L, "", "", "", "");
        FileDescription imageDescr = new FileDescription("", null, 0L, 0L);
        imageDescr.setIncomingName("image.png");
        FileDescription fileDescr = new FileDescription("", null, 0L, 0L);
        fileDescr.setIncomingName("doc.pdf");
        incomingPushes.add(connectedFemale);
        Tuple<Boolean, SpannableStringBuilder> out =
                new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("only 1 span with bold text", 1, out.second.getSpans(0, out.second.length(), Object.class).length);//test that 1 connection message from female show right
        assertTrue("only 1 span with bold text", out.second.getSpans(0, out.second.length(), Object.class)[0] instanceof StyleSpan);
        assertEquals(consultName + ": Подключилась к диалогу", out.second.toString());

        assertEquals(1, incomingPushes.size());
        assertEquals(connectedFemale, unreadMessages.get(0));
        assertEquals(false, out.first);

        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(disconnectedMale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals(1, incomingPushes.size());
        assertEquals(false, out.first);
        assertEquals(consultName + ": Покинул диалог", out.second.toString());
        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(connectedMale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals(consultName + ": Подключился к диалогу", out.second.toString());
        assertEquals(false, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        ConsultPhrase onlyImage = new ConsultPhrase(imageDescr, null, consultName, "", "", 0L, "", "", false, "",false);
        incomingPushes.add(onlyImage);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  Изображение", out.second.toString());
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        unreadMessages.add(connectedFemale);
        incomingPushes.add(onlyImage);

        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image"
                , 2
                , out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  Изображение | Подключилась к диалогу"
                , out.second.toString());
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        ConsultPhrase onlyFile = new ConsultPhrase(fileDescr, null, consultName, "", "", 0L, "", "", false, "",false);
        incomingPushes.add(onlyFile);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  doc.pdf", out.second.toString());
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyFile);
        unreadMessages.add(connectedMale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  doc.pdf | Подключился к диалогу", out.second.toString());
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  5 изображений", out.second.toString());
        assertEquals(true, out.first);

        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        unreadMessages.add(connectedFemale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  5 изображений | Подключилась к диалогу", out.second.toString());
        assertEquals(true, out.first);

        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyFile);
        unreadMessages.add(connectedFemale);

        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  5 файлов | Подключилась к диалогу", out.second.toString());
        assertEquals(true, out.first);

        incomingPushes.clear();
        unreadMessages.clear();

        ConsultPhrase messageWithFile = new ConsultPhrase(null, new Quote("", "", fileDescr, 0L), consultName, "", "phrase", 0L, "", "", false, "",false);
        incomingPushes.add(messageWithFile);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsSpannable();
        assertEquals("must be 2 spans, bold and image", 2, out.second.getSpans(0, out.second.length(), Object.class).length);
        assertEquals(consultName + ":  doc.pdf | phrase", out.second.toString());
        assertEquals(true, out.first);
    }

    @Test
    public void testGetFormattedMessageAsPushContents() throws Exception {
        String consultName = "Татьяна";
        ConsultConnectionMessage connectedFemale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, consultName, false, 0L, "", "", "", "");
        ConsultConnectionMessage connectedMale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_JOINED, consultName, true, 0L, "", "", "", "");
        ConsultConnectionMessage disconnectedMale
                = new ConsultConnectionMessage("", ConsultConnectionMessage.TYPE_LEFT, consultName, true, 0L, "", "", "", "");
        FileDescription imageDescr = new FileDescription("", null, 0L, 0L);
        imageDescr.setIncomingName("image.png");
        FileDescription fileDescr = new FileDescription("", null, 0L, 0L);
        fileDescr.setIncomingName("doc.pdf");

        incomingPushes.add(connectedFemale);
        Tuple<Boolean, MarshmellowPushMessageFormatter.PushContents> out =
                new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "Подключилась к диалогу", false, false), out.second);
        assertEquals(1, incomingPushes.size());
        assertEquals(connectedFemale, unreadMessages.get(0));
        assertEquals(false, out.first);

        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(disconnectedMale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(1, incomingPushes.size());
        assertEquals(false, out.first);
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "Покинул диалог", false, false), out.second);
        incomingPushes.clear();
        unreadMessages.clear();

        ConsultPhrase onlyImage = new ConsultPhrase(imageDescr, null, consultName, "", "", 0L, "", "", false, "",false);
        incomingPushes.add(onlyImage);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "Изображение", true, true), out.second);
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        unreadMessages.add(connectedFemale);
        incomingPushes.add(onlyImage);

        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "Изображение | Подключилась к диалогу", true, true), out.second);
        assertEquals(consultName, out.second.consultName);
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        ConsultPhrase onlyFile = new ConsultPhrase(fileDescr, null, consultName, "", "", 0L, "", "", false, "",false);
        incomingPushes.add(onlyFile);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "doc.pdf", true, false), out.second);
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyFile);
        unreadMessages.add(connectedMale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "doc.pdf | Подключился к диалогу", true, false), out.second);
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "5 изображений", true, true), out.second);
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        unreadMessages.add(connectedFemale);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "5 изображений | Подключилась к диалогу", true, true), out.second);
        assertEquals(true, out.first);

        incomingPushes.clear();
        unreadMessages.clear();

        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyImage);
        incomingPushes.add(onlyFile);
        unreadMessages.add(connectedFemale);

        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "5 файлов | Подключилась к диалогу", true, false), out.second);
        assertEquals(true, out.first);
        incomingPushes.clear();
        unreadMessages.clear();

        ConsultPhrase messageWithFile = new ConsultPhrase(null, new Quote("", "", fileDescr, 0L), consultName, "", "phrase", 0L, "", "", false, "",false);
        incomingPushes.add(messageWithFile);
        out = new MarshmellowPushMessageFormatter(mContext, unreadMessages, incomingPushes).getFormattedMessageAsPushContents();
        assertEquals(new MarshmellowPushMessageFormatter.PushContents(consultName, "doc.pdf | phrase", true, false), out.second);
        assertEquals(true, out.first);
    }
}