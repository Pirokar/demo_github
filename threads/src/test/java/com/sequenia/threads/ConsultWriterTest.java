package com.sequenia.threads;

import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.ConsultWriter;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuri on 01.09.2016.
 */
public class ConsultWriterTest {
    ConsultWriter consultWriter;

    @Before
    public void before() {
        consultWriter = new ConsultWriter(new MockSharedPrefs());
    }

    @Test
    public void testConsultWriter() {
        consultWriter.setCurrentConsultInfo(
                new ConsultConnectionMessage("1"
                        , ConsultConnectionMessage.TYPE_JOINED
                        , "Test Operator #0"
                        , false
                        , 1472652267246L
                        , "avatarApth"
                        , "status"
                        , "title"
                        , UUID.randomUUID().toString()));
        assertEquals("1", consultWriter.getCurrentConsultId());
        assertEquals("Test Operator #0", consultWriter.getCurrentConsultName());
        assertEquals("avatarApth", consultWriter.getCurrentAvatarPath());
        assertEquals("status", consultWriter.getCurrentConsultStatus());
        assertEquals("title", consultWriter.getCurrentConsultTitle());
        consultWriter.setCurrentConsultLeft();
        assertEquals(consultWriter.isConsultConnected(), false);
        assertEquals(consultWriter.getCurrentConsultId(), null);
        assertEquals(consultWriter.getName("1"), "Test Operator #0");
        assertEquals(consultWriter.getPhotoUrl("1"), "avatarApth");
        assertEquals(consultWriter.getStatus("1"), "status");
        assertEquals(consultWriter.getConsultTitle("1"), "title");

        assertEquals(null, consultWriter.getCurrentConsultName());
        assertEquals(null, consultWriter.getCurrentAvatarPath());
        assertEquals(null, consultWriter.getCurrentConsultStatus());
        assertEquals(null, consultWriter.getCurrentConsultTitle());

        consultWriter.setCurrentConsultInfo(new ConsultPhrase(null
                , null
                , "Test Operator #1"
                , UUID.randomUUID().toString()
                , ""
                , 0
                , "2"
                , "avatarPath2"
                , false
                , "status2"));
        assertEquals("2", consultWriter.getCurrentConsultId());
        assertEquals("Test Operator #1", consultWriter.getCurrentConsultName());
        assertEquals("avatarPath2", consultWriter.getCurrentAvatarPath());
        assertEquals("status2", consultWriter.getCurrentConsultStatus());
        assertEquals(null, consultWriter.getCurrentConsultTitle());
        consultWriter.setCurrentConsultLeft();

        assertEquals(consultWriter.isConsultConnected(), false);
        assertEquals(consultWriter.getCurrentConsultId(), null);
        assertEquals(consultWriter.getName("1"), "Test Operator #0");
        assertEquals(consultWriter.getPhotoUrl("1"), "avatarApth");
        assertEquals(consultWriter.getStatus("1"), "status");
        assertEquals(consultWriter.getConsultTitle("1"), "title");

        assertEquals(consultWriter.getName("2"), "Test Operator #1");
        assertEquals(consultWriter.getPhotoUrl("2"), "avatarPath2");
        assertEquals(consultWriter.getStatus("2"), "status2");
        assertEquals(consultWriter.getConsultTitle("2"), null);

        consultWriter.setCurrentConsultLeft();
        assertEquals(null, consultWriter.getCurrentConsultName());
        assertEquals(null, consultWriter.getCurrentAvatarPath());
        assertEquals(null, consultWriter.getCurrentConsultStatus());
        assertEquals(null, consultWriter.getCurrentConsultTitle());

        consultWriter.setCurrentConsultInfo(new ConsultConnectionMessage("1"
                , ConsultConnectionMessage.TYPE_JOINED
                , "Test Operator #0"
                , false
                , 1472652267246L
                , "avatarApth"
                , "status"
                , "title"
                , UUID.randomUUID().toString()));
        consultWriter.setCurrentConsultInfo(new ConsultPhrase(null
                , null
                , "Test Operator #1"
                , UUID.randomUUID().toString()//if new consult without connection message enters we erase previous consult title
                , ""
                , 0
                , "2"
                , "avatarPath2"
                , false
                , "status2"));
        assertEquals(null, consultWriter.getCurrentConsultTitle());

        consultWriter.setSearchingConsult(true);
        assertEquals(true, consultWriter.istSearchingConsult());
        consultWriter.setSearchingConsult(false);
        assertEquals(false, consultWriter.istSearchingConsult());
    }
}
