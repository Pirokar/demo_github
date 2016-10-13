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
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
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

import com.sequenia.threads.AnalyticsTracker;
import com.sequenia.threads.fragments.FilePickerFragment;
import com.sequenia.threads.fragments.QuickAnswerFragment;
import com.sequenia.threads.model.ConsultConnectionMessage;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.utils.Callback;
import com.sequenia.threads.utils.LateTextWatcher;
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
import java.util.UUID;

import static android.text.TextUtils.isEmpty;

/**
 *
 */
public class ChatActivity extends BaseActivity
        implements
        BottomSheetView.ButtonsListener
        , ChatAdapter.AdapterInterface
        , FilePickerFragment.SelectedListener {
    private static final String TAG = "ChatActivity ";
    private static final String TAG_DEF_TITLE = "TAG_DEF_TITLE";
    private static final String TAG_USER_NAME = "TAG_USER_NAME";
    private static final String TAG_PUSH_ICON_RESID = "TAG_PUSH_ICON_RESID";
    private static final String TAG_PUSH_TITLE = "TAG_PUSH_TITLE";
    private static final String TAG_GA_RESID = "TAG_GA_RESID";
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
        setContentView(R.layout.activity_chat_activity);
        getIncomingSettings(getIntent());
        initViews();
        initToolbar();
        initController();

    }

    private void getIncomingSettings(Intent intent) {
        if (intent.getStringExtra(TAG) == null && PrefUtils.getClientID(this) == null)
            throw new IllegalStateException("you must provide valid client id," +
                    "\r\n it is now null or it'ts length < 5");
        if (intent.getIntExtra(TAG_DEF_TITLE, -1) != -1) {
            PrefUtils.setDefaultChatTitle(this, getIntent().getIntExtra(TAG_DEF_TITLE, -1));
        }
        if (intent.getStringExtra(TAG_USER_NAME) != null) {
            PrefUtils.setUserName(this, intent.getStringExtra(TAG_USER_NAME));
        }
        if (intent.getBundleExtra("bundle") != null) {
            PrefUtils.setIncomingStyle(this, intent.getBundleExtra("bundle"));
        }
        if (intent.getIntExtra(TAG_PUSH_ICON_RESID, -1) != -1) {
            PrefUtils.setPushIconResid(this, intent.getIntExtra(TAG_PUSH_ICON_RESID, -1));
        }
        if (intent.getIntExtra(TAG_PUSH_TITLE, -1) != -1) {
            PrefUtils.setPushTitle(this, intent.getIntExtra(TAG_PUSH_TITLE, -1));
        }
        if (intent.getStringExtra(TAG_GA_RESID) != null) {
            PrefUtils.setGaTrackerId(this, intent.getStringExtra(TAG_GA_RESID));
        }
    }

    private void initController() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void notifyConsultAvatarChanged(final String newAvatarUrl, final String consultId) {
        h.post(new Runnable() {
            @Override
            public void run() {
                if (mChatAdapter != null) mChatAdapter.notifyAvatarChanged(newAvatarUrl, consultId);
            }
        });
    }

    private void initToolbar() {
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
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
        mWelcomeScreen.setText(getString(b.getInt("titleText")), getString(b.getInt("contentText")));
        mWelcomeScreen.setTitletextSize(b.getFloat("titleSize"));
        mWelcomeScreen.setSubtitleSize(b.getFloat("subtitleSize"));
        mSearchMoreButton = (Button) findViewById(R.id.search_more);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange);
        mConsultNameView = (TextView) findViewById(R.id.consult_name);
        mConsultTitle = (TextView) findViewById(R.id.subtitle);
        mCopyControls = findViewById(R.id.copy_controls);
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
                Log.e(TAG, "" + mQuote);// TODO: 13.10.2016
                List<UpcomingUserMessage> input = Arrays.asList(new UpcomingUserMessage[]{new UpcomingUserMessage(
                        mFileDescription
                        , mQuote
                        , mInputEditText.getText().toString().trim(), isCopy(mInputEditText.getText().toString()))});
                sendMessage(input, true);
            }
        });
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
        mInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
                    mWelcomeScreen.setVisibility(View.GONE);
                    mWelcomeScreen = null;
                }
            }
        });
        mInputEditText.addTextChangedListener(new LateTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mChatController.onUserTyping();
            }
        });
        mSearchMessageEditText.addTextChangedListener(new LateTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String request = "";
                if (!isInMessageSearchMode) return;
                if (s == null || s.length() == 0) {
                    request = UUID.randomUUID().toString();
                    mSearchMoreButton.setVisibility(View.GONE);
                } else {
                    request = s.toString();
                    if (mSearchMoreButton.getVisibility() == View.GONE)
                        mSearchMoreButton.setVisibility(View.VISIBLE);
                }
                if (!searchInFiles) {
                    mChatController.requestFilteredPhrases(false, request, new Callback<Pair<Boolean, List<ChatPhrase>>, Exception>() {
                        @Override
                        public void onSuccess(Pair<Boolean, List<ChatPhrase>> result) {
                            mChatAdapter.swapItems(result.second);
                        }

                        @Override
                        public void onFail(Exception error) {
                        }
                    });
                } else {
                    mChatController.requestFilteredFiles(false, request, new Callback<Pair<Boolean, List<ChatPhrase>>, Exception>() {
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
        });
    }

    private void sendMessage(List<UpcomingUserMessage> messages, boolean clearInput) {
        Log.e(TAG, "isInMessageSearchMode =" + isInMessageSearchMode);
        if (mChatController == null) return;
        for (UpcomingUserMessage message : messages) {
            mChatController.onUserInput(message);
        }
        if (null != mQuoteLayoutHolder && !isInMessageSearchMode)
            mQuoteLayoutHolder.setIsVisible(false);
        if (null != mChatAdapter) mChatAdapter.setAllMessagesRead();
        mBottomSheetView.setSelectedState(false);
        if (clearInput) {
            mInputEditText.setText("");
            if (!isInMessageSearchMode) mQuoteLayoutHolder.setIsVisible(false);
            mQuote = null;
            mFileDescription = null;
            setBottomStateDefault();
            hideCopyControls();
            mAttachedImages.clear();
            mBottomGallery.setVisibility(View.GONE);
            if (mChosenPhrase != null && mChatAdapter != null) {
                mChatAdapter.setItemChosen(false, mChosenPhrase);
                mChosenPhrase = null;
            }
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
                    new UpcomingUserMessage(new FileDescription(getString(R.string.I)
                            , photos.get(0)
                            , new File(photos.get(0).replaceAll("file://", "")).length()
                            , System.currentTimeMillis())
                            , null
                            , mInputEditText.getText().toString().trim()
                            , isCopy(mInputEditText.getText().toString()));
            mChatController.onUserInput(uum);
            mInputEditText.setText("");
            mQuoteLayoutHolder.setIsVisible(false);
            mQuote = null;
            mFileDescription = null;
            for (int i = 1; i < photos.size(); i++) {
                uum =
                        new UpcomingUserMessage(
                                new FileDescription(getString(R.string.I), photos.get(i), new File(photos.get(i).replaceAll("file://", "")).length(), System.currentTimeMillis())
                                , null
                                , null
                                , false);
                mChatController.onUserInput(uum);
            }
        } else if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            mFileDescription = new FileDescription(getResources().getString(R.string.image), data.getStringExtra(CameraActivity.IMAGE_EXTRA), new File(data.getStringExtra(CameraActivity.IMAGE_EXTRA).replace("file://", "")).length(), System.currentTimeMillis());
            UpcomingUserMessage uum = new UpcomingUserMessage(mFileDescription, null, null, false);
            sendMessage(Arrays.asList(new UpcomingUserMessage[]{uum}), true);
        }
    }

    private boolean isCopy(String text) {
        if (TextUtils.isEmpty(text)) return false;
        if (TextUtils.isEmpty(PrefUtils.getLastCopyText(this))) return false;
        return text.contains(PrefUtils.getLastCopyText(this));
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

            List<UpcomingUserMessage> messages = new ArrayList<>();
            messages.add(new UpcomingUserMessage(
                    new FileDescription(
                            getString(R.string.I)
                            , mAttachedImages.get(0)
                            , new File(mAttachedImages.get(0).replaceAll("file://", "")).length()
                            , System.currentTimeMillis())
                    , mQuote
                    , mInputEditText.getText().toString().trim()
                    , isCopy(mInputEditText.getText().toString())));
            for (int i = 1; i < mAttachedImages.size(); i++) {
                messages.add(new UpcomingUserMessage(
                        new FileDescription(getString(R.string.I), mAttachedImages.get(i), new File(mAttachedImages.get(i).replaceAll("file://", "")).length(), System.currentTimeMillis())
                        , null
                        , null
                        , false));
            }
            sendMessage(messages, true);
        }
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
        if (!isInMessageSearchMode) mSearchMessageEditText.setVisibility(View.GONE);
        if (!isInMessageSearchMode) mSearchMessageEditText.setText("");
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
                if (!isInMessageSearchMode)
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
        if (list.size() == 1 && list.get(0) instanceof ConsultTyping)
            return;//don't scroll if it is just typing item
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInMessageSearchMode)
                    mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }, 600);
    }

    public void showDownloading() {
    }

    public void removeDownloading() {
    }

    public void setMessageState(String messageId, MessageState state) {
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
                    mConsultNameView.setText(getString(PrefUtils.getDefaultChatTitle(ctx)));
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
        mChatController.onConsultChoose(this, consultConnectionMessage.getConsultId());
    }

    @Override
    public void onImageClick(ChatPhrase chatPhrase) {
        if (chatPhrase.getFileDescription().getFilePath() == null) return;
        if (chatPhrase instanceof UserPhrase) {
            if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_WAS_READ) {
                mChatController.checkAndResendPhrase((UserPhrase) chatPhrase);
            }
            if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_NOT_SENT) {
                startActivity(ImagesActivity.getStartIntent(this, chatPhrase.getFileDescription()));
            }
        } else if (chatPhrase instanceof ConsultPhrase) {
            AnalyticsTracker.getInstance(this, PrefUtils.getGaTrackerId(this)).setAttachmentWasOpened();
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
        final Context ctx = this;
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(new ClipData("", new String[]{"text/plain"}, new ClipData.Item(cp.getPhraseText())));
                hideCopyControls();
                PrefUtils.setLastCopyText(ctx, cp.getPhraseText());
                if (null != mChosenPhrase) unChooseItem(mChosenPhrase);
            }
        });
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String headerText = "";
                String text = cp.getPhraseText();
                hideCopyControls();
                mRecyclerView.scrollToPosition(position);
                FileDescription quoteFileDescription = cp.getFileDescription();
                if (quoteFileDescription == null && cp.getQuote() != null) {
                    quoteFileDescription = cp.getQuote().getFileDescription();
                }
                mQuote = new Quote(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? "" : text, quoteFileDescription, cp.getTimeStamp());
                mFileDescription = null;
                if (isEmpty(cp.getPhraseText())) {
                    mQuote = new Quote(headerText, cp.getPhraseText(), quoteFileDescription, System.currentTimeMillis());
                }
                if (cp instanceof UserPhrase) {
                    headerText = getString(R.string.I);
                    mQuote.setFromConsult(false);
                    mQuote.setPhraseOwnerTitle(headerText);
                } else if (cp instanceof ConsultPhrase) {
                    headerText = ((ConsultPhrase) cp).getConsultName();
                    mQuote.setFromConsult(true);
                    mQuote.setQuotedPhraseId(((ConsultPhrase) cp).getConsultId());
                    if (headerText == null) {
                        headerText = getString(R.string.consult);
                    }
                    mQuote.setPhraseOwnerTitle(headerText);
                }
                if (FileUtils.getExtensionFromFileDescription(cp.getFileDescription()) == FileUtils.JPEG
                        || FileUtils.getExtensionFromFileDescription(cp.getFileDescription()) == FileUtils.PNG) {
                    mQuoteLayoutHolder.setText(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? getString(R.string.image) : text, cp.getFileDescription().getFilePath());
                } else {
                    mQuoteLayoutHolder.setText(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? "" : text, null);
                }
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
            isInMessageSearchMode = false;
            mSearchMessageEditText.setText("");
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
        if (mQuoteLayoutHolder.isVisible()) {
            mQuoteLayoutHolder.setIsVisible(false);
            if (mChatAdapter != null && mChosenPhrase != null) {
                mChatAdapter.setItemChosen(false, mChosenPhrase);
            }
            return;
        }
        if (isNeedToClose) {
            super.onBackPressed();
        }
    }

    public void setPhraseSentStatus(String id, MessageState messageState) {
        Log.i(TAG, "setPhraseSentStatus: " + messageState);
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

    public void setAllMessagesWereRead() {
        if (null != mChatAdapter) {
            mChatAdapter.setAllMessagesRead();
        }
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
                    .centerCrop()
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
        final ChatActivity activity = this;
        h.post(new Runnable() {
            @Override
            public void run() {
                mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), activity, activity);
                mRecyclerView.setAdapter(mChatAdapter);
                setTitleStateDefault();
                if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
                    mWelcomeScreen.setVisibility(View.GONE);
                    mWelcomeScreen = null;
                }
                mInputEditText.clearFocus();
                showHelloScreen();
            }
        });
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
            AnalyticsTracker.getInstance(this, PrefUtils.getGaTrackerId(this)).setTextSearchWasOpened();
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

        public IntentBuilder setDefaultChatTitle(@StringRes int resId) {
            builder.i.putExtra(TAG_DEF_TITLE, resId);
            return builder;
        }

        public IntentBuilder setUserName(String clientName) {
            if (clientName == null) throw new IllegalArgumentException("null");
            builder.i.putExtra(TAG_USER_NAME, clientName);
            return builder;
        }

        public IntentBuilder setPushStyle(@DrawableRes int defIconResid, @StringRes int DefTitleResId) {
            builder.i.putExtra(TAG_PUSH_ICON_RESID, defIconResid);
            builder.i.putExtra(TAG_PUSH_TITLE, DefTitleResId);
            return builder;
        }

        public IntentBuilder setGATrackerId(String GATrackerId) {
            builder.i.putExtra(TAG_GA_RESID, GATrackerId);
            return builder;
        }

        public IntentBuilder setWelcomeScreenAttrs(
                @DrawableRes int logoResId
                , @StringRes int titleText
                , @StringRes int subtitleText
                , @ColorRes int textColorResId
                , float titleSize
                , float subtitleSize) {
            Bundle b = new Bundle();
            i.putExtra("bundle", b);
            b.putInt("logoResId", logoResId);
            b.putInt("textColorResId", textColorResId);
            b.putInt("titleText", titleText);
            b.putInt("contentText", subtitleText);
            b.putFloat("titleSize", titleSize);
            b.putFloat("subtitleSize", subtitleSize);
            return this;
        }

        public Intent build() {
            Intent i = builder.i;
            Bundle b = i.getBundleExtra("bundle");
            if (i.getStringExtra(TAG) == null)
                throw new IllegalStateException("you must provide clientId");
            if (i.getIntExtra(TAG_DEF_TITLE, -1) == -1)
                Log.e(TAG, "you must provide default chat res id");
            if (i.getIntExtra(TAG_PUSH_ICON_RESID, -1) == -1)
                Log.e(TAG, "you must provide default push icon resid");
            if (i.getIntExtra(TAG_PUSH_TITLE, -1) == -1)
                Log.e(TAG, "you must provide default push pushTitle");
            if (b.getInt("logoResId", -1) == -1)
                Log.e(TAG, "you must provide logo resource id");
            if (b.getInt("textColorResId", -1) == -1)
                Log.e(TAG, "you must provide textColorResId resource id");
            if (i.getStringExtra(TAG_GA_RESID) == null)
                Log.e(TAG, "you must provide google analytics id");
            if (b.getInt("titleText", -1) == -1)
                Log.e(TAG, "you must provide titleText");
            if (b.getInt("contentText", -1) == -1)
                Log.e(TAG, "you must provide contentText");
            if (b.getFloat("titleSize", -1f) == -1f)
                Log.e(TAG, "you must provide titleSize");
            if (b.getFloat("titleSize", -1f) == -1f)
                Log.e(TAG, "you must provide subtitleSize");

            builder = null;
            return i;
        }


    }
}
