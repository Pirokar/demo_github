package com.sequenia.threads.formatters;

import android.os.Bundle;

import com.sequenia.threads.utils.MessageMatcher;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.*;

import static org.junit.Assert.*;

/**
 * Created by yuri on 06.09.2016.
 */
public class MessageMatcherTest {

    @Test
    public void testGetType() throws Exception {
        Bundle b = Mockito.mock(Bundle.class);
        when(b.getString("type")).thenReturn("TYPING");
        when(b.getString("sessionKey")).thenReturn("b27d05ae-251b-4f2d-9b8d-39199a4de351");
        when(b.getString("collapse_key")).thenReturn("do_not_collapse");
        when(b.getInt("newMessagesAvailable")).thenReturn(1);
        when(b.getStringArrayList("readInMessageIds")).thenReturn(null);
        Assert.assertEquals(MessageMatcher.TYPE_OPERATOR_TYPING, MessageMatcher.getType(b));

        ArrayList<String> integers = new ArrayList<>(Arrays.asList(new String[]{
                "4211601"
                , "4210501"
                , "4210901"
                , "4211301"
                , "4211801"
                , "4211101"
                , "4211501"
                , "4210701"}));
        Mockito.reset(b);
        when(b.getStringArrayList("readInMessageIds"))
                .thenReturn(integers);
        when(b.containsKey("readInMessageIds")).thenReturn(true);
        assertEquals(MessageMatcher.TYPE_MESSAGES_READ, MessageMatcher.getType(b));
        Mockito.reset(b);
        when(b.getString("readInMessageIds")).thenReturn("1");
        when(b.containsKey("readInMessageIds")).thenReturn(true);
        assertEquals(MessageMatcher.TYPE_MESSAGES_READ, MessageMatcher.getType(b));
    }
}