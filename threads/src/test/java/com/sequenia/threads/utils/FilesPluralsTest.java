package com.sequenia.threads.utils;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Created by yuri on 12.09.2016.
 */
@SuppressWarnings("all")
public class FilesPluralsTest {

    @Test
    public void testGetForQuantity() throws Exception {
        PushMessageFormatter.FilesPlurals filesPlurals
                = new PushMessageFormatter.FilesPlurals(Locale.forLanguageTag("ru"));

        assertEquals("файл",filesPlurals.getForQuantity(1));
        assertEquals("файла",filesPlurals.getForQuantity(2));
        assertEquals("файла",filesPlurals.getForQuantity(3));
        assertEquals("файла",filesPlurals.getForQuantity(4));
        assertEquals("файлов",filesPlurals.getForQuantity(5));
        assertEquals("файлов",filesPlurals.getForQuantity(6));
        assertEquals("файлов",filesPlurals.getForQuantity(10));
        assertEquals("файлов",filesPlurals.getForQuantity(11));
        assertEquals("файл",filesPlurals.getForQuantity(21));
        assertEquals("файла",filesPlurals.getForQuantity(22));
        assertEquals("файла",filesPlurals.getForQuantity(24));
        assertEquals("файлов",filesPlurals.getForQuantity(28));
        assertEquals("файлов",filesPlurals.getForQuantity(29));
        assertEquals("файлов",filesPlurals.getForQuantity(30));
        assertEquals("файл",filesPlurals.getForQuantity(31));
        assertEquals("файла",filesPlurals.getForQuantity(32));
        assertEquals("файла",filesPlurals.getForQuantity(34));
        assertEquals("файлов",filesPlurals.getForQuantity(35));

        filesPlurals
                = new PushMessageFormatter.FilesPlurals(Locale.forLanguageTag("en"));
        assertEquals("file",filesPlurals.getForQuantity(1));
        assertEquals("files",filesPlurals.getForQuantity(2));
        assertEquals("files",filesPlurals.getForQuantity(3));
        assertEquals("files",filesPlurals.getForQuantity(4));
        assertEquals("files",filesPlurals.getForQuantity(5));
        assertEquals("files",filesPlurals.getForQuantity(6));
    }
}