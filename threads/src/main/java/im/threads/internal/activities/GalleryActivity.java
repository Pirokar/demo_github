package im.threads.internal.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import im.threads.R;
import im.threads.internal.adapters.GalleryAdapter;
import im.threads.internal.adapters.PhotoBucketsGalleryAdapter;
import im.threads.internal.model.MediaPhoto;
import im.threads.internal.model.PhotoBucketItem;
import im.threads.internal.utils.BucketsGalleryDecorator;
import im.threads.internal.utils.GalleryDecorator;
import im.threads.internal.utils.ThreadsLogger;

public class GalleryActivity extends BaseActivity
        implements
        PhotoBucketsGalleryAdapter.OnItemClick
        , GalleryAdapter.OnGalleryItemClick {
    private RecyclerView mRecyclerView;
    private static final String TAG = "GalleryActivity ";
    private ArrayList<ArrayList<MediaPhoto>> lists = new ArrayList<>();
    List<PhotoBucketItem> bucketItems = new ArrayList<>();
    private boolean isInBuckets = false;
    private BucketsGalleryDecorator mBucketsGalleryDecorator = new BucketsGalleryDecorator(4);
    private GalleryDecorator mGalleryDecorator = new GalleryDecorator(4);
    private EditText mSearchEdiText;
    private List<MediaPhoto> chosenItems;
    private Button mSendButton;
    public static final String PHOTOS_REQUEST_CODE_TAG = "PHOTOS_REQUEST_CODE_TAG";
    public static final String PHOTOS_TAG = "PHOTOS_TAG";
    private Toolbar mToolbar;
    private TextView mNothingFoundLabel;
    private ImageButton mClearButton;
    private View mBottomButton;

    public static Intent getStartIntent(Context ctx, int requestCode) {
        Intent i = new Intent(ctx, GalleryActivity.class);
        i.putExtra(PHOTOS_REQUEST_CODE_TAG, requestCode);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Workaround on vectors not working in background selector
        // - see GalleryItemHolder mCheckBox.setButtonDrawable R.drawable.bk_checkbox_blue
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_gallery);
        initViews();
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.recycler);
        findViewById(R.id.search_photo).setOnClickListener(v -> setStateSearchingPhoto());
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        //  t.showOverflowMenu();
        Drawable overflowDrawable = mToolbar.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            ThreadsLogger.e(TAG, "initViews", e);
        }
        mRecyclerView = findViewById(R.id.recycler);
        mSendButton = findViewById(R.id.send);
        mNothingFoundLabel = findViewById(R.id.nothing_found_label);
        mClearButton = findViewById(R.id.clear_search_button);
        mBottomButton = findViewById(R.id.bottom_buttons);

        mBucketsGalleryDecorator = new BucketsGalleryDecorator(4);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(new PhotoBucketsGalleryAdapter(bucketItems, this));
        mRecyclerView.addItemDecoration(mBucketsGalleryDecorator);
        isInBuckets = true;
        mSearchEdiText = findViewById(R.id.search_edit_text);
        findViewById(R.id.cancel).setOnClickListener(v -> onBackPressed());
        mSendButton.setOnClickListener(v -> {
            if (getIntent().getIntExtra(PHOTOS_REQUEST_CODE_TAG, -1) == -1) {
                finish();
            } else {
                ArrayList<String> list1 = new ArrayList<>();
                for (MediaPhoto mp : chosenItems) {
                    list1.add(mp.getImagePath());
                }
                Intent i = new Intent();
                i.putStringArrayListExtra(PHOTOS_TAG, list1);
                setResult(RESULT_OK, i);
                finish();
            }
        });
        String[] projection = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
        Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " desc");
        int BUCKET_DISPLAY_NAME = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        int DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
        if (c.getCount() == 0) return;
        ArrayList<MediaPhoto> allItems = new ArrayList<>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            allItems.add(new MediaPhoto(c.getString(DATA), c.getString(BUCKET_DISPLAY_NAME)));
        }
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
    }

    @Override
    public void onPhotoBucketClick(PhotoBucketItem item) {
        setStateGallery(item.getBucketName(), item);
    }

    private void setStateGallery(String title, PhotoBucketItem item) {
        chosenItems = null;
        isInBuckets = false;
        checkSendButtonState();
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(title);
        findViewById(R.id.search_label_layout).setVisibility(View.GONE);
        findViewById(R.id.bottom_buttons).setVisibility(View.VISIBLE);
        findViewById(R.id.search_layout).setVisibility(View.GONE);
        findViewById(R.id.nothing_found_label).setVisibility(View.GONE);
        mRecyclerView.removeItemDecoration(mBucketsGalleryDecorator);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        List<MediaPhoto> photos = null;
        for (List<MediaPhoto> list : lists) {
            if (list.get(0).getImagePath().equals(item.getImagePath())) {
                photos = list;
                break;
            }
        }
        if (photos != null) {
            for (MediaPhoto photo : photos) {
                photo.setChecked(false);
            }
        }
        mRecyclerView.setAdapter(new GalleryAdapter(photos, this));
        mRecyclerView.addItemDecoration(mGalleryDecorator);

    }

    private void setStatePhotoBuckets() {
        isInBuckets = true;
        chosenItems = null;
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(getResources().getString(R.string.threads_photos));
        findViewById(R.id.search_label_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_buttons).setVisibility(View.GONE);
        findViewById(R.id.search_layout).setVisibility(View.GONE);
        findViewById(R.id.nothing_found_label).setVisibility(View.GONE);
        ((Toolbar) findViewById(R.id.toolbar)).showOverflowMenu();
        mRecyclerView.removeItemDecoration(mGalleryDecorator);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(new PhotoBucketsGalleryAdapter(bucketItems, this));
        mRecyclerView.addItemDecoration(mBucketsGalleryDecorator);
    }

    private void setStateSearchingPhoto() {
        isInBuckets = false;
        chosenItems = null;
        checkSendButtonState();
        findViewById(R.id.search_label_layout).setVisibility(View.GONE);
        findViewById(R.id.search_layout).setVisibility(View.VISIBLE);
        mSearchEdiText.requestFocus();
        mNothingFoundLabel.setVisibility(View.VISIBLE);
        mRecyclerView.removeItemDecoration(mBucketsGalleryDecorator);
        mRecyclerView.addItemDecoration(mGalleryDecorator);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(null);
        mClearButton.setOnClickListener(v -> mSearchEdiText.setText(""));
        mBottomButton.setVisibility(View.GONE);
        mToolbar.hideOverflowMenu();
        mSearchEdiText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchString = "";
                if (s != null) {
                    searchString = s.toString();
                }
                search(searchString);
            }
        });
        mSearchEdiText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.getText().toString());
                return true;
            } else {
                return false;
            }
        });
    }

    protected void search(String searchString) {
        clearCheckedStateOfItems();
        chosenItems = null;
        checkSendButtonState();
        if (searchString.trim().length() == 0) {
            mNothingFoundLabel.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(null);
            mBottomButton.setVisibility(View.GONE);
        } else {
            mNothingFoundLabel.setVisibility(View.GONE);
            List<MediaPhoto> list = new ArrayList<>();
            for (List<MediaPhoto> photos : lists) {
                for (MediaPhoto mp : photos) {
                    if (mp.getImagePath().contains(searchString)) {
                        list.add(mp);
                    }
                }
            }
            mRecyclerView.setAdapter(new GalleryAdapter(list, this));
            if (list.size() == 0) {
                mBottomButton.setVisibility(View.GONE);
            } else {
                mBottomButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onGalleryItemsChosen(List<MediaPhoto> chosenItems) {
        this.chosenItems = chosenItems;
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
        if (null != chosenItems && chosenItems.size() > 0) {
            mSendButton.setEnabled(true);
            mSendButton.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mSendButton.setEnabled(false);
            mSendButton.setTextColor(getResources().getColor(R.color.threads_disabled_text_color));
        }
    }
}
