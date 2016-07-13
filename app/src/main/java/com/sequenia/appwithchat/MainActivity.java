package com.sequenia.appwithchat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.sequenia.threads.PermissionChecker;
import com.sequenia.threads.ThreadsInitializer;
import com.sequenia.threads.activities.ChatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity ";
    private static final int PERM_REQUEST_CODE = 1;
    private static final int PERM_REQUEST_CODE_CLICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean isCoarseLocGranted = PermissionChecker.isCoarseLocationPermissionGranted(this);
        boolean isSmsGranted = PermissionChecker.isReadSmsPermissionGranted(this);
        boolean isReadPhoneStateGranted = PermissionChecker.isReadPhoneStatePermissionGranted(this);
        if (isCoarseLocGranted && isSmsGranted && isReadPhoneStateGranted) {
            ThreadsInitializer.getInstance(this).init();
        } else {
            requestPermissionsAndInit(PERM_REQUEST_CODE);
        }

        //   Fabric.with(this, new Crashlytics());*/
    }

    public void onChatButtonClick(View v) {
        if (ThreadsInitializer.getInstance(this).isInited()) {
            startActivity(ChatActivity.getStartIntent(this));
        } else {
            requestPermissionsAndInit(PERM_REQUEST_CODE_CLICK);
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
                    startActivity(ChatActivity.getStartIntent(this));
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
}
