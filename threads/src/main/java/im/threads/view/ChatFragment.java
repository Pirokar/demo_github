package im.threads.view;

import static im.threads.internal.utils.PrefUtils.getFileDescriptionDraft;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.business.imageLoading.ImageLoader;
import im.threads.business.logger.LoggerEdna;
import im.threads.business.models.CampaignMessage;
import im.threads.business.models.ChatItem;
import im.threads.business.models.ChatPhrase;
import im.threads.business.models.ConsultInfo;
import im.threads.business.models.ConsultPhrase;
import im.threads.business.models.FileDescription;
import im.threads.business.models.MessageState;
import im.threads.business.models.QuickReply;
import im.threads.business.models.Quote;
import im.threads.business.models.Survey;
import im.threads.business.models.SystemMessage;
import im.threads.business.models.UserPhrase;
import im.threads.business.utils.FileUtils;
import im.threads.business.utils.FileUtilsKt;
import im.threads.databinding.FragmentChatBinding;
import im.threads.business.config.BaseConfig;
import im.threads.internal.activities.CameraActivity;
import im.threads.internal.activities.GalleryActivity;
import im.threads.internal.activities.ImagesActivity;
import im.threads.internal.activities.filesActivity.FilesActivity;
import im.threads.internal.adapters.ChatAdapter;
import im.threads.internal.broadcastReceivers.ProgressReceiver;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.controllers.ChatController;
import im.threads.internal.fragments.AttachmentBottomSheetDialogFragment;
import im.threads.internal.fragments.BaseFragment;
import im.threads.internal.fragments.FilePickerFragment;
import im.threads.internal.fragments.PermissionDescriptionAlertDialogFragment;
import im.threads.internal.helpers.FileHelper;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.helpers.MediaHelper;
import im.threads.internal.media.ChatCenterAudioConverter;
import im.threads.internal.media.ChatCenterAudioConverterCallback;
import im.threads.internal.media.FileDescriptionMediaPlayer;
import im.threads.internal.model.ClientNotificationDisplayType;
import im.threads.internal.model.ConsultRole;
import im.threads.internal.model.ConsultTyping;
import im.threads.internal.model.InputFieldEnableModel;
import im.threads.internal.model.QuickReplyItem;
import im.threads.internal.model.ScheduleInfo;
import im.threads.internal.model.UnreadMessages;
import im.threads.internal.model.UpcomingUserMessage;
import im.threads.internal.permissions.PermissionsActivity;
import im.threads.internal.useractivity.LastUserActivityTimeCounter;
import im.threads.internal.useractivity.LastUserActivityTimeCounterSingletonProvider;
import im.threads.internal.utils.ColorsHelper;
import im.threads.internal.utils.Keyboard;
import im.threads.internal.utils.PrefUtils;
import im.threads.internal.utils.RxUtils;
import im.threads.internal.utils.ThreadsPermissionChecker;
import im.threads.internal.views.VoiceTimeLabelFormatter;
import im.threads.internal.views.VoiceTimeLabelFormatterKt;
import im.threads.styles.permissions.PermissionDescriptionType;
import im.threads.ui.config.Config;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;

/**
 * Весь функционал чата находится здесь во фрагменте,
 * чтобы чат можно было встроить в приложене в навигацией на фрагментах
 */
