package com.sequenia.appwithchat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.pushserver.android.PushController;
import com.pushserver.android.RequestCallback;
import com.pushserver.android.exception.PushServerErrorException;
import com.sequenia.threads.ChatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
      /*  PushController.getInstance(this).init();
        PushController.getInstance(this).setClientIdAsync("9147088091", new RequestCallback<Void, PushServerErrorException>() {
            @Override
            public void onResult(Void aVoid) {
                Log.e(TAG, "" + aVoid);
            }

            @Override
            public void onError(PushServerErrorException e) {
                Log.e(TAG, "" + e);
            }
        });*/
        setContentView(R.layout.activity_main);
     /*   Fabric.with(this, new Crashlytics());*/
    }

    public void onChatButtonClick(View v) {

        startActivity(ChatActivity.getStartIntent(this));
    }
}
