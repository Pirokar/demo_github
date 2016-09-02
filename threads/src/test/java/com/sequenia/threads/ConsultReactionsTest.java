package com.sequenia.threads;

import com.sequenia.threads.controllers.ConsultMessageReactions;
import com.sequenia.threads.controllers.ConsultMessageReactor;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.ConsultWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuri on 01.09.2016.
 */

public class ConsultReactionsTest {
    ConsultMessageReactor consultMessageReactor;
    MockReactions reactions;
    private ConsultConnectionMessage connectionMessage;
    private ConsultConnectionMessage leftMessage;
    private ConsultPhrase consulPhrase;
    ConsultWriter consultWriter = new ConsultWriter(new MockSharedPrefs());
    private String operatorname1 = "operatorname1";
    private String operatorname2 = "operatorname2";
    private String operatorname3 = "operatorname3";
    private String operatorsid1 = "operatorstatus1";
    private String operatorsid2 = "operatorstatus2";
    private String operatorsid3 = "operatorstatus3";
    private String operatortitle1 = "operatortitle1";
    private String operatortitle2 = "operatortitle2";
    private String operatortitle3 = "operatortitle3";

    @Before
    public void setup() {
        reactions = new MockReactions();
        consultMessageReactor = new ConsultMessageReactor(
                consultWriter
                , reactions);
        connectionMessage = new ConsultConnectionMessage(operatorsid1, ConsultConnectionMessage.TYPE_JOINED, operatorname1, false, 1, ",", "", operatortitle1, "");
        leftMessage = new ConsultConnectionMessage(operatorsid1, ConsultConnectionMessage.TYPE_LEFT, operatorname1, false, 1, ",", "", operatortitle1, "");
        consulPhrase = new ConsultPhrase(null, null, operatorname1, "", "", 0, operatorsid1, "", false, "");
    }

    @Test
    public void testTestReaction() throws Exception {
        consultWriter.setSearchingConsult(true);
        consultMessageReactor.onPushMessage(connectionMessage);
        assertEquals(operatorsid1, reactions.getId());
        assertEquals(operatorname1, reactions.getName());
        assertEquals(operatortitle1, reactions.getTitle());//test inititial consult connection reaction
        assertEquals(operatorsid1, consultWriter.getCurrentConsultId());
        assertEquals(operatorname1, consultWriter.getCurrentConsultName());
        assertEquals(operatortitle1, consultWriter.getCurrentConsultTitle());
        assertEquals(consultWriter.istSearchingConsult(), false);
        assertEquals(consultWriter.isConsultConnected(), true);

        consultMessageReactor.onPushMessage(leftMessage);
        assertEquals(null, reactions.getId());
        assertEquals(null, reactions.getName());
        assertEquals(null, reactions.getTitle());
        assertEquals(consultWriter.istSearchingConsult(), false);
        assertEquals(consultWriter.isConsultConnected(), false);
        assertEquals(null, consultWriter.getCurrentConsultId());
        assertEquals(null, consultWriter.getCurrentConsultName());
        assertEquals(null, consultWriter.getCurrentConsultTitle());


        consultWriter.setSearchingConsult(true);
        consultMessageReactor.onPushMessage(consulPhrase);
        assertEquals(operatorsid1, reactions.getId());
        assertEquals(operatorname1, reactions.getName());
        assertEquals(null, reactions.getTitle());
        assertEquals(operatorsid1, consultWriter.getCurrentConsultId());
        assertEquals(operatorname1, consultWriter.getCurrentConsultName());

        assertEquals(null, consultWriter.getCurrentConsultTitle());
        assertEquals(consultWriter.istSearchingConsult(), false);
        assertEquals(consultWriter.isConsultConnected(), true);

        consulPhrase = new ConsultPhrase(null, null, operatorname2, "", "", 0, operatorsid2, "", false, "");
        consultMessageReactor.onPushMessage(consulPhrase);
        assertEquals(operatorsid2, reactions.getId());
        assertEquals(operatorname2, reactions.getName());
        assertEquals(null, reactions.getTitle());
        assertEquals(operatorsid2, consultWriter.getCurrentConsultId());
        assertEquals(operatorname2, consultWriter.getCurrentConsultName());
        assertEquals(null, consultWriter.getCurrentConsultTitle());
        assertEquals(consultWriter.istSearchingConsult(), false);
        assertEquals(consultWriter.isConsultConnected(), true);

        consultMessageReactor.onPushMessage(leftMessage);
        assertEquals(null, reactions.getId());
        assertEquals(null, reactions.getName());
        assertEquals(null, reactions.getTitle());
        assertEquals(null, consultWriter.getCurrentConsultId());
        assertEquals(null, consultWriter.getCurrentConsultName());
        assertEquals(null, consultWriter.getCurrentConsultTitle());
        assertEquals(consultWriter.istSearchingConsult(), false);
        assertEquals(consultWriter.isConsultConnected(), false);
    }

    class MockReactions implements ConsultMessageReactions {
        private String id;
        private String name;
        private String title;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public void consultConnected(String id, String name, String title) {
            this.id = id;
            this.name = name;
            this.title = title;
        }

        @Override
        public void onConsultLeft() {
            id = null;
            name = null;
            title = null;
        }
    }
}