public final class ChatFragment extends BaseFragment implements
        AttachmentBottomSheetDialogFragment.Callback,
        ProgressReceiver.Callback,
        PopupMenu.OnMenuItemClickListener,
        FilePickerFragment.SelectedListener,
        ChatCenterAudioConverterCallback,
        PermissionDescriptionAlertDialogFragment.OnAllowPermissionClickListener {

    public static final int REQUEST_CODE_PHOTOS = 100;
    public static final int REQUEST_CODE_PHOTO = 101;
    public static final int REQUEST_EXTERNAL_CAMERA_PHOTO = 102;
    public static final int REQUEST_CODE_FILE = 103;
    public static final int REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY = 200;
    public static final int REQUEST_PERMISSION_CAMERA = 201;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL = 202;
    public static final String ACTION_SEARCH_CHAT_FILES = "ACTION_SEARCH_CHAT_FILES";
    public static final String ACTION_SEARCH = "ACTION_SEARCH";
    public static final String ACTION_SEND_QUICK_MESSAGE = "ACTION_SEND_QUICK_MESSAGE";
    private static final int REQUEST_PERMISSION_RECORD_AUDIO = 204;
    private static final String ARG_OPEN_WAY = "arg_open_way";
    private static final float DISABLED_ALPHA = 0.5f;
    private static final float ENABLED_ALPHA = 1.0f;

    private static final int INVISIBLE_MSGS_COUNT = 3;
    private static final long INPUT_DELAY = 3000;

    private static final int INPUT_EDIT_VIEW_MIN_LINES_COUNT = 1;
    private static final int INPUT_EDIT_VIEW_MAX_LINES_COUNT = 7;

    private static boolean chatIsShown = false;
    private static boolean afterResume = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("dd.MM.yyyy.HH:mm:ss.S", Locale.getDefault());
    @NonNull
    private final ObservableField<String> inputTextObservable = new ObservableField<>("");
    @NonNull
    private final ObservableField<Optional<FileDescription>> fileDescription = new ObservableField<>(Optional.empty());
    @NonNull
    private final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    private final ChatCenterAudioConverter audioConverter = new ChatCenterAudioConverter();
    @Nullable
    private FileDescriptionMediaPlayer fdMediaPlayer;
    private ChatController mChatController;
    private ChatAdapter chatAdapter;
    private ChatAdapter.Callback chatAdapterCallback;
    private QuoteLayoutHolder mQuoteLayoutHolder;
    private Quote mQuote = null;
    private CampaignMessage campaignMessage = null;
    private ChatReceiver mChatReceiver;
    private boolean isInMessageSearchMode;
    private boolean isResumed;
    private boolean isSendBlocked = false;
    private ChatStyle style;
    private FragmentChatBinding binding;
    private File externalCameraPhotoFile;
    @Nullable
    private AttachmentBottomSheetDialogFragment bottomSheetDialogFragment;
    @Nullable
    private PermissionDescriptionAlertDialogFragment permissionDescriptionAlertDialogFragment;
    private List<String> cameraPermissions;
    private List<Uri> mAttachedImages = new ArrayList<>();
    @Nullable
    private MediaRecorder recorder = null;
    @Nullable
    private String voiceFilePath = null;
    private boolean isNewMessageUpdateTimeoutOn = false;

    private QuickReplyItem quickReplyItem = null;
    private int previousChatItemsCount = 0;
    private Config config = ((Config)BaseConfig.instance);

    public static ChatFragment newInstance() {
        return newInstance(OpenWay.DEFAULT);
    }

    public static ChatFragment newInstance(@OpenWay int from) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_OPEN_WAY, from);
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(arguments);
        return chatFragment;
    }

    public static boolean isShown() {
        return chatIsShown;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        style = config.getChatStyle();

        // Статус бар подкрашивается только при использовании чата в стандартном Activity.
        if (activity instanceof ChatActivity) {
            ColorsHelper.setStatusBarColor(activity, style.chatStatusBarColorResId, style.windowLightStatusBarResId);
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);
        binding.setInputTextObservable(inputTextObservable);
        chatAdapterCallback = new ChatFragment.AdapterCallback();
        AudioManager audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        fdMediaPlayer = new FileDescriptionMediaPlayer(audioManager);
        initViews();
        initRecording();
        bindViews();
        initToolbar();
        setHasOptionsMenu(true);
        initController();
        setFragmentStyle();

        initUserInputState();
        initQuickReplies();
        initMediaPlayer();
        subscribeToFileDescription();
        chatIsShown = true;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FileDescription fileDescriptionDraft = getFileDescriptionDraft();
        if (FileUtils.isVoiceMessage(fileDescriptionDraft)) {
            setFileDescription(fileDescriptionDraft);
            mQuoteLayoutHolder.setVoice();
        }
        CampaignMessage campaignMessage = PrefUtils.getCampaignMessage();
        Bundle arguments = getArguments();
        if (arguments != null && campaignMessage != null) {
            @OpenWay int from = arguments.getInt(ARG_OPEN_WAY);
            if (from == OpenWay.DEFAULT) {
                return;
            }
            String uid = UUID.randomUUID().toString();
            mQuote = new Quote(uid, campaignMessage.getSenderName(), campaignMessage.getText(), null, campaignMessage.getReceivedDate().getTime());
            this.campaignMessage = campaignMessage;
            mQuoteLayoutHolder.setContent(
                    campaignMessage.getSenderName(),
                    campaignMessage.getText(),
                    null
            );
            PrefUtils.setCampaignMessage(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fdMediaPlayer != null) {
            fdMediaPlayer.release();
            fdMediaPlayer = null;
        }
        mChatController.unbindFragment();
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(mChatReceiver);
        }
        chatIsShown = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseConfig.instance.transport.setLifecycle(null);
    }

    private void initController() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mChatController = ChatController.getInstance();
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
        if (activity == null || fdMediaPlayer == null) {
            return;
        }
        initInputLayout(activity);
        mQuoteLayoutHolder = new QuoteLayoutHolder();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        binding.recycler.setLayoutManager(mLayoutManager);
        chatAdapter = new ChatAdapter(activity, chatAdapterCallback, fdMediaPlayer, mediaMetadataRetriever);
        RecyclerView.ItemAnimator itemAnimator = binding.recycler.getItemAnimator();
        if (itemAnimator != null) {
            itemAnimator.setChangeDuration(0);
        }
        binding.recycler.setAdapter(chatAdapter);
        binding.searchDownIb.setAlpha(DISABLED_ALPHA);
        binding.searchUpIb.setAlpha(DISABLED_ALPHA);
    }

    private void initInputLayout(@NonNull Activity activity) {
        applyTintAndColorState(activity);
        int attachmentVisibility = config.getAttachmentEnabled() ? View.VISIBLE : View.GONE;
        binding.addAttachment.setVisibility(attachmentVisibility);
        binding.addAttachment.setOnClickListener(v -> openBottomSheetAndGallery());
        binding.sendMessage.setOnClickListener(v -> onSendButtonClick());
        binding.sendMessage.setEnabled(false);
    }

    private void applyTintAndColorState(@NonNull Activity activity) {
        binding.sendMessage.setImageResource(style.sendMessageIconResId);
        binding.addAttachment.setImageResource(style.attachmentIconResId);
        binding.quoteClear.setImageResource(style.quoteClearIconResId);
        int fullColorStateListSize = 3;
        if (style.chatBodyIconsColorState != null
                && style.chatBodyIconsColorState.length >= fullColorStateListSize) {
            ColorStateList chatImagesColorStateList = ColorsHelper.getColorStateList(activity,
                    style.chatBodyIconsColorState[0],
                    style.chatBodyIconsColorState[1],
                    style.chatBodyIconsColorState[2]
            );
            ColorsHelper.setTintColorStateList(binding.sendMessage, chatImagesColorStateList);
            ColorsHelper.setTintColorStateList(binding.addAttachment, chatImagesColorStateList);
            ColorsHelper.setTintColorStateList(binding.quoteClear, chatImagesColorStateList);
        } else {
            int iconTint = style.chatBodyIconsTint == 0
                    ? style.inputIconTintResId : style.chatBodyIconsTint;
            ColorsHelper.setTint(activity, binding.sendMessage, iconTint);
            ColorsHelper.setTint(activity, binding.addAttachment, iconTint);
            int quoteClearIconTintResId = style.chatBodyIconsTint == 0
                    ? style.quoteClearIconTintResId : style.chatBodyIconsTint;
            ColorsHelper.setTint(activity, binding.quoteClear, quoteClearIconTintResId);
        }
    }

    private void initRecording() {
        final RecordButton recordButton = binding.recordButton;
        if (!style.voiceMessageEnabled || !config.getAttachmentEnabled()) {
            recordButton.setVisibility(View.GONE);
            return;
        }
        RecordView recordView = binding.recordView;
        recordView.setRecordPermissionHandler(
                () -> ThreadsPermissionChecker.isRecordAudioPermissionGranted(requireContext())
        );
        recordButton.setRecordView(recordView);
        if (!ThreadsPermissionChecker.isRecordAudioPermissionGranted(requireContext())) {
            recordButton.setListenForRecord(false);
            recordButton.setOnRecordClickListener(v -> {
                if (style.arePermissionDescriptionDialogsEnabled) {
                    showSafelyPermissionDescriptionDialog(PermissionDescriptionType.RECORD_AUDIO,
                            REQUEST_PERMISSION_RECORD_AUDIO);
                } else {
                    startRecordAudioPermissionActivity(REQUEST_PERMISSION_RECORD_AUDIO);
                }
            });
        }
        Drawable drawable = AppCompatResources.getDrawable(requireContext(),
                style.threadsRecordButtonBackground);
        if (drawable != null) {
            drawable = drawable.mutate();
            ColorsHelper.setDrawableColor(requireContext(), drawable,
                    style.threadsRecordButtonBackgroundColor);
            recordButton.setBackground(drawable);
        }
        recordButton.setImageResource(style.threadsRecordButtonIcon);
        recordButton.setColorFilter(ContextCompat.getColor(requireContext(), style.threadsRecordButtonIconColor), PorterDuff.Mode.SRC_ATOP);
        recordView.setCancelBounds(8);
        recordView.setSmallMicColor(style.threadsRecordButtonSmallMicColor);
        recordView.setLessThanSecondAllowed(false);
        recordView.setSlideToCancelText(requireContext().getString(R.string.threads_voice_message_slide_to_cancel));
        recordView.setSoundEnabled(false);
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                if (fdMediaPlayer != null) {
                    fdMediaPlayer.reset();
                    fdMediaPlayer.requestAudioFocus();
                }
                recordView.setVisibility(View.VISIBLE);
                startRecorder();
            }

            @Override
            public void onCancel() {
                Date start = new Date();
                LoggerEdna.debug("RecordView: onCancel");
                subscribe(
                        releaseRecorder()
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {
                                        },
                                        error -> LoggerEdna.error("initRecording -> onCancel " + error))
                );
                recordButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                LoggerEdna.info("onStart performance: " + (new Date().getTime() - start.getTime()));
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                Date start = new Date();
                subscribe(
                        releaseRecorder()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                            if (voiceFilePath != null) {
                                                File file = new File(voiceFilePath);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    addVoiceMessagePreview(file);
                                                } else {
                                                    audioConverter.convertToWav(file, ChatFragment.this);
                                                }
                                            } else {
                                                LoggerEdna.error("error finishing voice message recording");
                                            }
                                        },
                                        error -> LoggerEdna.error("ChatFragment onFinish ", error)
                                )
                );
                recordView.setVisibility(View.INVISIBLE);
                LoggerEdna.debug("RecordView: onFinish");
                LoggerEdna.debug("RecordTime: " + recordTime);
                recordButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                LoggerEdna.info("onFinish performance: " + (new Date().getTime() - start.getTime()));
            }

            @Override
            public void onLessThanSecond() {
                recordView.setVisibility(View.INVISIBLE);
                subscribe(
                        releaseRecorder()
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {},
                                        error -> LoggerEdna.error("initRecording -> onLessThanSecond ", error))
                );
                showToast(getString(R.string.threads_hold_button_to_record_audio));
                LoggerEdna.debug("RecordView: onLessThanSecond");
                recordButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

            private void startRecorder() {
                subscribe(
                        Completable.fromAction(() -> {
                                    synchronized (this) {
                                        Context context = getContext();
                                        if (context == null) {
                                            return;
                                        }
                                        recorder = new MediaRecorder();
                                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            voiceFilePath = context.getFilesDir().getAbsolutePath() + String.format("/voice%s.ogg", fileNameDateFormat.format(new Date()));
                                            recorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
                                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS);
                                        } else {
                                            voiceFilePath = context.getFilesDir().getAbsolutePath() + String.format("/voice%s.wav", fileNameDateFormat.format(new Date()));
                                            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
                                            recorder.setAudioEncodingBitRate(128000);
                                            recorder.setAudioSamplingRate(44100);
                                        }
                                        recorder.setOutputFile(voiceFilePath);
                                        try {
                                            recorder.prepare();
                                        } catch (IOException e) {
                                            LoggerEdna.error("prepare() failed");
                                        }
                                        recorder.start();
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {},
                                        error -> LoggerEdna.error("initRecording -> startRecorder ", error))
                );
            }

            private Completable releaseRecorder() {
                if (fdMediaPlayer != null) {
                    fdMediaPlayer.abandonAudioFocus();
                }
                return Completable.fromAction(() -> {
                    synchronized (this) {
                        if (recorder != null) {
                            try {
                                recorder.stop();
                                recorder.release();
                            } catch (RuntimeException runtimeException) {
                                LoggerEdna.error("Exception occurred in releaseRecorder but it's fine", runtimeException);
                            }
                            recorder = null;
                        }
                    }
                });
            }
        });
        recordView.setOnBasketAnimationEndListener(() -> {
            recordView.setVisibility(View.INVISIBLE);
            LoggerEdna.debug("RecordView: Basket Animation Finished");
        });
    }

    private void stopRecording() {
        if (recorder != null) {
            MotionEvent motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_UP, 0f, 0f, 0);
            binding.recordButton.onTouch(binding.recordButton, motionEvent);
            motionEvent.recycle();
        }
    }

    private void addVoiceMessagePreview(@NonNull File file) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        FileDescription fd = new FileDescription(
                requireContext().getString(R.string.threads_voice_message).toLowerCase(),
                FileProviderHelper.getUriForFile(context, file),
                file.length(),
                System.currentTimeMillis()
        );
        setFileDescription(fd);
        mQuoteLayoutHolder.setVoice();
    }

    private void initUserInputState() {
        subscribe(ChatUpdateProcessor.getInstance().getUserInputEnableProcessor()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateInputEnable,
                        error -> LoggerEdna.error("initUserInputState ", error)
                ));
    }

    private void initQuickReplies() {
        subscribe(ChatUpdateProcessor.getInstance().getQuickRepliesProcessor()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(quickReplies -> {
                            if (quickReplies.getItems().isEmpty()) {
                                hideQuickReplies();
                            } else {
                                showQuickReplies(quickReplies);
                            }
                        },
                        error -> LoggerEdna.error("initQuickReplies ", error)
                ));
    }

    private void initMediaPlayer() {
        if (fdMediaPlayer == null) {
            return;
        }
        subscribe(fdMediaPlayer.getUpdateProcessor()
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(update -> {
                            if (fdMediaPlayer == null) {
                                return;
                            }
                            if (isPreviewPlaying()) {
                                if (mQuoteLayoutHolder.ignorePlayerUpdates) {
                                    return;
                                }
                                MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
                                if (mediaPlayer != null) {
                                    mQuoteLayoutHolder.updateProgress(mediaPlayer.getCurrentPosition());
                                    mQuoteLayoutHolder.updateIsPlaying(mediaPlayer.isPlaying());
                                }
                                chatAdapter.resetPlayingHolder();
                            } else {
                                chatAdapter.playerUpdate();
                                mQuoteLayoutHolder.resetProgress();
                            }
                        },
                        error -> LoggerEdna.error("initMediaPlayer ", error)
                )
        );
    }

    private void bindViews() {
        binding.swipeRefresh.setSwipeListener(() -> {
        });
        binding.swipeRefresh.setOnRefreshListener(ChatFragment.this::onRefresh);
        binding.consultName.setOnClickListener(v -> {
            if (mChatController.isConsultFound()) {
                chatAdapterCallback.onConsultAvatarClick(mChatController.getCurrentConsultInfo().getId());
            }
        });
        binding.subtitle.setOnClickListener(v -> {
            if (mChatController.isConsultFound()) {
                chatAdapterCallback.onConsultAvatarClick(mChatController.getCurrentConsultInfo().getId());
            }
        });
        configureUserTypingSubscription();
        configureRecordButtonVisibility();
        binding.searchUpIb.setOnClickListener(view -> {
            if (TextUtils.isEmpty(binding.search.getText())) return;
            doFancySearch(binding.search.getText().toString(), false);
        });
        binding.searchDownIb.setOnClickListener(view -> {
            if (TextUtils.isEmpty(binding.search.getText())) return;
            doFancySearch(binding.search.getText().toString(), true);
        });
        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isInMessageSearchMode) {
                    return;
                }
                doFancySearch(s.toString(), true);
            }
        });
        binding.search.setOnEditorActionListener((v, actionId, event) -> {
            if (isInMessageSearchMode && actionId == EditorInfo.IME_ACTION_SEARCH) {
                doFancySearch(v.getText().toString(), false);
                return true;
            } else {
                return false;
            }
        });
        binding.recycler.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                binding.recycler.postDelayed(() -> {
                    if (style.scrollChatToEndIfUserTyping) {
                        scrollToPosition(chatAdapter.getItemCount() - 1, false);
                    } else {
                        binding.recycler.smoothScrollBy(0, oldBottom - bottom);
                    }
                }, 100);
            }
        });
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    int itemCount = chatAdapter.getItemCount();
                    if (itemCount - 1 - lastVisibleItemPosition > INVISIBLE_MSGS_COUNT) {
                        if (binding.scrollDownButtonContainer.getVisibility() != View.VISIBLE) {
                            binding.scrollDownButtonContainer.setVisibility(View.VISIBLE);
                            showUnreadMsgsCount(chatAdapter.getUnreadCount());
                        }
                    } else {
                        binding.scrollDownButtonContainer.setVisibility(View.GONE);
                        recyclerView.post(() -> setMessagesAsRead());
                    }
                }
            }
        });
        binding.scrollDownButtonContainer.setOnClickListener(v -> {
            showUnreadMsgsCount(0);
            int unreadCount = chatAdapter.getUnreadCount();
            if (unreadCount > 0) {
                scrollToNewMessages();
            } else {
                scrollToPosition(chatAdapter.getItemCount() - 1, false);
            }
            setMessagesAsRead();
            binding.scrollDownButtonContainer.setVisibility(View.GONE);
            if (isInMessageSearchMode) {
                hideSearchMode();
            }
        });
    }

    private void setMessagesAsRead() {
        chatAdapter.setAllMessagesRead();
        setMessagesAsReadForStorages();
    }

    private void setMessagesAsReadForStorages() {
        if (previousChatItemsCount == 0 || chatAdapter.getItemCount() != previousChatItemsCount) {
            mChatController.setMessagesInCurrentThreadAsReadInDB();
            PrefUtils.setUnreadPushCount(0);
            previousChatItemsCount = chatAdapter.getItemCount();
        }
    }

    private void configureUserTypingSubscription() {
        Disposable userTypingDisposable = RxUtils.toObservable(inputTextObservable)
                .throttleLatest(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .filter(charSequence -> charSequence.length() > 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onInputChanged,
                        error -> LoggerEdna.error("configureInputChangesSubscription ", error)
                );
        subscribe(userTypingDisposable);
    }

    private void onInputChanged(@NonNull String input) {
        mChatController.onUserTyping(input);
        updateLastUserActivityTime();
    }

    private void updateLastUserActivityTime() {
        LastUserActivityTimeCounter timeCounter = LastUserActivityTimeCounterSingletonProvider
                .INSTANCE.getLastUserActivityTimeCounter();
        timeCounter.updateLastUserActivityTime();
    }

    private void configureRecordButtonVisibility() {
        Disposable recordButtonVisibilityDisposable = Observable.combineLatest(
                        RxUtils.toObservableImmediately(inputTextObservable),
                        RxUtils.toObservableImmediately(fileDescription),
                        (s, fileDescriptionOptional) -> (TextUtils.isEmpty(s) || s.trim().isEmpty())
                                && fileDescriptionOptional.isEmpty()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setRecordButtonVisibility,
                        error -> LoggerEdna.error("configureInputChangesSubscription ", error)
                );
        subscribe(recordButtonVisibilityDisposable);
    }

    private void setRecordButtonVisibility(@NonNull Boolean isInputEmpty) {
        boolean isButtonVisible = isInputEmpty && style.voiceMessageEnabled
                && config.getAttachmentEnabled();
        binding.recordButton.setVisibility(isButtonVisible ? View.VISIBLE : View.GONE);
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
        String inputText = inputTextObservable.get();
        if (inputText == null || inputText.trim().length() == 0 && getFileDescription() == null) {
            return;
        }
        welcomeScreenVisibility(false);
        List<UpcomingUserMessage> input = new ArrayList<>();
        UpcomingUserMessage message = new UpcomingUserMessage(
                getFileDescription(),
                campaignMessage,
                mQuote,
                inputText.trim(),
                isCopy(inputText)
        );
        input.add(message);
        sendMessage(input);
    }

    @Nullable
    public FileDescription getFileDescription() {
        Optional<FileDescription> fileDescriptionOptional = fileDescription.get();
        if (fileDescriptionOptional != null && fileDescriptionOptional.isPresent()) {
            return fileDescriptionOptional.get();
        }
        return null;
    }

    private void setFileDescription(@Nullable FileDescription fileDescription) {
        this.fileDescription.set(Optional.ofNullable(fileDescription));
    }

    private void subscribeToFileDescription() {
        subscribe(RxUtils.toObservable(fileDescription)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(files -> {
                            boolean isEnable = !files.isEmpty() || !TextUtils.isEmpty(binding.inputEditView.getText());
                            binding.sendMessage.setEnabled(isEnable);
                        }
                )
        );
    }

    private void onRefresh() {
        //TODO: не знаю почему 500 mills так было
        subscribe(mChatController.requestItems()
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::afterRefresh,
                        onError -> LoggerEdna.error("onRefresh ", onError))
        );
    }

    private void afterRefresh(List<ChatItem> result) {
        int itemsBefore = chatAdapter.getItemCount();
        chatAdapter.addItems(result);
        scrollToPosition(chatAdapter.getItemCount() - itemsBefore, true);
        for (int i = 1; i < 5; i++) {//for solving bug with refresh layout doesn't stop refresh animation
            handler.postDelayed(() -> {
                binding.swipeRefresh.setRefreshing(false);
                binding.swipeRefresh.clearAnimation();
                binding.swipeRefresh.destroyDrawingCache();
                binding.swipeRefresh.invalidate();
            }, i * 500);
        }
    }

    private void setFragmentStyle() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        ColorsHelper.setBackgroundColor(activity, binding.chatRoot, style.chatBackgroundColor);
        ColorsHelper.setBackgroundColor(activity, binding.inputLayout, style.chatMessageInputColor);
        ColorsHelper.setBackgroundColor(activity, binding.bottomLayout, style.chatMessageInputColor);
        ColorsHelper.setBackgroundColor(activity, binding.recordView, style.chatMessageInputColor);

        ColorsHelper.setDrawableColor(activity, binding.searchUpIb.getDrawable(), style.chatToolbarTextColorResId);
        ColorsHelper.setDrawableColor(activity, binding.searchDownIb.getDrawable(), style.chatToolbarTextColorResId);

        binding.searchMore.setBackgroundColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor));
        binding.searchMore.setTextColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor));

        binding.swipeRefresh.setColorSchemeColors(getResources().getIntArray(style.threadsSwipeRefreshColors));

        binding.scrollDownButton.setBackgroundResource(style.scrollDownBackgroundResId);
        binding.scrollDownButton.setImageResource(style.scrollDownIconResId);
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) binding.scrollDownButton.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(style.scrollDownButtonHeight);
        lp.width = getResources().getDimensionPixelSize(style.scrollDownButtonWidth);
        ViewCompat.setElevation(binding.scrollDownButton, getResources().getDimension(style.scrollDownButtonElevation));

        ViewGroup.MarginLayoutParams lpButtonContainer = (ViewGroup.MarginLayoutParams) binding.scrollDownButtonContainer.getLayoutParams();
        final int margin = getResources().getDimensionPixelSize(style.scrollDownButtonMargin);
        lpButtonContainer.setMargins(margin, margin, margin, margin);

        binding.unreadMsgSticker.getBackground().setColorFilter(ContextCompat.getColor(activity, style.unreadMsgStickerColorResId), PorterDuff.Mode.SRC_ATOP);
        ViewCompat.setElevation(binding.unreadMsgSticker, getResources().getDimension(style.scrollDownButtonElevation));

        binding.unreadMsgCount.setTextColor(ContextCompat.getColor(activity, style.unreadMsgCountTextColorResId));
        ViewCompat.setElevation(binding.unreadMsgCount, getResources().getDimension(style.scrollDownButtonElevation));

        binding.inputEditView.setMinHeight((int) activity.getResources().getDimension(style.inputHeight));
        binding.inputEditView.setBackground(AppCompatResources.getDrawable(activity, style.inputBackground));
        binding.inputEditView.setHint(style.inputHint);
        binding.inputEditView.setMaxLines(INPUT_EDIT_VIEW_MIN_LINES_COUNT);
        binding.inputEditView.setPadding(
                getResources().getDimensionPixelSize(style.inputFieldPaddingLeft),
                getResources().getDimensionPixelSize(style.inputFieldPaddingTop),
                getResources().getDimensionPixelSize(style.inputFieldPaddingRight),
                getResources().getDimensionPixelSize(style.inputFieldPaddingBottom)
        );
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.inputEditView.getLayoutParams();
        params.setMargins(
                getResources().getDimensionPixelSize(style.inputFieldMarginLeft),
                getResources().getDimensionPixelSize(style.inputFieldMarginTop),
                getResources().getDimensionPixelSize(style.inputFieldMarginRight),
                getResources().getDimensionPixelSize(style.inputFieldMarginBottom)
        );
        binding.inputEditView.setLayoutParams(params);

        binding.inputEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(binding.inputEditView.getText())) {
                    binding.inputEditView.setMaxLines(INPUT_EDIT_VIEW_MIN_LINES_COUNT);
                } else {
                    binding.inputEditView.setMaxLines(INPUT_EDIT_VIEW_MAX_LINES_COUNT);
                }
                binding.sendMessage.setEnabled(!TextUtils.isEmpty(s) || hasAttachments());
            }
        });

        ColorsHelper.setTextColor(activity, binding.search, style.chatToolbarTextColorResId);
        ColorsHelper.setTextColor(activity, binding.subtitle, style.chatToolbarTextColorResId);
        ColorsHelper.setTextColor(activity, binding.consultName, style.chatToolbarTextColorResId);

        ColorsHelper.setTextColor(activity, binding.subtitle, style.chatToolbarTextColorResId);
        ColorsHelper.setTextColor(activity, binding.consultName, style.chatToolbarTextColorResId);

        ColorsHelper.setHintTextColor(activity, binding.inputEditView, style.chatMessageInputHintTextColor);

        ColorsHelper.setHintTextColor(activity, binding.search, style.chatToolbarHintTextColor);

        ColorsHelper.setTextColor(activity, binding.inputEditView, style.inputTextColor);

        if (!TextUtils.isEmpty(style.inputTextFont)) {
            try {
                Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), style.inputTextFont);
                this.binding.inputEditView.setTypeface(custom_font);
            } catch (Exception e) {
                LoggerEdna.error("setFragmentStyle", e);
            }
        }

        binding.flEmpty.setBackgroundColor(ContextCompat.getColor(activity, style.emptyStateBackgroundColorResId));
        Drawable progressDrawable = binding.progressBar.getIndeterminateDrawable().mutate();
        ColorsHelper.setDrawableColor(activity, progressDrawable, style.emptyStateProgressBarColorResId);
        binding.progressBar.setIndeterminateDrawable(progressDrawable);
        ColorsHelper.setTextColor(activity, binding.tvEmptyStateHint, style.emptyStateHintColorResId);
    }

    @Override
    public void onAllowClick(@NonNull PermissionDescriptionType type, int requestCode) {
        switch (type) {
            case STORAGE:
                startStoragePermissionActivity(requestCode);
                break;
            case RECORD_AUDIO:
                startRecordAudioPermissionActivity(requestCode);
                break;
            case CAMERA:
                startCameraPermissionActivity(requestCode);
                break;
            default:
                break;
        }
    }

    private boolean hasAttachments() {
        boolean hasVoice = !TextUtils.isEmpty(voiceFilePath);
        boolean hasFile = getFileDescription() != null;
        boolean hasImages = !(mAttachedImages == null || mAttachedImages.isEmpty());
        return hasVoice || hasFile || hasImages;
    }

    private void startStoragePermissionActivity(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL) {
            PermissionsActivity.startActivityForResult(
                    this,
                    REQUEST_PERMISSION_READ_EXTERNAL,
                    R.string.threads_permissions_read_external_storage_help_text,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);
        } else if (requestCode == REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY) {
            PermissionsActivity.startActivityForResult(
                    this,
                    REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY,
                    R.string.threads_permissions_read_external_storage_help_text,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void startRecordAudioPermissionActivity(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_RECORD_AUDIO) {
            PermissionsActivity.startActivityForResult(
                    this,
                    REQUEST_PERMISSION_RECORD_AUDIO,
                    R.string.threads_permissions_record_audio_help_text,
                    android.Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startCameraPermissionActivity(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            PermissionsActivity.startActivityForResult(
                    this,
                    REQUEST_PERMISSION_CAMERA,
                    R.string.threads_permissions_camera_and_write_external_storage_help_text,
                    cameraPermissions.toArray(new String[]{}));
        }
    }

    @Override
    public void onDialogDetached() {
        cameraPermissions = null;
        permissionDescriptionAlertDialogFragment = null;
    }

    @Override
    public void onCameraClick() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        boolean isCameraGranted = ThreadsPermissionChecker.isCameraPermissionGranted(activity);
        boolean isWriteGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ThreadsPermissionChecker.isWriteExternalPermissionGranted(activity);
        LoggerEdna.info("isCameraGranted = " + isCameraGranted + " isWriteGranted " + isWriteGranted);
        if (isCameraGranted && isWriteGranted) {
            if (config.getChatStyle().useExternalCameraApp) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    externalCameraPhotoFile = FileHelper.INSTANCE.createImageFile(activity);
                    Uri photoUri = FileProviderHelper.getUriForFile(activity, externalCameraPhotoFile);
                    LoggerEdna.debug("Image File uri resolved: " + photoUri.toString());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    // https://stackoverflow.com/a/48391446/1321401
                    MediaHelper.grantPermissions(activity, intent, photoUri);
                    startActivityForResult(intent, REQUEST_EXTERNAL_CAMERA_PHOTO);
                } catch (IllegalArgumentException e) {
                    LoggerEdna.error("Could not start external camera", e);
                    showToast(requireContext().getString(R.string.threads_camera_could_not_start_error));
                }

            } else {
                setBottomStateDefault();
                startActivityForResult(CameraActivity.getStartIntent(activity), REQUEST_CODE_PHOTO);
            }
        } else {
            ArrayList<String> permissions = new ArrayList<>();
            if (!isCameraGranted) {
                permissions.add(android.Manifest.permission.CAMERA);
            }
            if (!isWriteGranted) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (style.arePermissionDescriptionDialogsEnabled) {
                showSafelyCameraPermissionDescriptionDialog(permissions);
            } else {
                this.cameraPermissions = permissions;
                startCameraPermissionActivity(REQUEST_PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onGalleryClick() {
        startActivityForResult(GalleryActivity.getStartIntent(getActivity(), REQUEST_CODE_PHOTOS), REQUEST_CODE_PHOTOS);
    }

    @Override
    public void onImageSelectionChanged(List<Uri> imageList) {
        mAttachedImages = new ArrayList<>(imageList);
    }

    @Override
    public void onBottomSheetDetached() {
        bottomSheetDialogFragment = null;
    }

    private void showPopup() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        PopupMenu popup = new PopupMenu(activity, binding.popupMenuButton);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.threads_menu_main, popup.getMenu());
        Menu menu = popup.getMenu();
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        if (searchMenuItem != null) {
            SpannableString s = new SpannableString(searchMenuItem.getTitle());
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, style.menuItemTextColorResId)), 0, s.length(), 0);
            searchMenuItem.setTitle(s);
        }
        MenuItem filesAndMedia = menu.findItem(R.id.files_and_media);
        if (filesAndMedia != null) {
            SpannableString s2 = new SpannableString(filesAndMedia.getTitle());
            s2.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity, style.menuItemTextColorResId)), 0, s2.length(), 0);
            filesAndMedia.setTitle(s2);
        }
        filesAndMedia.setVisible(config.getFilesAndMediaMenuItemEnabled());
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }
        if (item.getItemId() == R.id.files_and_media) {
            if (isInMessageSearchMode) {
                onActivityBackPressed();
            }
            FilesActivity.startActivity(activity);
            return true;
        }
        if (item.getItemId() == R.id.search) {
            if (!isInMessageSearchMode) {
                search(false);
                binding.chatBackButton.setVisibility(View.VISIBLE);
            } else {
                return true;
            }
        }
        return false;
    }

    private void onReplyClick(ChatPhrase cp, int position) {
        hideCopyControls();
        scrollToPosition(position, true);
        UserPhrase userPhrase = cp instanceof UserPhrase ? (UserPhrase) cp : null;
        ConsultPhrase consultPhrase = cp instanceof ConsultPhrase ? (ConsultPhrase) cp : null;
        String text = cp.getPhraseText();
        if (userPhrase != null) {
            mQuote = new Quote(userPhrase.getId(),
                    requireContext().getString(R.string.threads_I),
                    userPhrase.getPhraseText(),
                    userPhrase.getFileDescription(),
                    userPhrase.getTimeStamp());
            mQuote.setFromConsult(false);
        } else if (consultPhrase != null) {
            mQuote = new Quote(consultPhrase.getId(),
                    consultPhrase.getConsultName() != null
                            ? consultPhrase.getConsultName()
                            : requireContext().getString(R.string.threads_consult),
                    consultPhrase.getPhraseText(),
                    consultPhrase.getFileDescription(),
                    consultPhrase.getTimeStamp());
            mQuote.setFromConsult(true);
            mQuote.setQuotedPhraseConsultId(consultPhrase.getConsultId());
        }
        setFileDescription(null);
        if (FileUtils.isImage(cp.getFileDescription())) {
            mQuoteLayoutHolder.setContent(
                    TextUtils.isEmpty(mQuote.getPhraseOwnerTitle()) ? "" : mQuote.getPhraseOwnerTitle(),
                    TextUtils.isEmpty(text) ? requireContext().getString(R.string.threads_image) : text,
                    cp.getFileDescription().getFileUri()
            );
        } else if (cp.getFileDescription() != null) {
            String fileName = "";
            try {
                Uri fileUri = cp.getFileDescription().getFileUri();
                fileName = cp.getFileDescription().getIncomingName() != null
                        ? cp.getFileDescription().getIncomingName()
                        : (fileUri != null ? FileUtils.getFileName(fileUri) : "");
            } catch (Exception e) {
                LoggerEdna.error("onReplyClick", e);
            }
            mQuoteLayoutHolder.setContent(TextUtils.isEmpty(mQuote.getPhraseOwnerTitle()) ? "" : mQuote.getPhraseOwnerTitle(),
                    fileName,
                    null);
        } else {
            mQuoteLayoutHolder.setContent(TextUtils.isEmpty(mQuote.getPhraseOwnerTitle()) ? "" : mQuote.getPhraseOwnerTitle(),
                    TextUtils.isEmpty(text) ? "" : text,
                    null);
        }
    }

    private void onCopyClick(Activity activity, ChatPhrase cp) {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            return;
        }
        cm.setPrimaryClip(new ClipData("", new String[]{"text/plain"}, new ClipData.Item(cp.getPhraseText())));
        PrefUtils.setLastCopyText(cp.getPhraseText());
        unChooseItem();
    }

    @Override
    public void onFilePickerClick() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        setBottomStateDefault();
        if (ThreadsPermissionChecker.isReadExternalPermissionGranted(activity)) {
            openFile();
        } else if (style.arePermissionDescriptionDialogsEnabled) {
            showSafelyPermissionDescriptionDialog(PermissionDescriptionType.STORAGE,
                    REQUEST_PERMISSION_READ_EXTERNAL);
        } else {
            startStoragePermissionActivity(REQUEST_PERMISSION_READ_EXTERNAL);
        }
    }

    @Override
    public void onSendClick() {
        if (mAttachedImages == null || mAttachedImages.isEmpty()) {
            showToast(getString(R.string.threads_failed_to_open_file));
            return;
        }
        subscribe(
                Single.fromCallable(() -> Stream.of(mAttachedImages)
                                .filter(value -> FileUtils.canBeSent(requireContext(), value))
                                .toList()
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(filteredPhotos -> {
                                    if (filteredPhotos.isEmpty()) {
                                        showToast(getString(R.string.threads_failed_to_open_file));
                                        return;
                                    }
                                    String inputText = inputTextObservable.get();
                                    if (inputText == null) {
                                        return;
                                    }
                                    List<UpcomingUserMessage> messages = new ArrayList<>();
                                    Uri fileUri = filteredPhotos.get(0);
                                    messages.add(new UpcomingUserMessage(
                                            new FileDescription(
                                                    requireContext().getString(R.string.threads_I),
                                                    fileUri,
                                                    FileUtils.getFileSize(fileUri),
                                                    System.currentTimeMillis()),
                                            campaignMessage,
                                            mQuote,
                                            inputText.trim(),
                                            isCopy(inputText))
                                    );
                                    for (int i = 1; i < filteredPhotos.size(); i++) {
                                        fileUri = filteredPhotos.get(i);
                                        FileDescription fileDescription = new FileDescription(
                                                requireContext().getString(R.string.threads_I),
                                                fileUri,
                                                FileUtils.getFileSize(fileUri),
                                                System.currentTimeMillis()
                                        );
                                        UpcomingUserMessage upcomingUserMessage = new UpcomingUserMessage(
                                                fileDescription, null, null, null, false
                                        );
                                        messages.add(upcomingUserMessage);
                                    }
                                    if (isSendBlocked) {
                                        clearInput();
                                        showToast(requireContext().getString(R.string.threads_message_were_unsent));
                                    } else {
                                        sendMessage(messages);
                                    }
                                }, onError -> {
                                    LoggerEdna.error("onSendClick ", onError);
                                }
                        ));
    }

    public void hideBottomSheet() {
        if (bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment.dismiss();
            bottomSheetDialogFragment = null;
        }
    }

    public void showBottomSheet() {
        if (bottomSheetDialogFragment == null) {
            bottomSheetDialogFragment = new AttachmentBottomSheetDialogFragment();
            bottomSheetDialogFragment.show(getChildFragmentManager(), AttachmentBottomSheetDialogFragment.TAG);
        }
    }

    private void doFancySearch(final String request, final boolean forward) {
        updateLastUserActivityTime();
        if (TextUtils.isEmpty(request)) {
            chatAdapter.removeHighlight();
            binding.searchUpIb.setAlpha(DISABLED_ALPHA);
            binding.searchDownIb.setAlpha(DISABLED_ALPHA);
            return;
        }
        onSearch(request, forward);
    }

    private void onSearch(String request, boolean forward) {
        mChatController.fancySearch(request, forward, this::onSearchEnd);
    }

    private void onSearchEnd(Pair<List<ChatItem>, ChatItem> dataPair) {
        int first = -1;
        int last = -1;
        List<ChatItem> data = dataPair.getFirst();
        ChatItem highlightedItem = dataPair.getSecond();
        //для поиска - ищем индекс первого совпадения
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof ChatPhrase) {
                if (((ChatPhrase) data.get(i)).getFound()) {
                    first = i;
                    break;
                }
            }
        }
        //для поиска - ищем индекс последнего совпадения
        for (int i = data.size() - 1; i >= 0; i--) {
            if (data.get(i) instanceof ChatPhrase) {
                if (((ChatPhrase) data.get(i)).getFound()) {
                    last = i;
                    break;
                }
            }
        }
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof ChatPhrase) {
                if ((data.get(i)).equals(highlightedItem)) {
                    //для поиска - если можно перемещаться, подсвечиваем
                    if (first != -1 && i > first) {
                        binding.searchUpIb.setAlpha(ENABLED_ALPHA);
                    } else {
                        binding.searchUpIb.setAlpha(DISABLED_ALPHA);
                    }
                    //для поиска - если можно перемещаться, подсвечиваем
                    if (last != -1 && i < last) {
                        binding.searchDownIb.setAlpha(ENABLED_ALPHA);
                    } else {
                        binding.searchDownIb.setAlpha(DISABLED_ALPHA);
                    }
                    break;
                }
            }
        }
        chatAdapter.addItems(data);
        if (highlightedItem != null) {
            chatAdapter.removeHighlight();
            scrollToPosition(chatAdapter.setItemHighlighted(highlightedItem), true);
        }
    }

    private void onPhotosResult(@NonNull Intent data) {
        ArrayList<Uri> photos = data.getParcelableArrayListExtra(GalleryActivity.PHOTOS_TAG);
        hideBottomSheet();
        welcomeScreenVisibility(false);
        String inputText = inputTextObservable.get();
        if (photos == null || photos.size() == 0 || inputText == null) {
            return;
        }
        subscribe(
                Single.fromCallable(() -> Stream.of(photos)
                                .filter(value -> FileUtils.canBeSent(requireContext(), value))
                                .toList()
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(filteredPhotos -> {
                            if (filteredPhotos.isEmpty()) {
                                showToast(getString(R.string.threads_failed_to_open_file));
                                return;
                            }
                            unChooseItem();
                            Uri fileUri = filteredPhotos.get(0);
                            UpcomingUserMessage uum =
                                    new UpcomingUserMessage(
                                            new FileDescription(
                                                    requireContext().getString(R.string.threads_I),
                                                    fileUri,
                                                    FileUtils.getFileSize(fileUri),
                                                    System.currentTimeMillis()
                                            ),
                                            campaignMessage,
                                            mQuote,
                                            inputText.trim(),
                                            isCopy(inputText)
                                    );
                            if (isSendBlocked) {
                                showToast(getString(R.string.threads_message_were_unsent));
                            } else {
                                mChatController.onUserInput(uum);
                            }
                            inputTextObservable.set("");
                            mQuoteLayoutHolder.clear();
                            for (int i = 1; i < filteredPhotos.size(); i++) {
                                fileUri = filteredPhotos.get(i);
                                uum = new UpcomingUserMessage(
                                        new FileDescription(
                                                requireContext().getString(R.string.threads_I),
                                                fileUri,
                                                FileUtils.getFileSize(fileUri),
                                                System.currentTimeMillis()
                                        ),
                                        null,
                                        null,
                                        null,
                                        false
                                );
                                mChatController.onUserInput(uum);
                            }
                        }, onError -> {
                            LoggerEdna.error("onPhotosResult ", onError);
                        })
        );


    }

    private void onExternalCameraPhotoResult() {
        setFileDescription(
                new FileDescription(
                        requireContext().getString(R.string.threads_image),
                        FileProviderHelper.getUriForFile(BaseConfig.instance.context, externalCameraPhotoFile),
                        externalCameraPhotoFile.length(),
                        System.currentTimeMillis()
                )
        );
        String inputText = inputTextObservable.get();
        sendMessage(Collections.singletonList(
                        new UpcomingUserMessage(
                                getFileDescription(),
                                campaignMessage,
                                mQuote,
                                inputText != null ? inputText.trim() : null,
                                false)
                )
        );
    }

    private void onFileResult(@NonNull Intent data) {
        Uri uri = data.getData();
        if (uri != null) {
            if (FileHelper.INSTANCE.isAllowedFileExtension(FileUtils.getExtensionFromMediaStore(BaseConfig.instance.context, uri))) {
                if (FileHelper.INSTANCE.isAllowedFileSize(FileUtils.getFileSizeFromMediaStore(BaseConfig.instance.context, uri))) {
                    try {
                        if (FileUtils.canBeSent(requireContext(), uri)) {
                            onFileResult(uri);
                            final int takeFlags = data.getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            requireActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } else {
                            showToast(getString(R.string.threads_failed_to_open_file));
                        }
                    } catch (SecurityException e) {
                        LoggerEdna.error("file can't be sent", e);
                        showToast(getString(R.string.threads_failed_to_open_file));
                    }
                } else {
                    // Недопустимый размер файла
                    showToast(getString(R.string.threads_not_allowed_file_size,
                            FileHelper.INSTANCE.getMaxAllowedFileSize()));
                }
            } else {
                // Недопустимое расширение файла
                showToast(getString(R.string.threads_not_allowed_file_extension));
            }
        }
    }

    private void onFileResult(@NonNull Uri uri) {
        LoggerEdna.info("onFileSelected: " + uri);
        setFileDescription(new FileDescription(requireContext().getString(R.string.threads_I), uri, FileUtils.getFileSize(uri), System.currentTimeMillis()));
        mQuoteLayoutHolder.setContent(requireContext().getString(R.string.threads_I), FileUtils.getFileName(uri), null);
    }

    private void onPhotoResult(@NonNull Intent data) {
        String imageExtra = data.getStringExtra(CameraActivity.IMAGE_EXTRA);
        if (imageExtra != null) {
            File file = new File(imageExtra);
            FileDescription fileDescription = new FileDescription(
                    requireContext().getString(R.string.threads_image),
                    FileProviderHelper.getUriForFile(requireContext(), file),
                    file.length(),
                    System.currentTimeMillis()
            );
            setFileDescription(
                    fileDescription
            );
            String inputText = inputTextObservable.get();
            UpcomingUserMessage uum = new UpcomingUserMessage(
                    fileDescription,
                    campaignMessage,
                    mQuote,
                    inputText != null ? inputText.trim() : null,
                    false
            );
            sendMessage(Collections.singletonList(uum));
        }
    }

    private void openBottomSheetAndGallery() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (ThreadsPermissionChecker.isReadExternalPermissionGranted(activity)) {
            setTitleStateCurrentOperatorConnected();
            if (bottomSheetDialogFragment == null) {
                showBottomSheet();
                scrollToPosition(chatAdapter.getItemCount() - 1, false);
            } else {
                hideBottomSheet();
            }
        } else if (style.arePermissionDescriptionDialogsEnabled) {
            showSafelyPermissionDescriptionDialog(PermissionDescriptionType.STORAGE,
                    REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY);
        } else {
            startStoragePermissionActivity(REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY);
        }
    }

    private void sendMessage(List<UpcomingUserMessage> messages) {
        sendMessage(messages, true);
    }

    private void sendMessage(List<UpcomingUserMessage> messages, boolean clearInput) {
        LoggerEdna.info("isInMessageSearchMode =" + isInMessageSearchMode);
        if (mChatController == null) {
            return;
        }
        for (UpcomingUserMessage message : messages) {
            mChatController.onUserInput(message);
        }
        if (null != chatAdapter) {
            setMessagesAsRead();
        }
        if (clearInput) {
            clearInput();
        }
        mChatController.hideQuickReplies();
    }

    private void clearInput() {
        inputTextObservable.set("");
        mQuoteLayoutHolder.clear();
        setBottomStateDefault();
        hideCopyControls();
        mAttachedImages.clear();
        if (isInMessageSearchMode) {
            onActivityBackPressed();
        }
    }

    public void addChatItem(final ChatItem item) {
        LoggerEdna.info("addChatItem: " + item);
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
        if (layoutManager == null) {
            return;
        }

        boolean isLastMessageVisible =
                (chatAdapter.getItemCount() - 1 - layoutManager.findLastVisibleItemPosition())
                        < INVISIBLE_MSGS_COUNT;
        if (item instanceof ConsultPhrase) {
            ConsultPhrase consultPhrase = (ConsultPhrase) item;
            consultPhrase.setRead(isLastMessageVisible && isResumed && !isInMessageSearchMode);
            chatAdapter.setAvatar(consultPhrase.getConsultId(), consultPhrase.getAvatarPath());
        }
        if (needsAddMessage(item)) {
            welcomeScreenVisibility(false);
            chatAdapter.addItems(Collections.singletonList(item));
            if (!isLastMessageVisible) {
                binding.scrollDownButtonContainer.setVisibility(View.VISIBLE);
                showUnreadMsgsCount(chatAdapter.getUnreadCount());
            }
            scrollDelayedOnNewMessageReceived(item instanceof UserPhrase, isLastMessageVisible);
        } else if (needsModifyImage(item)) {
            chatAdapter.modifyImageInItem(((ChatPhrase) item).getFileDescription());
        }
    }

    /**
     * Отлистывает сообщения до последнего в случаях, когда сообщение отправлено пользователем или
     * когда отображается последнее сообщение. <p>
     * Подробнее о логике подкрутки сообщений: https://jira.edna.ru/browse/EC-12190. <p>
     * После отправки сообщения пользователем чат-бот может ответить сразу несколькими
     * сообщениями. Установка флага isNewMessageUpdateTimeoutOn нужна, чтобы handler
     * запустился только 1 раз в течение указанной задержки delayMillis. <p>
     * Значение isLastMessageVisible берётся перед добавлением нового сообщения и используется
     * при срабатывании Runnable хендлера, itemCount используется актуальный на момент
     * срабатывания Runnable.
     *
     * @param isUserPhrase         true, если добавляется сообщение от пользователя
     * @param isLastMessageVisible отображается ли на экране последнее сообщение на момент получения
     *                             нового сообщения
     */
    private void scrollDelayedOnNewMessageReceived(boolean isUserPhrase,
                                                   boolean isLastMessageVisible) {
        if (!isNewMessageUpdateTimeoutOn) {
            isNewMessageUpdateTimeoutOn = true;
            handler.postDelayed(() -> {
                if (!isInMessageSearchMode) {
                    int itemCount = chatAdapter.getItemCount();
                    if (isLastMessageVisible || isUserPhrase) {
                        scrollToPosition(itemCount - 1, false);
                    }
                }
                isNewMessageUpdateTimeoutOn = false;
            }, 100);
        }
    }

    private void scrollToPosition(int itemCount, boolean smooth) {
        LoggerEdna.info("scrollToPosition: " + itemCount);
        if (itemCount >= 0) {
            if (smooth) {
                binding.recycler.smoothScrollToPosition(itemCount);
            } else {
                binding.recycler.scrollToPosition(itemCount);
            }
        }
    }

    private boolean needsAddMessage(ChatItem item) {
        if (item instanceof ScheduleInfo) {
            // Если сообщение о расписании уже показано, то снова отображать не нужно.
            // Если в сообщении о расписании указано, что сейчас чат работет,
            // то расписание отображать не нужно.
            return !((ScheduleInfo) item).isChatWorking() && !chatAdapter.hasSchedule();
        } else {
            ChatPhrase chatPhrase;
            try {
                chatPhrase = (ChatPhrase) item;
                return chatPhrase.getFileDescription() == null
                        || TextUtils.isEmpty(chatPhrase.getFileDescription().getOriginalPath());
            } catch (Exception exception) {
                return true;
            }
        }
    }

    private boolean needsModifyImage(ChatItem item) {
        ChatPhrase chatPhrase;
        try {
            chatPhrase = (ChatPhrase) item;
            return chatPhrase.getFileDescription() != null
                    && !TextUtils.isEmpty(chatPhrase.getFileDescription().getOriginalPath());
        } catch (Exception exception) {
            return false;
        }
    }

    public void addChatItems(final List<ChatItem> list) {
        if (list.size() == 0) {
            return;
        }
        int oldAdapterSize = chatAdapter.getList().size();
        welcomeScreenVisibility(false);
        chatAdapter.addItems(list);
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
        if (layoutManager == null ||
                list.size() == 1 && list.get(0) instanceof ConsultTyping ||
                isInMessageSearchMode) {
            return;
        }
        /*String firstUnreadUuid = mChatController.getFirstUnreadUuidId();
        ArrayList<ChatItem> newList = chatAdapter.getList();
        if (newList != null && !newList.isEmpty() && firstUnreadUuid != null) {
            for (int i = 1; i < newList.size(); i++) {
                if (newList.get(i) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) newList.get(i);
                    if (firstUnreadUuid.equalsIgnoreCase(cp.getId())) {
                        final int index = i;
                        h.postDelayed(
                                () -> binding.recycler.post(() -> layoutManager.scrollToPositionWithOffset(index - 1, 0)),
                                600
                        );
                        return;
                    }
                }
            }
        }*/
        int newAdapterSize = chatAdapter.getList().size();
        if (oldAdapterSize == 0) {
            scrollToPosition(chatAdapter.getItemCount() - 1, false);
        } else if (afterResume) {
            scrollToPosition(chatAdapter.getItemCount() - 1, false);
            afterResume = false;
        } else if (newAdapterSize > oldAdapterSize) {
            handler.postDelayed(() -> scrollToPosition(chatAdapter.getItemCount() - 1, false), 100);
            afterResume = false;
        }
    }

    public void setStateConsultConnected(ConsultInfo info) {
        if (!isAdded()) {
            return;
        }
        handler.post(
                () -> {
                    Context context = getContext();
                    if (context != null && isAdded()) {
                        if (!isInMessageSearchMode) {
                            binding.consultName.setVisibility(View.VISIBLE);
                        }
                        if (!getResources().getBoolean(style.fixedChatTitle)) {
                            if (!isInMessageSearchMode) {
                                binding.subtitle.setVisibility(View.VISIBLE);
                            }
                            if (!TextUtils.isEmpty(info.getName()) && !info.getName().equals("null")) {
                                binding.consultName.setText(info.getName());
                            } else {
                                binding.consultName.setText(context.getString(R.string.threads_unknown_operator));
                            }
                            setSubtitle(info, context);
                        }
                        if (!getResources().getBoolean(style.isChatSubtitleVisible)) {
                            binding.subtitle.setVisibility(View.GONE);
                        }
                        chatAdapter.removeConsultSearching();
                        showOverflowMenu();
                    }
                }
        );
    }

    private void setSubtitle(@NonNull ConsultInfo info, @NonNull Context context) {
        String subtitle;
        if (style.chatSubtitleShowOrgUnit
                && !TextUtils.isEmpty(info.getOrganizationUnit())) {
            subtitle = info.getOrganizationUnit();
        } else if (getResources().getBoolean(style.fixedChatSubtitle)
                || TextUtils.isEmpty(info.getRole())) {
            subtitle = context.getString(style.chatSubtitleTextResId);
        } else {
            ConsultRole role = ConsultRole.consultRoleFromString(info.getRole());
            if (ConsultRole.BOT == role
                    || ConsultRole.EXTERNAL_BOT == role) {
                subtitle = context.getString(R.string.threads_bot);
            } else {
                subtitle = context.getString(R.string.threads_operator);
            }
        }
        binding.subtitle.setText(subtitle);
    }

    public void setTitleStateDefault() {
        handler.post(
                () -> {
                    if (!isInMessageSearchMode) {
                        binding.subtitle.setVisibility(View.GONE);
                        binding.consultName.setVisibility(View.VISIBLE);
                        binding.searchLo.setVisibility(View.GONE);
                        binding.search.setText("");
                        binding.consultName.setText(style.chatTitleTextResId);
                    }
                }
        );
    }

    public void showConnectionError() {
        showToast(requireContext().getString(R.string.threads_message_not_sent));
    }

    public void showToast(final String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    public void setMessageState(String providerId, MessageState state) {
        chatAdapter.changeStateOfMessageByProviderId(providerId, state);
    }

    public void setSurveySentStatus(long uuid, MessageState sentState) {
        chatAdapter.changeStateOfSurvey(uuid, sentState);
    }

    private boolean isCopy(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        if (TextUtils.isEmpty(PrefUtils.getLastCopyText())) {
            return false;
        }
        return text.contains(PrefUtils.getLastCopyText());
    }

    private void hideCopyControls() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        setTitleStateCurrentOperatorConnected();
        ColorsHelper.setTint(activity, binding.chatBackButton, style.chatToolbarTextColorResId);
        ColorsHelper.setTint(activity, binding.popupMenuButton, style.chatToolbarTextColorResId);
        ColorsHelper.setBackgroundColor(activity, binding.toolbar, style.chatToolbarColorResId);

        binding.copyControls.setVisibility(View.GONE);
        if (!isInMessageSearchMode) {
            binding.consultName.setVisibility(View.VISIBLE);
        }

        boolean isFixedChatTitle = getResources().getBoolean(style.fixedChatTitle);
        boolean isVisibleSubtitle = getResources().getBoolean(style.isChatSubtitleVisible);
        if (mChatController != null && mChatController.isConsultFound() && !isInMessageSearchMode && !isFixedChatTitle && isVisibleSubtitle) {
            binding.subtitle.setVisibility(View.VISIBLE);
        }
    }

    private void setBottomStateDefault() {
        hideBottomSheet();
        if (!isInMessageSearchMode) binding.searchLo.setVisibility(View.GONE);
        if (!isInMessageSearchMode) binding.search.setText("");
    }

    private void setTitleStateCurrentOperatorConnected() {
        if (isInMessageSearchMode) return;

        if (mChatController.isConsultFound()) {
            if (!getResources().getBoolean(style.fixedChatTitle)) {
                binding.subtitle.setVisibility(View.VISIBLE);
            }
            binding.consultName.setVisibility(View.VISIBLE);
            binding.searchLo.setVisibility(View.GONE);
            binding.search.setText("");
        }
        if (!getResources().getBoolean(style.isChatSubtitleVisible)) {
            binding.subtitle.setVisibility(View.GONE);
        }
    }

    public void cleanChat() {
        final Activity activity = getActivity();
        if (!isAdded() || activity == null) {
            return;
        }
        handler.post(() -> {
            if (fdMediaPlayer == null) {
                return;
            }
            chatAdapter = new ChatAdapter(activity, chatAdapterCallback, fdMediaPlayer, mediaMetadataRetriever);
            binding.recycler.setAdapter(chatAdapter);
            setTitleStateDefault();
            welcomeScreenVisibility(false);
            binding.inputEditView.clearFocus();
            welcomeScreenVisibility(true);
        });
    }

    private void welcomeScreenVisibility(boolean show) {
        binding.welcome.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Remove close request from the thread history
     *
     * @return true - if deletion occurred, false - if there was no resolve request in the history
     */
    public boolean removeResolveRequest() {
        return chatAdapter.removeResolveRequest();
    }

    /**
     * Remove survey from the thread history
     *
     * @return true - if deletion occurred, false - if there was no survey in the history
     */
    public boolean removeSurvey(long sendingId) {
        return chatAdapter.removeSurvey(sendingId);
    }

    public int getCurrentItemsCount() {
        return chatAdapter.getCurrentItemCount();
    }

    public void setAllMessagesWereRead() {
        if (null != chatAdapter) {
            setMessagesAsRead();
        }
    }

    @Override
    public void updateProgress(FileDescription filedescription) {
        chatAdapter.updateProgress(filedescription);
    }

    @Override
    public void onDownloadError(FileDescription fileDescription, Throwable t) {
        if (isAdded()) {
            Activity activity = getActivity();
            if (activity != null) {
                updateProgress(fileDescription);
                if (t instanceof FileNotFoundException) {
                    showToast(getString(R.string.threads_error_no_file));
                    chatAdapter.onDownloadError(fileDescription);
                }
                if (t instanceof UnknownHostException) {
                    showToast(getString(R.string.threads_check_connection));
                    chatAdapter.onDownloadError(fileDescription);
                }
            }
        }
    }

    public void notifyConsultAvatarChanged(final String newAvatarUrl, final String consultId) {
        handler.post(() -> {
            if (chatAdapter != null) {
                chatAdapter.notifyAvatarChanged(newAvatarUrl, consultId);
            }
        });
    }

    private void setTitleStateSearchingConsult() {
        if (isInMessageSearchMode) {
            return;
        }

        binding.subtitle.setVisibility(View.GONE);
        binding.consultName.setVisibility(View.VISIBLE);
        binding.searchLo.setVisibility(View.GONE);
        binding.search.setText("");
        if (isAdded() && !getResources().getBoolean(style.fixedChatTitle)) {
            binding.consultName.setText(requireContext().getString(R.string.threads_searching_operator));
        }
    }

    public void setTitleStateSearchingMessage() {
        binding.subtitle.setVisibility(View.GONE);
        binding.consultName.setVisibility(View.GONE);
        binding.searchLo.setVisibility(View.VISIBLE);
        binding.search.setText("");
    }

    public void setStateSearchingConsult() {
        handler.post(() -> {
            setTitleStateSearchingConsult();
            chatAdapter.setSearchingConsult();
        });
    }

    public void removeSearching() {
        if (null != chatAdapter) {
            chatAdapter.removeConsultSearching();
            showOverflowMenu();
        }
    }

    private void unChooseItem() {
        hideCopyControls();
        if (chatAdapter != null) {
            chatAdapter.removeHighlight();
        }
    }

    public void removeSchedule(boolean checkSchedule) {
        chatAdapter.removeSchedule(checkSchedule);
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

    private void updateInputEnable(InputFieldEnableModel enableModel) {
        isSendBlocked = !enableModel.isEnabledSendButton();
        binding.sendMessage.setEnabled(enableModel.isEnabledSendButton() &&
                (!TextUtils.isEmpty(binding.inputEditView.getText()) || hasAttachments()));
        binding.inputEditView.setEnabled(enableModel.isEnabledInputField());
        binding.addAttachment.setEnabled(enableModel.isEnabledInputField());
        if (!enableModel.isEnabledInputField()) {
            Keyboard.hide(requireContext(), binding.inputEditView, 100);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PHOTOS:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onPhotosResult(data);
                }
                break;
            case REQUEST_EXTERNAL_CAMERA_PHOTO:
                if (resultCode == Activity.RESULT_OK && externalCameraPhotoFile != null) {
                    onExternalCameraPhotoResult();
                }
                externalCameraPhotoFile = null;
                break;
            case REQUEST_CODE_FILE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onFileResult(data);
                }
                break;
            case REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onPhotoResult(data);
                }
                break;
            case REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY:
                if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                    openBottomSheetAndGallery();
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                    onCameraClick();
                }
                break;
            case REQUEST_PERMISSION_READ_EXTERNAL:
                if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                    openFile();
                }
                break;
            case REQUEST_PERMISSION_RECORD_AUDIO:
                if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                    binding.recordButton.setListenForRecord(true);
                    showToast(requireContext().getString(R.string.threads_hold_button_to_record_audio));
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mChatController.setActivityIsForeground(true);
        scrollToFirstUnreadMessage();
        isResumed = true;
        chatIsShown = true;
        afterResume = true;
    }

    private void scrollToNewMessages() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        List<ChatItem> list = chatAdapter.getList();
        for (int i = 1; i < list.size(); i++) {
            ChatItem currentItem = list.get(i);
            if (currentItem instanceof UnreadMessages ||
                    currentItem instanceof ConsultPhrase
                            && !((ConsultPhrase) currentItem).isRead()) {
                layoutManager.scrollToPositionWithOffset(i - 1, 0);
                break;
            }
        }
    }

    private void scrollToFirstUnreadMessage() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        List<ChatItem> list = chatAdapter.getList();
        String firstUnreadUuid = mChatController.getFirstUnreadUuidId();
        if (list != null && !list.isEmpty() && firstUnreadUuid != null) {
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) instanceof ConsultPhrase) {
                    ConsultPhrase cp = (ConsultPhrase) list.get(i);
                    if (firstUnreadUuid.equalsIgnoreCase(cp.getId())) {
                        final int index = i;
                        handler.post(() -> {
                            if (!isInMessageSearchMode) {
                                binding.recycler.post(() -> layoutManager.scrollToPositionWithOffset(index - 1, 0));
                            }
                        });
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setCurrentThreadId(PrefUtils.getThreadId());
        BaseConfig.instance.transport.setLifecycle(getLifecycle());
        BaseConfig.instance.transport.getSettings();
        ChatController.getInstance().loadHistory();
    }

    @Override
    public void onStop() {
        super.onStop();
        isResumed = false;
        chatIsShown = false;
        isInMessageSearchMode = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecording();
        FileDescription fileDescription = getFileDescription();
        if (fileDescription == null || FileUtils.isVoiceMessage(fileDescription)) {
            PrefUtils.setFileDescriptionDraft(fileDescription);
        }
        mChatController.setActivityIsForeground(false);
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setRefreshing(false);
            binding.swipeRefresh.destroyDrawingCache();
            binding.swipeRefresh.clearAnimation();
        }
    }

    private void initToolbar() {
        Activity activity = getActivity();
        if (activity == null) return;
        binding.toolbar.setTitle("");
        ColorsHelper.setBackgroundColor(activity, binding.toolbar, style.chatToolbarColorResId);
        initToolbarShadow();
        if (activity instanceof ChatActivity) {
            binding.chatBackButton.setVisibility(View.VISIBLE);
        } else {
            binding.chatBackButton.setVisibility(style.showBackButton ? View.VISIBLE : View.GONE);
        }
        binding.chatBackButton.setOnClickListener(v -> onActivityBackPressed());
        binding.chatBackButton.setImageResource(style.chatToolbarBackIconResId);
        ColorsHelper.setTint(activity, binding.chatBackButton, style.chatToolbarTextColorResId);

        binding.popupMenuButton.setImageResource(style.chatToolbarPopUpMenuIconResId);
        ColorsHelper.setTint(activity, binding.popupMenuButton, style.chatToolbarTextColorResId);
        binding.popupMenuButton.setOnClickListener(v -> showPopup());
        showOverflowMenu();
        int toolbarInverseIconTint = style.chatBodyIconsTint == 0
                ? style.chatToolbarInverseIconTintResId : style.chatBodyIconsTint;
        binding.contentCopy.setImageResource(style.chatToolbarContentCopyIconResId);
        ColorsHelper.setTint(activity, binding.contentCopy, toolbarInverseIconTint);
        binding.reply.setImageResource(style.chatToolbarReplyIconResId);
        ColorsHelper.setTint(activity, binding.reply, toolbarInverseIconTint);
        if (getResources().getBoolean(style.fixedChatTitle)) {
            setTitleStateDefault();
        }
    }

    private void initToolbarShadow() {
        boolean isShadowVisible = getResources().getBoolean(style.isChatTitleShadowVisible);
        binding.toolbarShadow.setVisibility(isShadowVisible ? View.VISIBLE : View.INVISIBLE);
        if (!isShadowVisible) {
            binding.toolbar.setElevation(0);
        }
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
        Activity activity = getActivity();
        if (activity == null) {
            return true;
        }
        if (null != chatAdapter) {
            chatAdapter.removeHighlight();
        }
        boolean isNeedToClose = true;
        if (bottomSheetDialogFragment != null) {
            hideBottomSheet();
            return false;
        }
        if (binding.copyControls.getVisibility() == View.VISIBLE
                && binding.searchLo.getVisibility() == View.VISIBLE) {
            unChooseItem();
            binding.search.requestFocus();
            Keyboard.show(activity, binding.search, 100);
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
            if (chatAdapter != null) {
                scrollToPosition(chatAdapter.getItemCount() - 1, false);
            }
        }
        if (mQuoteLayoutHolder.isVisible()) {
            mQuoteLayoutHolder.clear();
            return false;
        }
        return isNeedToClose;
    }

    private void hideSearchMode() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        binding.searchLo.setVisibility(View.GONE);
        setMenuVisibility(true);
        isInMessageSearchMode = false;
        binding.search.setText("");
        Keyboard.hide(activity, binding.search, 100);
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

    private void search(final boolean searchInFiles) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        LoggerEdna.info("searchInFiles: " + searchInFiles);
        isInMessageSearchMode = true;
        setBottomStateDefault();
        setTitleStateSearchingMessage();
        binding.search.requestFocus();
        hideOverflowMenu();
        setMenuVisibility(false);
        Keyboard.show(activity, binding.search, 100);
        binding.swipeRefresh.setEnabled(false);
        binding.searchMore.setVisibility(View.GONE);
    }

    private void updateUIonPhraseLongClick(ChatPhrase chatPhrase, int position) {
        unChooseItem();
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        int toolbarInverseIconTint = style.chatBodyIconsTint == 0
                ? style.chatToolbarInverseIconTintResId : style.chatBodyIconsTint;
        //для случая, если popupMenuButton отображается при выделении сообщения
        ColorsHelper.setTint(activity, binding.popupMenuButton, toolbarInverseIconTint);
        ColorsHelper.setTint(activity, binding.chatBackButton, toolbarInverseIconTint);

        ColorsHelper.setBackgroundColor(activity, binding.toolbar,
                style.chatToolbarContextMenuColorResId);
        binding.toolbar.setElevation(0);

        binding.copyControls.setVisibility(View.VISIBLE);
        binding.consultName.setVisibility(View.GONE);
        binding.subtitle.setVisibility(View.GONE);

        if (binding.chatBackButton.getVisibility() == View.GONE) {
            binding.chatBackButton.setVisibility(View.VISIBLE);
        }
        binding.contentCopy.setOnClickListener(v -> {
            onCopyClick(activity, chatPhrase);
            hideBackButton();
        });
        binding.reply.setOnClickListener(v -> {
            onReplyClick(chatPhrase, position);
            hideBackButton();
        });
        chatAdapter.setItemHighlighted(chatPhrase);
    }

    public void showQuickReplies(QuickReplyItem quickReplies) {
        quickReplyItem = quickReplies;
        addChatItem(quickReplyItem);
        scrollToPosition(chatAdapter.getItemCount() - 1, false);
        hideBottomSheet();
    }

    public void hideQuickReplies() {
        if (chatAdapter != null && quickReplyItem != null) {
            chatAdapter.removeItem(quickReplyItem);
        }
    }

    private void openFile() {
        startActivityForResult(
                new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("*/*"), REQUEST_CODE_FILE);
    }

    @Override
    public void onFileSelected(File file) {
        final Uri uri = FileProviderHelper.getUriForFile(requireContext(), file);
        if (FileUtils.canBeSent(requireContext(), uri)) {
            onFileResult(uri);
        } else {
            showToast(getString(R.string.threads_failed_to_open_file));
        }
    }

    public void setClientNotificationDisplayType(ClientNotificationDisplayType type) {
        chatAdapter.setClientNotificationDisplayType(type);
    }

    public void setCurrentThreadId(long threadId) {
        chatAdapter.setCurrentThreadId(threadId);
    }

    private boolean isPreviewPlaying() {
        if (fdMediaPlayer != null) {
            return ObjectsCompat.equals(fdMediaPlayer.getFileDescription(), getFileDescription());
        }
        return false;
    }

    public void showEmptyState() {
        binding.flEmpty.setVisibility(View.VISIBLE);
        binding.tvEmptyStateHint.setText(R.string.threads_empty_state_hint);
    }

    public void hideEmptyState() {
        binding.flEmpty.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        binding.flEmpty.setVisibility(View.VISIBLE);
        binding.tvEmptyStateHint.setText(style.loaderTextResId);
    }

    public void hideProgressBar() {
        binding.flEmpty.setVisibility(View.GONE);
    }

    @Override
    public void acceptConvertedFile(@NonNull File convertedFile) {
        addVoiceMessagePreview(convertedFile);
    }

    private void showSafelyCameraPermissionDescriptionDialog(
            @NonNull List<String> cameraPermissions) {
        if (permissionDescriptionAlertDialogFragment == null) {
            this.cameraPermissions = cameraPermissions;
            showPermissionDescriptionDialog(PermissionDescriptionType.CAMERA, REQUEST_PERMISSION_CAMERA);
        }
    }

    private void showSafelyPermissionDescriptionDialog(@NonNull PermissionDescriptionType type,
                                                       int requestCode) {
        if (permissionDescriptionAlertDialogFragment == null) {
            showPermissionDescriptionDialog(type, requestCode);
        }
    }

    private void showPermissionDescriptionDialog(@NonNull PermissionDescriptionType type,
                                                 int requestCode) {
        permissionDescriptionAlertDialogFragment =
                PermissionDescriptionAlertDialogFragment.newInstance(type, requestCode);
        permissionDescriptionAlertDialogFragment.show(getChildFragmentManager(),
                PermissionDescriptionAlertDialogFragment.TAG);
    }

    private class QuoteLayoutHolder {
        private boolean ignorePlayerUpdates = false;
        @NonNull
        private String formattedDuration = "";

        private QuoteLayoutHolder() {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            binding.quoteButtonPlayPause.setColorFilter(ContextCompat.getColor(requireContext(), style.previewPlayPauseButtonColor), PorterDuff.Mode.SRC_ATOP);
            binding.quoteHeader.setTextColor(ContextCompat.getColor(activity, style.incomingMessageTextColor));
            binding.quoteClear.setOnClickListener(v -> clear());
            binding.quoteButtonPlayPause.setOnClickListener(v -> {
                if (fdMediaPlayer == null) {
                    return;
                }
                FileDescription fileDescription = getFileDescription();
                if (fileDescription != null && FileUtils.isVoiceMessage(fileDescription)) {
                    fdMediaPlayer.processPlayPause(fileDescription);
                    MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
                    if (mediaPlayer != null) {
                        init(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), mediaPlayer.isPlaying());
                    }
                }
            });
            binding.quoteSlider.addOnChangeListener((slider, value, fromUser) -> {
                if (fdMediaPlayer == null) {
                    return;
                }
                if (fromUser) {
                    MediaPlayer mediaPlayer = fdMediaPlayer.getMediaPlayer();
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo((int) value);
                    }
                }
            });
            binding.quoteSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    ignorePlayerUpdates = true;
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    ignorePlayerUpdates = false;
                }
            });
            binding.quoteSlider.setLabelFormatter(new VoiceTimeLabelFormatter());
        }

        private boolean isVisible() {
            return binding.quoteLayout.getVisibility() == View.VISIBLE;
        }

        private void setIsVisible(boolean isVisible) {
            if (isVisible) {
                binding.quoteLayout.setVisibility(View.VISIBLE);
                binding.delimeter.setVisibility(View.VISIBLE);
            } else {
                binding.quoteLayout.setVisibility(View.GONE);
                binding.delimeter.setVisibility(View.GONE);
            }
        }

        private void clear() {
            binding.quoteHeader.setText("");
            binding.quoteText.setText("");
            setIsVisible(false);
            mQuote = null;
            campaignMessage = null;
            setFileDescription(null);
            resetProgress();
            if (fdMediaPlayer != null && isPreviewPlaying()) {
                fdMediaPlayer.reset();
            }
            unChooseItem();
            ChatUpdateProcessor.getInstance().postAttachAudioFile(false);
        }

        private void setContent(String header, String text, Uri imagePath) {
            setIsVisible(true);
            if (header == null || header.equals("null")) {
                binding.quoteHeader.setVisibility(View.INVISIBLE);
            } else {
                binding.quoteHeader.setVisibility(View.VISIBLE);
                binding.quoteHeader.setText(header);
            }
            binding.quoteText.setVisibility(View.VISIBLE);
            binding.quotePast.setVisibility(View.VISIBLE);
            binding.quoteButtonPlayPause.setVisibility(View.GONE);
            binding.quoteSlider.setVisibility(View.GONE);
            binding.quoteDuration.setVisibility(View.GONE);
            binding.quoteText.setText(text);
            if (imagePath != null) {
                binding.quoteImage.setVisibility(View.VISIBLE);
                ImageLoader
                        .get()
                        .load(imagePath.toString())
                        .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP)
                        .into(binding.quoteImage);
            } else {
                binding.quoteImage.setVisibility(View.GONE);
            }
        }

        private void setVoice() {
            setIsVisible(true);
            binding.quoteButtonPlayPause.setVisibility(View.VISIBLE);
            binding.quoteSlider.setVisibility(View.VISIBLE);
            binding.quoteDuration.setVisibility(View.VISIBLE);
            binding.quoteHeader.setVisibility(View.GONE);
            binding.quoteText.setVisibility(View.GONE);
            binding.quotePast.setVisibility(View.GONE);
            formattedDuration = getFormattedDuration(getFileDescription());
            binding.quoteDuration.setText(formattedDuration);
            ChatUpdateProcessor.getInstance().postAttachAudioFile(true);
        }

        private void init(int maxValue, int progress, boolean isPlaying) {
            int effectiveProgress = Math.min(progress, maxValue);
            binding.quoteDuration.setText(VoiceTimeLabelFormatterKt.formatAsDuration(effectiveProgress));
            binding.quoteSlider.setEnabled(true);
            binding.quoteSlider.setValueTo(maxValue);
            binding.quoteSlider.setValue(effectiveProgress);
            binding.quoteButtonPlayPause.setImageResource(isPlaying ? style.voiceMessagePauseButton : style.voiceMessagePlayButton);
        }

        private void updateProgress(int progress) {
            LoggerEdna.info("updateProgress: " + progress);
            binding.quoteDuration.setText(VoiceTimeLabelFormatterKt.formatAsDuration(progress));
            binding.quoteSlider.setValue(Math.min(progress, binding.quoteSlider.getValueTo()));
        }

        private void updateIsPlaying(boolean isPlaying) {
            binding.quoteButtonPlayPause.setImageResource(isPlaying ? style.voiceMessagePauseButton : style.voiceMessagePlayButton);
        }

        private void resetProgress() {
            binding.quoteDuration.setText(formattedDuration);
            ignorePlayerUpdates = false;
            binding.quoteSlider.setEnabled(false);
            binding.quoteSlider.setValue(0);
            binding.quoteButtonPlayPause.setImageResource(style.voiceMessagePlayButton);
        }

        private String getFormattedDuration(@Nullable FileDescription fileDescription) {
            long duration = 0L;
            if (fileDescription != null && fileDescription.getFileUri() != null) {
                duration = FileUtilsKt.getDuration(mediaMetadataRetriever, fileDescription.getFileUri());
            }
            return VoiceTimeLabelFormatterKt.formatAsDuration(duration);
        }
    }

    private class AdapterCallback implements ChatAdapter.Callback {

        @Override
        public void onFileClick(FileDescription filedescription) {
            mChatController.onFileClick(filedescription);
        }

        @Override
        public void onPhraseLongClick(final ChatPhrase chatPhrase, final int position) {
            updateUIonPhraseLongClick(chatPhrase, position);
        }

        @Override
        public void onUserPhraseClick(final UserPhrase userPhrase, int position) {
            mChatController.forceResend(userPhrase);
        }

        @Override
        public void onQuoteClick(Quote quote) {
            if (quote == null) {
                return;
            }
            subscribe(
                    mChatController.downloadMessagesTillEnd()
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(list -> {
                                chatAdapter.addItems(list);
                                final int itemHighlightedIndex = chatAdapter.setItemHighlighted(quote.getUuid());
                                scrollToPosition(itemHighlightedIndex, true);
                                return list;
                            })
                            .delay(1500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    list -> {
                                        chatAdapter.removeHighlight();
                                        chatAdapter.addItems(list);
                                    },
                                    LoggerEdna::error
                            )
            );
        }

        @Override
        public void onConsultAvatarClick(String consultId) {
            if (config.getChatStyle().canShowSpecialistInfo) {
                Activity activity = getActivity();
                if (activity != null) {
                    mChatController.onConsultChoose(activity, consultId);
                }
            }
        }

        @Override
        public void onImageClick(ChatPhrase chatPhrase) {
            if (chatPhrase.getFileDescription().getFileUri() == null) {
                return;
            }
            if (chatPhrase instanceof UserPhrase) {
                if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_WAS_READ) {
                    mChatController.forceResend((UserPhrase) chatPhrase);
                }
                if (((UserPhrase) chatPhrase).getSentState() != MessageState.STATE_NOT_SENT) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.startActivity(ImagesActivity.getStartIntent(activity, chatPhrase.getFileDescription()));
                    }
                }
            } else if (chatPhrase instanceof ConsultPhrase) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.startActivity(ImagesActivity.getStartIntent(activity, chatPhrase.getFileDescription()));
                }
            }
        }

        @Override
        public void onFileDownloadRequest(FileDescription fileDescription) {
            mChatController.onFileDownloadRequest(fileDescription);
        }

        @Override
        public void onSystemMessageClick(SystemMessage systemMessage) {
        }

        @Override
        public void onRatingClick(@NonNull Survey survey, int rating) {
            Activity activity = getActivity();
            if (activity != null) {
                survey.getQuestions().get(0).setRate(rating);
                mChatController.onRatingClick(survey);
            }
        }

        @Override
        public void onResolveThreadClick(boolean approveResolve) {
            Activity activity = getActivity();
            if (activity != null) {
                mChatController.onResolveThreadClick(approveResolve);
            }
        }

        @Override
        public void onQiuckReplyClick(QuickReply quickReply) {
            hideQuickReplies();
            sendMessage(Collections.singletonList(
                            new UpcomingUserMessage(
                                    null,
                                    null,
                                    null,
                                    quickReply.getText().trim(),
                                    isCopy(quickReply.getText()))
                    ),
                    false
            );
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
