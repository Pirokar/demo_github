package com.sequenia.threads.adapters;

import android.util.Log;

import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.DateRow;
import com.sequenia.threads.model.Space;
import com.sequenia.threads.model.UnreadMessages;
import com.sequenia.threads.model.UserPhrase;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by yuri on 31.08.2016.
 */
public class ChatMessagesOrdererTest {
    private static final String TAG = "MessageOrdererTest ";
    ChatAdapter.ChatMessagesOrderer mChatMessagesOrderer = new ChatAdapter.ChatMessagesOrderer();
    List<ChatItem> listToInsertTo = new ArrayList<>();
    private static AtomicLong mAtomicLong = new AtomicLong(0);

    @Test
    public void testInsertion() {
        listToInsertTo = new ArrayList<>();
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 100, null)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 200, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);
        assertEquals(((Space) listToInsertTo.get(2)).getHeight(), 24);//spacing between user phrase and consult phrase
        assertTrue(listToInsertTo.get(3) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(3)).isAvatarVisible(), true);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 300, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);
        assertTrue(listToInsertTo.get(3) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(3)).isAvatarVisible(), false);
        assertEquals(((ConsultPhrase) listToInsertTo.get(5)).isAvatarVisible(), true);

        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 250, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);//check insertion of consult phrase to the middle of list
        assertTrue(listToInsertTo.get(3) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(6) instanceof Space);
        assertTrue(listToInsertTo.get(7) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(3)).isAvatarVisible(), false);
        assertEquals(((ConsultPhrase) listToInsertTo.get(5)).isAvatarVisible(), false);//check right visibilty. if we insert consult phrase to the middle
        assertEquals(((ConsultPhrase) listToInsertTo.get(7)).isAvatarVisible(), true);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 400, null)));
        assertTrue(listToInsertTo.get(6) instanceof Space);
        assertTrue(listToInsertTo.get(7) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(8) instanceof Space);
        assertTrue(listToInsertTo.get(9) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 350, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(8) instanceof Space);
        assertTrue(listToInsertTo.get(9) instanceof ConsultPhrase);//check insertion of consult phrase before user phrase
        assertTrue(listToInsertTo.get(10) instanceof Space);
        assertTrue(listToInsertTo.get(11) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 99, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(1)).isAvatarVisible(), true);
        assertTrue(listToInsertTo.get(2) instanceof Space);//check inserting to start of list
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultConnectionMessage(random(), ConsultConnectionMessage.TYPE_JOINED, random(), true, 450, random(), random(), random(), random())));
        assertTrue(listToInsertTo.get(listToInsertTo.size() - 3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(listToInsertTo.size() - 2) instanceof Space);
        assertEquals(((Space) listToInsertTo.get(2)).getHeight(), 12);//spacing between user phrase and consult phrase
        assertTrue(listToInsertTo.get(listToInsertTo.size() - 1) instanceof ConsultConnectionMessage);


    }

    @Test
    public void testReverseInsertion() {
        listToInsertTo = new ArrayList<>();
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 100, random(), random(), true, random(), false)));
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 99, random(), random(), true, random(), false)));
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 98, random(), random(), true, random(), false)));
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 97, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(1) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(1)).isAvatarVisible(), false);
        assertEquals(listToInsertTo.get(1).getTimeStamp(), 97);
        assertEquals(((ConsultPhrase) listToInsertTo.get(3)).isAvatarVisible(), false);
        assertEquals(listToInsertTo.get(3).getTimeStamp(), 98);
        assertEquals(((ConsultPhrase) listToInsertTo.get(5)).isAvatarVisible(), false);
        assertEquals(listToInsertTo.get(5).getTimeStamp(), 99);
        assertEquals(((ConsultPhrase) listToInsertTo.get(7)).isAvatarVisible(), true);
        assertEquals(listToInsertTo.get(7).getTimeStamp(), 100);
    }

    @Test
    public void testUnreadMessagesInsertion() {
        listToInsertTo = new ArrayList<>();
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 201, random(), random(), false, random(), false)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UnreadMessages);
        assertTrue(listToInsertTo.get(2) instanceof ConsultPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 202, random(), random(), true, random(), false)));
        assertTrue(listToInsertTo.get(1) instanceof UnreadMessages);
        assertEquals(((UnreadMessages) listToInsertTo.get(1)).getCount(), 2);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 204, random(), random(), false, random(), false)));
        assertEquals(3, ((UnreadMessages) listToInsertTo.get(1)).getCount());
    }

    @Test
    public void testDateInsertionTest() throws Exception {
        listToInsertTo.clear();
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 100, null)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 1472694892339L, null)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof DateRow);
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
    }

    @Test
    public void testaddingConsultIsTyping() {
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 100, null)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultTyping(random(), 120, "")));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);
        assertTrue(listToInsertTo.get(3) instanceof ConsultTyping);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 150, null)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);//test proper localization of typing
        System.out.println(listToInsertTo);
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultTyping);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultTyping(random(), 130, "")));
        assertTrue(listToInsertTo.get(5) instanceof ConsultTyping);//test that only 1 typing item  can exist
        assertEquals(6, listToInsertTo.size());

        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultTyping(random(), 120, "")));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);//test proper localization of typing
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultTyping);

        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, "", random(), "", 160, "", null, true, "", false)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);//test proper localization of typing
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(6) instanceof Space);
        assertTrue(listToInsertTo.get(7) instanceof ConsultTyping);

        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, "", random(), "", 170, "", null, true, "", false)));

        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);//test proper localization of typing
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(6) instanceof Space);
//        assertTrue(listToInsertTo.get(7) instanceof ConsultPhrase);
       // assertTrue(listToInsertTo.get(8) instanceof Space);
     //   assertTrue(listToInsertTo.get(9) instanceof ConsultTyping);

    }

    private String random() {
        return String.valueOf(mAtomicLong.incrementAndGet());
    }

    private List<ChatItem> toList(ChatItem... item) {
        List<ChatItem> list = new ArrayList<>();
        for (ChatItem ci : item) {
            list.add(ci);
        }
        return list;
    }
}
