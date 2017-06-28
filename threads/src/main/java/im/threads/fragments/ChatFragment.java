package im.threads.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import im.threads.AnalyticsTracker;
import im.threads.R;
import im.threads.activities.CameraActivity;
import im.threads.activities.ChatActivity;
import im.threads.activities.FilesActivity;
import im.threads.activities.GalleryActivity;
import im.threads.activities.ImagesActivity;
import im.threads.adapters.BottomGalleryAdapter;
import im.threads.adapters.ChatAdapter;
import im.threads.controllers.ChatController;
import im.threads.model.ChatItem;
import im.threads.model.ChatPhrase;
import im.threads.model.ChatStyle;
import im.threads.model.ConsultConnectionMessage;
import im.threads.model.ConsultPhrase;
import im.threads.model.ConsultTyping;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.Quote;
import im.threads.model.ScheduleInfo;
import im.threads.model.Survey;
import im.threads.model.UpcomingUserMessage;
import im.threads.model.UserPhrase;
import im.threads.permissions.PermissionsActivity;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.Callback;
import im.threads.utils.CallbackNoError;
import im.threads.utils.ColorsHelper;
import im.threads.utils.FileUtils;
import im.threads.utils.LateTextWatcher;
import im.threads.utils.MyFileFilter;
import im.threads.utils.PermissionChecker;
import im.threads.utils.PrefUtils;
import im.threads.views.BottomGallery;
import im.threads.views.BottomSheetView;
import im.threads.views.MySwipeRefreshLayout;
import im.threads.views.WelcomeScreen;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.text.TextUtils.isEmpty;

/**
 * Весь функционал чата находится здесь во фрагменте,
 * чтобы чат можно было встроить в приложене в навигацией на фрагментах
 * Created by chybakut2004 on 10.04.17.
 */

