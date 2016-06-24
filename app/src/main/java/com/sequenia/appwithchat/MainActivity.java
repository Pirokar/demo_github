package com.sequenia.appwithchat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pushserver.android.PushController;
import com.sequenia.threads.ChatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushController.getInstance(this).init();
        setContentView(R.layout.activity_main);
     /*   Fabric.with(this, new Crashlytics());*/
    }

    public void onChatButtonClick(View v) {

        startActivity(ChatActivity.getStartIntent(this));
    }
}
