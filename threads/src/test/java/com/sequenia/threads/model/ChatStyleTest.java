package com.sequenia.threads.model;

import android.content.Context;
import android.content.Intent;

import com.sequenia.threads.BuildConfig;
import com.sequenia.threads.utils.PrefUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Created by yuri on 10.11.2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml"
        , packageName = "com.sequenia.threads"
        , resourceDir = "src/main/res/"
        , sdk = 21
        , constants = BuildConfig.class
)
public class ChatStyleTest {
    @Test
    public void styleFromIntent() throws Exception {
        Context ctx = RuntimeEnvironment.application;
        ChatStyle.IntentBuilder builder = ChatStyle.IntentBuilder.getBuilder(ctx, "id", "name");
        Intent i = builder
                .setChatBodyStyle(1, 26, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
                .setChatTitleStyle(12, 13, 14, 15)
                .setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(16, 17, 18, 19)
                .setWelcomeScreenStyle(20, 21, 22, 23, 24, 25)
                .build();
        ChatStyle manual = new ChatStyle(1,2,26, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, false, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);
        assertEquals(manual, ChatStyle.styleFromIntent(i));
        builder = ChatStyle.IntentBuilder.getBuilder(ctx, "id", "name");
        i = builder
                .setChatTitleStyle(12, 13, 14, 15)
                .setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(16, 17, 18, 19)
                .setWelcomeScreenStyle(20, 21, 22, 23, 24, 25)
                .build();
        manual = new ChatStyle(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 12, 13, 14, 15, false, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);
        assertEquals(manual, ChatStyle.styleFromIntent(i));

        builder = ChatStyle.IntentBuilder.getBuilder(ctx, "id", "name");
        i = builder
                .setGoogleAnalyticsEnabled(false)
                .setPushNotificationStyle(16, 17, 18, 19)
                .setWelcomeScreenStyle(20, 21, 22, 23, 24, 25)
                .build();
        manual = new ChatStyle(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, false, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);
        assertEquals(manual, ChatStyle.styleFromIntent(i));

        builder = ChatStyle.IntentBuilder.getBuilder(ctx, "id", "name");
        i = builder
                .setWelcomeScreenStyle(20, 21, 22, 23, 24, 25)
                .build();
        manual = new ChatStyle(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, true, -1, -1, -1, -1, 20, 21, 22, 23, 24, 25);
        assertEquals(manual, ChatStyle.styleFromIntent(i));

        builder = ChatStyle.IntentBuilder.getBuilder(ctx, "id", "name");
        i = builder
                .setWelcomeScreenStyle(20, 21, 22, 23, 24, 25)
                .build();
        manual = new ChatStyle(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, true, -1, -1, -1, -1, 20, 21, 22, 23, 24, 25);
        assertEquals(manual, ChatStyle.styleFromIntent(i));

        builder = ChatStyle.IntentBuilder.getBuilder(ctx, "id", "name");
        i = builder
                .build();
        manual = new ChatStyle(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, true, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
        assertEquals(manual, ChatStyle.styleFromIntent(i));
    }

    @Test
    public void testDeserialize() {
        Context ctx = RuntimeEnvironment.application;
        ChatStyle style = new ChatStyle(1, 26, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, false, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25);
        PrefUtils.setIncomingStyle(ctx, style);
        assertEquals(style, PrefUtils.getIncomingStyle(ctx));
    }

}