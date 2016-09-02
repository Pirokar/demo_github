package com.sequenia.appwithchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sequenia.threads.utils.PermissionChecker;
import com.sequenia.threads.utils.ThreadsInitializer;
import com.sequenia.threads.activities.ChatActivity;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity ";
    private static final int PERM_REQUEST_CODE = 1;
    private static final int PERM_REQUEST_CODE_CLICK = 2;
    EditText mEditText;
    TextView nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View v = findViewById(R.id.version);
        if(null != v && v instanceof TextView){
            ((TextView)v).setText(((TextView) v).getText()+" " + BuildConfig.VERSION_NAME);
        }
        mEditText = (EditText) findViewById(R.id.edit_text);
        nameTextView = (TextView) findViewById(R.id.client_name);
        mEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("edit", null) == null ?
                "79139055742"
                : PreferenceManager.getDefaultSharedPreferences(this).getString("edit", null));
        nameTextView.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("name", null) == null ?
                "Серегй Петрович Иванов"
                : PreferenceManager.getDefaultSharedPreferences(this).getString("name", null));
        boolean isCoarseLocGranted = PermissionChecker.isCoarseLocationPermissionGranted(this);
        boolean isSmsGranted = PermissionChecker.isReadSmsPermissionGranted(this);
        boolean isReadPhoneStateGranted = PermissionChecker.isReadPhoneStatePermissionGranted(this);
        if (isCoarseLocGranted && isSmsGranted && isReadPhoneStateGranted) {
            Log.e(TAG, "initing");
            ThreadsInitializer.getInstance(this).init();
        } else {
            requestPermissionsAndInit(PERM_REQUEST_CODE);
        }
        Fabric.with(this, new Crashlytics());
    }

    public void onChatButtonClick(View v) {

        if (!PermissionChecker.isCoarseLocationPermissionGranted(this)
                || !PermissionChecker.isReadSmsPermissionGranted(this)
                || !PermissionChecker.isReadPhoneStatePermissionGranted(this)) {
            requestPermissionsAndInit(PERM_REQUEST_CODE_CLICK);
        } else if (mEditText.getText().length() > 5) {
            ThreadsInitializer.getInstance(this).init();
            Intent i = ChatActivity
                    .IntentBuilder
                    .getBuilder(this, mEditText.getText().toString())
                    .setDefaultChatTitle(getString(R.string.contact_center))
                    .setClientName(nameTextView.getText().toString())
                    .setPushIconResid(R.drawable.sample_card)
                    .setWelcomeScreenAttrs(R.drawable.logo
                            , "Добро пожаловать"
                            , "мелкий текст мелкий текст мелкий текст мелкий текст мелкий текст мелкий текст мелкий текст мелкий текст мелкий текст"
                            , R.color.green_dark
                            , 18
                            , 14)
                    .build();
            startActivity(i);
        } else if (mEditText.getText().length() < 5) {
            Toast.makeText(this, "client id must have length more than 4 chars", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE || requestCode == PERM_REQUEST_CODE_CLICK) {
            int grantedum = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedum++;
                }
            }
            if (grantedum == grantResults.length) {
                ThreadsInitializer.getInstance(this).init();
                if (requestCode == PERM_REQUEST_CODE_CLICK) {
                    onChatButtonClick(null);
                }
            } else {
                Toast.makeText(this, "Without that permissions, application may not work properly", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void requestPermissionsAndInit(int requestCode) {
        boolean isCoarseLocGranted = PermissionChecker.isCoarseLocationPermissionGranted(this);
        boolean isSmsGranted = PermissionChecker.isReadSmsPermissionGranted(this);
        boolean isReadPhoneStateGranted = PermissionChecker.isReadPhoneStatePermissionGranted(this);
        ArrayList<String> permissions = new ArrayList<>();
        if (!isCoarseLocGranted) permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (!isSmsGranted) permissions.add(Manifest.permission.READ_SMS);
        if (!isReadPhoneStateGranted) permissions.add(Manifest.permission.READ_PHONE_STATE);
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[]{}), requestCode);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("edit", mEditText.getText().toString()).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("name", nameTextView.getText().toString()).apply();
    }
}
