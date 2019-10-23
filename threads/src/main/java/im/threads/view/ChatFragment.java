package im.threads.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.databinding.FragmentChatBinding;
import im.threads.internal.Config;
import im.threads.internal.activities.CameraActivity;
import im.threads.internal.activities.FilesActivity;
import im.threads.internal.activities.GalleryActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.adapters.ChatAdapter;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.fragments.BaseFragment;
import im.threads.internal.fragments.FilePickerFragment;
import im.threads.internal.helpers.FileHelper;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.helpers.MediaHelper;
import im.threads.internal.model.ChatItem;
import im.threads.internal.model.ChatPhrase;
import im.threads.internal.model.ConsultConnectionMessage;
import im.threads.internal.model.ConsultInfo;
import im.threads.internal.model.ConsultPhrase;
import im.threads.internal.model.ConsultTyping;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.model.Quote;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.Survey;
import im.threads.internal.model.UnreadMessages;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.model.UserPhrase;
import im.threads.internal.permissions.PermissionsActivity;
import im.threads.internal.picasso_url_connection_only.Picasso;
import im.threads.internal.utils.Callback;
import im.threads.internal.utils.CallbackNoError;
import im.threads.internal.utils.ColorsHelper;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.LateTextWatcher;
import im.threads.internal.utils.MyFileFilter;
import im.threads.internal.utils.PermissionChecker;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.ThreadsLogger;
import im.threads.internal.utils.UrlUtils;
import im.threads.internal.views.BottomSheetView;
import im.threads.internal.views.MySwipeRefreshLayout;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Весь функционал чата находится здесь во фрагменте,
 * чтобы чат можно было встроить в приложене в навигацией на фрагментах
 */
