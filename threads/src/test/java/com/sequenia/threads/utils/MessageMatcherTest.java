package com.sequenia.threads.utils;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
        assertEquals(MessageMatcher.TYPE_OPERATOR_TYPING, MessageMatcher.getType(b));
    }
}