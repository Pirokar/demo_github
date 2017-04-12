package com.sequenia.appwithchatdev;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import im.threads.fragments.ChatFragment;

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

    private String clientId;
    private String userName;

    private BottomNavigationView navigation;

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
                    showChatFragment();
                    return true;
            }
            return false;
        }
    };

    private void showHomeFragment() {
        ActionBar actionBar = getSupportActionBar();
        Fragment fragment = BottomNavigationHomeFragment.newInstance();
        showFragment(fragment);
        if(actionBar != null) {
            actionBar.show();
        }
    }

    private void showChatFragment() {
        ActionBar actionBar = getSupportActionBar();
        Bundle bundle = MainActivity.getBundleBuilder(BottomNavigationActivity.this, clientId, userName).buildBundle();
        ChatFragment chatFragment = ChatFragment.newInstance(bundle);
        showFragment(chatFragment);
        if(actionBar != null) {
            actionBar.hide();
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        clientId = intent.getStringExtra(ARG_CLIENT_ID);
        userName = intent.getStringExtra(ARG_USER_NAME);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        View view = navigation.findViewById(R.id.navigation_home);
        view.performClick();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content);
        if(fragment instanceof ChatFragment) {
            boolean needsCloseChat = ((ChatFragment) fragment).onBackPressed();
            if (needsCloseChat) {
                View view = navigation.findViewById(R.id.navigation_home);
                view.performClick();
            }
        } else {
            super.onBackPressed();
        }
    }
}
