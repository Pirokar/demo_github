package com.sequenia.threads.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by yuri on 09.09.2016.
 */
public class ConsultInfoTest {

    @Test
    public void testToJson() throws Exception {
        ConsultInfo consultInfo = new ConsultInfo("Test Operator #1","2","все плохо","http:\\/\\/pushservertest.mfms.ru\\/push-test\\/file\\/download\\/jm7upm65581xkd10l2froa60tqg1z035w8rk");
        assertEquals("{\"photoUrl\":\"http://pushservertest.mfms.ru/push-test/file/download/jm7upm65581xkd10l2froa60tqg1z035w8rk\"," +
                "\"name\":\"Test Operator #1\",\"id\":\"2\"," +
                "\"status\":\"все плохо\"}",consultInfo.toJson().toString().replaceAll("\\\\",""));

    }
}