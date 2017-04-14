package com.sequenia.appwithchatdev;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pushserver.android.PushController;

import im.threads.controllers.ChatController;
import im.threads.utils.PermissionChecker;
import io.fabric.sdk.android.Fabric;

/**
 * Активность с примерами открытия чата:
 * - в виде новой Активности
 * - в виде активности, где чат выступает в качестве фрагмента
 */
public class MainActivity extends AppCompatActivity {

    private static final int CHAT_PERMISSIONS_REQUEST_CODE = 1;

    private EditText clientIdEditText;
    private EditText userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Перед работой с чатом должна быть настроена библиотека пуш уведомлений
        PushController.getInstance(this).init();

        setContentView(R.layout.activity_main);

        TextView versionView = (TextView) findViewById(R.id.version);
        versionView.setText(versionView.getText() + " " + BuildConfig.VERSION_NAME);

        clientIdEditText = (EditText) findViewById(R.id.client_id);
        userNameEditText = (EditText) findViewById(R.id.user_name);

        Fabric.with(this, new Crashlytics());
    }

    public void onChatButtonClick(View v) {
        showChatAsActivity();
    }

    public void onFragmentChatButtonClick(View v) {
        showChatAsFragment();
    }

    /**
     * Пример открытия чата в виде Активности
     */
    private void showChatAsActivity() {
        String clientId = clientIdEditText.getText().toString();
        String userName = userNameEditText.getText().toString();

        if (clientId.length() < 5) {
            Toast.makeText(this, "client id must have length more than 4 chars", Toast.LENGTH_SHORT).show();
            return;
        }

        // При открытии чата нужно проверить, выданы ли необходимые разрешения.
        if (!PermissionChecker.checkPermissions(this)) {
            PermissionChecker.requestPermissionsAndInit(CHAT_PERMISSIONS_REQUEST_CODE, this);
        } else {
            Intent i = ChatBundleHelper.getBundleBuilder(this, clientId, userName).build();
            startActivity(i);
        }
    }

    /**
     * Пример открытя чата в виде фрагмента
     */
    private void showChatAsFragment() {
        String clientId = clientIdEditText.getText().toString();
        String userName = userNameEditText.getText().toString();

        if (clientId.length() < 5) {
            Toast.makeText(this, "client id must have length more than 4 chars", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = BottomNavigationActivity.createIntent(this, clientId, userName);
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CHAT_PERMISSIONS_REQUEST_CODE) {
            if(PermissionChecker.checkGrantResult(grantResults)) {
                showChatAsActivity();
            } else {
                Toast.makeText(this, "Without that permissions, application may not work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
