package com.sequenia.threads.utils;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Created by yuri on 12.09.2016.
 */
@SuppressWarnings("all")
public class ImagesPluralsTest {

    @Test
    public void testGetForQuantity() throws Exception {
        PushMessageFormatter.ImagesPlurals filesPlurals
                = new PushMessageFormatter.ImagesPlurals(Locale.forLanguageTag("ru"));

        assertEquals("Изображение",filesPlurals.getForQuantity(1));
        assertEquals("изображения",filesPlurals.getForQuantity(2));
        assertEquals("изображения",filesPlurals.getForQuantity(3));
        assertEquals("изображения",filesPlurals.getForQuantity(4));
        assertEquals("изображений",filesPlurals.getForQuantity(5));
        assertEquals("изображений",filesPlurals.getForQuantity(6));
        assertEquals("изображений",filesPlurals.getForQuantity(10));
        assertEquals("изображений",filesPlurals.getForQuantity(11));
        assertEquals("изображение",filesPlurals.getForQuantity(21));
        assertEquals("изображения",filesPlurals.getForQuantity(22));
        assertEquals("изображения",filesPlurals.getForQuantity(24));
        assertEquals("изображений",filesPlurals.getForQuantity(28));
        assertEquals("изображений",filesPlurals.getForQuantity(29));
        assertEquals("изображений",filesPlurals.getForQuantity(30));
        assertEquals("изображение",filesPlurals.getForQuantity(31));
        assertEquals("изображения",filesPlurals.getForQuantity(32));
        assertEquals("изображения",filesPlurals.getForQuantity(34));
        assertEquals("изображений",filesPlurals.getForQuantity(35));

        filesPlurals
                = new PushMessageFormatter.ImagesPlurals(Locale.forLanguageTag("en"));
        assertEquals("image",filesPlurals.getForQuantity(1));
        assertEquals("images",filesPlurals.getForQuantity(2));
        assertEquals("images",filesPlurals.getForQuantity(3));
        assertEquals("images",filesPlurals.getForQuantity(4));
        assertEquals("images",filesPlurals.getForQuantity(5));
        assertEquals("images",filesPlurals.getForQuantity(6));
    }
}