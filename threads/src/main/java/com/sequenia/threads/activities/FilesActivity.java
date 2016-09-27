package com.sequenia.threads.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sequenia.threads.AnalyticsTracker;
import com.sequenia.threads.R;
import com.sequenia.threads.adapters.FilesAndMediaAdapter;
import com.sequenia.threads.controllers.FilesAndMediaController;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.utils.PrefUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yuri on 01.07.2016.
 */
public class FilesActivity extends BaseActivity implements FilesAndMediaAdapter.OnFileClick {
    private static final String TAG = "FilesActivity ";
    private FilesAndMediaController mFilesAndMediaController;
    private RecyclerView mRecyclerView;
    private EditText mSearchEditText;
    private Toolbar mToolbar;
    private FilesAndMediaAdapter mFilesAndMediaAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final Context ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_and_media);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getFragmentManager().findFragmentByTag(TAG) == null) {
            mFilesAndMediaController = FilesAndMediaController.getInstance();
            getFragmentManager().beginTransaction().add(mFilesAndMediaController, TAG).commit();
        } else {
            mFilesAndMediaController = (FilesAndMediaController) getFragmentManager().findFragmentByTag(TAG);
        }
        mFilesAndMediaController.bindActivity(this);
        mFilesAndMediaController.getFilesAcync();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsTracker.getInstance(ctx, PrefUtils.getGaTrackerId(ctx)).setFileSearchWasOpened();
                AnalyticsTracker.getInstance(ctx, PrefUtils.getGaTrackerId(ctx)).setImageSearchWasOpened();
                if (mSearchEditText.getVisibility() == View.VISIBLE) {
                    mSearchEditText.setText("");
                    mSearchEditText.setVisibility(View.GONE);
                    mToolbar.setTitle(getString(R.string.files_and_media));
                    if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.undoClear();
                } else {
                    mSearchEditText.setVisibility(View.VISIBLE);
                    mSearchEditText.requestFocus();
                    mToolbar.setTitle("");
                    if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.backupAndClear();
                    mSearchEditText.setText("");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mSearchEditText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }, 100);
                }
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = null;
                if (s == null) {
                    str = "";
                } else {
                    str = s.toString();
                }
                if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.filter(str);
            }
        });
    }

    public void onFileReceive(List<FileDescription> descriptions) {
        if (descriptions != null && descriptions.size() > 0) {
            for (Iterator<FileDescription> iter = descriptions.iterator(); iter.hasNext(); ) {
                if (iter.next().getFilePath() == null) iter.remove();
            }
            mFilesAndMediaAdapter = new FilesAndMediaAdapter(descriptions, this);
            mRecyclerView.setAdapter(mFilesAndMediaAdapter);
        }
    }

    @Override
    public void onFileClick(FileDescription fileDescription) {
        mFilesAndMediaController.onFileClick(fileDescription);
    }

    public static Intent getStartIntetent(Activity activity) {
        Intent i = new Intent(activity, FilesActivity.class);
        return i;
    }

    @Override
    public void onBackPressed() {
        if (mSearchEditText.getVisibility() == View.VISIBLE) {
            mSearchEditText.setText("");
            mSearchEditText.setVisibility(View.GONE);
            mToolbar.setTitle(getString(R.string.files_and_media));
            if (null != mFilesAndMediaAdapter) mFilesAndMediaAdapter.undoClear();
        } else {
            super.onBackPressed();
        }
    }
}
