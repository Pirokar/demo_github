package im.threads.activities;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import im.threads.AnalyticsTracker;
import im.threads.R;
import im.threads.adapters.BottomGalleryAdapter;
import im.threads.adapters.ChatAdapter;
import im.threads.controllers.ChatController;
import im.threads.fragments.FilePickerFragment;
import im.threads.fragments.NoConnectionDialogFragment;
import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.ChatStyle;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultPhrase;
import im.threads.model.ConsultTyping;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.Quote;
import im.threads.model.UpcomingUserMessage;
import im.threads.model.UserPhrase;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.Callback;
import im.threads.utils.CallbackNoError;
import im.threads.utils.FileUtils;
import im.threads.utils.LateTextWatcher;
import im.threads.utils.MyFileFilter;
import im.threads.utils.PermissionChecker;
import im.threads.utils.PrefUtils;
import im.threads.views.BottomGallery;
import im.threads.views.BottomSheetView;
import im.threads.views.MySwipeRefreshLayout;
import im.threads.views.WelcomeScreen;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public static final String TAG_GAENABLED = "TAG_GAENABLED";
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
    private View mSearchLo;
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
    private ChatStyle style;
    private Handler mSearchHandler = new Handler(Looper.getMainLooper());

    private ImageButton searchUp;
    private ImageButton searchDown;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIncomingSettings(getIntent());
        if (style.chatStatusBarColorResId != ChatStyle.INVALID && Build.VERSION.SDK_INT > 20) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(style.chatStatusBarColorResId));
        }
        setContentView(R.layout.activity_chat_activity);
        initViews();
        initToolbar();
        initController();
        setActivityStyle(PrefUtils.getIncomingStyle(this));
    }

    private void getIncomingSettings(Intent intent) {
        if (intent.getStringExtra("clientId") == null && PrefUtils.getClientID(this).equals(""))
            throw new IllegalStateException("you must provide valid client id," +
                    "\r\n it is now null or it'ts length < 5");
        if (intent.getBooleanExtra("style", false)) {
            ChatStyle style = ChatStyle.styleFromIntent(intent);
            PrefUtils.setIncomingStyle(this, style);
        }
        if (intent.getStringExtra("userName") != null)
            PrefUtils.setUserName(this, intent.getStringExtra("userName"));
        style = PrefUtils.getIncomingStyle(this);

    }

    private void initController() {
        mChatController = ChatController.getInstance(this, getIntent().getStringExtra("clientId"));
        mChatController.bindActivity(this);
        if (mChatController.isNeedToShowWelcome()) mWelcomeScreen.setVisibility(View.VISIBLE);
        mChatActivityReceiver = new ChatActivityReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SEARCH_CHAT_FILES);
        intentFilter.addAction(ACTION_SEARCH);
        intentFilter.addAction(ACTION_SEND_QUICK_MESSAGE);
        registerReceiver(mChatActivityReceiver, intentFilter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchMenuItem = menu.getItem(0);
        SpannableString s = new SpannableString(searchMenuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, style.menuItemTextColorResId)), 0, s.length(), 0);
        searchMenuItem.setTitle(s);

        MenuItem filesAndMedia = menu.getItem(1);
        SpannableString s2 = new SpannableString(filesAndMedia.getTitle());
        s2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, style.menuItemTextColorResId)), 0, s2.length(), 0);
        filesAndMedia.setTitle(s2);

        return super.onPrepareOptionsMenu(menu);
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
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.showOverflowMenu();
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
        mSearchLo = findViewById(R.id.search_lo);
        searchUp = (ImageButton) findViewById(R.id.search_up_ib);
        searchDown = (ImageButton) findViewById(R.id.search_down_ib);
        if (style != null && style.chatToolbarTextColorResId != ChatStyle.INVALID) {
            searchUp.getDrawable().setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
            searchUp.setAlpha(0.5f);
            searchDown.getDrawable().setColorFilter(ContextCompat.getColor(this, style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
            searchDown.setAlpha(0.5f);
        }
        mWelcomeScreen = (WelcomeScreen) findViewById(R.id.welcome);
        mSearchMoreButton = (Button) findViewById(R.id.search_more);
        mSearchMoreButton.setBackgroundColor(ContextCompat.getColor(this, style.welcomeScreenTextColorResId));
        mSearchMoreButton.setTextColor(ContextCompat.getColor(this, style.welcomeScreenTextColorResId));
        mSwipeRefreshLayout.setColorSchemeResources(style.chatToolbarColorResId);
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
        searchUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(mSearchMessageEditText.getText())) return;
                doFancySearch(mSearchMessageEditText.getText().toString(), true);
            }
        });
        searchDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmpty(mSearchMessageEditText.getText())) return;
                doFancySearch(mSearchMessageEditText.getText().toString(), false);
            }
        });
        mSearchMessageEditText.addTextChangedListener(new LateTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String request = "";
                if (!isInMessageSearchMode) return;
                doFancySearch(s.toString(), true);
            }
        });
    }

    private void doFancySearch(final String request,
                               final boolean forward) {
        if (isEmpty(request)) {
            mChatAdapter.removeHighlight();
            mSearchHandler.removeCallbacksAndMessages(null);
            searchUp.setAlpha(0.5f);
            searchDown.setAlpha(0.5f);
            return;
        }
        mSearchHandler.removeCallbacksAndMessages(null);
        mSearchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final ChatPhrase[] highlighted = {null};
                mChatController.fancySearch(request, forward, new CallbackNoError<List<ChatItem>>() {
                    @Override
                    public void onCall(final List<ChatItem> data) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                int first = -1;
                                int last = -1;

                                //для поиска - ищем индекс первого совпадения
                                for (int i = 0; i < data.size(); i++) {
                                    if (data.get(i) instanceof ChatPhrase) {
                                        if (((ChatPhrase) data.get(i)).isFound()) {
                                            first = i;
                                            break;
                                        }
                                    }
                                }
                                //для поиска - ищем индекс последнего совпадения
                                for (int i = data.size() - 1; i >= 0; i--) {
                                    if (data.get(i) instanceof ChatPhrase) {
                                        if (((ChatPhrase) data.get(i)).isFound()) {
                                            last = i;
                                            break;
                                        }
                                    }
                                }

                                for (int i = 0; i < data.size(); i++) {
                                    if (data.get(i) instanceof ChatPhrase) {
                                        if (((ChatPhrase) data.get(i)).isHighlight()) {
                                            highlighted[0] = (ChatPhrase) data.get(i);

                                            //для поиска - если можно перемещаться, подсвечиваем
                                            if (first != -1 && i > first) {
                                                searchDown.setAlpha(1.0f);
                                            } else {
                                                searchDown.setAlpha(0.5f);
                                            }
                                            //для поиска - если можно перемещаться, подсвечиваем
                                            if (last != -1 && i < last) {
                                                searchUp.setAlpha(1.0f);
                                            } else {
                                                searchUp.setAlpha(0.5f);
                                            }

                                            break;
                                        }
                                    }
                                }



                                mChatAdapter.addItems(data);
                                mChatAdapter.removeHighlight();
                            }
                        });
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (highlighted[0] == null) return;
                                int index = mChatAdapter.setItemHighlighted(highlighted[0]);
                                if (index != -1) mRecyclerView.scrollToPosition(index);
                            }
                        }, 60);
                    }
                });
            }
        }, 400);
    }


    @Override
    protected void setActivityStyle(ChatStyle style) {
        if (style != null) {
            if (style.chatBackgroundColor != ChatStyle.INVALID)
                findViewById(R.id.chat_root).setBackgroundColor(getColorInt(style.chatBackgroundColor));
            if (style.chatMessageInputColor != ChatStyle.INVALID) {
                findViewById(R.id.input_layout).setBackgroundColor(getColorInt(style.chatMessageInputColor));
                findViewById(R.id.input).setBackgroundColor(getColorInt(style.chatMessageInputColor));
                mBottomSheetView.setBackgroundColor(getColorInt(style.chatMessageInputColor));
                mCopyControls.setBackgroundColor(getColorInt(style.chatMessageInputColor));
                mBottomGallery.setBackgroundColor(getColorInt(style.chatMessageInputColor));
                findViewById(R.id.bottom_layout).setBackgroundColor(getColorInt(style.chatMessageInputColor));
            }
            if (style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                mSearchMessageEditText.setTextColor(getColorInt(style.chatToolbarTextColorResId));
                mToolbar.getOverflowIcon().setColorFilter(new PorterDuffColorFilter(getResources().getColor(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP));
                mToolbar.getNavigationIcon().setColorFilter(new PorterDuffColorFilter(getResources().getColor(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP));
            }
            if (style.chatMessageInputHintTextColor != ChatStyle.INVALID) {
                mInputEditText.setHintTextColor(getColorInt(style.chatMessageInputHintTextColor));
                mSearchMessageEditText.setHintTextColor(getColorInt(style.chatToolbarHintTextColor));

            }
            if (style.incomingMessageTextColor != ChatStyle.INVALID) {
                mInputEditText.setTextColor(getColorInt(style.incomingMessageTextColor));
            }
            if (style.chatBodyIconsTint != ChatStyle.INVALID) {
                @IdRes int views[] = {
                        R.id.content_copy
                        , R.id.reply
                        , R.id.send_message
                        , R.id.add_attachment
                        , R.id.quote_clear
                        , R.id.quote_clear,};
                for (@IdRes int i : views) {
                    ImageView iv = (ImageView) findViewById(i);
                    setTint(iv, style.chatBodyIconsTint);
                }
                mBottomSheetView.setButtonsTint(style.chatBodyIconsTint);
            }
            if (style.welcomeScreenLogoResId != ChatStyle.INVALID)
                mWelcomeScreen.setLogo(style.welcomeScreenLogoResId);
            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID)
                mWelcomeScreen.setTextColor(style.welcomeScreenTextColorResId);
            if (style.welcomeScreenTitleTextResId != ChatStyle.INVALID)
                mWelcomeScreen.setText(getString(style.welcomeScreenTitleTextResId), getString(style.welcomeScreenSubtitleTextResId));
            if (style.titleSizeInSp != ChatStyle.INVALID)
                mWelcomeScreen.setTitletextSize(style.titleSizeInSp);
            if (style.subtitleSizeInSp != ChatStyle.INVALID)
                mWelcomeScreen.setSubtitleSize(style.subtitleSizeInSp);
            if (style.chatBackgroundColor != ChatStyle.INVALID)
                mWelcomeScreen.setBackground(style.chatBackgroundColor);

        }
        Drawable overflowDrawable = mToolbar.getOverflowIcon();
        if (style != null) {
            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, style.chatToolbarColorResId));
            }
            if (style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                mConsultTitle.setTextColor(ContextCompat.getColor(this, style.chatToolbarTextColorResId));
                mConsultNameView.setTextColor(ContextCompat.getColor(this, style.chatToolbarTextColorResId));
            }

        }

        try {
            if (style != null && style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP));
            } else {
                overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    private void sendMessage(List<UpcomingUserMessage> messages, boolean clearInput) {
        Log.i(TAG, "isInMessageSearchMode =" + isInMessageSearchMode);
        if (mChatController == null) return;
        for (UpcomingUserMessage message : messages) {
            mChatController.onUserInput(message);
        }
        if (null != mQuoteLayoutHolder)
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
            if (isInMessageSearchMode) onBackPressed();
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

        Log.i(TAG, "onFileSelected: " + fileOrDirectory);

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
        if (!isInMessageSearchMode) mSearchLo.setVisibility(View.GONE);
        if (!isInMessageSearchMode) mSearchMessageEditText.setText("");
        mBottomGallery.setVisibility(View.GONE);
    }

    public void addChatItem(ChatItem item) {
        if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
            mWelcomeScreen.setVisibility(View.GONE);
            mWelcomeScreen = null;
        }
        boolean isUserSeesMessage = (mChatAdapter.getItemCount() - ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition()) < 40;
        if (item instanceof ConsultPhrase) {
            if (isUserSeesMessage && isResumed && !isInMessageSearchMode) {
                ((ConsultPhrase) item).setRead(true);
            } else {
                ((ConsultPhrase) item).setRead(false);
            }
        }
        mChatAdapter.addItems(Arrays.asList(item));
        if (item instanceof ConsultPhrase) {
            mChatAdapter.setAvatar(((ConsultPhrase) item).getConsultId(), ((ConsultPhrase) item).getAvatarPath());
        }
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInMessageSearchMode) {
                    if ((mChatAdapter.getItemCount() - ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition()) < 40) {
                        mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
                    }
                }
            }
        }, 100);
    }

    public void addChatItems(final List<ChatItem> list) {
        if (list.size() == 0) return;
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


    public void removeSearching() {
        if (null != mChatAdapter) mChatAdapter.removeConsultSearching();
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
                    mSearchLo.setVisibility(View.GONE);
                    mSearchMessageEditText.setText("");
                    if (style != null && style.chatTitleTextResId != ChatStyle.INVALID) {
                        mConsultNameView.setText(style.chatTitleTextResId);
                    } else {
                        mConsultNameView.setText(getString(R.string.contact_center));
                    }
                }
                connectedConsultId = String.valueOf(-1);
            }
        }, 50);
    }

    private void setTitleStateSearchingConsult() {
        if (isInMessageSearchMode) return;
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.VISIBLE);
        mSearchLo.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        mConsultNameView.setText(getResources().getString(R.string.searching_operator));
    }

    public void setStateSearchingConsult() {
        mToolbar.hideOverflowMenu();
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
        mSearchLo.setVisibility(View.VISIBLE);
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
            mSearchLo.setVisibility(View.GONE);
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
        if (t instanceof UnknownHostException) {
            Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show();
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
        if (style != null && style.chatBodyIconsTint != ChatStyle.INVALID) {
            Drawable d = getResources().getDrawable(R.drawable.ic_arrow_back_blue_24dp);
            d.setColorFilter(getColorInt(style.chatBodyIconsTint), PorterDuff.Mode.SRC_ATOP);
            mToolbar.setNavigationIcon(d);
            mToolbar.getOverflowIcon().setColorFilter(getColorInt(style.chatBodyIconsTint), PorterDuff.Mode.SRC_ATOP);
        } else mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        if (style != null && style.chatMessageInputColor != ChatStyle.INVALID) {
            mToolbar.setBackgroundColor(getColorInt(style.chatMessageInputColor));
        } else {
            mToolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
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
                } else if (FileUtils.getExtensionFromFileDescription(cp.getFileDescription()) == FileUtils.PDF) {
                    String fileName = "";
                    try {
                        fileName = cp.getFileDescription().getIncomingName() == null ? FileUtils.getLastPathSegment((cp.getFileDescription().getFilePath())) : cp.getFileDescription().getIncomingName();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mQuoteLayoutHolder.setText(isEmpty(headerText) ? "" : headerText,
                            fileName,
                            null);
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
        if (style != null && style.chatToolbarTextColorResId != ChatStyle.INVALID) {
            Drawable d = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            d.setColorFilter(getColorInt(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
            mToolbar.setNavigationIcon(d);
            mToolbar.getOverflowIcon().setColorFilter(getColorInt(style.chatToolbarTextColorResId), PorterDuff.Mode.SRC_ATOP);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
        if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
            mToolbar.setBackgroundColor(getColorInt(style.chatToolbarColorResId));
        } else {
            mToolbar.setBackgroundColor(getResources().getColor(R.color.green_light));
        }


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
        if (null != mChatAdapter) mChatAdapter.removeHighlight();
        boolean isNeedToClose = true;
        if (mBottomSheetView.getVisibility() == View.VISIBLE && mBottomGallery.getVisibility() == View.VISIBLE) {
            mBottomSheetView.setVisibility(View.GONE);
            findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
            onHideClick();
            return;
        }
        if (mBottomSheetView.getVisibility() == View.VISIBLE) {
            mBottomSheetView.setVisibility(View.GONE);
            findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
            return;
        }
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
        if (mSearchLo.getVisibility() == View.VISIBLE) {
            mSearchLo.setVisibility(View.GONE);
            setMenuVisibility(true);
            isInMessageSearchMode = false;
            mSearchMessageEditText.setText("");
            //  mChatAdapter.undoClear();
            mRecyclerView.scrollToPosition(mChatAdapter.getCurrentItemCount() - 1);
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
            mQuote = null;
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
            mHeader.setTextColor(ContextCompat.getColor(ChatActivity.this, style.incomingMessageTextColor));
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

    public void showFullError(String error) {
        AlertDialog d = new AlertDialog
                .Builder(this)
                .setMessage(error)
                .setCancelable(true)
                .create();
        d.show();
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
            if (isInMessageSearchMode) onBackPressed();
            startActivity(FilesActivity.getStartIntetent(this));
            return true;
        }

        if (item.getItemId() == R.id.search && isInMessageSearchMode) {
            return true;

        } else if (item.getItemId() == R.id.search) {
            if (mWelcomeScreen != null) {
                mWelcomeScreen.setVisibility(View.GONE);
                ((ViewGroup) findViewById(android.R.id.content)).removeView(mWelcomeScreen);
                mWelcomeScreen = null;
            }
            AnalyticsTracker.getInstance(this, PrefUtils.getGaTrackerId(this)).setTextSearchWasOpened();
            search(false);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMenuVisibility(boolean isVisible) {
        if (isVisible) {
            mToolbar.getMenu().findItem(R.id.search).setVisible(true);
            mToolbar.getMenu().findItem(R.id.files_and_media).setVisible(true);
        } else {
            mToolbar.getMenu().findItem(R.id.search).setVisible(false);
            mToolbar.getMenu().findItem(R.id.files_and_media).setVisible(false);
        }
    }

    private void search(final boolean searchInFiles) {
        Log.i(TAG, "searchInFiles: " + searchInFiles);
        isInMessageSearchMode = true;
        this.searchInFiles = searchInFiles;
        setBottomStateDefault();
        setTitleStateSearchingMessage();
        mSearchMessageEditText.requestFocus();
        mToolbar.hideOverflowMenu();
        setMenuVisibility(false);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchMessageEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
        //   mChatAdapter.backupAndClear();
        mSwipeRefreshLayout.setEnabled(false);

        mSearchMoreButton.setVisibility(View.GONE);

      /*  mSearchMoreButton.setOnClickListener(new View.OnClickListener() {
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
        });*/
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

    private void setTint(ImageView view, @ColorRes int colorRes) {
        @ColorInt int color = ContextCompat.getColor(this, colorRes);
        view.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public ChatStyle getStyle() {
        return style;
    }
}