package com.sequenia.threads;

import android.accounts.NetworkErrorException;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnected;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Activity. all bells and whistles in activity,
 * all work here, mvc, right?
 * don't forget to unbindActivity() in ChatActivity onDestroy, to avoid leaks;
 */
public class ChatController extends Fragment {
    private static final Handler h = new Handler(Looper.getMainLooper());
    public static final String TAG = "ChatController ";
    private ChatActivity activity;
    private boolean isSearchingConsult;
    private HashMap<String, ArrayList<ProgressRunnable>> runableMap = new HashMap<>();
    private ArrayList<String> completedDownloads = new ArrayList<>();
    private DatabaseHolder mDatabaseHolder;
    private boolean isConsultFound;
    private Context appContext;


    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void bindActivity(ChatActivity ca) {
        activity = ca;
        appContext = activity.getApplicationContext();
        mDatabaseHolder = DatabaseHolder.getInstance(activity);
        if (mDatabaseHolder.getMessagesCount() > 0) {
            mDatabaseHolder.getChatItemsAsync(0, 1000, new CompletionHandler<List<ChatItem>>() {
                @Override
                public void onComplete(List<ChatItem> data) {
                    if (null != activity) activity.addMessages(data);
                }

                @Override
                public void onError(Throwable e, String message, List<ChatItem> data) {
                }
            });
        }
        isConsultFound = appContext.getSharedPreferences(TAG, Context.MODE_PRIVATE).getBoolean("isConsultFound", false);
        if (isConsultFound) {
            String[] nameAndTitle = getCurrentConsultName().split("%%");
            activity.setTitleStateOperatorConnected(nameAndTitle[0], nameAndTitle[1]);
        }
    }

    public void unbindActivity() {
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public void onUserInput(UpcomingUserMessage upcomingUserMessage) {
        if (upcomingUserMessage.getText().trim().equalsIgnoreCase("Thanks a lot")) {
            cleanAll();
            return;
        }
        if (upcomingUserMessage == null) return;
        postUserPhrase(convert(upcomingUserMessage), 1000, new CompletionHandler<UserPhrase>() {
            @Override
            public void onComplete(UserPhrase data) {
                answerToUser(data, true);
            }

            @Override
            public void onError(Throwable e, String message, UserPhrase data) {
                data.setSentState(MessageState.STATE_NOT_SENT);
            }
        });
    }

    private String getSmallConsultAvatarPath() {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + appContext.getPackageName() + "/drawable/" + "sample_consult_avatar_small").toString();
    }

