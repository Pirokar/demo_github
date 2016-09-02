package com.sequenia.threads;

import android.util.Log;

import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.DateRow;
import com.sequenia.threads.model.Space;
import com.sequenia.threads.model.UserPhrase;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by yuri on 31.08.2016.
 */
public class MessageOrdererTest {
    private static final String TAG = "MessageOrdererTest ";
    ChatAdapter.ChatMessagesOrderer mChatMessagesOrderer = new ChatAdapter.ChatMessagesOrderer();
    List<ChatItem> listToInsertTo = new ArrayList<>();

    @Test
    public void testInsertion() {
        listToInsertTo = new ArrayList<>();
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new UserPhrase(random(), random(), null, 100, null)));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 200, random(), random(), true, random())));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);
        assertEquals(((Space) listToInsertTo.get(2)).getHeight(),24);//spacing between user phrase and consult phrase
        assertTrue(listToInsertTo.get(3) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(3)).isAvatarVisible(), true);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 300, random(), random(), true, random())));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(2) instanceof Space);
        assertTrue(listToInsertTo.get(3) instanceof ConsultPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(3)).isAvatarVisible(), false);
        assertEquals(((ConsultPhrase) listToInsertTo.get(5)).isAvatarVisible(), true);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 250, random(), random(), true, random())));
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
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 350, random(), random(), true, random())));
        assertTrue(listToInsertTo.get(8) instanceof Space);
        assertTrue(listToInsertTo.get(9) instanceof ConsultPhrase);//check insertion of consult phrase before user phrase
        assertTrue(listToInsertTo.get(10) instanceof Space);
        assertTrue(listToInsertTo.get(11) instanceof UserPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultPhrase(null, null, random(), random(), random(), 99, random(), random(), true, random())));
        assertTrue(listToInsertTo.get(0) instanceof DateRow);
        assertTrue(listToInsertTo.get(1) instanceof ConsultPhrase);
        assertEquals(((ConsultPhrase) listToInsertTo.get(1)).isAvatarVisible(), true);
        assertTrue(listToInsertTo.get(2) instanceof Space);//check inserting to start of list
        assertTrue(listToInsertTo.get(3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(4) instanceof Space);
        assertTrue(listToInsertTo.get(5) instanceof ConsultPhrase);
        mChatMessagesOrderer.addAndOrder(listToInsertTo, toList(new ConsultConnectionMessage(random(),ConsultConnectionMessage.TYPE_JOINED,random(),true,450,random(),random(),random(),random())));
        assertTrue(listToInsertTo.get(listToInsertTo.size()-3) instanceof UserPhrase);
        assertTrue(listToInsertTo.get(listToInsertTo.size()-2) instanceof Space);
        assertEquals(((Space) listToInsertTo.get(2)).getHeight(),12);//spacing between user phrase and consult phrase
        assertTrue(listToInsertTo.get(listToInsertTo.size()-1) instanceof ConsultConnectionMessage);
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

    private String random() {
        return UUID.randomUUID().toString();
    }

    private List<ChatItem> toList(ChatItem... item) {
        List<ChatItem> list = new ArrayList<>();
        for (ChatItem ci : item) {
            list.add(ci);
        }
        return list;
    }
}
