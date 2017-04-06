package com.sequenia.appwithchatdev;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pushserver.android.PushController;

import java.util.ArrayList;

import im.threads.model.ChatStyle;
import im.threads.utils.PermissionChecker;
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
        PushController.getInstance(this).init();
        setContentView(R.layout.activity_main);
        View v = findViewById(R.id.version);
        if (null != v && v instanceof TextView) {
            ((TextView) v).setText(((TextView) v).getText() + " " + BuildConfig.VERSION_NAME);
        }
        mEditText = (EditText) findViewById(R.id.edit_text);
        nameTextView = (TextView) findViewById(R.id.client_name);
//        mEditText.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("edit", null) == null ?
//                "79139055742"
//                : PreferenceManager.getDefaultSharedPreferences(this).getString("edit", null));
//        nameTextView.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("name", null) == null ?
//                "Серегй Петрович Иванов"
//                : PreferenceManager.getDefaultSharedPreferences(this).getString("name", null));
        boolean isCoarseLocGranted = PermissionChecker.isCoarseLocationPermissionGranted(this);
        boolean isSmsGranted = PermissionChecker.isReadSmsPermissionGranted(this);
        boolean isReadPhoneStateGranted = PermissionChecker.isReadPhoneStatePermissionGranted(this);
        boolean isWriteExternalGranted
                = android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
        boolean isAccessNetworkStateGranted
                = android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
        if (isCoarseLocGranted
                && isSmsGranted
                && isReadPhoneStateGranted
                && isWriteExternalGranted
                && isAccessNetworkStateGranted) {
            Log.e(TAG, "initing");
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
            Intent i = ChatStyle
                    .IntentBuilder
                    .getBuilder(this, mEditText.getText().toString(), nameTextView.getText().toString())
                    .setChatTitleStyle(R.string.contact_center,//заголовок ToolBar chatTitleTextResId
                            R.color.toolbar_background,//ToolBar background chatTitleBackgroundColorResId
                            R.color.toolbar_widget,//Toolbar widget chatTitleWidgetsColorResId
                            R.color.status_bar,//status bar chatStatusBarColorResId
                            R.color.menu_item_text,//menu item text menuItemTextColorResId
                            R.color.toolbar_edit_text_hint)//Toolbar EditText hint color chatToolbarHintTextColor
                    .setChatBodyStyle(
                            R.color.chat_background,//фон чата chatBackgroundColor
                            R.color.toolbar_background_transparent,//подсветка выделения элементов chatHighlightingColor
                            R.color.chat_message_hint_input_text,//подсказка в EditText chatMessageHintInputTextColor
                            R.color.chat_message_input_background,//заливка EditText chatMessageInputBackgroundColor
                            R.color.incoming_message_bubble_background,//заливка бабла входящего сообщения incomingMessageBubbleColor
                            R.color.outgoing_message_bubble_background,//заливка бабла исходящего сообщения outgoingMessageBubbleColor
                            R.color.incoming_message_text,//цвет текста входящего сообщения incomingMessageTextColor
                            R.color.outgoing_message_text,//цвет текста исходящего сообщения outgoingMessageTextColor
                            R.color.chatbody_icons_tint,//цвет иконок в поле сообщения chatBodyIconsTint
                            R.color.connection_message_text_color,//цвет текста сообщения о соединениии connectionMessageTextColor
                            R.drawable.blank_avatar_round_main,//аватар по умолчанию входящего сообщения defaultIncomingMessageAvatar
                            R.drawable.blank_avatar_round_main,//заглушка картинки тайпинга imagePlaceholder
                            R.style.FileDialogStyle)//стиль диалога выбора файла fileBrowserDialogStyleResId
//                    .setGoogleAnalyticsEnabled(false)
                    .setPushNotificationStyle(R.drawable.push_icon_def,
                            R.string.default_title,
                            ChatStyle.INVALID,
                            ChatStyle.INVALID)
                    .setWelcomeScreenStyle(
                            R.drawable.welcom_screen_image//логотип экрана приветствия welcomeScreenLogoResId
                            , R.string.welcome//заголовок экрана приветствия welcomeScreenTitleTextResId
                            , R.string.subtitle_text//подзаголовок экрана приветствия welcomeScreenSubtitleTextResId
                            , R.color.welcome_screen_text//цвет текста на экране приветствия welcomeScreenTextColorResId
                            , 18//размер шрифта заголовка titleSizeInSp
                            , 14)//размер шрифта подзаголовка subtitleSizeInSp
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
                if (requestCode == PERM_REQUEST_CODE_CLICK) {
                    onChatButtonClick(null);
                }
            } else {
                Toast.makeText(this, "Without that permissions, application may not work properly", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void requestPermissionsAndInit(int requestCode) {
        Log.e(TAG, "requestPermissionsAndInit");
        boolean isCoarseLocGranted = PermissionChecker.isCoarseLocationPermissionGranted(this);
        boolean isSmsGranted = PermissionChecker.isReadSmsPermissionGranted(this);
        boolean isReadPhoneStateGranted = PermissionChecker.isReadPhoneStatePermissionGranted(this);
        boolean isWriteExternalGranted
                = android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
        boolean isAccessNetworkStateGranted
                = android.support.v4.content.PermissionChecker.checkCallingOrSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
        ArrayList<String> permissions = new ArrayList<>();
        if (!isCoarseLocGranted) permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (!isSmsGranted) permissions.add(Manifest.permission.READ_SMS);
        if (!isReadPhoneStateGranted) permissions.add(Manifest.permission.READ_PHONE_STATE);
        if (!isWriteExternalGranted) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.e(TAG, "isAccessNetworkStateGranted = " + isAccessNetworkStateGranted);
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[]{}), requestCode);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("edit", mEditText.getText().toString()).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("name", nameTextView.getText().toString()).apply();
    }
}