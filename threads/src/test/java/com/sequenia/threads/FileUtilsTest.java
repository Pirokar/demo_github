package com.sequenia.threads;

import com.sequenia.threads.utils.FileUtils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by yuri on 29.08.2016.
 */
public class FileUtilsTest {

    @Test
    public void testUtils(){
        assertEquals(FileUtils.getExtensionFromFileDescription(null),FileUtils.UNKNOWN);
        assertEquals(FileUtils.getExtensionFromPath(null),FileUtils.UNKNOWN);
    }
}
