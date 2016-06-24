package com.sequenia.threads;

import android.app.Fragment;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pushserver.android.PushController;
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
import java.util.concurrent.atomic.AtomicLong;

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
    private ArrayList<UserPhrase> mUserPhrases = new ArrayList<>();
    private ArrayList<ConsultPhrase> mConsultPhrases = new ArrayList<>();
    private AtomicLong counter = new AtomicLong();
    private boolean isSearchingConsult;
    private HashMap<String, ArrayList<ProgressRunnable>> runableMap = new HashMap<>();
    private ArrayList<String> completedDownloads = new ArrayList<>();


    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

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
                    counter.incrementAndGet()
                    , upcomingUserMessage.getText()
                    , upcomingUserMessage.getQuote()
                    , System.currentTimeMillis()
                    , upcomingUserMessage.getFileDescription()
                    , upcomingUserMessage.getAttachments().get(0)
            );
        } else {
            up = new UserPhrase(
                    counter.incrementAndGet()
                    , upcomingUserMessage.getText()
                    , upcomingUserMessage.getQuote()
                    , System.currentTimeMillis()
                    , upcomingUserMessage.getFileDescription()
                    , null);
        }
        final UserPhrase finalUp = up;
        addMessage(up);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.changeChatPhraseStatus(finalUp.getId(), MessageState.STATE_SENT_AND_SERVER_RECEIVED);
            }
        }, 1000);
        if (mConsultPhrases.size() == 0 && !isSearchingConsult) {
            isSearchingConsult = true;
            activity.setTitleStateSearching();
            addMessage(new SearchingConsult(System.currentTimeMillis()));
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.setTitleStateOperatorConnected("Мария Павлова", "оператор");
                    addMessage(new ConsultConnected("Мария Павлова"
                            , false
                            , System.currentTimeMillis()
                            , getBigConsultAvatarPath()));
                    addMessage(new ConsultTyping(System.currentTimeMillis(), getBigConsultAvatarPath()));
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addMessage(convert(finalUp));
                        }
                    }, 3500);
                }
            }, 5000);
        } else {
            activity.addMessage(new ConsultTyping(System.currentTimeMillis(), getBigConsultAvatarPath()));
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.addMessage(convert(finalUp));
                }
            }, 3500);
        }
        if (upcomingUserMessage.getAttachments() != null && upcomingUserMessage.getAttachments().size() > 1) {
            for (String str : upcomingUserMessage.getAttachments()) {
                up = new UserPhrase(counter.incrementAndGet(), null, null, System.currentTimeMillis(), null, str);
                mUserPhrases.add(up);
            }
        }
    }

    public ConsultPhrase getConsultPhraseById(long id) {
        for (ConsultPhrase cff : mConsultPhrases) {
            if (cff.getMessageId() == id) {
                return cff;
            }
        }
        return null;
    }

    public UserPhrase getUserPhraseById(long id) {
        for (UserPhrase uff : mUserPhrases) {
            if (uff.getMessageId() == id) {
                return uff;
            }

        }
        return null;
    }

    private String getSmallConsultAvatarPath() {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + activity.getPackageName() + "/drawable/" + "sample_consult_avatar_small").toString();
    }

    private String getBigConsultAvatarPath() {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + activity.getPackageName() + "/drawable/" + "sample_consult_avatar_big").toString();
    }

    private ConsultPhrase convert(final UserPhrase up) {
        if (up.isWithQuote()) {
            return new ConsultPhrase(getSmallConsultAvatarPath(), null, System.currentTimeMillis(), up.getPhrase(), counter.incrementAndGet(), getConsultName(), up.getQuote(), null);
        } else if (up.isWithFile()) {
            return new ConsultPhrase(getSmallConsultAvatarPath(), up.getFilePath(), System.currentTimeMillis(), up.getPhrase(), counter.incrementAndGet(), getConsultName(), null, up.getFileDescription());
        } else {
            return new ConsultPhrase(getSmallConsultAvatarPath(), null, System.currentTimeMillis(), up.getPhrase(), counter.incrementAndGet(), getConsultName(), null, null);
        }
    }

    private void addMessage(ChatItem cm) {
        if (cm instanceof UserPhrase) {
            mUserPhrases.add((UserPhrase) cm);
            activity.addMessage(cm);
        } else if (cm instanceof ConsultPhrase) {
            mConsultPhrases.add((ConsultPhrase) cm);
            activity.addMessage(cm);
        } else {
            activity.addMessage(cm);
        }

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
