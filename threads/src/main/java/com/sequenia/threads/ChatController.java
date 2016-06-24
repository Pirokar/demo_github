package com.sequenia.threads;

import android.app.Fragment;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sequenia.threads.database.DatabaseHolder;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ConsultConnected;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.SearchingConsult;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by yuri on 08.06.2016.
 * controller for chat Activity. all bells and whistles in activity,
 * all work here, mvc, right?
 * don't forget to unbindActivity() in ChatActivity onDestroy, to avoid leaks;
 */
public class ChatController extends Fragment {
    private static final Handler h = new Handler();
    public static final String TAG = "ChatController ";
    private ChatActivity activity;
    private boolean isSearchingConsult;
    private HashMap<String, ArrayList<ProgressRunnable>> runableMap = new HashMap<>();
    private ArrayList<String> completedDownloads = new ArrayList<>();
    private DatabaseHolder mDatabaseHolder;
    private boolean isConsultFound;


    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDatabaseHolder = DatabaseHolder.getInstance(getActivity());
    }

    public void bindActivity(ChatActivity ca) {
        activity = ca;
    }

    public void unbindActivity() {
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    public boolean isWelcomeScreenShowed() {
        return false;
    }

    public void onUserInput(UpcomingUserMessage upcomingUserMessage) {
        if (upcomingUserMessage == null) return;
        UserPhrase up;
        if (upcomingUserMessage.getAttachments() != null && upcomingUserMessage.getAttachments().size() > 0) {
            up = new UserPhrase(
                    UUID.randomUUID().toString()
                    , upcomingUserMessage.getText()
                    , upcomingUserMessage.getQuote()
                    , System.currentTimeMillis()
                    , upcomingUserMessage.getFileDescription()
                    , upcomingUserMessage.getAttachments().get(0)
            );
        } else {
            up = new UserPhrase(
                    UUID.randomUUID().toString()
                    , upcomingUserMessage.getText()
                    , upcomingUserMessage.getQuote()
                    , System.currentTimeMillis()
                    , upcomingUserMessage.getFileDescription()
                    , null);
        }
        final UserPhrase finalUp = up;
        DatabaseHolder.getInstance(getActivity()).putChatItem(finalUp);
        addMessage(up);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.changeChatPhraseStatus(finalUp.getId(), MessageState.STATE_SENT_AND_SERVER_RECEIVED);
                DatabaseHolder.getInstance(getActivity()).setStateOfUserPhrase(finalUp.getId(), MessageState.STATE_SENT_AND_SERVER_RECEIVED);
            }
        }, 1000);
        if (!isConsultFound && !isSearchingConsult) {
            isSearchingConsult = true;
            isConsultFound = true;
            activity.setTitleStateSearching();
            addMessage(new SearchingConsult(System.currentTimeMillis()));
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.setTitleStateOperatorConnected("Мария Павлова", "оператор");
                    ConsultConnected cc = new ConsultConnected("Мария Павлова"
                            , false
                            , System.currentTimeMillis()
                            , getBigConsultAvatarPath());
                    addMessage(cc);
                    mDatabaseHolder.putChatItem(cc);
                    addMessage(new ConsultTyping(System.currentTimeMillis(), getBigConsultAvatarPath()));
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ConsultPhrase cp = convert(finalUp);
                            mDatabaseHolder.putChatItem(cp);
                            addMessage(cp);
                        }
                    }, 3500);
                }
            }, 5000);
        } else {
            activity.addMessage(new ConsultTyping(System.currentTimeMillis(), getBigConsultAvatarPath()));
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConsultPhrase cp = convert(finalUp);
                    activity.addMessage(cp);
                    mDatabaseHolder.putChatItem(cp);
                }
            }, 3500);
        }
        if (upcomingUserMessage.getAttachments() != null && upcomingUserMessage.getAttachments().size() > 1) {
            for (String str : upcomingUserMessage.getAttachments()) {
                up = new UserPhrase(UUID.randomUUID().toString(), null, null, System.currentTimeMillis(), null, str);
                mDatabaseHolder.putChatItem(up);
            }
        }
    }


    private String getSmallConsultAvatarPath() {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + activity.getPackageName() + "/drawable/" + "sample_consult_avatar_small").toString();
    }

    private String getBigConsultAvatarPath() {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + activity.getPackageName() + "/drawable/" + "sample_consult_avatar_big").toString();
    }

    private ConsultPhrase convert(final UserPhrase up) {
        if (up.isWithQuote()) {
            return new ConsultPhrase(getSmallConsultAvatarPath(), null, System.currentTimeMillis(), up.getPhrase(), UUID.randomUUID().toString(), getConsultName(), up.getQuote(), null);
        } else if (up.isWithFile()) {
            return new ConsultPhrase(getSmallConsultAvatarPath(), up.getFilePath(), System.currentTimeMillis(), up.getPhrase(), UUID.randomUUID().toString(), getConsultName(), null, up.getFileDescription());
        } else {
            return new ConsultPhrase(getSmallConsultAvatarPath(), null, System.currentTimeMillis(), up.getPhrase(), UUID.randomUUID().toString(), getConsultName(), null, null);
        }
    }

    private void addMessage(ChatItem cm) {
        mDatabaseHolder.putChatItem(cm);
        activity.addMessage(cm);
    }

    public String getConsultName() {
        return "Мария Павлова";
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

    public void checkAndResendPhrase(UserPhrase userPhrase) {

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
            activity.updateProgress(path, progress);
        }


    }
}
