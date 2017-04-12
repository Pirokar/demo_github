package com.sequenia.appwithchatdev;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Пустой фрагмент для примера использования чата в качестве фрагмента в нижней навигации
 * Created by chybakut2004 on 12.04.17.
 */

public class BottomNavigationHomeFragment extends Fragment {

    public static BottomNavigationHomeFragment newInstance() {
        return new BottomNavigationHomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_navigation_home, container, false);
    }
}