public final class ChatFragment extends BaseFragment implements
        BottomSheetView.ButtonsListener,
        ChatAdapter.AdapterInterface,
        FilePickerFragment.SelectedListener,
        PopupMenu.OnMenuItemClickListener {

    private static final String TAG = ChatFragment.class.getSimpleName();

    public static final int REQUEST_CODE_PHOTOS = 100;
    public static final int REQUEST_CODE_PHOTO = 101;
    public static final int REQUEST_EXTERNAL_CAMERA_PHOTO = 105;
    public static final int REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY = 102;
    public static final int REQUEST_PERMISSION_CAMERA = 103;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL = 104;

    public static final String ACTION_SEARCH_CHAT_FILES = "ACTION_SEARCH_CHAT_FILES";
    public static final String ACTION_SEARCH = "ACTION_SEARCH";
    public static final String ACTION_SEND_QUICK_MESSAGE = "ACTION_SEND_QUICK_MESSAGE";

    private static final float DISABLED_ALPHA = 0.5f;
    private static final float ENABLED_ALPHA = 1.0f;

    private static final int INVISIBLE_MSGS_COUNT = 3;
    private static final long INPUT_DELAY = 3000;

    private static boolean chatIsShown = false;

    private Context appContext;

    private Handler mSearchHandler = new Handler(Looper.getMainLooper());
    private Handler h = new Handler(Looper.getMainLooper());

    private ChatController mChatController;
    private ChatAdapter mChatAdapter;
    private QuoteLayoutHolder mQuoteLayoutHolder;
    private Quote mQuote = null;
    private FileDescription mFileDescription = null;
    private ChatPhrase mChosenPhrase = null;
    private List<String> mAttachedImages = new ArrayList<>();
    private ChatReceiver mChatReceiver;

    private boolean isInMessageSearchMode;
    private boolean isResumed;

    private ChatStyle style;

    private FragmentChatBinding binding;

    private Toast mToast;
    private File externalCameraPhotoFile;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        appContext = Config.instance.context;
        style = Config.instance.getChatStyle();
        // Статус бар подкрашивается только при использовании чата в стандартном Activity.

        if (activity instanceof ChatActivity) {
            ColorsHelper.setStatusBarColor(activity, style.chatStatusBarColorResId);
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);

        initViews();
        bindViews();
        initToolbar();
        setHasOptionsMenu(true);
        initController();
        setFragmentStyle(style);

        updateInputEnable(true);
        chatIsShown = true;

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mChatController.unbindFragment();
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(mChatReceiver);
        }

        chatIsShown = false;
    }

    private void initController() {
        Activity activity = getActivity();
        mChatController = ChatController.getInstance(activity);
        mChatController.bindFragment(this);
        welcomeScreenVisibility(mChatController.isNeedToShowWelcome());
        mChatReceiver = new ChatReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_SEARCH_CHAT_FILES);
        intentFilter.addAction(ACTION_SEARCH);
        intentFilter.addAction(ACTION_SEND_QUICK_MESSAGE);
        activity.registerReceiver(mChatReceiver, intentFilter);
    }

    private void initViews() {
        Activity activity = getActivity();

        mQuoteLayoutHolder = new QuoteLayoutHolder();
        binding.fileInputSheet.setButtonsListener(this);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        binding.recycler.setLayoutManager(mLayoutManager);
        mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), activity, this);
        binding.recycler.getItemAnimator().setChangeDuration(0);
        binding.recycler.setAdapter(mChatAdapter);

        binding.searchDownIb.setAlpha(DISABLED_ALPHA);
        binding.searchUpIb.setAlpha(DISABLED_ALPHA);
    }

    private void bindViews() {
        binding.swipeRefresh.setSwipeListener(new MySwipeRefreshLayout.SwipeListener() {
            @Override
            public void onSwipe() {
            }
        });

        binding.addAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetAndGallery();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ChatFragment.this.onRefresh();
            }
        });

        binding.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonClick();
            }
        });

        binding.consultName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatController.isConsultFound())
                    onConsultAvatarClick(mChatController.getCurrentConsultInfo().getId());
            }
        });

        binding.subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatController.isConsultFound())
                    onConsultAvatarClick(mChatController.getCurrentConsultInfo().getId());
            }
        });

        configureInputChangesSubscription();

        binding.searchUpIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(binding.search.getText())) return;
                doFancySearch(binding.search.getText().toString(), true);
            }
        });

        binding.searchDownIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(binding.search.getText())) return;
                doFancySearch(binding.search.getText().toString(), false);
            }
        });

        binding.search.addTextChangedListener(new LateTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isInMessageSearchMode) return;
                doFancySearch(s.toString(), true);
            }
        });

        binding.search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (isInMessageSearchMode && actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doFancySearch(v.getText().toString(), false);
                    return true;
                } else {
                    return false;
                }
            }
        });

        binding.recycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, final int bottom,
                                       int oldLeft, int oldTop, int oldRight, final int oldBottom) {
                if (bottom < oldBottom) {
                    binding.recycler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (style.scrollChatToEndIfUserTyping) {
                                scrollToPosition(binding.recycler.getAdapter().getItemCount() - 1);
                            } else {
                                binding.recycler.smoothScrollBy(0, oldBottom - bottom);
                            }
                        }
                    }, 100);
                }
            }
        });

        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager) binding.recycler.getLayoutManager()).findLastVisibleItemPosition();
                int itemCount = mChatAdapter.getItemCount();
                if (itemCount - lastVisibleItemPosition > INVISIBLE_MSGS_COUNT) {
                    if (binding.scrollDownButtonContainer.getVisibility() != View.VISIBLE) {
                        binding.scrollDownButtonContainer.setVisibility(View.VISIBLE);
                        showUnreadMsgsCount(mChatAdapter.getUnreadCount());
                    }
                } else {
                    binding.scrollDownButtonContainer.setVisibility(View.GONE);
                    recyclerView.post(() -> {
                        mChatAdapter.setAllMessagesRead();
                    });
                }
            }
        });

        binding.scrollDownButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showUnreadMsgsCount(0);
                int unreadCount = mChatAdapter.getUnreadCount();
                if (unreadCount > 0) {
                    scrollToNewMessages();
                } else {
                    scrollToPosition(binding.recycler.getAdapter().getItemCount() - 1);
                }
                mChatAdapter.setAllMessagesRead();
                binding.scrollDownButtonContainer.setVisibility(View.GONE);

                if (isInMessageSearchMode) {
                    hideSearchMode();
                }
            }
        });
    }

    private void configureInputChangesSubscription() {
        subscribe(RxTextView.textChanges(binding.input)
                .throttleLatest(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .map(CharSequence::toString)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        input -> mChatController.onUserTyping(input)
                )
        );
    }

    private void showUnreadMsgsCount(int unreadCount) {
        if (binding.scrollDownButtonContainer.getVisibility() == View.VISIBLE) {
            boolean hasUnreadCount = unreadCount > 0;
            binding.unreadMsgCount.setText(hasUnreadCount ? String.valueOf(unreadCount) : "");
            binding.unreadMsgCount.setVisibility(hasUnreadCount ? View.VISIBLE : View.GONE);
            binding.unreadMsgSticker.setVisibility(hasUnreadCount ? View.VISIBLE : View.GONE);
        }
    }

    private void onSendButtonClick() {
        if (binding.input.getText().toString().trim().length() == 0 && mFileDescription == null) {
            return;
        }

        welcomeScreenVisibility(false);

        List<UpcomingUserMessage> input = new ArrayList<>();
        UpcomingUserMessage message = new UpcomingUserMessage(
                mFileDescription,
                mQuote,
                binding.input.getText().toString().trim(),
                isCopy(binding.input.getText().toString())
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
            public void onError(Throwable error) {
            }
        });
    }

    private void afterRefresh(List<ChatItem> result) {
        int itemsBefore = mChatAdapter.getItemCount();
        mChatController.checkAndLoadOgData(result);
        mChatAdapter.addItems(result);
        int itemsAfter = mChatAdapter.getItemCount();
        scrollToPosition(itemsAfter - itemsBefore);
        for (int i = 1; i < 5; i++) {//for solving bug with refresh layout doesn't stop refresh animation
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.swipeRefresh.setRefreshing(false);
                    binding.swipeRefresh.clearAnimation();
                    binding.swipeRefresh.destroyDrawingCache();
                    binding.swipeRefresh.invalidate();
                }
            }, i * 500);
        }
    }

    protected void setFragmentStyle(@NonNull ChatStyle style) {
        Activity activity = getActivity();

        ColorsHelper.setBackgroundColor(activity, binding.chatRoot, style.chatBackgroundColor);

        ColorsHelper.setBackgroundColor(activity, binding.inputLayout, style.chatMessageInputColor);
        ColorsHelper.setBackgroundColor(activity, binding.fileInputSheet, style.chatMessageInputColor);
        ColorsHelper.setBackgroundColor(activity, binding.bottomGallery, style.chatMessageInputColor);
        ColorsHelper.setBackgroundColor(activity, binding.bottomLayout, style.chatMessageInputColor);

        ColorsHelper.setDrawableColor(activity, binding.searchUpIb.getDrawable(), style.chatToolbarTextColorResId);
        ColorsHelper.setDrawableColor(activity, binding.searchDownIb.getDrawable(), style.chatToolbarTextColorResId);

        binding.searchMore.setBackgroundColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor));
        binding.searchMore.setTextColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor));

        binding.swipeRefresh.setColorSchemeResources(style.chatToolbarColorResId);

        binding.scrollDownButton.setImageResource(style.scrollDownButtonResId);

        binding.unreadMsgSticker.getBackground().setColorFilter(getColorInt(style.unreadMsgStickerColorResId), PorterDuff.Mode.SRC_ATOP);

        binding.unreadMsgCount.setTextColor(ContextCompat.getColor(activity, style.unreadMsgCountTextColorResId));

        binding.input.setMinHeight((int) activity.getResources().getDimension(style.inputHeight));
        binding.input.setBackground(AppCompatResources.getDrawable(activity, style.inputBackground));
        binding.input.setHint(style.inputHint);

        binding.addAttachment.setImageResource(style.attachmentsIconResId);

        binding.sendMessage.setImageResource(style.sendMessageIconResId);

        ColorsHelper.setTextColor(activity, binding.search, style.chatToolbarTextColorResId);
        ColorsHelper.setDrawableColor(activity, binding.popupMenuButton.getDrawable(), style.chatToolbarTextColorResId);
        ColorsHelper.setDrawableColor(activity, binding.chatBackButton.getDrawable(), style.chatToolbarTextColorResId);
        ColorsHelper.setTextColor(activity, binding.subtitle, style.chatToolbarTextColorResId);
        ColorsHelper.setTextColor(activity, binding.consultName, style.chatToolbarTextColorResId);

        ColorsHelper.setTextColor(activity, binding.subtitle, style.chatToolbarTextColorResId);
        ColorsHelper.setTextColor(activity, binding.consultName, style.chatToolbarTextColorResId);

        ColorsHelper.setHintTextColor(activity, binding.input, style.chatMessageInputHintTextColor);

        ColorsHelper.setHintTextColor(activity, binding.search, style.chatToolbarHintTextColor);

        ColorsHelper.setTextColor(activity, binding.input, style.inputTextColor);

        if (!TextUtils.isEmpty(style.inputTextFont)) {
            try {
                Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), style.inputTextFont);
                this.binding.input.setTypeface(custom_font);
            } catch (Exception e) {
                ThreadsLogger.e(TAG, "setFragmentStyle", e);
            }
        }

        ColorsHelper.setTint(activity, binding.contentCopy, style.chatBodyIconsTint);
        ColorsHelper.setTint(activity, binding.reply, style.chatBodyIconsTint);
        ColorsHelper.setTint(activity, binding.sendMessage, style.chatBodyIconsTint);
        ColorsHelper.setTint(activity, binding.addAttachment, style.chatBodyIconsTint);
        ColorsHelper.setTint(activity, binding.quoteClear, style.chatBodyIconsTint);
        binding.fileInputSheet.setButtonsTint(style.chatBodyIconsTint);

        ColorsHelper.setBackgroundColor(activity, binding.toolbar, style.chatToolbarColorResId);

        try {
            Drawable overflowDrawable = binding.popupMenuButton.getDrawable();
            ColorsHelper.setDrawableColor(activity, overflowDrawable, style.chatToolbarTextColorResId);
        } catch (Resources.NotFoundException e) {
            ThreadsLogger.e(TAG, "setFragmentStyle", e);
        }
    }

    @Override
    public void onCameraClick() {
        Activity activity = getActivity();
        boolean isCameraGranted = PermissionChecker.isCameraPermissionGranted(activity);
        boolean isWriteGranted = PermissionChecker.isWriteExternalPermissionGranted(activity);
        ThreadsLogger.i(TAG, "isCameraGranted = " + isCameraGranted + " isWriteGranted " + isWriteGranted);
        if (isCameraGranted && isWriteGranted) {
            if (Config.instance.getChatStyle().useExternalCameraApp) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    externalCameraPhotoFile = FileHelper.createImageFile(getContext());
                    Uri photoUri = FileProviderHelper.getUriForFile(activity, externalCameraPhotoFile);
                    ThreadsLogger.d(TAG, "Image File uri resolved: " + photoUri.toString());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) { // https://stackoverflow.com/a/48391446/1321401
                        MediaHelper.grantPermissions(getContext(), intent, photoUri);
                    }
                    startActivityForResult(intent, REQUEST_EXTERNAL_CAMERA_PHOTO);
                } catch (IllegalArgumentException e) {
                    ThreadsLogger.w(TAG, "Could not start external camera", e);
                    showToast(getString(R.string.threads_camera_could_not_start_error));
                }

            } else {
                setBottomStateDefault();
                binding.bottomGallery.setVisibility(View.GONE);
                startActivityForResult(new Intent(activity, CameraActivity.class), REQUEST_CODE_PHOTO);
            }
        } else {
            ArrayList<String> permissions = new ArrayList<>();
            if (!isCameraGranted) permissions.add(android.Manifest.permission.CAMERA);
            if (!isWriteGranted)
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION_CAMERA, R.string.threads_permissions_camera_and_write_external_storage_help_text, permissions.toArray(new String[]{}));
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
        if (Config.instance.getChatStyle().canShowSpecialistInfo) {
            mChatController.onConsultChoose(getActivity(), consultConnectionMessage.getConsultId());
        }
    }

    @Override
    public void onRatingClick(@NonNull Survey survey, int rating) {
        if (getActivity() != null) {
            survey.getQuestions().get(0).setRate(rating);
            mChatController.onRatingClick(survey);
        }
    }

    @Override
    public void onResolveThreadClick(boolean approveResolve) {
        if (getActivity() != null) {
            mChatController.onResolveThreadClick(approveResolve);
        }
    }

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
            startActivity(ImagesActivity.getStartIntent(activity, chatPhrase.getFileDescription()));
        }
    }

    @Override
    public void onImageDownloadRequest(FileDescription fileDescription) {
        mChatController.onImageDownloadRequest(fileDescription);
    }

    @Override
    public void onOpenGraphClicked(String ogUrl, int adapterPosition) {
        UrlUtils.openUrl(getContext(), ogUrl);
    }

    @Override
    public void onUserPhraseClick(final UserPhrase userPhrase, int position) {
        mChatController.checkAndResendPhrase(userPhrase);
    }

    public void updateUi() {
        mChatAdapter.notifyDataSetChangedOnUi();
    }

    public void updateChatItem(ChatItem chatItem, boolean needsReordering) {

        if (needsReordering) {
            mChatAdapter.reorder();
            mChatAdapter.notifyDataSetChangedOnUi();
        } else {
            mChatAdapter.notifyItemChangedOnUi(chatItem);
        }
    }

    private void showPopup() {
        PopupMenu popup = new PopupMenu(getActivity(), binding.popupMenuButton);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popup.getMenu());

        Menu menu = popup.getMenu();
        MenuItem searchMenuItem = menu.getItem(0);
        SpannableString s = new SpannableString(searchMenuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), style.menuItemTextColorResId)), 0, s.length(), 0);
        searchMenuItem.setTitle(s);

        MenuItem filesAndMedia = menu.getItem(1);
        SpannableString s2 = new SpannableString(filesAndMedia.getTitle());
        s2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), style.menuItemTextColorResId)), 0, s2.length(), 0);
        filesAndMedia.setTitle(s2);

        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Activity activity = getActivity();

        if (item.getItemId() == R.id.files_and_media) {
            if (isInMessageSearchMode) onActivityBackPressed();
            startActivity(FilesActivity.getStartIntent(activity));
            return true;
        }

        if (item.getItemId() == R.id.search && isInMessageSearchMode) {
            return true;
        } else if (item.getItemId() == R.id.search) {
            search(false);
            binding.chatBackButton.setVisibility(View.VISIBLE);
        }
        return false;
    }

    @Override
    public void onPhraseLongClick(final ChatPhrase cp, final int position) {
        final Activity activity = getActivity();
        final Context ctx = activity.getApplicationContext();

        unChooseItem();

        if (cp == mChosenPhrase) {
            return;
        }

        Drawable d = AppCompatResources.getDrawable(ctx, R.drawable.ic_arrow_back_blue_24dp);

        ColorsHelper.setDrawableColor(ctx, binding.popupMenuButton.getDrawable(), style.chatBodyIconsTint);
        ColorsHelper.setDrawableColor(ctx, d, style.chatBodyIconsTint);
        binding.chatBackButton.setImageDrawable(d);

        ColorsHelper.setBackgroundColor(getContext(), binding.toolbar, style.chatToolbarTextColorResId);

        binding.copyControls.setVisibility(View.VISIBLE);
        binding.consultName.setVisibility(View.GONE);
        binding.subtitle.setVisibility(View.GONE);

        if (binding.chatBackButton.getVisibility() == View.GONE) {
            binding.chatBackButton.setVisibility(View.VISIBLE);
        }

        binding.contentCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCopyClick(activity, cp);
                hideBackButton();
            }
        });

        binding.reply.setOnClickListener(new View.OnClickListener() {
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

        hideCopyControls();
        scrollToPosition(position);

        UserPhrase userPhrase = cp instanceof UserPhrase ? (UserPhrase) cp : null;
        ConsultPhrase consultPhrase = cp instanceof ConsultPhrase ? (ConsultPhrase) cp : null;

        String text = cp.getPhraseText();

        if (userPhrase != null) {
            mQuote = new Quote(userPhrase.getUuid(),
                    appContext.getString(R.string.threads_I),
                    userPhrase.getPhraseText(),
                    userPhrase.getFileDescription(),
                    userPhrase.getTimeStamp());
            mQuote.setFromConsult(false);

        } else if (consultPhrase != null) {

            mQuote = new Quote(consultPhrase.getUuid(),
                    consultPhrase.getConsultName() != null
                            ? consultPhrase.getConsultName()
                            : appContext.getString(R.string.threads_consult),
                    consultPhrase.getPhraseText(),
                    consultPhrase.getFileDescription(),
                    consultPhrase.getTimeStamp());
            mQuote.setFromConsult(true);
            mQuote.setQuotedPhraseConsultId(consultPhrase.getConsultId());
        }

        mFileDescription = null;

        if (FileUtils.getExtensionFromFileDescription(cp.getFileDescription()) == FileUtils.JPEG
                || FileUtils.getExtensionFromFileDescription(cp.getFileDescription()) == FileUtils.PNG) {

            mQuoteLayoutHolder.setText(TextUtils.isEmpty(mQuote.getPhraseOwnerTitle()) ? "" : mQuote.getPhraseOwnerTitle(),
                    TextUtils.isEmpty(text) ? appContext.getString(R.string.threads_image) : text,
                    cp.getFileDescription().getFilePath());

        } else if (FileUtils.getExtensionFromFileDescription(cp.getFileDescription()) == FileUtils.PDF) {

            String fileName = "";
            try {
                fileName = cp.getFileDescription().getIncomingName() == null
                        ? FileUtils.getLastPathSegment((cp.getFileDescription().getFilePath()))
                        : cp.getFileDescription().getIncomingName();
            } catch (Exception e) {
                ThreadsLogger.e(TAG, "onReplyClick", e);
            }
            mQuoteLayoutHolder.setText(TextUtils.isEmpty(mQuote.getPhraseOwnerTitle()) ? "" : mQuote.getPhraseOwnerTitle(),
                    fileName,
                    null);

        } else {
            mQuoteLayoutHolder.setText(TextUtils.isEmpty(mQuote.getPhraseOwnerTitle()) ? "" : mQuote.getPhraseOwnerTitle(),
                    TextUtils.isEmpty(text) ? "" : text,
                    null);
        }
    }

    private void onCopyClick(Activity activity, ChatPhrase cp) {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(new ClipData("", new String[]{"text/plain"}, new ClipData.Item(cp.getPhraseText())));
        hideCopyControls();
        PrefUtils.setLastCopyText(cp.getPhraseText());
        if (null != mChosenPhrase) unChooseItem();
    }

    @Override
    public void onConsultAvatarClick(String consultId) {
        if (Config.instance.getChatStyle().canShowSpecialistInfo) {
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
            PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION_READ_EXTERNAL, R.string.threads_permissions_read_external_storage_help_text, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onHideClick() {
        binding.bottomGallery.setVisibility(View.GONE);
        binding.fileInputSheet.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.fileInputSheet.setVisibility(View.GONE);
                binding.inputLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onSendClick() {
        if (mAttachedImages == null || mAttachedImages.size() == 0) {
            binding.bottomGallery.setVisibility(View.GONE);
        } else {
            List<UpcomingUserMessage> messages = new ArrayList<>();
            messages.add(new UpcomingUserMessage(
                    new FileDescription(
                            appContext.getString(R.string.threads_I),
                            mAttachedImages.get(0),
                            new File(mAttachedImages.get(0)).length(),
                            System.currentTimeMillis()),
                    mQuote,
                    binding.input.getText().toString().trim(),
                    isCopy(binding.input.getText().toString())));

            for (int i = 1; i < mAttachedImages.size(); i++) {
                FileDescription fileDescription = new FileDescription(
                        appContext.getString(R.string.threads_I),
                        mAttachedImages.get(i),
                        new File(mAttachedImages.get(i)).length(),
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
        ThreadsLogger.i(TAG, "onFileSelected: " + fileOrDirectory);
        mFileDescription = new FileDescription(appContext.getString(R.string.threads_I), fileOrDirectory.getAbsolutePath(), fileOrDirectory.length(), System.currentTimeMillis());
        mQuoteLayoutHolder.setText(appContext.getString(R.string.threads_I), FileUtils.getLastPathSegment(fileOrDirectory.getAbsolutePath()), null);
        mQuote = null;
    }

    private void doFancySearch(final String request,
                               final boolean forward) {
        if (TextUtils.isEmpty(request)) {
            mChatAdapter.removeHighlight();
            mSearchHandler.removeCallbacksAndMessages(null);
            binding.searchUpIb.setAlpha(DISABLED_ALPHA);
            binding.searchDownIb.setAlpha(DISABLED_ALPHA);
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
                h.post(() -> onSearchEnd(data, highlighted));
                h.postDelayed(() -> {
                    if (highlighted[0] == null) return;
                    int index = mChatAdapter.setItemHighlighted(highlighted[0]);
                    if (index != -1) scrollToPosition(index);
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
                        binding.searchDownIb.setAlpha(ENABLED_ALPHA);
                    } else {
                        binding.searchDownIb.setAlpha(DISABLED_ALPHA);
                    }
                    //для поиска - если можно перемещаться, подсвечиваем
                    if (last != -1 && i < last) {
                        binding.searchUpIb.setAlpha(ENABLED_ALPHA);
                    } else {
                        binding.searchUpIb.setAlpha(DISABLED_ALPHA);
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

            if (binding.fileInputSheet.getVisibility() == View.GONE) {
                binding.fileInputSheet.setVisibility(View.VISIBLE);
                binding.fileInputSheet.setAlpha(0.0f);
                binding.fileInputSheet.animate().alpha(1.0f).setDuration(300).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.fileInputSheet.setVisibility(View.VISIBLE);
                    }
                });
                binding.inputLayout.setVisibility(View.GONE);
                scrollToPosition(mChatAdapter.getItemCount() - 1);
                String[] projection = new String[]{MediaStore.Images.Media.DATA};

                ArrayList<String> allItems = new ArrayList<>();
                Cursor c = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " desc");
                if (c != null) {
                    int DATA = c.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (c.getCount() == 0) return;

                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        allItems.add(c.getString(DATA));
                    }
                }
                binding.bottomGallery.setVisibility(View.VISIBLE);
                binding.bottomGallery.setAlpha(0.0f);
                binding.bottomGallery.animate().alpha(1.0f).setDuration(200).start();
                binding.bottomGallery.setImages(allItems, items -> {
                    mAttachedImages = new ArrayList<>(items);
                    if (mAttachedImages.size() > 0) {
                        binding.fileInputSheet.setSelectedState(true);
                    } else {
                        binding.fileInputSheet.setSelectedState(false);
                    }
                });
            } else {
                binding.fileInputSheet.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.fileInputSheet.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            PermissionsActivity.startActivityForResult(this, REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY, R.string.threads_permissions_read_external_storage_help_text, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    private void sendMessage(List<UpcomingUserMessage> messages, boolean clearInput) {
        ThreadsLogger.i(TAG, "isInMessageSearchMode =" + isInMessageSearchMode);
        if (mChatController == null) return;
        for (UpcomingUserMessage message : messages) {
            mChatController.onUserInput(message);
        }
        if (null != mQuoteLayoutHolder)
            mQuoteLayoutHolder.setIsVisible(false);
        if (null != mChatAdapter) mChatAdapter.setAllMessagesRead();
        binding.fileInputSheet.setSelectedState(false);
        if (clearInput) {
            binding.input.setText("");
            if (!isInMessageSearchMode) mQuoteLayoutHolder.setIsVisible(false);
            mQuote = null;
            mFileDescription = null;
            setBottomStateDefault();
            hideCopyControls();
            mAttachedImages.clear();
            binding.bottomGallery.setVisibility(View.GONE);
            if (mChosenPhrase != null && mChatAdapter != null) {
                mChatAdapter.setItemChosen(false, mChosenPhrase);
                mChosenPhrase = null;
            }
            if (isInMessageSearchMode) onActivityBackPressed();
        }
    }

    public void addChatItem(final ChatItem item) {
        boolean isUserSeesMessage = (mChatAdapter.getItemCount() - ((LinearLayoutManager) binding.recycler.getLayoutManager()).findLastVisibleItemPosition()) < INVISIBLE_MSGS_COUNT;
        if (item instanceof ConsultPhrase) {
            if (isUserSeesMessage && isResumed && !isInMessageSearchMode) {
                ((ConsultPhrase) item).setRead(true);
            } else {
                ((ConsultPhrase) item).setRead(false);
            }
        }
        if (needsAddMessage(item)) {
            welcomeScreenVisibility(false);
            mChatAdapter.addItems(Arrays.asList(item));

            if (!isUserSeesMessage) {
                showUnreadMsgsCount(mChatAdapter.getUnreadCount());
            }
        }
        if (item instanceof ConsultPhrase) {
            mChatAdapter.setAvatar(((ConsultPhrase) item).getConsultId(), ((ConsultPhrase) item).getAvatarPath());
        }
        // do not scroll when consult is typing or write
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInMessageSearchMode) {
                    int itemCount = mChatAdapter.getItemCount();
                    int lastVisibleItemPosition = ((LinearLayoutManager) binding.recycler.getLayoutManager()).findLastVisibleItemPosition();
                    boolean isUserSeesMessages = (itemCount - 1) - lastVisibleItemPosition < INVISIBLE_MSGS_COUNT;
                    boolean isConsultMsg = (item instanceof ConsultPhrase) || (item instanceof ConsultTyping);
                    if (isUserSeesMessages || !isConsultMsg) {
                        scrollToPosition(itemCount - 1);
                    }
                }
            }
        }, 100);
    }

    private void scrollToPosition(int itemCount) {
        if (itemCount >= 0) {
            binding.recycler.scrollToPosition(itemCount);
        }
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
        h.post(() -> {
            welcomeScreenVisibility(false);
            mChatAdapter.addItems(list);
        });
        if (list.size() == 1 && list.get(0) instanceof ConsultTyping)
            return;//don't scroll if it is just typing item

        String firstUnreadProviderId = mChatController.getFirstUnreadProviderId();
        ArrayList<ChatItem> newList = mChatAdapter.getList();
        if (newList != null && !newList.isEmpty() && firstUnreadProviderId != null) {
            for (int i = 1; i < newList.size(); i++) {
                if (newList.get(i) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) newList.get(i);
                    if (firstUnreadProviderId.equalsIgnoreCase(cp.getProviderId())) {
                        final int index = i;
                        h.postDelayed(() -> {
                            if (!isInMessageSearchMode) {
                                binding.recycler.post(() -> ((LinearLayoutManager) binding.recycler.getLayoutManager()).scrollToPositionWithOffset(index - 1, 0));
                            }
                        }, 600);
                        return;
                    }
                }
            }
        }
        h.post(() -> {
            if (!isInMessageSearchMode)
                scrollToPosition(mChatAdapter.getItemCount() - 1);
        });

    }

    public void setStateConsultConnected(ConsultInfo info) {
        h.postDelayed(() -> {
            if (isAdded()) {
                if (!isInMessageSearchMode) {
                    binding.subtitle.setVisibility(View.VISIBLE);
                    binding.consultName.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(info.getName()) && !info.getName().equals("null")) {
                    binding.consultName.setText(info.getName());
                } else {
                    binding.consultName.setText(appContext.getString(R.string.threads_unknown_operator));
                }

                binding.subtitle.setText((!style.chatSubtitleShowOrgUnit || info.getOrganizationUnit() == null)
                        ? getString(style.chatSubtitleTextResId)
                        : info.getOrganizationUnit());

                mChatAdapter.removeConsultSearching();
                showOverflowMenu();
            }
        }, 50);
    }

    public void setTitleStateDefault() {
        h.postDelayed(() -> {
            if (!isInMessageSearchMode) {
                binding.subtitle.setVisibility(View.GONE);
                binding.consultName.setVisibility(View.VISIBLE);
                binding.searchLo.setVisibility(View.GONE);
                binding.search.setText("");
                binding.consultName.setText(style.chatTitleTextResId);
            }
        }, 50);
    }

    public void setUserPhraseProviderId(String uuid, String providerId) {
        mChatAdapter.setUserPhraseProviderId(uuid, providerId);
    }

    public void showConnectionError() {
        showToast(getString(R.string.threads_message_not_sent));
    }

    public void showToast(final String message) {
        if (null != mToast) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        mToast.show();
    }

    public void setMessageState(String providerId, MessageState state) {
        mChatAdapter.changeStateOfMessageByProviderId(providerId, state);
    }

    private boolean isCopy(String text) {
        if (TextUtils.isEmpty(text)) return false;
        if (TextUtils.isEmpty(PrefUtils.getLastCopyText())) return false;
        return text.contains(PrefUtils.getLastCopyText());
    }

    private void hideCopyControls() {
        Activity activity = getActivity();
        Context context = activity.getApplicationContext();
        setTitleStateCurrentOperatorConnected();
        Drawable d = AppCompatResources.getDrawable(context, R.drawable.ic_arrow_back_white_24dp);
        ColorsHelper.setDrawableColor(context, d, style.chatToolbarTextColorResId);
        binding.chatBackButton.setImageDrawable(d);
        ColorsHelper.setDrawableColor(context, binding.popupMenuButton.getDrawable(), style.chatToolbarTextColorResId);
        ColorsHelper.setBackgroundColor(context, binding.toolbar, style.chatToolbarColorResId);

        binding.copyControls.setVisibility(View.GONE);
        if (!isInMessageSearchMode) binding.consultName.setVisibility(View.VISIBLE);
        if (mChatController != null && mChatController.isConsultFound() && !isInMessageSearchMode) {
            binding.subtitle.setVisibility(View.VISIBLE);
        }
    }

    private void setBottomStateDefault() {
        binding.fileInputSheet.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.fileInputSheet.setVisibility(View.GONE);
                binding.inputLayout.setVisibility(View.VISIBLE);
            }
        });
        if (!isInMessageSearchMode) binding.searchLo.setVisibility(View.GONE);
        if (!isInMessageSearchMode) binding.search.setText("");
        binding.bottomGallery.setVisibility(View.GONE);
    }


    private void setTitleStateCurrentOperatorConnected() {
        if (isInMessageSearchMode) return;
        if (mChatController.isConsultFound()) {
            binding.subtitle.setVisibility(View.VISIBLE);
            binding.consultName.setVisibility(View.VISIBLE);
            binding.searchLo.setVisibility(View.GONE);
            binding.search.setText("");
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
                    binding.recycler.setAdapter(mChatAdapter);
                    setTitleStateDefault();
                    welcomeScreenVisibility(false);
                    binding.input.clearFocus();
                    welcomeScreenVisibility(true);
                }
            });
        }
    }

    private void welcomeScreenVisibility(boolean show) {
        binding.welcome.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public void setSurveySentStatus(long uuid, MessageState sentState) {
        mChatAdapter.changeStateOfSurvey(uuid, sentState);
    }

    public void setPhraseSentStatusByProviderId(String providerId, MessageState messageState) {
        mChatAdapter.changeStateOfMessageByProviderId(providerId, messageState);
    }

    /**
     * Remove close request from the thread history
     *
     * @return true - if deletion occurred, false - if there was no resolve request in the history
     */
    public boolean removeResolveRequest() {
        return mChatAdapter.removeResolveRequest();
    }

    /**
     * Remove survey from the thread history
     *
     * @return true - if deletion occurred, false - if there was no survey in the history
     */
    public boolean removeSurvey(long sendingId) {
        return mChatAdapter.removeSurvey(sendingId);
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
                    Toast.makeText(activity, R.string.threads_error_no_file, Toast.LENGTH_SHORT).show();
                    mChatAdapter.onDownloadError(fileDescription);
                }
                if (t instanceof UnknownHostException) {
                    Toast.makeText(activity, R.string.threads_check_connection, Toast.LENGTH_SHORT).show();
                    mChatAdapter.onDownloadError(fileDescription);
                }
            }
        }
    }

    public void notifyConsultAvatarChanged(final String newAvatarUrl, final String consultId) {
        h.post(() -> {
            if (mChatAdapter != null) {
                mChatAdapter.notifyAvatarChanged(newAvatarUrl, consultId);
            }
        });
    }

    private void setTitleStateSearchingConsult() {
        if (isInMessageSearchMode) return;
        binding.subtitle.setVisibility(View.GONE);
        binding.consultName.setVisibility(View.VISIBLE);
        binding.searchLo.setVisibility(View.GONE);
        binding.search.setText("");
        if (isAdded()) {
            binding.consultName.setText(appContext.getString(R.string.threads_searching_operator));
        }
    }

    public void setTitleStateSearchingMessage() {
        binding.subtitle.setVisibility(View.GONE);
        binding.consultName.setVisibility(View.GONE);
        binding.searchLo.setVisibility(View.VISIBLE);
        binding.search.setText("");
    }

    public void setStateSearchingConsult() {
        h.postDelayed(() -> {
            setTitleStateSearchingConsult();
            mChatAdapter.setSearchingConsult();
        }, 50);
    }

    public void removeSearching() {
        if (null != mChatAdapter) {
            mChatAdapter.removeConsultSearching();
            showOverflowMenu();
        }
    }

    private void unChooseItem() {
        hideCopyControls();
        mChatAdapter.setItemChosen(false, mChosenPhrase);
        mChosenPhrase = null;
    }

    public void removeSchedule(boolean checkSchedule) {
        mChatAdapter.removeSchedule(checkSchedule);
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

    public void updateInputEnable(boolean enabled) {
        binding.input.setEnabled(enabled);
        binding.addAttachment.setEnabled(enabled);
        binding.sendMessage.setEnabled(enabled);

        ColorsHelper.setTint(getActivity(), binding.addAttachment, enabled ? style.chatBodyIconsTint : style.chatDisabledTextColor);
        ColorsHelper.setTint(getActivity(), binding.sendMessage, enabled ? style.chatBodyIconsTint : style.chatDisabledTextColor);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PHOTOS && resultCode == Activity.RESULT_OK) {
            ArrayList<String> photos = data.getStringArrayListExtra(GalleryActivity.PHOTOS_TAG);
            onHideClick();
            welcomeScreenVisibility(false);
            if (photos.size() == 0) return;
            unChooseItem();
            UpcomingUserMessage uum =
                    new UpcomingUserMessage(new FileDescription(appContext.getString(R.string.threads_I)
                            , photos.get(0)
                            , new File(photos.get(0)).length()
                            , System.currentTimeMillis())
                            , null
                            , binding.input.getText().toString().trim()
                            , isCopy(binding.input.getText().toString()));
            mChatController.onUserInput(uum);
            binding.input.setText("");
            mQuoteLayoutHolder.setIsVisible(false);
            mQuote = null;
            mFileDescription = null;
            for (int i = 1; i < photos.size(); i++) {
                uum =
                        new UpcomingUserMessage(
                                new FileDescription(appContext.getString(R.string.threads_I),
                                        photos.get(i),
                                        new File(photos.get(i)).length(),
                                        System.currentTimeMillis())
                                , null
                                , null
                                , false);
                mChatController.onUserInput(uum);
            }

        } else if (requestCode == REQUEST_EXTERNAL_CAMERA_PHOTO) {

            if (resultCode == Activity.RESULT_OK && externalCameraPhotoFile != null) {
                mFileDescription = new FileDescription(appContext.getString(R.string.threads_image),
                        externalCameraPhotoFile.getAbsolutePath(),
                        externalCameraPhotoFile.length(), System.currentTimeMillis());

                UpcomingUserMessage uum = new UpcomingUserMessage(mFileDescription, null, null, false);
                sendMessage(Arrays.asList(new UpcomingUserMessage[]{uum}), true);
            }

            externalCameraPhotoFile = null;

        } else if (requestCode == REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            mFileDescription = new FileDescription(appContext.getString(R.string.threads_image),
                    data.getStringExtra(CameraActivity.IMAGE_EXTRA),
                    new File(data.getStringExtra(CameraActivity.IMAGE_EXTRA)).length(),
                    System.currentTimeMillis());
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
        scrollToFirstUnreadMessage();
        isResumed = true;
        chatIsShown = true;
    }

    private void scrollToNewMessages() {
        List<ChatItem> list = mChatAdapter.getList();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) instanceof UnreadMessages) {
                ((LinearLayoutManager) binding.recycler.getLayoutManager()).scrollToPositionWithOffset(i - 1, 0);
            }
        }
    }

    private void scrollToFirstUnreadMessage() {
        List<ChatItem> list = mChatAdapter.getList();
        String firstUnreadProviderId = mChatController.getFirstUnreadProviderId();
        if (list != null && !list.isEmpty() && firstUnreadProviderId != null) {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) list.get(i);
                    if (firstUnreadProviderId.equalsIgnoreCase(cp.getProviderId())) {
                        final int index = i;
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!isInMessageSearchMode) {
                                    binding.recycler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((LinearLayoutManager) binding.recycler.getLayoutManager()).scrollToPositionWithOffset(index - 1, 0);
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
        isInMessageSearchMode = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mChatController.setActivityIsForeground(false);
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setRefreshing(false);
            binding.swipeRefresh.destroyDrawingCache();
            binding.swipeRefresh.clearAnimation();
        }
    }

    private void initToolbar() {
        binding.toolbar.setTitle("");

        Activity activity = getActivity();
        if (activity instanceof ChatActivity) {
            binding.chatBackButton.setVisibility(View.VISIBLE);
        } else {
            binding.chatBackButton.setVisibility(style.showBackButton ? View.VISIBLE : View.GONE);
        }
        binding.chatBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActivityBackPressed();
            }
        });

        binding.popupMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
        showOverflowMenu();
    }

    private void showOverflowMenu() {
        binding.popupMenuButton.setVisibility(View.VISIBLE);
    }

    private void hideOverflowMenu() {
        binding.popupMenuButton.setVisibility(View.GONE);
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
        if (binding.fileInputSheet.getVisibility() == View.VISIBLE && binding.bottomGallery.getVisibility() == View.VISIBLE) {
            binding.fileInputSheet.setVisibility(View.GONE);
            binding.inputLayout.setVisibility(View.VISIBLE);
            onHideClick();
            return false;
        }
        if (binding.fileInputSheet.getVisibility() == View.VISIBLE) {
            binding.fileInputSheet.setVisibility(View.GONE);
            binding.inputLayout.setVisibility(View.VISIBLE);
            return false;
        }

        if (binding.copyControls.getVisibility() == View.VISIBLE
                && binding.searchLo.getVisibility() == View.VISIBLE) {
            unChooseItem();
            binding.search.requestFocus();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(binding.search, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
            return false;
        }
        if (binding.copyControls.getVisibility() == View.VISIBLE) {
            unChooseItem();
            hideBackButton();
            isNeedToClose = false;
        }
        if (binding.searchLo.getVisibility() == View.VISIBLE) {
            isNeedToClose = false;
            hideSearchMode();
            if (binding.recycler != null && mChatAdapter != null) {
                scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }
        if (binding.bottomGallery.getVisibility() == View.VISIBLE) {
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

    private void hideSearchMode() {
        binding.searchLo.setVisibility(View.GONE);
        setMenuVisibility(true);
        isInMessageSearchMode = false;
        binding.search.setText("");
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.search.getWindowToken(), 0);
            }
        }, 100);

        binding.searchMore.setVisibility(View.GONE);
        binding.swipeRefresh.setEnabled(true);
        int state = mChatController.getStateOfConsult();
        switch (state) {
            case ChatController.CONSULT_STATE_DEFAULT:
                setTitleStateDefault();
                break;
            case ChatController.CONSULT_STATE_FOUND:
                setStateConsultConnected(mChatController.getCurrentConsultInfo());
                break;
            case ChatController.CONSULT_STATE_SEARCHING:
                setTitleStateSearchingConsult();
                break;
        }

        hideBackButton();
    }

    private void hideBackButton() {
        Activity activity = getActivity();
        if (!(activity instanceof ChatActivity)) {
            if (!style.showBackButton) {
                binding.chatBackButton.setVisibility(View.GONE);
            }
        }
    }

    public static boolean isShown() {
        return chatIsShown;
    }

    private void search(final boolean searchInFiles) {
        ThreadsLogger.i(TAG, "searchInFiles: " + searchInFiles);
        isInMessageSearchMode = true;
        setBottomStateDefault();
        setTitleStateSearchingMessage();
        binding.search.requestFocus();
        hideOverflowMenu();
        setMenuVisibility(false);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.search, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
        binding.swipeRefresh.setEnabled(false);
        binding.searchMore.setVisibility(View.GONE);
    }

    @ColorInt
    int getColorInt(@ColorRes int color) {
        return ContextCompat.getColor(getActivity(), color);
    }

    private class QuoteLayoutHolder {
        public QuoteLayoutHolder() {
            binding.quoteHeader.setTextColor(ContextCompat.getColor(getActivity(), style.incomingMessageTextColor));
            binding.quoteClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.quoteHeader.setText("");
                    binding.quoteText.setText("");
                    binding.quoteLayout.setVisibility(View.GONE);
                    mQuote = null;
                    mFileDescription = null;
                    unChooseItem();
                }
            });
        }

        public boolean isVisible() {
            return binding.quoteLayout.getVisibility() == View.VISIBLE;
        }

        public void setIsVisible(boolean isVisible) {
            if (isVisible) {
                binding.quoteLayout.setVisibility(View.VISIBLE);
            } else {
                binding.quoteLayout.setVisibility(View.GONE);
            }
        }

        private void setImage(String path) {
            binding.quoteImage.setVisibility(View.VISIBLE);
            Picasso
                    .with(getActivity().getApplicationContext())
                    .load(new File(path))
                    .fit()
                    .centerCrop()
                    .into(binding.quoteImage);
        }

        private void removeImage() {
            binding.quoteImage.setVisibility(View.GONE);
        }

        void setText(String header, String text, String imagePath) {
            setIsVisible(true);
            if (header == null || header.equals("null")) {
                binding.quoteHeader.setVisibility(View.INVISIBLE);
            } else {
                binding.quoteHeader.setVisibility(View.VISIBLE);
            }
            binding.quoteHeader.setText(header);
            binding.quoteText.setText(text);
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

}