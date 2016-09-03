package com.sequenia.threads.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.fragments.QuickAnswerFragment;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.utils.PrefUtils;

/**
 * Created by yuri on 03.09.2016.
 */
public class TranslucentActivity extends AppCompatActivity implements QuickAnswerFragment.OnQuickAnswer {
    @Override
    protected void onResume() {
        super.onResume();
        ChatController.getInstance(this, PrefUtils.getClientID(this)).getLastUnreadConsultPhrase(new CompletionHandler<ConsultPhrase>() {
            @Override
            public void onComplete(ConsultPhrase data) {
                if (null != data) {
                    QuickAnswerFragment fr =
                            QuickAnswerFragment.getInstance(data.getAvatarPath(),
                                    data.getConsultName(),
                                    data.getPhrase());
                    fr.show(getFragmentManager(), null);
                }
            }
            @Override
            public void onError(Throwable e, String message, ConsultPhrase data) {
            }
        });
    }

    @Override
    public void onQuickAnswer(String answer) {
        Intent i = new Intent(ChatActivity.ACTION_SEND_QUICK_MESSAGE);
        i.putExtra(ChatActivity.ACTION_SEND_QUICK_MESSAGE, answer);
        sendBroadcast(i);
        finish();
    }
}


