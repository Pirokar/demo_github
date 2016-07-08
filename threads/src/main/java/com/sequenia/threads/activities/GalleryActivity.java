package com.sequenia.threads.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sequenia.threads.BucketGalleryDecorator;
import com.sequenia.threads.GalleryDecorator;
import com.sequenia.threads.R;
import com.sequenia.threads.adapters.GalleryAdaper;
import com.sequenia.threads.adapters.PhotoBucketsGalleryAdapter;
import com.sequenia.threads.model.MediaPhoto;
import com.sequenia.threads.model.PhotoBucketItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuri on 06.07.2016.
 */
public class GalleryActivity extends AppCompatActivity
        implements
        PhotoBucketsGalleryAdapter.OnItemClick
        , GalleryAdaper.OnGalleryItemClick {
    private RecyclerView mRecyclerView;
    private static final String TAG = "GalleryActivity ";
    private ArrayList<ArrayList<MediaPhoto>> lists;
    List<PhotoBucketItem> bucketItems = new ArrayList<>();
    private boolean isInBuckets = false;
    private BucketGalleryDecorator mBucketGalleryDecorator = new BucketGalleryDecorator(4);
    private GalleryDecorator mGalleryDecorator = new GalleryDecorator(4);
    private EditText mSearchEdiText;
    private List<MediaPhoto> chosentItems;
    private Button mSendButton;
    public static final String PHOTOS_REQUEST_CODE_TAG = "PHOTOS_REQUEST_CODE_TAG";
    public static final String PHOTOS_TAG = "PHOTOS_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        initViews();
    }

    private void initViews() {
        final Context ctx = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

        findViewById(R.id.search_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStateSearchingPhoto();
            }
        });
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //  t.showOverflowMenu();
        Drawable overflowDrawable = t.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mSendButton = (Button) findViewById(R.id.send);
        String[] projection = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
        Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " desc");
        int BUCKET_DISPLAY_NAME = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        int DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
        if (c.getCount() == 0) return;
        ArrayList<MediaPhoto> allItems = new ArrayList<>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            allItems.add(new MediaPhoto(c.getString(DATA), c.getString(BUCKET_DISPLAY_NAME)));
        }
        lists = new ArrayList<>();
        ArrayList<MediaPhoto> list = new ArrayList<>();
        list.add(allItems.get(0));
        lists.add(list);
        for (int i = 1; i < allItems.size(); i++) {
            if (allItems.get(i - 1).getBucketName().equalsIgnoreCase(allItems.get(i).getBucketName())) {
                for (int j = 0; j < lists.size(); j++) {
                    if (lists.get(j).get(0).getBucketName().equalsIgnoreCase(allItems.get(i).getBucketName())) {
                        lists.get(j).add(allItems.get(i));
                        break;
                    }
                }
            } else {
                list = new ArrayList<>();
                list.add(allItems.get(i));
                lists.add(list);
            }
        }
        for (ArrayList<MediaPhoto> itemList : lists) {
            bucketItems.add(new PhotoBucketItem(itemList.get(0).getBucketName(), String.valueOf(itemList.size()), itemList.get(0).getImagePath()));
        }
        mBucketGalleryDecorator = new BucketGalleryDecorator(4);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(new PhotoBucketsGalleryAdapter(bucketItems, this));
        mRecyclerView.addItemDecoration(mBucketGalleryDecorator);
        isInBuckets = true;
        mSearchEdiText = (EditText) findViewById(R.id.search_edit_text);
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getIntExtra(PHOTOS_REQUEST_CODE_TAG, -1) == -1) {
                    finish();
                } else {
                    ArrayList<String> list1 = new ArrayList<String>();
                    for (MediaPhoto mp : chosentItems) {
                        list1.add(mp.getImagePath());
                    }
                    Intent i = new Intent();
                    i.putStringArrayListExtra(PHOTOS_TAG, list1);
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });
    }

    public static Intent getStartIntent(Context ctx, int requestCode) {
        Intent i = new Intent(ctx, GalleryActivity.class);
        i.putExtra(PHOTOS_REQUEST_CODE_TAG, requestCode);
        return i;
    }

    @Override
    public void onPhotoBucketClick(PhotoBucketItem item) {
        setStateGallery(item.getBucketName(), item);
    }

    private void setStateGallery(String title, PhotoBucketItem item) {
        chosentItems = null;
        isInBuckets = false;
        checkSendButtonState();
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(title);
        findViewById(R.id.search_label_layout).setVisibility(View.GONE);
        findViewById(R.id.bottom_buttons).setVisibility(View.VISIBLE);
        findViewById(R.id.search_layout).setVisibility(View.GONE);
        findViewById(R.id.nothing_found_label).setVisibility(View.GONE);
        mRecyclerView.removeItemDecoration(mBucketGalleryDecorator);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        List<MediaPhoto> photos = null;
        for (List<MediaPhoto> list : lists) {
            if (list.get(0).getImagePath().equals(item.getImagePath())) {
                photos = list;
                break;
            }
        }
        for (MediaPhoto photo : photos) {
            photo.setChecked(false);
        }
        mRecyclerView.setAdapter(new GalleryAdaper(photos, this));
        mRecyclerView.addItemDecoration(mGalleryDecorator);

    }

    private void setStatePhotoBuckets() {
        isInBuckets = true;
        chosentItems = null;
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(getResources().getString(R.string.photos));
        findViewById(R.id.search_label_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_buttons).setVisibility(View.GONE);
        findViewById(R.id.search_layout).setVisibility(View.GONE);
        findViewById(R.id.nothing_found_label).setVisibility(View.GONE);
        ((Toolbar) findViewById(R.id.toolbar)).showOverflowMenu();
        mRecyclerView.removeItemDecoration(mGalleryDecorator);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(new PhotoBucketsGalleryAdapter(bucketItems, this));
        mRecyclerView.addItemDecoration(mBucketGalleryDecorator);
    }

    private void setStateSearchingPhoto() {
        isInBuckets = false;
        chosentItems = null;
        checkSendButtonState();
        findViewById(R.id.search_label_layout).setVisibility(View.GONE);
        findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
        mSearchEdiText.requestFocus();
        ImageButton clearButton = (ImageButton) findViewById(R.id.clear_search_button);
        final TextView nothingFoundLabel = (TextView) findViewById(R.id.nothing_found_label);
        nothingFoundLabel.setVisibility(View.VISIBLE);
        mRecyclerView.removeItemDecoration(mBucketGalleryDecorator);
        mRecyclerView.addItemDecoration(mGalleryDecorator);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(null);
        final View bottomButton = findViewById(R.id.bottom_buttons);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEdiText.setText("");
            }
        });
        final GalleryAdaper.OnGalleryItemClick listener = this;
        bottomButton.setVisibility(View.GONE);
        ((Toolbar) findViewById(R.id.toolbar)).hideOverflowMenu();
        mSearchEdiText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                clearCheckedStateOfItems();
                chosentItems = null;
                checkSendButtonState();
                if (s.length() == 0) {
                    nothingFoundLabel.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(null);
                    bottomButton.setVisibility(View.GONE);
                } else {
                    nothingFoundLabel.setVisibility(View.GONE);
                    List<MediaPhoto> list = new ArrayList<>();
                    for (List<MediaPhoto> photoes : lists) {
                        for (MediaPhoto mp : photoes) {
                            if (mp.getImagePath().contains(s.toString())) {
                                list.add(mp);
                            }
                        }
                    }
                    mRecyclerView.setAdapter(new GalleryAdaper(list, listener));
                    if (list.size() == 0) {
                        bottomButton.setVisibility(View.GONE);
                    } else {
                        bottomButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onGalleryItemsChosen(List<MediaPhoto> chosenItems) {
        this.chosentItems = chosenItems;
        checkSendButtonState();
    }

    @Override
    public void onBackPressed() {
        if (!isInBuckets) {
            mSearchEdiText.setText("");
            clearCheckedStateOfItems();
            findViewById(R.id.nothing_found_label).setVisibility(View.GONE);
            setStatePhotoBuckets();
        } else {
            super.onBackPressed();
        }
    }

    private void clearCheckedStateOfItems() {
        for (List<MediaPhoto> list : lists) {
            for (MediaPhoto mp : list) {
                mp.setChecked(false);
            }
        }
    }

    private void checkSendButtonState() {
        if (null != chosentItems && chosentItems.size() > 0) {
            mSendButton.setEnabled(true);
            mSendButton.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mSendButton.setEnabled(false);
            mSendButton.setTextColor(getResources().getColor(R.color.disabled_text_color));
        }
    }
}
