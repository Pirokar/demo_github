package com.sequenia.threads.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.sequenia.threads.R;
import com.sequenia.threads.adapters.FilesAndMediaAdapter;
import com.sequenia.threads.controllers.FilesAndMediaController;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.utils.PrefUtils;

import java.util.List;

/**
 * Created by yuri on 01.07.2016.
 */
public class FilesActivity extends AppCompatActivity implements FilesAndMediaAdapter.OnFileClick {
    private static final String TAG = "FilesActivity ";
    private FilesAndMediaController mFilesAndMediaController;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final Context ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_and_media);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.sendBroadcast(new Intent(ChatActivity.ACTION_SEARCH_CHAT));
                finish();
            }
        });
    }

    public void onFileReceive(List<FileDescription> descriptions) {
        if (descriptions != null && descriptions.size() > 0) {
            mRecyclerView.setAdapter(new FilesAndMediaAdapter(descriptions, this));
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
}
