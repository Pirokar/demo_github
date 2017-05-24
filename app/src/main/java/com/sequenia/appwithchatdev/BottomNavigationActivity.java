package com.sequenia.appwithchatdev;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pushserver.android.PushController;

import im.threads.controllers.ChatController;
import im.threads.fragments.ChatFragment;
import im.threads.utils.PermissionChecker;

/**
 * Пример активности с нижней навигацией,
 * где чат выступает в роли одного из пунктов меню.
 *
 * Для использования чата в виде фрагмента
 * нужно создать его экземпляр, вызвав метод ChatFragment.newInstance(Bundle bundle),
 * передав в него Bundle с настройками.
 * Для подробностей смотрите метод showChatFragment.
 *
 * Чтобы корректно обработать навигацию внутри чата,
 * переопределите у Активности метод onBackPressed()
 * и вызовите метод onBackPressed() у ChatFragment,
 * если в данный момент показан он.
 * Метод вернет true, если чат должен быть закрыт.
 * Для подробностей смотрите метод onBackPressed().
 */
public class BottomNavigationActivity extends AppCompatActivity {

    public static final String ARG_CLIENT_ID = "clientId";
    public static final String ARG_USER_NAME = "userName";
    public static final String ARG_NEEDS_SHOW_CHAT = "needsShowChat";

    private static final int PERM_REQUEST_CODE_CLICK = 1;

    private String clientId;
    private String userName;

    private BottomNavigationView navigation;

    private boolean flag;

    /**
     * @return intent для открытия BottomNavigationActivity
     * с передачей clientId и userName.
     */
    public static Intent createIntent(Activity activity, String clientId, String userName) {
        Intent intent = new Intent(activity, BottomNavigationActivity.class);
        intent.putExtra(ARG_CLIENT_ID, clientId);
        intent.putExtra(ARG_USER_NAME, userName);
        return intent;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showHomeFragment();
                    return true;
                case R.id.navigation_chat:
                    // При попытке открыть чат нужно спросить разрешения.
                    if (!PermissionChecker.checkPermissions(BottomNavigationActivity.this)) {
                        PermissionChecker.requestPermissionsAndInit(PERM_REQUEST_CODE_CLICK, BottomNavigationActivity.this);
                        return false;
                    } else {
                        showChatFragment();
                        return true;
                    }
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        // Перед работой с чатом нужно инициализировать библиотеку пушей.
        PushController.getInstance(this).init();

        // При использовании чата в виде фрагмента нужно настроить логику открытия приложения
        // из пуш уведомления, так как по умолчанию открывается ChatActivity.
        ChatController.setPendingIntentCreator(chatWithFragmentPendingIntentCreator());

        Intent intent = getIntent();
        clientId = intent.getStringExtra(ARG_CLIENT_ID);
        userName = intent.getStringExtra(ARG_USER_NAME);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // При открытии Активности из пуш уведомления нужно сразу открыть чат,
        // а не главный экран
        boolean needsShowChat = intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false);
        View view;
        if(needsShowChat) {
            view = navigation.findViewById(R.id.navigation_chat);
        } else {
            view = navigation.findViewById(R.id.navigation_home);
        }
        view.performClick();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.getBooleanExtra(ARG_NEEDS_SHOW_CHAT, false)) {
            View view = navigation.findViewById(R.id.navigation_chat);
            view.performClick();
        }
    }

    /**
     * Показывает Fragment главного экрана с отображением Toolbar
     */
    private void showHomeFragment() {
        ActionBar actionBar = getSupportActionBar();
        Fragment fragment = BottomNavigationHomeFragment.newInstance();
        showFragment(fragment);
        if(actionBar != null) {
            actionBar.show();
        }
    }

    /**
     * Показывает фрагмент Чата, скрывая основной Toolbar.
     * Внутри чата реализован свой Toolbar.
     */
    private void showChatFragment() {
        ActionBar actionBar = getSupportActionBar();
        Bundle bundle = ChatIntentHelper.getIntentBuilder(BottomNavigationActivity.this, clientId, userName).buildBundle();
        ChatFragment chatFragment = ChatFragment.newInstance(bundle);
        showFragment(chatFragment);
        if(actionBar != null) {
            actionBar.hide();
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    /**
     * Здесь необходимо обработать навигацию при нажатии кнопки назад,
     * если в данный момент показан фрагмент чата
     */
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
        if(fragment instanceof ChatFragment) {
            // Если чат нужно закрыть, возвращаем пользователя на предыдущий открытый экран
            boolean needsCloseChat = ((ChatFragment) fragment).onBackPressed();
            if (needsCloseChat) {
                View view = navigation.findViewById(R.id.navigation_home);
                view.performClick();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST_CODE_CLICK) {
            if(PermissionChecker.checkGrantResult(grantResults)) {
                flag = true;
            } else {
                Toast.makeText(this, "Without that permissions, application may not work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ChatController.PendingIntentCreator chatWithFragmentPendingIntentCreator() {
        return new ChatController.PendingIntentCreator() {
            @Override
            public PendingIntent createPendingIntent(Context context) {
                Intent i = new Intent(context, BottomNavigationActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(ARG_NEEDS_SHOW_CHAT, true);
                return PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flag) {
            View view = navigation.findViewById(R.id.navigation_chat);
            view.performClick();
            flag = false;
        }
    }
}