    private String getBigConsultAvatarPath() {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + appContext.getPackageName() + "/drawable/" + "sample_consult_avatar_big").toString();
    }

    private ConsultPhrase convert(final UserPhrase up) {
        return new ConsultPhrase(getSmallConsultAvatarPath(), System.currentTimeMillis(), up.getPhrase(), UUID.randomUUID().toString(), getCurrentConsultName().split("%%")[0], up.getQuote(), up.getFileDescription());
    }

    private void addMessage(ChatItem cm) {
        mDatabaseHolder.putChatItem(cm);
        if (null != activity) activity.addMessage(cm);
    }

    public String getCurrentConsultName() {
        return "Мария Павлова%%оператор";
    }

    public void onDownloadRequest(final String path) {
        if (completedDownloads.contains(path)) return;
        if (runableMap.containsKey(path)) {
            ArrayList<ProgressRunnable> runnables = runableMap.remove(path);
            for (ProgressRunnable pr : runnables) {
                h.removeCallbacks(pr);
            }
            activity.updateProgress(path, 0);
            return;
        }
        ArrayList<ProgressRunnable> runnables = new ArrayList<>();
        runnables.add(new ProgressRunnable(path, 1));
        runnables.add(new ProgressRunnable(path, 15));
        runnables.add(new ProgressRunnable(path, 40));
        runnables.add(new ProgressRunnable(path, 75));
        runnables.add(new ProgressRunnable(path, 100) {
            @Override
            public void run() {
                super.run();
                completedDownloads.add(getPath());
            }
        });
        runableMap.put(path, runnables);
        h.postDelayed(runnables.get(0), 50);
        h.postDelayed(runnables.get(1), 1000);
        h.postDelayed(runnables.get(2), 2000);
        h.postDelayed(runnables.get(3), 3500);
        h.postDelayed(runnables.get(4), 4000);
    }

    public void checkAndResendPhrase(final UserPhrase userPhrase) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMessageState(userPhrase, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                    answerToUser(userPhrase, true);
                }
            }, 2000);
        }
    }

    private class ProgressRunnable implements Runnable {
        private String path;
        private int progress;

        public ProgressRunnable(String path, int progress) {
            this.path = path;
            this.progress = progress;
        }

        public String getPath() {
            return path;
        }

        @Override
        public void run() {
            if (null != activity)
                activity.updateProgress(path, progress);
        }
    }

    private void cleanAll() {
        mDatabaseHolder.cleanDatabase();
        activity.cleanChat();
        isConsultFound = false;
        isSearchingConsult = false;
        h.removeCallbacksAndMessages(null);
        runableMap = new HashMap<>();
        completedDownloads = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sp = appContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        sp.edit().putBoolean("isConsultFound", isConsultFound).apply();
    }

    private void answerToUser(final UserPhrase up, final boolean showIsTyping) {
        if (up.getSentState() == MessageState.STATE_SENT_AND_SERVER_RECEIVED) {
            if (!isSearchingConsult && !isConsultFound) {
                isSearchingConsult = true;
                isConsultFound = true;
                activity.setTitleStateSearching();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String[] nameAndTitle = getCurrentConsultName().split("%%");
                        if (activity != null) {
                            activity.setTitleStateOperatorConnected(nameAndTitle[0], nameAndTitle[1]);
                        }
                        ConsultConnected cc = new ConsultConnected(nameAndTitle[0]
                                , false
                                , System.currentTimeMillis()
                                , getBigConsultAvatarPath());
                        addMessage(cc);
                        if (showIsTyping)
                            addMessage(new ConsultTyping(System.currentTimeMillis(), getBigConsultAvatarPath()));
                        postConsultPhrase(up, 2000, null);
                    }
                }, 3500);
            } else {
                if (showIsTyping) {
                    activity.addMessage(new ConsultTyping(System.currentTimeMillis(), getBigConsultAvatarPath()));
                }
                postConsultPhrase(up, 2000, null);
            }
        }
    }

    private void postConsultPhrase(final UserPhrase up, long delay, final CompletionHandler<ConsultPhrase> handler) {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConsultPhrase cp = convert(up);
                addMessage(cp);
                if (handler == null) return;
                handler.setSuccessful(true);
                handler.onComplete(cp);

            }
        }, delay);
    }

    private void postUserPhrase(final UserPhrase up, long sendDelay, final CompletionHandler<UserPhrase> handler) {
        addMessage(up);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDatabaseHolder.getMessagesCount() % 2 == 0) {
                    setMessageState(up, MessageState.STATE_NOT_SENT);
                    if (handler == null) return;
                    handler.setSuccessful(false);
                    handler.onError(new NetworkErrorException("no connection"), "check internet connection", up);
                } else {
                    setMessageState(up, MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                    if (handler == null) return;
                    handler.setSuccessful(true);
                    handler.onComplete(up);
                }
            }
        }, sendDelay);
    }

    private void setMessageState(UserPhrase up, MessageState messageState) {
        up.setSentState(messageState);
        if (activity != null) {
            activity.setPhraseSentStatus(up.getId(), up.getSentState());
        }
        mDatabaseHolder.setStateOfUserPhrase(up.getId(), up.getSentState());
    }

    private UserPhrase convert(UpcomingUserMessage message) {
        if (message == null)
            return new UserPhrase(UUID.randomUUID().toString(), "", null, System.currentTimeMillis(), null);
        return new UserPhrase(UUID.randomUUID().toString(), message.getText(), message.getQuote(), System.currentTimeMillis(), message.getFileDescription());
    }
}
