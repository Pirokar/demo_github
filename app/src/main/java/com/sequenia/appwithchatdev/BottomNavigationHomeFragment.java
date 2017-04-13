package com.sequenia.appwithchatdev;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.threads.controllers.ChatController;

/**
 * Пустой фрагмент для примера использования чата в качестве фрагмента в нижней навигации
 * Created by chybakut2004 on 12.04.17.
 */

public class BottomNavigationHomeFragment extends Fragment {

    private TextView unreadMessagesCount;

    public static BottomNavigationHomeFragment newInstance() {
        return new BottomNavigationHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_navigation_home, container, false);
        unreadMessagesCount = (TextView) view.findViewById(R.id.unread_messages_count);

        showUnreadMessagesCount(ChatController.getUnreadMessagesCount(getActivity().getApplicationContext()));

        // Обработка изменения количества непрочитанных в чате сообщений
        ChatController.setUnreadMessagesCountListener(new ChatController.UnreadMessagesCountListener() {
            @Override
            public void onUnreadMessagesCountChanged(int count) {
                showUnreadMessagesCount(count);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ChatController.removeUnreadMessagesCountListener();
    }

    public void showUnreadMessagesCount(int count) {
        unreadMessagesCount.setText(String.valueOf(count));
    }
}
