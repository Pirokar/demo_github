package com.sequenia.threads.activities;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sequenia.threads.fragments.FilePickerFragment;
import com.sequenia.threads.fragments.QuickAnswerFragment;
import com.sequenia.threads.model.CompletionHandler;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.MyFileFilter;
import com.sequenia.threads.R;
import com.sequenia.threads.adapters.BottomGalleryAdapter;
import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.fragments.NoConnectionDialogFragment;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.FileUtils;
import com.sequenia.threads.utils.PermissionChecker;
import com.sequenia.threads.utils.PrefUtils;
import com.sequenia.threads.views.BottomGallery;
import com.sequenia.threads.views.BottomSheetView;
import com.sequenia.threads.views.MySwipeRefreshLayout;
import com.sequenia.threads.views.WelcomeScreen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 *
 */
public class ChatActivity extends AppCompatActivity
        implements
        BottomSheetView.ButtonsListener
        , ChatAdapter.AdapterInterface
        , FilePickerFragment.SelectedListener
        , QuickAnswerFragment.OnQuickAnswer {
    private static final String TAG = "ChatActivity ";
    private static final String TAG_DEF_TITLE = "TAG_DEF_TITLE";
    private static final String TAG_CLIENT_NAME = "TAG_CLIENT_NAME";
    private static final String TAG_PUSH_RESID = "TAG_PUSH_RESID";
    public static final String ACTION_SHOW_QUICK_ANSWER = "ACTION_SHOW_QUICK_ANSWER";
    private ChatController mChatController;
    private WelcomeScreen mWelcomeScreen;
    private EditText mInputEditText;
    private BottomSheetView mBottomSheetView;
    private BottomGallery mBottomGallery;
    private ChatAdapter mChatAdapter;
    private TextView mConsultNameView;
    private TextView mConsultTitle;
    private View mCopyControls;
    private Toolbar mToolbar;
    private QuoteLayoutHolder mQuoteLayoutHolder;
    private RecyclerView mRecyclerView;
    private Quote mQuote = null;
    private FileDescription mFileDescription = null;
    private ChatPhrase mChosenPhrase = null;
    private List<String> mAttachedImages = new ArrayList<>();
    private AppCompatEditText mSearchMessageEditText;
    private MySwipeRefreshLayout mSwipeRefreshLayout;
    private Button mSearchMoreButton;
    public static final int REQUEST_CODE_PHOTOS = 100;
    public static final int REQUEST_CODE_PHOTO = 101;
    public static final int REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY = 102;
    public static final int REQUEST_PERMISSION_CAMERA = 103;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL = 104;
    public static final String ACTION_SEARCH_CHAT_FILES = "ACTION_SEARCH_CHAT_FILES";
    public static final String ACTION_SEARCH = "ACTION_SEARCH";
    public static final String ACTION_SEND_QUICK_MESSAGE = "ACTION_SEND_QUICK_MESSAGE";
    private String connectedConsultId;
    private ChatActivityReceiver mChatActivityReceiver;
    private Handler h = new Handler(Looper.getMainLooper());
    private boolean isInMessageSearchMode;
    private boolean searchInFiles;
    private boolean isResumed;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ThreadsStyle);
        setContentView(R.layout.activity_chat_activity);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        if (null != t) initToolbar(t);
        getIncomingSettings(getIntent());
        initViews();
        if (null != getFragmentManager().findFragmentByTag(ChatController.TAG)) {//mb, someday, we will support orientation change
            mChatController = (ChatController) getFragmentManager().findFragmentByTag(ChatController.TAG);
        } else {
            mChatController = ChatController.getInstance(this, getIntent().getStringExtra(TAG));
            getFragmentManager().beginTransaction().add(mChatController, ChatController.TAG).commit();
        }
        mChatController.bindActivity(this);
        if (mChatController.isNeedToShowWelcome()) mWelcomeScreen.setVisibility(View.VISIBLE);
        mChatActivityReceiver = new ChatActivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SEARCH_CHAT_FILES);
        intentFilter.addAction(ACTION_SEARCH);
        intentFilter.addAction(ACTION_SEND_QUICK_MESSAGE);
        registerReceiver(mChatActivityReceiver, intentFilter);
    }

    private void getIncomingSettings(Intent intent) {
        if (intent.getStringExtra(TAG) == null && PrefUtils.getClientID(this) == null)
            throw new IllegalStateException("you must provide valid client id," +
                    "\r\n it is now null or it'ts length < 5");
        if (intent.getStringExtra(TAG_DEF_TITLE) != null) {
            PrefUtils.setDefaultTitle(this, getIntent().getStringExtra(TAG_DEF_TITLE));
        }
        if (intent.getStringExtra(TAG_CLIENT_NAME) != null) {
            PrefUtils.setClientName(this, intent.getStringExtra(TAG_CLIENT_NAME));
        }
        if (intent.getBundleExtra("bundle") != null) {
            PrefUtils.setIncomingStyle(this, intent.getBundleExtra("bundle"));
        }
        if (intent.getIntExtra(TAG_PUSH_RESID, -1) != -1 && PrefUtils.getPushIconResid(this) != -1)
            PrefUtils.setPushIconResid(this, intent.getIntExtra(TAG_PUSH_RESID, -1));
    }

    static Intent getStartIntent(Context ctx) {
        Intent i = new Intent(ctx, ChatActivity.class);
        return i;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //initialy  sets title,click listeners on toolbar.
    private void initToolbar(@NonNull Toolbar t) {
        t.setTitle("");
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        t.showOverflowMenu();
        Drawable overflowDrawable = t.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatController.setActivityIsForeground(true);
        isResumed = true;
    }


    @Override
    protected void onStop() {
        super.onStop();
        mChatController.setActivityIsForeground(false);
        isResumed = false;
    }

    @SuppressWarnings("all")
    private void initViews() {
        mQuoteLayoutHolder = new QuoteLayoutHolder();
        mBottomSheetView = (BottomSheetView) findViewById(R.id.file_input_sheet);
        mBottomSheetView.setButtonsListener(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSearchMessageEditText = (AppCompatEditText) findViewById(R.id.search);
        mBottomGallery = (BottomGallery) findViewById(R.id.bottom_gallery);
        mSwipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setmSwipeListener(new MySwipeRefreshLayout.SwipeListener() {
            @Override
            public void onSwipe() {
                finish();
            }
        });
        mInputEditText = (EditText) findViewById(R.id.input);
        ImageButton SendButton = (ImageButton) findViewById(R.id.send_message);
        final Context c = this;
        ImageButton AddAttachmentButton = (ImageButton) findViewById(R.id.add_attachment);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), this, this);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setAdapter(mChatAdapter);
        AddAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetAndGallery();
            }
        });
        Bundle b = PrefUtils.getIncomingStyle(this);
        mWelcomeScreen = (WelcomeScreen) findViewById(R.id.welcome);
        mWelcomeScreen.setLogo(b.getInt("logoResId"));
        mWelcomeScreen.setTextColor(b.getInt("textColorResId"));
        mWelcomeScreen.setText(b.getString("titleText"), b.getString("subtitleText"));
        mWelcomeScreen.setTitletextSize(b.getFloat("titleSize"));
        mWelcomeScreen.setSubtitleSize(b.getFloat("subtitleSize"));
        mSearchMoreButton = (Button) findViewById(R.id.search_more);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mChatController.requestItems(new Callback<List<ChatItem>, Throwable>() {
                    @Override
                    public void onSuccess(final List<ChatItem> result) {
                        final Handler h = new Handler(Looper.getMainLooper());
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int itemsBefore = mChatAdapter.getItemCount();
                                mChatAdapter.addItems(result);
                                int itemsAfter = mChatAdapter.getItemCount();
                                mRecyclerView.scrollToPosition(itemsAfter - itemsBefore);
                                for (int i = 1; i < 5; i++) {//for solving bug with refresh layout doesn't stop refresh animation
                                    h.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSwipeRefreshLayout.setRefreshing(false);
                                            mSwipeRefreshLayout.clearAnimation();
                                            mSwipeRefreshLayout.destroyDrawingCache();
                                            mSwipeRefreshLayout.invalidate();
                                        }
                                    }, i * 500);
                                }
                            }
                        }, 500);
                    }

                    @Override
                    public void onFail(Throwable error) {
                    }
                });
            }
        });
        final Context ctx = this;
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputEditText.getText().length() == 0 && ((mQuote == null) && (mFileDescription == null)))
                    return;
                if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
                    mWelcomeScreen.setVisibility(View.GONE);
                    mWelcomeScreen = null;
                }
                unChooseItem(mChosenPhrase);
                UpcomingUserMessage uum = new UpcomingUserMessage(mInputEditText.getText().toString().trim(), mQuote, mFileDescription);
                mChatController.onUserInput(uum);
                mInputEditText.setText("");
                mQuoteLayoutHolder.setIsVisible(false);
                mQuote = null;
                mFileDescription = null;
                mChosenPhrase = null;
                mChatAdapter.setAllMessagesRead();
            }
        });
        mConsultNameView = (TextView) findViewById(R.id.consult_name);
        mConsultTitle = (TextView) findViewById(R.id.subtitle);
        mConsultNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatController.isConsultFound())
                    onConsultAvatarClick(connectedConsultId);
            }
        });
        mConsultTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatController.isConsultFound())
                    onConsultAvatarClick(connectedConsultId);
            }
        });
        mCopyControls = findViewById(R.id.copy_controls);
        mInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
                    mWelcomeScreen.setVisibility(View.GONE);
                    mWelcomeScreen = null;
                }
            }
        });
        mInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mChatController.onUserTyping();
            }
        });
        mSearchMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || s.length() == 0) {
                    return;
                } else {
                    if (!searchInFiles) {
                        mChatController.requestFilteredPhrases(false, s.toString(), new Callback<Pair<Boolean, List<ChatPhrase>>, Exception>() {
                            @Override
                            public void onSuccess(Pair<Boolean, List<ChatPhrase>> result) {
                                mChatAdapter.swapItems(result.second);
                            }

                            @Override
                            public void onFail(Exception error) {
                            }
                        });
                    } else {
                        mChatController.requestFilteredFiles(false, s.toString(), new Callback<Pair<Boolean, List<ChatPhrase>>, Exception>() {
                            @Override
                            public void onSuccess(Pair<Boolean, List<ChatPhrase>> result) {
                                mChatAdapter.swapItems(result.second);
                            }

                            @Override
                            public void onFail(Exception error) {
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onQuickAnswer(String answer) {
        if (answer == null) return;
        if (null != mChatController) {
            mChatController.onUserInput(new UpcomingUserMessage(answer, null, null));
            if (null != mQuoteLayoutHolder) mQuoteLayoutHolder.setIsVisible(false);
            if (null != mChatAdapter) mChatAdapter.setAllMessagesRead();
        }
    }

    private void openBottomSheetAndGallery() {
        if (PermissionChecker.isReadExternalPermissionGranted(this)) {
            setTitleStateCurrentOperatorConnected();
            final View inputLayout = findViewById(R.id.input_layout);
            if (mBottomSheetView.getVisibility() == View.GONE) {
                mBottomSheetView.setVisibility(View.VISIBLE);
                mBottomSheetView.setAlpha(0.0f);
                mBottomSheetView.animate().alpha(1.0f).setDuration(300).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mBottomSheetView.setVisibility(View.VISIBLE);
                    }
                });
                inputLayout.setVisibility(View.GONE);
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
                String[] projection = new String[]{MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " desc");
                int DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
                if (c.getCount() == 0) return;
                ArrayList<String> allItems = new ArrayList<>();
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    allItems.add("file://" + c.getString(DATA));
                }
                mBottomGallery.setVisibility(View.VISIBLE);
                mBottomGallery.setAlpha(0.0f);
                mBottomGallery.animate().alpha(1.0f).setDuration(200).start();
                mBottomGallery.setImages(allItems, new BottomGalleryAdapter.OnChooseItemsListener() {
                    @Override
                    public void onChosenItems(List<String> items) {
                        mAttachedImages = new ArrayList<>(items);
                        if (mAttachedImages.size() > 0) {
                            mBottomSheetView.setSelectedState(true);
                        } else {
                            mBottomSheetView.setSelectedState(false);
                        }
                    }
                });
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
            } else {
                mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mBottomSheetView.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY);
        }

    }

    public void setUserPhraseMessageId(String oldId, String newId) {
        mChatAdapter.setUserPhraseMessageId(oldId, newId);
    }

    @Override
    public void onCameraClick() {
        boolean isCameraGranted = PermissionChecker.isCameraPermissionGranted(this);
        boolean isWriteGranted = PermissionChecker.isWriteExternalPermissionGranted(this);
        Log.i(TAG, "isCameraGranted = " + isCameraGranted + " isWriteGranted " + isWriteGranted);
        if (isCameraGranted && isWriteGranted) {
            setBottomStateDefault();
            mBottomGallery.setVisibility(View.GONE);
            startActivityForResult(new Intent(this, CameraActivity.class), REQUEST_CODE_PHOTO);
        } else {
            ArrayList<String> permissions = new ArrayList<>();
            if (!isCameraGranted) permissions.add(android.Manifest.permission.CAMERA);
            if (!isWriteGranted)
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[]{}), REQUEST_PERMISSION_CAMERA);
        }
    }

    @Override
    public void onGalleryClick() {
        startActivityForResult(GalleryActivity.getStartIntent(this, REQUEST_CODE_PHOTOS), REQUEST_CODE_PHOTOS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mChatController.setActivityIsForeground(false);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onFileSelected(File fileOrDirectory) {
        mFileDescription = new FileDescription(getString(R.string.I), fileOrDirectory.getAbsolutePath(), fileOrDirectory.length(), System.currentTimeMillis());
        mQuoteLayoutHolder.setText(getString(R.string.I), FileUtils.getLastPathSegment(fileOrDirectory.getAbsolutePath()), null);
        mQuote = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTOS && resultCode == RESULT_OK) {
            ArrayList<String> photos = data.getStringArrayListExtra(GalleryActivity.PHOTOS_TAG);
            onHideClick();
            if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
                mWelcomeScreen.setVisibility(View.GONE);
                mWelcomeScreen = null;
            }
            if (photos.size() == 0) return;
            unChooseItem(mChosenPhrase);
            UpcomingUserMessage uum =
                    new UpcomingUserMessage(mInputEditText.getText().toString().trim()
                            , null
                            , new FileDescription(getString(R.string.I), photos.get(0), new File(photos.get(0).replaceAll("file://", "")).length(), System.currentTimeMillis()));
            mChatController.onUserInput(uum);
            mInputEditText.setText("");
            mQuoteLayoutHolder.setIsVisible(false);
            mQuote = null;
            mFileDescription = null;
            for (int i = 1; i < photos.size(); i++) {
                uum =
                        new UpcomingUserMessage(null
                                , null
                                , new FileDescription(getString(R.string.I), photos.get(i), new File(photos.get(i).replaceAll("file://", "")).length(), System.currentTimeMillis()));
                mChatController.onUserInput(uum);
            }
        } else if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            mFileDescription = new FileDescription(getResources().getString(R.string.image), data.getStringExtra(CameraActivity.IMAGE_EXTRA), new File(data.getStringExtra(CameraActivity.IMAGE_EXTRA).replace("file://", "")).length(), System.currentTimeMillis());
            mQuoteLayoutHolder.setText(mChatController.getCurrentConsultName().split("%%")[0], getResources().getString(R.string.image), "file://" + data.getStringExtra(CameraActivity.IMAGE_EXTRA));
            mQuote = null;
        }
    }

    @Override
    public void onFilePickerClick() {
        setBottomStateDefault();
        if (PermissionChecker.isReadExternalPermissionGranted(this)) {
            FilePickerFragment frag = FilePickerFragment.newInstance(null);
            frag.setFileFilter(new MyFileFilter());
            frag.setOnDirSelectedListener(this);
            frag.show(getFragmentManager(), null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL);
        }

    }

    @Override
    public void onHideClick() {
        final View input = findViewById(R.id.input_layout);
        mBottomGallery.setVisibility(View.GONE);
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
                input.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onSendClick() {
        if (mAttachedImages == null || mAttachedImages.size() == 0) {
            mBottomGallery.setVisibility(View.GONE);
        } else {
            UpcomingUserMessage uum = new UpcomingUserMessage(mInputEditText.getText().toString().trim(), mQuote, new FileDescription(getString(R.string.I), mAttachedImages.get(0), new File(mAttachedImages.get(0).replaceAll("file://", "")).length(), System.currentTimeMillis()));
            mChatController.onUserInput(uum);
            for (int i = 1; i < mAttachedImages.size(); i++) {
                uum = new UpcomingUserMessage(null, null, new FileDescription(getString(R.string.I), mAttachedImages.get(i), new File(mAttachedImages.get(i).replaceAll("file://", "")).length(), System.currentTimeMillis()));
                mChatController.onUserInput(uum);
            }
        }
        mBottomSheetView.setSelectedState(false);
        mInputEditText.setText("");
        mQuoteLayoutHolder.setIsVisible(false);
        mQuote = null;
        mFileDescription = null;
        setBottomStateDefault();
        hideCopyControls();
        mAttachedImages.clear();
        mBottomGallery.setVisibility(View.GONE);
    }

    private void setBottomStateDefault() {
        final View input = findViewById(R.id.input_layout);
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
                input.setVisibility(View.VISIBLE);
            }
        });
        mSearchMessageEditText.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        mBottomGallery.setVisibility(View.GONE);
    }

    public void addChatItem(ChatItem item) {
        if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
            mWelcomeScreen.setVisibility(View.GONE);
            mWelcomeScreen = null;
        }
        if (item instanceof ConsultPhrase && isResumed) {
            ((ConsultPhrase) item).setRead(true);
        } else if (item instanceof ConsultPhrase) {
            ((ConsultPhrase) item).setRead(false);
        }
        mChatAdapter.addItems(Arrays.asList(item));
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }, 100);
    }

    public void addChatItems(final List<ChatItem> list) {
        if (list.size() == 0) return;
        Log.d(TAG, "addChatItems: " + list);
        h.post(new Runnable() {
            @Override
            public void run() {
                if (mWelcomeScreen != null) {
                    mWelcomeScreen.setVisibility(View.GONE);
                    ((ViewGroup) findViewById(android.R.id.content)).removeView(mWelcomeScreen);
                    mWelcomeScreen = null;
                }
                mChatAdapter.addItems(list);
            }
        });
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }, 600);
    }

    public void showDownloading() {
    }

    public void removeDownloading() {
    }

    public void changeStateOfMessage(String messageId, MessageState state) {
        mChatAdapter.changeStateOfMessage(messageId, state);
    }

    public void setTitleStateDefault() {
        final Context ctx = this;
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInMessageSearchMode) {
                    mConsultTitle.setVisibility(View.GONE);
                    mConsultNameView.setVisibility(View.VISIBLE);
                    mSearchMessageEditText.setVisibility(View.GONE);
                    mSearchMessageEditText.setText("");
                    mConsultNameView.setText(PrefUtils.getDefaultTitle(ctx));
                }
                connectedConsultId = String.valueOf(-1);
            }
        }, 50);
    }

    private void setTitleStateSearchingConsult() {
        if (isInMessageSearchMode) return;
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.VISIBLE);
        mSearchMessageEditText.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        mConsultNameView.setText(getResources().getString(R.string.searching_operator));
    }

    public void setStateSearchingConsult() {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                setTitleStateSearchingConsult();
                mChatAdapter.setSearchingConsult();
            }
        }, 50);
    }

    public void setTitleStateSearchingMessage() {
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.GONE);
        mSearchMessageEditText.setVisibility(View.VISIBLE);
        mSearchMessageEditText.setText("");
    }


    public void setStateConsultConnected(final String connectedConsultId, final String ConsultName, final String consultTitle) {
        final ChatActivity a = this;
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInMessageSearchMode) {
                    mConsultTitle.setVisibility(View.VISIBLE);
                    mConsultNameView.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(ConsultName) && !ConsultName.equals("null")) {
                    mConsultNameView.setText(ConsultName);
                } else {
                    mConsultNameView.setText(getString(R.string.unknown_operator));
                }
                if (!TextUtils.isEmpty(consultTitle) && !consultTitle.equals("null")) {
                    mConsultTitle.setText(consultTitle);
                } else {
                    mConsultTitle.setText("");
                }
                a.connectedConsultId = connectedConsultId;
                mChatAdapter.removeConsultSearching();
            }
        }, 50);

    }

    private void setTitleStateCurrentOperatorConnected() {
        if (isInMessageSearchMode) return;
        if (mChatController.isConsultFound()) {
            mConsultTitle.setVisibility(View.VISIBLE);
            mConsultNameView.setVisibility(View.VISIBLE);
            mSearchMessageEditText.setVisibility(View.GONE);
            mSearchMessageEditText.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatController.unbindActivity();
        unregisterReceiver(mChatActivityReceiver);

    }

    @Override
    public void onFileClick(FileDescription filedescription) {
        mChatController.onFileClick(filedescription);
    }

    @Override
    public void onConsultConnectionClick(ConsultConnectionMessage consultConnectionMessage) {
        startActivity(ConsultActivity.getStartIntent(this
                , consultConnectionMessage.getAvatarPath()
                , consultConnectionMessage.getName()
                , consultConnectionMessage.getStatus()));
    }

    @Override
    public void onImageClick(ChatPhrase chatPhrase) {
        if (chatPhrase.getFileDescription().getFilePath() == null) return;
        if (chatPhrase instanceof UserPhrase) {
            if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_SENT_AND_SERVER_RECEIVED) {
                mChatController.checkAndResendPhrase((UserPhrase) chatPhrase);
            }
            if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_NOT_SENT) {
                startActivity(ImagesActivity.getStartIntent(this, chatPhrase.getFileDescription()));
            }
        } else if (chatPhrase instanceof ConsultPhrase) {
            startActivity(ImagesActivity.getStartIntent(this, ((ChatPhrase) chatPhrase).getFileDescription()));
        }
    }

    @Override
    public void onImageDownloadRequest(FileDescription fileDescription) {
        mChatController.onImageDownloadRequest(fileDescription);
    }

    public void onDownloadError(FileDescription fileDescription, Throwable t) {
        updateProgress(fileDescription);
        if (t instanceof FileNotFoundException) {
            Toast.makeText(this, R.string.error_no_file, Toast.LENGTH_SHORT).show();
            mChatAdapter.onDownloadError(fileDescription);
        }
    }

    @Override
    public void onPhraseLongClick(final ChatPhrase cp, final int position) {
        if (cp == mChosenPhrase) {
            unChooseItem(cp);
            return;
        }
        unChooseItem(cp);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        mToolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
        mCopyControls.setVisibility(View.VISIBLE);
        mConsultNameView.setVisibility(View.GONE);
        mConsultTitle.setVisibility(View.GONE);
        ImageButton reply = (ImageButton) mCopyControls.findViewById(R.id.reply);
        ImageButton copy = (ImageButton) mCopyControls.findViewById(R.id.content_copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(new ClipData("", new String[]{"text/plain"}, new ClipData.Item(cp.getPhraseText())));
                hideCopyControls();
                if (null != mChosenPhrase) unChooseItem(mChosenPhrase);
            }
        });
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String headerText = "";

                if (cp instanceof UserPhrase) {
                    headerText = getString(R.string.I);

                } else if (cp instanceof ConsultPhrase) {
                    headerText = mChatController.getConsultNameById(((ConsultPhrase) cp).getConsultId());
                    if (headerText == null) {
                        headerText = getString(R.string.consult);
                    }
                }
                if (isEmpty(cp.getPhraseText())) {
                    mQuote = new Quote(headerText, cp.getPhraseText(), null, System.currentTimeMillis());
                }
                String text = cp.getPhraseText();
                mQuoteLayoutHolder.setText(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? "" : text, null);
                hideCopyControls();
                mRecyclerView.scrollToPosition(position);
                FileDescription quoteFileDescription = cp.getFileDescription();
                if (quoteFileDescription == null && cp.getQuote() != null) {
                    quoteFileDescription = cp.getQuote().getFileDescription();
                }
                mQuote = new Quote(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? "" : text, quoteFileDescription, cp.getTimeStamp());
                mFileDescription = null;

            }
        });
        mChosenPhrase = cp;
        mChatAdapter.setItemChosen(true, cp);
    }

    @Override
    public void onConsultAvatarClick(String consultId) {
        mChatController.onConsultChoose(this, consultId);
    }

    private void showHelloScreen() {
        if (mWelcomeScreen == null) mWelcomeScreen = (WelcomeScreen) findViewById(R.id.welcome);
        mWelcomeScreen.setVisibility(View.VISIBLE);
    }

    private void hideCopyControls() {
        setTitleStateCurrentOperatorConnected();
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.green_light));
        mCopyControls.setVisibility(View.GONE);
        if (!isInMessageSearchMode) mConsultNameView.setVisibility(View.VISIBLE);
        if (mChatController != null && mChatController.isConsultFound() && !isInMessageSearchMode) {
            mConsultTitle.setVisibility(View.VISIBLE);
        }
    }

    public int getCurrentItemsCount() {
        return mChatAdapter.getCurrentItemCount();
    }

    @Override
    public void onBackPressed() {
        boolean isNeedToClose = true;
        if (mCopyControls.getVisibility() == View.VISIBLE
                && mSearchMessageEditText.getVisibility() == View.VISIBLE) {
            unChooseItem(mChosenPhrase);
            mSearchMessageEditText.requestFocus();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mSearchMessageEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
            return;
        }

        if (mCopyControls.getVisibility() == View.VISIBLE) {
            unChooseItem(mChosenPhrase);
            isNeedToClose = false;
        }
        if (mSearchMessageEditText.getVisibility() == View.VISIBLE) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    mChatAdapter.setAllMessagesRead();
                }
            });
            mSearchMessageEditText.setVisibility(View.GONE);
            mSearchMessageEditText.setText("");
            isInMessageSearchMode = false;
            mChatAdapter.undoClear();
            mSearchMoreButton.setVisibility(View.GONE);
            mSwipeRefreshLayout.setEnabled(true);
            int state = mChatController.getStateOfConsult();
            switch (state) {
                case ChatController.CONSULT_STATE_DEFAULT:
                    setTitleStateDefault();
                    break;
                case ChatController.CONSULT_STATE_FOUND:
                    String nameTitle[] = mChatController.getCurrentConsultName().split("%%");
                    setStateConsultConnected(connectedConsultId, nameTitle[0], nameTitle[1]);
                    break;
                case ChatController.CONSULT_STATE_SEARCHING:
                    setTitleStateSearchingConsult();
                    break;
            }
            isNeedToClose = false;
            if (mRecyclerView != null && mChatAdapter != null) {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }
        if (mBottomGallery.getVisibility() == View.VISIBLE) {
            onHideClick();
            return;
        }
        if (isNeedToClose) {
            super.onBackPressed();
        }
    }

    public void setPhraseSentStatus(String id, MessageState messageState) {
        mChatAdapter.changeStateOfMessage(id, messageState);
    }

    private void unChooseItem(ChatPhrase cp) {
        hideCopyControls();
        mChatAdapter.setItemChosen(false, mChosenPhrase);
        mChosenPhrase = null;
    }

    public void updateProgress(FileDescription filedescription) {
        mChatAdapter.updateProgress(filedescription);
    }

    private class QuoteLayoutHolder {
        private View view;
        private TextView mHeader;
        private TextView mText;
        private ImageView mQuoteImage;

        public QuoteLayoutHolder() {
            view = findViewById(R.id.quote_layout);
            mHeader = (TextView) view.findViewById(R.id.quote_header);
            mText = (TextView) view.findViewById(R.id.quote_text);
            mQuoteImage = (ImageView) findViewById(R.id.quote_image);
            View clear = view.findViewById(R.id.quote_clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHeader.setText("");
                    mText.setText("");
                    view.setVisibility(View.GONE);
                    mQuote = null;
                    mFileDescription = null;
                    unChooseItem(mChosenPhrase);
                }
            });
        }

        public boolean isVisible() {
            return view.getVisibility() == View.VISIBLE;
        }

        public void setIsVisible(boolean isVisible) {
            if (isVisible) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }

        private void setImage(String path) {
            mQuoteImage.setVisibility(View.VISIBLE);
            Picasso
                    .with(getApplicationContext())
                    .load(path)
                    .fit()
                    .centerInside()
                    .into(mQuoteImage);
        }

        private void removeImage() {
            mQuoteImage.setVisibility(View.GONE);
        }

        void setText(String header, String text, String imagePath) {
            setIsVisible(true);
            if (header == null || header.equals("null")) {
                mHeader.setVisibility(View.INVISIBLE);
            } else {
                mHeader.setVisibility(View.VISIBLE);
            }
            mHeader.setText(header);
            mText.setText(text);
            if (imagePath != null) {
                setImage(imagePath);
            } else {
                removeImage();
            }
        }

    }

    @Override
    public void onUserPhraseClick(final UserPhrase userPhrase, int position) {
        mChatController.checkAndResendPhrase(userPhrase);
    }

    public void showConnectionError() {
        final NoConnectionDialogFragment ncdf = NoConnectionDialogFragment.getInstance(new NoConnectionDialogFragment.OnCancelListener() {
            @Override
            public void onCancel() {
            }
        });
        ncdf.setCancelable(true);
        ncdf.show(getFragmentManager(), null);
    }

    public void cleanChat() {
        mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), this, this);
        mRecyclerView.setAdapter(mChatAdapter);
        setTitleStateDefault();
        if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
            mWelcomeScreen.setVisibility(View.GONE);
            mWelcomeScreen = null;
        }
        mInputEditText.clearFocus();
        showHelloScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.files_and_media) {
            if (isInMessageSearchMode) mChatAdapter.undoClear();
            startActivity(FilesActivity.getStartIntetent(this));
            return true;
        }

        if (item.getItemId() == R.id.search && isInMessageSearchMode) {
            return true;

        } else if (item.getItemId() == R.id.search) {
            search(false);
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(final boolean searchInFiles) {
        Log.d(TAG, "searchInFiles: " + searchInFiles);
        isInMessageSearchMode = true;
        this.searchInFiles = searchInFiles;
        setBottomStateDefault();
        setTitleStateSearchingMessage();
        mSearchMessageEditText.requestFocus();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchMessageEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
        mChatAdapter.backupAndClear();
        mSwipeRefreshLayout.setEnabled(false);
        mSearchMoreButton.setVisibility(View.VISIBLE);
        mSearchMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchMessageEditText.getText() != null && mSearchMessageEditText.getText().length() > 0) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    if (!searchInFiles) {
                        mChatController.requestFilteredPhrases(true, mSearchMessageEditText.getText().toString(), new Callback<Pair<Boolean, List<ChatPhrase>>, Exception>() {
                            @Override
                            public void onSuccess(Pair<Boolean, List<ChatPhrase>> result) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                if (result.first) {
                                    mSearchMoreButton.setVisibility(View.VISIBLE);
                                } else {
                                    mSearchMoreButton.setVisibility(View.GONE);
                                }
                                mChatAdapter.swapItems(result.second);
                            }

                            @Override
                            public void onFail(Exception error) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    } else {
                        mChatController.requestFilteredFiles(true, mSearchMessageEditText.getText().toString(), new Callback<Pair<Boolean, List<ChatPhrase>>, Exception>() {
                            @Override
                            public void onSuccess(Pair<Boolean, List<ChatPhrase>> result) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                if (result.first) {
                                    mSearchMoreButton.setVisibility(View.VISIBLE);
                                } else {
                                    mSearchMoreButton.setVisibility(View.GONE);
                                }
                                mChatAdapter.swapItems(result.second);
                            }

                            @Override
                            public void onFail(Exception error) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openBottomSheetAndGallery();
            } else {

            }
        }
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            int granted = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted++;
                }
            }
            if (granted == grantResults.length) {
                onCameraClick();
            } else {
                Toast.makeText(this, getResources().getString(R.string.unavailible), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FilePickerFragment picker = FilePickerFragment.newInstance(null);
                picker.setFileFilter(new MyFileFilter());
                picker.setOnDirSelectedListener(this);
                picker.show(getFragmentManager(), null);
            } else {
                Toast.makeText(this, getResources().getString(R.string.unavailible), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ChatActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ChatActivity.ACTION_SEARCH_CHAT_FILES)) {
                search(true);
            } else if (intent.getAction() != null && intent.getAction().equals(ChatActivity.ACTION_SEARCH)) {
                search(false);
            } else if (intent.getAction() != null && intent.getAction().equals(ACTION_SEND_QUICK_MESSAGE)) {
                String message = intent.getStringExtra(ACTION_SEND_QUICK_MESSAGE);
                if (null != message) {
                    onQuickAnswer(message);
                }
            }
        }
    }

    public static class IntentBuilder {
        private Intent i;
        static IntentBuilder builder;
        private Context ctx;

        private IntentBuilder() {
        }

        public static IntentBuilder getBuilder(Context ctx, String clientId) {
            builder = new IntentBuilder();
            builder.i = new Intent(ctx, ChatActivity.class);
            builder.i.putExtra(TAG, clientId);
            builder.ctx = ctx;
            return builder;
        }

        public IntentBuilder setDefaultChatTitle(String title) {
            if (title == null) throw new IllegalArgumentException("null");
            builder.i.putExtra(TAG_DEF_TITLE, title);
            return builder;
        }

        public IntentBuilder setClientName(String clientName) {
            if (clientName == null) throw new IllegalArgumentException("null");
            builder.i.putExtra(TAG_CLIENT_NAME, clientName);
            return builder;
        }

        public IntentBuilder setPushIconResid(int resid) {
            builder.i.putExtra(TAG_PUSH_RESID, resid);
            return builder;
        }

        public IntentBuilder setWelcomeScreenAttrs(
                int logoResId
                , String titleText
                , String subtitleText
                , int textColorResId
                , float titleSize
                , float subtitleSize) {
            Bundle b = new Bundle();
            i.putExtra("bundle", b);
            b.putInt("logoResId", logoResId);
            b.putInt("textColorResId", textColorResId);
            b.putString("titleText", titleText);
            b.putString("subtitleText", subtitleText);
            b.putFloat("titleSize", titleSize);
            b.putFloat("subtitleSize", subtitleSize);
            return this;
        }

        public Intent build() {
            Intent i = builder.i;
            Bundle b = i.getBundleExtra("bundle");
            if (i.getStringExtra(TAG) == null)
                throw new IllegalStateException("you must provide clientId");
            if (i.getStringExtra(TAG_DEF_TITLE) == null)
                throw new IllegalStateException("you must provide default chat title string");
            if (i.getIntExtra(TAG_PUSH_RESID, -1) == -1)
                throw new IllegalStateException("you must provide default push icon resid");
            if (b.getInt("logoResId", -1) == -1)
                throw new IllegalStateException("you must provide logo resource id");
            if (b.getInt("textColorResId", -1) == -1)
                throw new IllegalStateException("you must provide textColorResId resource id");
            if (b.getString("titleText", null) == null)
                throw new IllegalStateException("you must provide titleText");
            if (b.getString("subtitleText", null) == null)
                throw new IllegalStateException("you must provide subtitleText");
            if (b.getFloat("titleSize", -1f) == -1f)
                throw new IllegalStateException("you must provide titleSize");
            if (b.getFloat("titleSize", -1f) == -1f)
                throw new IllegalStateException("you must provide subtitleSize");
            builder = null;
            return i;
        }
    }
}
