package im.threads.internal.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.GridLayoutManager;
import im.threads.R;
import im.threads.databinding.ActivityGalleryBinding;
import im.threads.internal.adapters.GalleryAdapter;
import im.threads.internal.adapters.PhotoBucketsGalleryAdapter;
import im.threads.internal.helpers.MediaHelper;
import im.threads.internal.model.MediaPhoto;
import im.threads.internal.model.PhotoBucketItem;
import im.threads.internal.utils.BucketsGalleryDecorator;
import im.threads.internal.utils.GalleryDecorator;

public final class GalleryActivity
        extends BaseActivity
        implements PhotoBucketsGalleryAdapter.OnItemClick, GalleryAdapter.OnGalleryItemClick {

    private static final String PHOTOS_REQUEST_CODE_TAG = "PHOTOS_REQUEST_CODE_TAG";
    public static final String PHOTOS_TAG = "PHOTOS_TAG";

    private final List<List<MediaPhoto>> lists = new ArrayList<>();
    private final List<PhotoBucketItem> bucketItems = new ArrayList<>();
    private final List<MediaPhoto> chosenItems = new ArrayList<>();

    public final ObservableField<ScreenState> screenState = new ObservableField<>(ScreenState.BUCKET_LIST);
    public final ObservableField<Boolean> dataEmpty = new ObservableField<>(false);

    private final BucketsGalleryDecorator bucketsGalleryDecorator = new BucketsGalleryDecorator(4);
    private final GalleryDecorator galleryDecorator = new GalleryDecorator(4);

    private ActivityGalleryBinding binding;

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery);
        binding.setViewModel(this);
        initViews();
        initData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!ScreenState.BUCKET_LIST.equals(screenState.get())) {
            binding.searchEditText.setText("");
            showBucketListState();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPhotoBucketClick(PhotoBucketItem item) {
        showPhotoListState(item.getBucketName(), item);
    }

    @Override
    public void onGalleryItemsChosen(List<MediaPhoto> chosenItems) {
        this.chosenItems.clear();
        this.chosenItems.addAll(chosenItems);
        syncSendButtonState();
    }

    public void clearSearch() {
        binding.searchEditText.setText("");
    }

    public void showSearch() {
        showSearchState();
        search("");
    }

    public void send() {
        ArrayList<String> list1 = new ArrayList<>();
        for (MediaPhoto mp : chosenItems) {
            list1.add(mp.getImagePath());
        }
        Intent i = new Intent();
        i.putStringArrayListExtra(PHOTOS_TAG, list1);
        setResult(RESULT_OK, i);
        finish();
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        showBucketListState();
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    search(s.toString());
                } else {
                    search("");
                }
            }
        });
        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.getText().toString());
                return true;
            } else {
                return false;
            }
        });
    }

    private void initData() {
        try (Cursor c = MediaHelper.getAllPhotos(this)) {
            if (c == null) {
                return;
            }
            int BUCKET_DISPLAY_NAME = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
            if (c.getCount() == 0) return;
            List<MediaPhoto> allItems = new ArrayList<>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                allItems.add(new MediaPhoto(c.getString(DATA), c.getString(BUCKET_DISPLAY_NAME)));
            }
            List<MediaPhoto> list = new ArrayList<>();
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
            for (List<MediaPhoto> itemList : lists) {
                bucketItems.add(new PhotoBucketItem(itemList.get(0).getBucketName(), String.valueOf(itemList.size()), itemList.get(0).getImagePath()));
            }
        }
    }

    private void showBucketListState() {
        screenState.set(ScreenState.BUCKET_LIST);
        chosenItems.clear();
        binding.toolbar.setTitle(getResources().getString(R.string.threads_photos));
        binding.recycler.removeItemDecoration(galleryDecorator);
        binding.recycler.addItemDecoration(bucketsGalleryDecorator);
        binding.recycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recycler.setAdapter(new PhotoBucketsGalleryAdapter(bucketItems, this));
        dataEmpty.set(bucketItems.isEmpty());
    }

    private void showPhotoListState(String title, PhotoBucketItem item) {
        screenState.set(ScreenState.PHOTO_LIST);
        chosenItems.clear();
        syncSendButtonState();
        binding.toolbar.setTitle(title);
        binding.recycler.removeItemDecoration(bucketsGalleryDecorator);
        binding.recycler.addItemDecoration(galleryDecorator);
        binding.recycler.setLayoutManager(new GridLayoutManager(this, 3));
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
        binding.recycler.setAdapter(new GalleryAdapter(photos, this));
        dataEmpty.set(photos == null || photos.isEmpty());
    }

    private void showSearchState() {
        screenState.set(ScreenState.SEARCH);
        chosenItems.clear();
        syncSendButtonState();
        binding.searchEditText.requestFocus();
        binding.recycler.removeItemDecoration(bucketsGalleryDecorator);
        binding.recycler.addItemDecoration(galleryDecorator);
        binding.recycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recycler.setAdapter(null);
        dataEmpty.set(true);
    }

    private void search(String searchString) {
        clearCheckedStateOfItems();
        chosenItems.clear();
        syncSendButtonState();
        List<MediaPhoto> list = new ArrayList<>();
        for (List<MediaPhoto> photos : lists) {
            for (MediaPhoto mp : photos) {
                if (mp.getImagePath().contains(searchString)) {
                    list.add(mp);
                }
            }
        }
        binding.recycler.setAdapter(new GalleryAdapter(list, this));
        dataEmpty.set(list.isEmpty());
    }

    private void clearCheckedStateOfItems() {
        for (List<MediaPhoto> list : lists) {
            for (MediaPhoto mp : list) {
                mp.setChecked(false);
            }
        }
    }

    private void syncSendButtonState() {
        if (chosenItems.size() > 0) {
            binding.send.setEnabled(true);
            binding.send.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            binding.send.setEnabled(false);
            binding.send.setTextColor(ContextCompat.getColor(this, R.color.threads_disabled_text_color));
        }
    }

    public enum ScreenState {
        BUCKET_LIST,
        PHOTO_LIST,
        SEARCH
    }
}
