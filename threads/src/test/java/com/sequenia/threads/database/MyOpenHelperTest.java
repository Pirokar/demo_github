package com.sequenia.threads.database;

import com.sequenia.threads.model.ConsultPhrase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by yuri on 13.09.2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(resourceDir = "src/main/res"
        , manifest = "src/main/AndroidManifest.xml"
        , sdk = 21
        , packageName = "com.sequenia.threads")
public class MyOpenHelperTest {
    MyOpenHelper mMyOpenHelper;

    @Before
    public void setUp() throws Exception {
        mMyOpenHelper = new MyOpenHelper(RuntimeEnvironment.application);
    }

    @Test
    public void testGetUnreadMessagesId() throws Exception {
        ConsultPhrase consultPhrase = new ConsultPhrase(null, null, "", "id1", "", 0L, "sdg", "", false, "");
        ConsultPhrase consultPhrase2 = new ConsultPhrase(null, null, "", "id2", "", 0L, "sdgwet", "", false, "");
        mMyOpenHelper.putChatPhrase(consultPhrase);
        List<String> ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(1, ids.size());
        assertEquals("id1", ids.get(0));
        mMyOpenHelper.setMessageWereRead("id1");
        ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(0, ids.size());
        assertEquals(((ConsultPhrase) mMyOpenHelper.getChatItems(0, -1).get(0)).isRead(), true);

        mMyOpenHelper.putChatPhrase(consultPhrase2);
        ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(1, ids.size());
        assertEquals("id2", ids.get(0));
        mMyOpenHelper.setMessageWereRead("id2");
        ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(0, ids.size());
        assertEquals(((ConsultPhrase) mMyOpenHelper.getChatItems(0, -1).get(0)).isRead(), true);
        assertEquals(((ConsultPhrase) mMyOpenHelper.getChatItems(0, -1).get(1)).isRead(), true);
        ConsultPhrase consultPhrase3 = new ConsultPhrase(null, null, "", "id3", "", 0L, "sdg", "", false, "");
        ConsultPhrase consultPhrase4 = new ConsultPhrase(null, null, "", "id4", "", 0L, "sdgwet", "", false, "");
        mMyOpenHelper.putChatPhrase(consultPhrase3);
        mMyOpenHelper.putChatPhrase(consultPhrase4);
        ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(2, ids.size());
        mMyOpenHelper.setMessageWereRead("id3");
        ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(1, ids.size());
        assertEquals(mMyOpenHelper.getUnreadMessagesId().get(0),"id4");
        mMyOpenHelper.setAllRead();
        ids = mMyOpenHelper.getUnreadMessagesId();
        assertEquals(0, ids.size());
    }
}