public class ChatFragment extends Fragment implements
        BottomSheetView.ButtonsListener,
        ChatAdapter.AdapterInterface,
        FilePickerFragment.SelectedListener,
        PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "ChatFragment ";

    private static AnalyticsTracker tracker;

    public static final int REQUEST_CODE_PHOTOS = 100;
    public static final int REQUEST_CODE_PHOTO = 101;
    public static final int REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY = 102;
    public static final int REQUEST_PERMISSION_CAMERA = 103;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL = 104;

    public static final String ACTION_SEARCH_CHAT_FILES = "ACTION_SEARCH_CHAT_FILES";
    public static final String ACTION_SEARCH = "ACTION_SEARCH";
    public static final String ACTION_SEND_QUICK_MESSAGE = "ACTION_SEND_QUICK_MESSAGE";

    private static final float DISABLED_ALPHA = 0.5f;
    private static final float ENABLED_ALPHA = 1.0f;

    private static boolean chatIsShown = false;

    private View rootView;

    private Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private Handler h = new Handler(Looper.getMainLooper());

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
    private ImageButton backButton;
    private ImageButton popupMenuButton;
    private ImageButton SendButton;
    private ImageButton AddAttachmentButton;
    private String connectedConsultId;
    private ChatReceiver mChatReceiver;

    private boolean isInMessageSearchMode;
    private boolean isResumed;
    private ChatStyle style;

    private ImageButton searchUp;
    private ImageButton searchDown;

    public static ChatFragment newInstance(Bundle bundle) {
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        Context context = activity.getApplicationContext();

        style = ChatStyle.getStyleFromBundleWithThrow(activity, getArguments());

        // Статус бар подкрашивается только при использовании чата в стандартном Activity.

        if (activity instanceof ChatActivity) {
            if (style != null && style.chatStatusBarColorResId != ChatStyle.INVALID) {
                ColorsHelper.setStatusBarColor(activity, style.chatStatusBarColorResId);
            }
        }

        rootView = inflater.inflate(R.layout.fragment_chat_fragment, container, false);

        initViews();
        bindViews();
        initToolbar();
        setHasOptionsMenu(true);
        initController();
        setFragmentStyle(PrefUtils.getIncomingStyle(activity));
        sendOpenChatAnalyticsEvent(context);

        chatIsShown = true;

        return rootView;
    }

    private void sendOpenChatAnalyticsEvent(Context context) {
        if (tracker == null) {
            tracker = AnalyticsTracker.getInstance(context, PrefUtils.getGaTrackerId(context));
        }
        tracker.chatWasOpened(PrefUtils.getClientID(context));
        tracker.setUserEnteredChat();
    }

    private void initController() {
        Activity activity = getActivity();
        Bundle bundle = getArguments();
        mChatController = ChatController.getInstance(activity, bundle == null ? null : bundle.getString("clientId"));
        mChatController.bindFragment(this);
        if (mChatController.isNeedToShowWelcome()) {
            mWelcomeScreen.setVisibility(View.VISIBLE);
        }
        mChatReceiver = new ChatReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SEARCH_CHAT_FILES);
        intentFilter.addAction(ACTION_SEARCH);
        intentFilter.addAction(ACTION_SEND_QUICK_MESSAGE);
        activity.registerReceiver(mChatReceiver, intentFilter);


        String token = PrefUtils.getDeviceAddress(getActivity()) + ":" + PrefUtils.getClientID(getActivity());
        Log.i("TOKEN", token);
    }

    private void initViews() {
        Activity activity = getActivity();

        mQuoteLayoutHolder = new QuoteLayoutHolder();
        mBottomSheetView = (BottomSheetView) rootView.findViewById(R.id.file_input_sheet);
        mBottomSheetView.setButtonsListener(this);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mSearchMessageEditText = (AppCompatEditText) rootView.findViewById(R.id.search);
        mBottomGallery = (BottomGallery) rootView.findViewById(R.id.bottom_gallery);
        mSwipeRefreshLayout = (MySwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        mInputEditText = (EditText) rootView.findViewById(R.id.input);
        SendButton = (ImageButton) rootView.findViewById(R.id.send_message);
        AddAttachmentButton = (ImageButton) rootView.findViewById(R.id.add_attachment);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), activity, this);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setAdapter(mChatAdapter);

        mSearchLo = rootView.findViewById(R.id.search_lo);
        searchUp = (ImageButton) rootView.findViewById(R.id.search_up_ib);
        searchDown = (ImageButton) rootView.findViewById(R.id.search_down_ib);

        mWelcomeScreen = (WelcomeScreen) rootView.findViewById(R.id.welcome);
        mSearchMoreButton = (Button) rootView.findViewById(R.id.search_more);
        mConsultNameView = (TextView) rootView.findViewById(R.id.consult_name);
        mConsultTitle = (TextView) rootView.findViewById(R.id.subtitle);
        mCopyControls = rootView.findViewById(R.id.copy_controls);

        searchDown.setAlpha(DISABLED_ALPHA);
        searchUp.setAlpha(DISABLED_ALPHA);

        if (style != null && style.chatToolbarTextColorResId != ChatStyle.INVALID) {
            ColorsHelper.setDrawableColor(activity, searchUp.getDrawable(), style.chatToolbarTextColorResId);
            ColorsHelper.setDrawableColor(activity, searchDown.getDrawable(), style.chatToolbarTextColorResId);
        }

        if (style != null && style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
            mSearchMoreButton.setBackgroundColor(ContextCompat.getColor(activity, style.welcomeScreenTextColorResId));
            mSearchMoreButton.setTextColor(ContextCompat.getColor(activity, style.welcomeScreenTextColorResId));
        }

        if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
            mSwipeRefreshLayout.setColorSchemeResources(style.chatToolbarColorResId);
        }

    }

    private void bindViews() {
        mSwipeRefreshLayout.setmSwipeListener(new MySwipeRefreshLayout.SwipeListener() {
            @Override
            public void onSwipe() {
                //finish();
            }
        });

        AddAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetAndGallery();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ChatFragment.this.onRefresh();
            }
        });

        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonClick();
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
                if (!isInMessageSearchMode) return;
                doFancySearch(s.toString(), true);
            }
        });
    }

    private void onSendButtonClick() {
        if (mInputEditText.getText().length() == 0 && ((mQuote == null) && (mFileDescription == null))) {
            return;
        }

        if (mWelcomeScreen != null && mWelcomeScreen.getVisibility() == View.VISIBLE) {
            mWelcomeScreen.setVisibility(View.GONE);
            mWelcomeScreen = null;
        }

        List<UpcomingUserMessage> input = new ArrayList<>();
        UpcomingUserMessage message = new UpcomingUserMessage(
                mFileDescription,
                mQuote,
                mInputEditText.getText().toString().trim(),
                isCopy(mInputEditText.getText().toString())
        );
        input.add(message);
        sendMessage(input, true);
    }

    private void onRefresh() {
        mChatController.requestItems(new Callback<List<ChatItem>, Throwable>() {
            @Override
            public void onSuccess(final List<ChatItem> result) {
                final Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        afterRefresh(result);
                    }
                }, 500);
            }

            @Override
            public void onFail(Throwable error) {
            }
        });
    }

    private void afterRefresh(List<ChatItem> result) {
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

    protected void setFragmentStyle(ChatStyle style) {
        Activity activity = getActivity();

        if (style != null) {
            if (style.chatBackgroundColor != ChatStyle.INVALID) {
                ColorsHelper.setBackgroundColor(activity, rootView.findViewById(R.id.chat_root), style.chatBackgroundColor);
            }

            if (style.chatMessageInputColor != ChatStyle.INVALID) {
                ColorsHelper.setBackgroundColor(activity, rootView.findViewById(R.id.input_layout), style.chatMessageInputColor);
                ColorsHelper.setBackgroundColor(activity, rootView.findViewById(R.id.input), style.chatMessageInputColor);
                ColorsHelper.setBackgroundColor(activity, mBottomSheetView, style.chatMessageInputColor);
                ColorsHelper.setBackgroundColor(activity, mCopyControls, style.chatMessageInputColor);
                ColorsHelper.setBackgroundColor(activity, mBottomGallery, style.chatMessageInputColor);
                ColorsHelper.setBackgroundColor(activity, rootView.findViewById(R.id.bottom_layout), style.chatMessageInputColor);
            }

            if (style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                ColorsHelper.setTextColor(activity, mSearchMessageEditText, style.chatToolbarTextColorResId);
                ColorsHelper.setDrawableColor(activity, popupMenuButton.getDrawable(), style.chatToolbarTextColorResId);
                ColorsHelper.setDrawableColor(activity, backButton.getDrawable(), style.chatToolbarTextColorResId);
                ColorsHelper.setTextColor(activity, mConsultTitle, style.chatToolbarTextColorResId);
                ColorsHelper.setTextColor(activity, mConsultNameView, style.chatToolbarTextColorResId);
            }

            if (style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                ColorsHelper.setTextColor(activity, mConsultTitle, style.chatToolbarTextColorResId);
                ColorsHelper.setTextColor(activity, mConsultNameView, style.chatToolbarTextColorResId);
            } else {
                ColorsHelper.setTextColor(activity, mConsultTitle, android.R.color.white);
                ColorsHelper.setTextColor(activity, mConsultNameView, android.R.color.white);
            }

            if (style.chatMessageInputHintTextColor != ChatStyle.INVALID) {
                ColorsHelper.setHintTextColor(activity, mInputEditText, style.chatMessageInputHintTextColor);
            }

            if (style.chatToolbarHintTextColor != ChatStyle.INVALID) {
                ColorsHelper.setHintTextColor(activity, mSearchMessageEditText, style.chatToolbarHintTextColor);
            }

            if (style.inputTextColor != ChatStyle.INVALID) {
                ColorsHelper.setTextColor(activity, this.mInputEditText, style.inputTextColor);
            } else if (style.incomingMessageTextColor != ChatStyle.INVALID) {
                ColorsHelper.setTextColor(activity, this.mInputEditText, style.incomingMessageTextColor);
            }

            if (style.inputTextFont != null) {
                try {
                    Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), style.inputTextFont);
                    this.mInputEditText.setTypeface(custom_font);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (style.chatBodyIconsTint != ChatStyle.INVALID) {
                ColorsHelper.setTint(activity, (ImageView) rootView.findViewById(R.id.content_copy), style.chatBodyIconsTint);
                ColorsHelper.setTint(activity, (ImageView) rootView.findViewById(R.id.reply), style.chatBodyIconsTint);
                ColorsHelper.setTint(activity, (ImageView) rootView.findViewById(R.id.send_message), style.chatBodyIconsTint);
                ColorsHelper.setTint(activity, (ImageView) rootView.findViewById(R.id.add_attachment), style.chatBodyIconsTint);
                ColorsHelper.setTint(activity, (ImageView) rootView.findViewById(R.id.quote_clear), style.chatBodyIconsTint);
                mBottomSheetView.setButtonsTint(style.chatBodyIconsTint);
            }

            if (style.welcomeScreenLogoResId != ChatStyle.INVALID) {
                mWelcomeScreen.setLogo(style.welcomeScreenLogoResId);
            }

            if (style.welcomeScreenTextColorResId != ChatStyle.INVALID) {
                mWelcomeScreen.setTextColor(style.welcomeScreenTextColorResId);
            }

            if (style.welcomeScreenTitleTextResId != ChatStyle.INVALID && style.welcomeScreenSubtitleTextResId != ChatStyle.INVALID) {
                mWelcomeScreen.setText(getString(style.welcomeScreenTitleTextResId), getString(style.welcomeScreenSubtitleTextResId));
            }

            if (style.welcomeScreenTitleSizeInSp != ChatStyle.INVALID) {
                mWelcomeScreen.setTitletextSize(style.welcomeScreenTitleSizeInSp);
            }

            if (style.welcomeScreenSubtitleSizeInSp != ChatStyle.INVALID) {
                mWelcomeScreen.setSubtitleSize(style.welcomeScreenSubtitleSizeInSp);
            }

            if (style.chatBackgroundColor != ChatStyle.INVALID) {
                mWelcomeScreen.setBackground(style.chatBackgroundColor);
            }

            if (style.chatToolbarColorResId != ChatStyle.INVALID) {
                ColorsHelper.setBackgroundColor(activity, mToolbar, style.chatToolbarColorResId);
            } else {
                ColorsHelper.setBackgroundColor(activity, mToolbar, android.R.color.holo_green_light);
            }
        }

        try {
            Drawable overflowDrawable = popupMenuButton.getDrawable();
            if (style != null && style.chatToolbarTextColorResId != ChatStyle.INVALID) {
                ColorsHelper.setDrawableColor(activity, overflowDrawable, style.chatToolbarTextColorResId);
            } else {
                ColorsHelper.setDrawableColor(activity, overflowDrawable, android.R.color.white);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraClick() {
        Activity activity = getActivity();
        boolean isCameraGranted = PermissionChecker.isCameraPermissionGranted(activity);
        boolean isWriteGranted = PermissionChecker.isWriteExternalPermissionGranted(activity);
        Log.i(TAG, "isCameraGranted = " + isCameraGranted + " isWriteGranted " + isWriteGranted);
        if (isCameraGranted && isWriteGranted) {
            setBottomStateDefault();
            mBottomGallery.setVisibility(View.GONE);
            startActivityForResult(new Intent(activity, CameraActivity.class), REQUEST_CODE_PHOTO);
        } else {
            ArrayList<String> permissions = new ArrayList<>();
            if (!isCameraGranted) permissions.add(android.Manifest.permission.CAMERA);
            if (!isWriteGranted)
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION_CAMERA, R.string.permissions_camera_and_write_external_storage_help_text, permissions.toArray(new String[]{}));
        }
    }

    @Override
    public void onGalleryClick() {
        startActivityForResult(GalleryActivity.getStartIntent(getActivity(), REQUEST_CODE_PHOTOS), REQUEST_CODE_PHOTOS);
    }

    @Override
    public void onFileClick(FileDescription filedescription) {
        mChatController.onFileClick(filedescription);
    }

    @Override
    public void onConsultConnectionClick(ConsultConnectionMessage consultConnectionMessage) {
        if (canShowSpecialistInfo(getActivity())) {
            mChatController.onConsultChoose(getActivity(), consultConnectionMessage.getConsultId());
        }
    }

    @Override
    public void onRatingClick(Survey survey, int rating) {
        if (getActivity() != null) {
            survey.getQuestions().get(0).setRate(rating);
            mChatController.onRatingClick(getActivity(), survey);
        }
    }

//    @Override
//    public void onRatingStarsClick(Survey survey, int rating) {
//        if (getActivity() != null) {
//            survey.getQuestions().get(0).setRate(rating);
//            mChatController.onRatingClick(getActivity(), survey);
//        }
//    }

    @Override
    public void onImageClick(ChatPhrase chatPhrase) {
        Activity activity = getActivity();
        if (chatPhrase.getFileDescription().getFilePath() == null) return;
        if (chatPhrase instanceof UserPhrase) {
            if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_WAS_READ) {
                mChatController.checkAndResendPhrase((UserPhrase) chatPhrase);
            }
            if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_NOT_SENT) {
                startActivity(ImagesActivity.getStartIntent(activity, chatPhrase.getFileDescription()));
            }
        } else if (chatPhrase instanceof ConsultPhrase) {
            AnalyticsTracker.getInstance(activity, PrefUtils.getGaTrackerId(activity)).setAttachmentWasOpened();
            startActivity(ImagesActivity.getStartIntent(activity, chatPhrase.getFileDescription()));
        }
    }

    @Override
    public void onImageDownloadRequest(FileDescription fileDescription) {
        mChatController.onImageDownloadRequest(fileDescription);
    }

    @Override
    public void onUserPhraseClick(final UserPhrase userPhrase, int position) {
        mChatController.checkAndResendPhrase(userPhrase);
    }

    public void updateUi() {
        mChatAdapter.notifyDataSetChanged();
    }

    private void showPopup() {
        PopupMenu popup = new PopupMenu(getActivity(), popupMenuButton);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popup.getMenu());

        Menu menu = popup.getMenu();
        MenuItem searchMenuItem = menu.getItem(0);
        SpannableString s = new SpannableString(searchMenuItem.getTitle());
        if (style != null && style.menuItemTextColorResId != ChatStyle.INVALID) {
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), style.menuItemTextColorResId)), 0, s.length(), 0);
        }
        searchMenuItem.setTitle(s);

        MenuItem filesAndMedia = menu.getItem(1);
        SpannableString s2 = new SpannableString(filesAndMedia.getTitle());
        if (style != null && style.menuItemTextColorResId != ChatStyle.INVALID) {
            s2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), style.menuItemTextColorResId)), 0, s2.length(), 0);
        }
        filesAndMedia.setTitle(s2);

        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Activity activity = getActivity();

        if (item.getItemId() == R.id.files_and_media) {
            if (isInMessageSearchMode) onActivityBackPressed();
            startActivity(FilesActivity.getStartIntetent(activity));
            return true;
        }

        if (item.getItemId() == R.id.search && isInMessageSearchMode) {
            return true;
        } else if (item.getItemId() == R.id.search) {
            if (mWelcomeScreen != null) {
                mWelcomeScreen.setVisibility(View.GONE);
                ((ViewGroup) rootView).removeView(mWelcomeScreen);
                mWelcomeScreen = null;
            }
            AnalyticsTracker.getInstance(activity, PrefUtils.getGaTrackerId(activity)).setTextSearchWasOpened();
            search(false);
            if (backButton.getVisibility() == View.GONE) {
                backButton.setVisibility(View.VISIBLE);
            }
        }
        return false;
    }

    @Override
    public void onPhraseLongClick(final ChatPhrase cp, final int position) {
        final Activity activity = getActivity();
        final Context ctx = activity.getApplicationContext();

        unChooseItem(cp);

        if (cp == mChosenPhrase) {
            return;
        }

        if (style != null && style.chatBodyIconsTint != ChatStyle.INVALID) {
            ColorsHelper.setDrawableColor(ctx, popupMenuButton.getDrawable(), style.chatBodyIconsTint);
            Drawable d = ContextCompat.getDrawable(ctx, R.drawable.ic_arrow_back_blue_24dp);
            ColorsHelper.setDrawableColor(ctx, d, style.chatBodyIconsTint);
            backButton.setImageDrawable(d);
        } else {
//            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
            ColorsHelper.setDrawableColor(ctx, popupMenuButton.getDrawable(), android.R.color.black);
            Drawable d = ContextCompat.getDrawable(ctx, R.drawable.ic_arrow_back_blue_24dp);
            ColorsHelper.setDrawableColor(ctx, d, android.R.color.black);
            backButton.setImageDrawable(d);
        }

        if (style != null && style.chatMessageInputColor != ChatStyle.INVALID) {
            mToolbar.setBackgroundColor(getColorInt(style.chatMessageInputColor));
        } else {
            mToolbar.setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.white));
        }

        mCopyControls.setVisibility(View.VISIBLE);
        mConsultNameView.setVisibility(View.GONE);
        mConsultTitle.setVisibility(View.GONE);

        if (backButton.getVisibility() == View.GONE) {
            backButton.setVisibility(View.VISIBLE);
        }

        ImageButton reply = (ImageButton) mCopyControls.findViewById(R.id.reply);
        ImageButton copy = (ImageButton) mCopyControls.findViewById(R.id.content_copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCopyClick(activity, cp);
                hideBackButton();
            }
        });

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReplyClick(cp, position);
                hideBackButton();
            }
        });

        mChosenPhrase = cp;
        mChatAdapter.setItemChosen(true, cp);
    }

    private void onReplyClick(ChatPhrase cp, int position) {
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
            UserPhrase userPhrase = (UserPhrase) cp;
            headerText = getString(R.string.I);
            mQuote.setFromConsult(false);
            mQuote.setPhraseOwnerTitle(headerText);
            mQuote.setMessageId(userPhrase.getMessageId());
            mQuote.setBackendId(userPhrase.getBackendId());
        } else if (cp instanceof ConsultPhrase) {
            ConsultPhrase consultPhrase = (ConsultPhrase) cp;
            headerText = ((ConsultPhrase) cp).getConsultName();
            mQuote.setFromConsult(true);
            mQuote.setQuotedPhraseId(((ConsultPhrase) cp).getConsultId());
            if (headerText == null) {
                headerText = getString(R.string.consult);
            }
            mQuote.setPhraseOwnerTitle(headerText);
            mQuote.setMessageId(consultPhrase.getMessageId());
            mQuote.setBackendId(consultPhrase.getBackendId());
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

    private void onCopyClick(Activity activity, ChatPhrase cp) {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(new ClipData("", new String[]{"text/plain"}, new ClipData.Item(cp.getPhraseText())));
        hideCopyControls();
        PrefUtils.setLastCopyText(activity.getApplicationContext(), cp.getPhraseText());
        if (null != mChosenPhrase) unChooseItem(mChosenPhrase);
    }

    @Override
    public void onConsultAvatarClick(String consultId) {
        if (canShowSpecialistInfo(getActivity())) {
            mChatController.onConsultChoose(getActivity(), consultId);
        }
    }

    @Override
    public void onFilePickerClick() {
        Activity activity = getActivity();
        setBottomStateDefault();
        if (PermissionChecker.isReadExternalPermissionGranted(activity)) {
            FilePickerFragment frag = FilePickerFragment.newInstance(null);
            frag.setFileFilter(new MyFileFilter());
            frag.setOnDirSelectedListener(this);
            frag.show(getFragmentManager(), null);
        } else {
            PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION_READ_EXTERNAL, R.string.permissions_read_external_storage_help_text, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onHideClick() {
        final View input = rootView.findViewById(R.id.input_layout);
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
                            getString(R.string.I),
                            mAttachedImages.get(0),
                            new File(mAttachedImages.get(0).replaceAll("file://", "")).length(),
                            System.currentTimeMillis()),
                    mQuote,
                    mInputEditText.getText().toString().trim(),
                    isCopy(mInputEditText.getText().toString())));

            for (int i = 1; i < mAttachedImages.size(); i++) {
                FileDescription fileDescription = new FileDescription(
                        getString(R.string.I),
                        mAttachedImages.get(i),
                        new File(mAttachedImages.get(i).replaceAll("file://", "")).length(),
                        System.currentTimeMillis());

                UpcomingUserMessage upcomingUserMessage = new UpcomingUserMessage(
                        fileDescription, null, null, false);
                messages.add(upcomingUserMessage);
            }
            sendMessage(messages, true);
        }
    }

    @Override
    public void onFileSelected(File fileOrDirectory) {
        Log.i(TAG, "onFileSelected: " + fileOrDirectory);

        mFileDescription = new FileDescription(getString(R.string.I), fileOrDirectory.getAbsolutePath(), fileOrDirectory.length(), System.currentTimeMillis());
        mQuoteLayoutHolder.setText(getString(R.string.I), FileUtils.getLastPathSegment(fileOrDirectory.getAbsolutePath()), null);
        mQuote = null;
    }

    private void doFancySearch(final String request,
                               final boolean forward) {
        if (isEmpty(request)) {
            mChatAdapter.removeHighlight();
            mSearchHandler.removeCallbacksAndMessages(null);
            searchUp.setAlpha(DISABLED_ALPHA);
            searchDown.setAlpha(DISABLED_ALPHA);
            return;
        }
        mSearchHandler.removeCallbacksAndMessages(null);
        mSearchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onSearch(request, forward);
            }
        }, 400);
    }

    private void onSearch(String request, boolean forward) {
        final ChatPhrase[] highlighted = {null};
        mChatController.fancySearch(request, forward, new CallbackNoError<List<ChatItem>>() {
            @Override
            public void onCall(final List<ChatItem> data) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        onSearchEnd(data, highlighted);
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

    private void onSearchEnd(List<ChatItem> data, ChatPhrase[] highlighted) {
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
                        searchDown.setAlpha(ENABLED_ALPHA);
                    } else {
                        searchDown.setAlpha(DISABLED_ALPHA);
                    }
                    //для поиска - если можно перемещаться, подсвечиваем
                    if (last != -1 && i < last) {
                        searchUp.setAlpha(ENABLED_ALPHA);
                    } else {
                        searchUp.setAlpha(DISABLED_ALPHA);
                    }

                    break;
                }
            }
        }

        mChatAdapter.addItems(data);
        mChatAdapter.removeHighlight();
    }

    private void openBottomSheetAndGallery() {
        if (PermissionChecker.isReadExternalPermissionGranted(getActivity())) {
            setTitleStateCurrentOperatorConnected();
            final View inputLayout = rootView.findViewById(R.id.input_layout);
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
                Cursor c = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " desc");
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
            PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY, R.string.permissions_read_external_storage_help_text, android.Manifest.permission.READ_EXTERNAL_STORAGE);
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
            if (isInMessageSearchMode) onActivityBackPressed();
        }
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
        if (needsAddMessage(item)) {
            mChatAdapter.addItems(Arrays.asList(item));
        }
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

    private boolean needsAddMessage(ChatItem item) {
        if (item instanceof ScheduleInfo) {
            // Если сообщение о расписании уже показано, то снова отображать не нужно.
            // Если в сообщении о расписании указано, что сейчас чат работет,
            // то расписание отображать не нужно.
            return !((ScheduleInfo) item).isChatWorking() && !mChatAdapter.hasSchedule();
        } else {
            return true;
        }
    }

    public void addChatItems(final List<ChatItem> list) {
        if (list.size() == 0) return;
        h.post(new Runnable() {
            @Override
            public void run() {
                if (mWelcomeScreen != null) {
                    mWelcomeScreen.setVisibility(View.GONE);
                    ((ViewGroup) rootView).removeView(mWelcomeScreen);
                    mWelcomeScreen = null;
                }
                mChatAdapter.addItems(list);
            }
        });
        if (list.size() == 1 && list.get(0) instanceof ConsultTyping)
            return;//don't scroll if it is just typing item

        String firstUnreadMessageId = mChatController.getFirstUnreadMessageId();
        ArrayList<ChatItem> newList = mChatAdapter.getList();
        if (newList != null && !newList.isEmpty() && firstUnreadMessageId != null) {
            for (int i = 1; i < newList.size(); i++) {
                if (newList.get(i) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) newList.get(i);
                    if (cp.getMessageId().equalsIgnoreCase(firstUnreadMessageId)) {
                        final int index = i;
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!isInMessageSearchMode) {
                                    mRecyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(index - 1, 0);
                                        }
                                    });
                                }
                            }
                        }, 600);
                        return;
                    }
                }
            }
        }

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInMessageSearchMode)
                    mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }, 600);

    }

    public void setStateConsultConnected(final String connectedConsultId, final String ConsultName, final String consultTitle) {
        final ChatFragment f = this;
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
                f.connectedConsultId = connectedConsultId;
                mChatAdapter.removeConsultSearching();
                showOverflowMenu();
            }
        }, 50);
    }

    public void setTitleStateDefault() {
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

    public void setUserPhraseMessageId(String oldId, String newId) {
        mChatAdapter.setUserPhraseMessageId(oldId, newId);
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

    public void setMessageState(String messageId, MessageState state) {
        mChatAdapter.changeStateOfMessage(messageId, state);
    }

    private boolean isCopy(String text) {
        if (TextUtils.isEmpty(text)) return false;
        if (TextUtils.isEmpty(PrefUtils.getLastCopyText(getActivity()))) return false;
        return text.contains(PrefUtils.getLastCopyText(getActivity()));
    }

    private void hideCopyControls() {
        Activity activity = getActivity();
        Context context = activity.getApplicationContext();
        setTitleStateCurrentOperatorConnected();
        if (style != null && style.chatToolbarTextColorResId != ChatStyle.INVALID) {
            Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_white_24dp);
            ColorsHelper.setDrawableColor(context, d, style.chatToolbarTextColorResId);
            backButton.setImageDrawable(d);
            ColorsHelper.setDrawableColor(context, popupMenuButton.getDrawable(), style.chatToolbarTextColorResId);
        } else {
//            backButton.setImageResource(R.drawable.ic_arrow_back_white_24dp);
            Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_white_24dp);
            ColorsHelper.setDrawableColor(context, d, android.R.color.white);
            backButton.setImageDrawable(d);
            ColorsHelper.setDrawableColor(context, popupMenuButton.getDrawable(), android.R.color.white);
        }
        if (style != null && style.chatToolbarColorResId != ChatStyle.INVALID) {
            ColorsHelper.setBackgroundColor(context, mToolbar, style.chatToolbarColorResId);
        } else {
            ColorsHelper.setBackgroundColor(activity, mToolbar, android.R.color.holo_green_light);
        }

        mCopyControls.setVisibility(View.GONE);
        if (!isInMessageSearchMode) mConsultNameView.setVisibility(View.VISIBLE);
        if (mChatController != null && mChatController.isConsultFound() && !isInMessageSearchMode) {
            mConsultTitle.setVisibility(View.VISIBLE);
        }
    }

    private void setBottomStateDefault() {
        final View input = rootView.findViewById(R.id.input_layout);
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


    private void setTitleStateCurrentOperatorConnected() {
        if (isInMessageSearchMode) return;
        if (mChatController.isConsultFound()) {
            mConsultTitle.setVisibility(View.VISIBLE);
            mConsultNameView.setVisibility(View.VISIBLE);
            mSearchLo.setVisibility(View.GONE);
            mSearchMessageEditText.setText("");
        }
    }

    public void cleanChat() {
        final ChatFragment fragment = this;
        final Activity activity = getActivity();
        if (isAdded() && activity != null) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), activity, fragment);
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
    }

    private void showHelloScreen() {
        if (mWelcomeScreen == null)
            mWelcomeScreen = (WelcomeScreen) rootView.findViewById(R.id.welcome);
        mWelcomeScreen.setVisibility(View.VISIBLE);
    }

    public void setPhraseSentStatus(String id, MessageState messageState) {
        mChatAdapter.changeStateOfMessage(id, messageState);
    }

    public int getCurrentItemsCount() {
        return mChatAdapter.getCurrentItemCount();
    }

    public void setAllMessagesWereRead() {
        if (null != mChatAdapter) {
            mChatAdapter.setAllMessagesRead();
        }
    }

    public void updateProgress(FileDescription filedescription) {
        mChatAdapter.updateProgress(filedescription);
    }


    public void onDownloadError(FileDescription fileDescription, Throwable t) {
        if (isAdded()) {
            Activity activity = getActivity();
            if (activity != null) {
                updateProgress(fileDescription);
                if (t instanceof FileNotFoundException) {
                    Toast.makeText(activity, R.string.error_no_file, Toast.LENGTH_SHORT).show();
                    mChatAdapter.onDownloadError(fileDescription);
                }
                if (t instanceof UnknownHostException) {
                    Toast.makeText(activity, R.string.check_connection, Toast.LENGTH_SHORT).show();
                    mChatAdapter.onDownloadError(fileDescription);
                }
            }
        }
    }

    public void notifyConsultAvatarChanged(final String newAvatarUrl, final String consultId) {
        h.post(new Runnable() {
            @Override
            public void run() {
                if (mChatAdapter != null) mChatAdapter.notifyAvatarChanged(newAvatarUrl, consultId);
            }
        });
    }

    private void setTitleStateSearchingConsult() {
        if (isInMessageSearchMode) return;
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.VISIBLE);
        mSearchLo.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        if (isAdded()) {
            mConsultNameView.setText(getResources().getString(R.string.searching_operator));
        }
    }

    public void setTitleStateSearchingMessage() {
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.GONE);
        mSearchLo.setVisibility(View.VISIBLE);
        mSearchMessageEditText.setText("");
    }

    public void setStateSearchingConsult() {
        hideOverflowMenu();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                setTitleStateSearchingConsult();
                mChatAdapter.setSearchingConsult();
            }
        }, 50);
    }

    public void removeSearching() {
        if (null != mChatAdapter) {
            mChatAdapter.removeConsultSearching();
            showOverflowMenu();
        }

    }

    private void unChooseItem(ChatPhrase cp) {
        hideCopyControls();
        mChatAdapter.setItemChosen(false, mChosenPhrase);
        mChosenPhrase = null;
    }

    public void removeSchedule(boolean checkSchedule) {
        mChatAdapter.removeSchedule(checkSchedule);
    }

    public void showFullError(String error) {
        if (isAdded()) {
            Activity activity = getActivity();
            if (activity != null) {
                AlertDialog d = new AlertDialog
                        .Builder(getActivity())
                        .setMessage(error)
                        .setCancelable(true)
                        .create();
                d.show();
            }
        }
    }

    @Override
    public void setMenuVisibility(boolean isVisible) {
        if (isVisible) {
            showOverflowMenu();
        } else {
            hideOverflowMenu();
        }
    }

    public ChatStyle getStyle() {
        return style;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        } else if (requestCode == REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY && resultCode == PermissionsActivity.RESPONSE_GRANTED) {
            openBottomSheetAndGallery();
        } else if (requestCode == REQUEST_PERMISSION_CAMERA && resultCode == PermissionsActivity.RESPONSE_GRANTED) {
            onCameraClick();
        } else if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL && resultCode == PermissionsActivity.RESPONSE_GRANTED) {
            FilePickerFragment picker = FilePickerFragment.newInstance(null);
            picker.setFileFilter(new MyFileFilter());
            picker.setOnDirSelectedListener(this);
            picker.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mChatController.setActivityIsForeground(true);
        scrollToFirstUnreadMessage(mChatAdapter.getList());
        isResumed = true;
        chatIsShown = true;
    }

    private void scrollToFirstUnreadMessage(List<ChatItem> list) {
        String firstUnreadMessageId = mChatController.getFirstUnreadMessageId();
        if (list != null && !list.isEmpty() && firstUnreadMessageId != null) {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) list.get(i);
                    if (cp.getMessageId().equalsIgnoreCase(firstUnreadMessageId)) {
                        final int index = i;
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isInMessageSearchMode) {
                                    mRecyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(index - 1, 0);
                                        }
                                    });
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mChatController.setActivityIsForeground(false);
        isResumed = false;
        chatIsShown = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mChatController.setActivityIsForeground(false);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }

    private void initToolbar() {
        mToolbar.setTitle("");

        backButton = (ImageButton) rootView.findViewById(R.id.chat_back_button);
        Activity activity = getActivity();
        if (activity instanceof ChatActivity) {
            backButton.setVisibility(View.VISIBLE);
        } else {
            if (style != null && style.showBackButton) {
                backButton.setVisibility(View.VISIBLE);
            } else {
                backButton.setVisibility(View.GONE);
            }
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActivityBackPressed();
            }
        });

        popupMenuButton = (ImageButton) rootView.findViewById(R.id.popup_menu_button);
        popupMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
        showOverflowMenu();
    }

    private void showOverflowMenu() {
        popupMenuButton.setVisibility(View.VISIBLE);
    }

    private void hideOverflowMenu() {
        popupMenuButton.setVisibility(View.GONE);
    }

    private void onActivityBackPressed() {
        if (isAdded()) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();
            }
        }
    }

    /**
     * @return true, if chat should be closed
     */
    public boolean onBackPressed() {
        if (null != mChatAdapter) mChatAdapter.removeHighlight();
        boolean isNeedToClose = true;
        if (mBottomSheetView.getVisibility() == View.VISIBLE && mBottomGallery.getVisibility() == View.VISIBLE) {
            mBottomSheetView.setVisibility(View.GONE);
            rootView.findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
            onHideClick();
            return false;
        }
        if (mBottomSheetView.getVisibility() == View.VISIBLE) {
            mBottomSheetView.setVisibility(View.GONE);
            rootView.findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
            return false;
        }

        if (mCopyControls.getVisibility() == View.VISIBLE
                && mSearchLo.getVisibility() == View.VISIBLE) {
            unChooseItem(mChosenPhrase);
            mSearchMessageEditText.requestFocus();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mSearchMessageEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
            return false;
        }
        if (mCopyControls.getVisibility() == View.VISIBLE) {
            unChooseItem(mChosenPhrase);
            hideBackButton();
            isNeedToClose = false;
        }
        if (mSearchLo.getVisibility() == View.VISIBLE) {
            mSearchLo.setVisibility(View.GONE);
            setMenuVisibility(true);
            isInMessageSearchMode = false;
            mSearchMessageEditText.setText("");
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearchMessageEditText.getWindowToken(), 0);
                }
            }, 100);
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

            hideBackButton();
        }
        if (mBottomGallery.getVisibility() == View.VISIBLE) {
            onHideClick();
            return false;
        }
        if (mQuoteLayoutHolder.isVisible()) {
            mQuoteLayoutHolder.setIsVisible(false);
            if (mChatAdapter != null && mChosenPhrase != null) {
                mChatAdapter.setItemChosen(false, mChosenPhrase);
            }
            mQuote = null;
            return false;
        }
        return isNeedToClose;
    }

    private void hideBackButton() {
        Activity activity = getActivity();
        if (!(activity instanceof ChatActivity)) {
            if (style != null && !style.showBackButton) {
                backButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mChatController.unbindFragment();
        Activity activity = getActivity();
        Context context = activity.getApplicationContext();
        activity.unregisterReceiver(mChatReceiver);
        if (tracker == null) {
            tracker = AnalyticsTracker.getInstance(context, PrefUtils.getGaTrackerId(context));
        }
        tracker.setUserLeftChat();
        chatIsShown = false;
    }

    public static boolean isShown() {
        return chatIsShown;
    }

    private void search(final boolean searchInFiles) {
        Log.i(TAG, "searchInFiles: " + searchInFiles);
        isInMessageSearchMode = true;
        setBottomStateDefault();
        setTitleStateSearchingMessage();
        mSearchMessageEditText.requestFocus();
        hideOverflowMenu();
        setMenuVisibility(false);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchMessageEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
        mSwipeRefreshLayout.setEnabled(false);
        mSearchMoreButton.setVisibility(View.GONE);
    }

    @ColorInt
    int getColorInt(@ColorRes int color) {
        return ContextCompat.getColor(getActivity(), color);
    }

    private class QuoteLayoutHolder {
        private View view;
        private TextView mHeader;
        private TextView mText;
        private ImageView mQuoteImage;

        public QuoteLayoutHolder() {
            view = rootView.findViewById(R.id.quote_layout);
            mHeader = (TextView) view.findViewById(R.id.quote_header);

            if (style != null && style.incomingMessageTextColor != ChatStyle.INVALID) {
                mHeader.setTextColor(ContextCompat.getColor(getActivity(), style.incomingMessageTextColor));
            } else {
                mHeader.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
            }

            mText = (TextView) view.findViewById(R.id.quote_text);
            mQuoteImage = (ImageView) rootView.findViewById(R.id.quote_image);
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
                    .with(getActivity().getApplicationContext())
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

    private class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_SEARCH_CHAT_FILES)) {
                search(true);
            } else if (intent.getAction() != null && intent.getAction().equals(ACTION_SEARCH)) {
                search(false);
            }
        }
    }

    private boolean canShowSpecialistInfo(Context ctx) {
        ChatStyle style = PrefUtils.getIncomingStyle(ctx);
        return style != null ? style.canShowSpecialistInfo : ChatStyle.DEFAULT_CAN_SHOW_SPECIALIST_INFO;
    }
}
