package im.threads.internal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import im.threads.ThreadsLib;

public class Config {

    public static Config instance;

    @NonNull
    public final Context context;
    @NonNull
    public final ThreadsLib.PendingIntentCreator pendingIntentCreator;
    @Nullable
    public final ThreadsLib.ShortPushListener shortPushListener;
    @Nullable
    public final ThreadsLib.FullPushListener fullPushListener;
    @Nullable
    public final ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener;

    public Config(@NonNull Context context,
                  @NonNull ThreadsLib.PendingIntentCreator pendingIntentCreator,
                  @Nullable ThreadsLib.ShortPushListener shortPushListener,
                  @Nullable ThreadsLib.FullPushListener fullPushListener,
                  @Nullable ThreadsLib.UnreadMessagesCountListener unreadMessagesCountListener) {
        this.context = context.getApplicationContext();
        this.pendingIntentCreator = pendingIntentCreator;
        this.shortPushListener = shortPushListener;
        this.fullPushListener = fullPushListener;
        this.unreadMessagesCountListener = unreadMessagesCountListener;
    }
}
