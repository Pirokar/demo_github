package im.threads.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.util.ObjectsCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.annimon.stream.Optional
import com.annimon.stream.Stream
import com.devlomi.record_view.OnRecordListener
import com.google.android.material.slider.Slider
import im.threads.R
import im.threads.business.annotation.OpenWay
import im.threads.business.audio.audioRecorder.AudioRecorder
import im.threads.business.broadcastReceivers.ProgressReceiver
import im.threads.business.chat_updates.ChatUpdateProcessorJavaGetter
import im.threads.business.config.BaseConfig
import im.threads.business.extensions.withMainContext
import im.threads.business.imageLoading.ImageLoader.Companion.get
import im.threads.business.logger.LogZipSender
import im.threads.business.logger.LoggerEdna.debug
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.logger.LoggerEdna.info
import im.threads.business.media.ChatCenterAudioConverter
import im.threads.business.media.ChatCenterAudioConverterCallback
import im.threads.business.media.FileDescriptionMediaPlayer
import im.threads.business.models.CampaignMessage
import im.threads.business.models.ChatItem
import im.threads.business.models.ChatPhrase
import im.threads.business.models.ClientNotificationDisplayType
import im.threads.business.models.ConsultInfo
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.ConsultRole
import im.threads.business.models.ConsultRole.Companion.consultRoleFromString
import im.threads.business.models.ConsultTyping
import im.threads.business.models.DateRow
import im.threads.business.models.FileDescription
import im.threads.business.models.InputFieldEnableModel
import im.threads.business.models.MessageStatus
import im.threads.business.models.QuickReply
import im.threads.business.models.QuickReplyItem
import im.threads.business.models.Quote
import im.threads.business.models.ScheduleInfo
import im.threads.business.models.Survey
import im.threads.business.models.SystemMessage
import im.threads.business.models.UnreadMessages
import im.threads.business.models.UpcomingUserMessage
import im.threads.business.models.UserPhrase
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.business.utils.Balloon.show
import im.threads.business.utils.FileProviderHelper
import im.threads.business.utils.FileUtils.canBeSent
import im.threads.business.utils.FileUtils.createImageFile
import im.threads.business.utils.FileUtils.getExtensionFromMediaStore
import im.threads.business.utils.FileUtils.getFileName
import im.threads.business.utils.FileUtils.getFileSize
import im.threads.business.utils.FileUtils.getFileSizeFromMediaStore
import im.threads.business.utils.FileUtils.getUpcomingUserMessagesFromSelection
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.isVoiceMessage
import im.threads.business.utils.MediaHelper.grantPermissionsForImageUri
import im.threads.business.utils.RxUtils
import im.threads.business.utils.ThreadsPermissionChecker
import im.threads.business.utils.ThreadsPermissionChecker.isRecordAudioPermissionGranted
import im.threads.business.utils.copyToBuffer
import im.threads.business.utils.getDuration
import im.threads.business.utils.isLastCopyText
import im.threads.databinding.EccFragmentChatBinding
import im.threads.ui.ChatStyle
import im.threads.ui.activities.CameraActivity
import im.threads.ui.activities.ChatActivity
import im.threads.ui.activities.GalleryActivity
import im.threads.ui.activities.GalleryActivity.Companion.getStartIntent
import im.threads.ui.activities.ImagesActivity.Companion.getStartIntent
import im.threads.ui.activities.filesActivity.FilesActivity.Companion.startActivity
import im.threads.ui.adapters.ChatAdapter
import im.threads.ui.config.Config
import im.threads.ui.controllers.ChatController
import im.threads.ui.files.FileSelectedListener
import im.threads.ui.fragments.PermissionDescriptionAlertFragment.Companion.newInstance
import im.threads.ui.fragments.PermissionDescriptionAlertFragment.OnAllowPermissionClickListener
import im.threads.ui.holders.BaseHolder.Companion.statuses
import im.threads.ui.permissions.PermissionsActivity
import im.threads.ui.styles.permissions.PermissionDescriptionType
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.FileHelper.isAllowedFileExtension
import im.threads.ui.utils.FileHelper.isAllowedFileSize
import im.threads.ui.utils.FileHelper.maxAllowedFileSize
import im.threads.ui.utils.hideKeyboard
import im.threads.ui.utils.isNotVisible
import im.threads.ui.utils.isVisible
import im.threads.ui.utils.runOnUiThread
import im.threads.ui.utils.showKeyboard
import im.threads.ui.utils.visible
import im.threads.ui.views.VoiceTimeLabelFormatter
import im.threads.ui.views.formatAsDuration
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Весь функционал чата находится здесь во фрагменте,
 * чтобы чат можно было встроить в приложене в навигацией на фрагментах
 */
class ChatFragment :
    BaseFragment(),
    AttachmentBottomSheetDialogFragment.Callback,
    ProgressReceiver.Callback,
    PopupMenu.OnMenuItemClickListener,
    FileSelectedListener,
    ChatCenterAudioConverterCallback,
    OnAllowPermissionClickListener {

    private val handler = Handler(Looper.getMainLooper())
    private val fileNameDateFormat = SimpleDateFormat("dd.MM.yyyy.HH:mm:ss.S", Locale.getDefault())
    private val inputTextObservable = ObservableField("")
    val fileDescription = ObservableField(Optional.empty<FileDescription?>())
    private val mediaMetadataRetriever = MediaMetadataRetriever()
    private val audioConverter = ChatCenterAudioConverter()
    private val chatUpdateProcessor = ChatUpdateProcessorJavaGetter()
    private var fdMediaPlayer: FileDescriptionMediaPlayer? = null
    private val chatController: ChatController by lazy { ChatController.getInstance() }
    private var chatAdapter: ChatAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var chatAdapterCallback: ChatAdapter.Callback? = null
    private var mQuoteLayoutHolder: QuoteLayoutHolder? = null
    private var mQuote: Quote? = null
    private var campaignMessage: CampaignMessage? = null
    private var mChatReceiver: ChatReceiver? = null
    private var isInMessageSearchMode = false
    private var isResumed = false
    private var isSendBlocked = false
    private var _binding: EccFragmentChatBinding? = null
    private val binding get() = _binding!!
    private var externalCameraPhotoFile: File? = null
    private var bottomSheetDialogFragment: AttachmentBottomSheetDialogFragment? = null
    private var permissionDescriptionAlertDialogFragment: PermissionDescriptionAlertFragment? = null
    private var cameraPermissions: List<String>? = null
    private var mAttachedImages: MutableList<Uri>? = ArrayList()
    private var recorder: AudioRecorder? = null
    private var isNewMessageUpdateTimeoutOn = false
    private var quickReplyItem: QuickReplyItem? = null
    private var previousChatItemsCount = 0
    private val config = Config.getInstance()
    var style: ChatStyle = config.getChatStyle()
        private set
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        info(ChatFragment::class.java.simpleName + " onCreateView.")
        val activity: Activity? = activity

        // Статус бар подкрашивается только при использовании чата в стандартном Activity.
        if (activity is ChatActivity) {
            ColorsHelper.setStatusBarColor(WeakReference(activity), style.chatStatusBarColorResId, style.windowLightStatusBarResId)
        }
        _binding = DataBindingUtil.inflate(inflater, R.layout.ecc_fragment_chat, container, false)
        binding.inputTextObservable = inputTextObservable
        chatAdapterCallback = AdapterCallback()
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        fdMediaPlayer = FileDescriptionMediaPlayer(audioManager)
        initViews()
        initRecording()
        bindViews()
        initToolbar()
        setHasOptionsMenu(true)
        initController()
        setFragmentStyle()
        initUserInputState()
        initQuickReplies()
        initMediaPlayer()
        subscribeToFileDescription()
        isShown = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        info(ChatFragment::class.java.simpleName + " onViewCreated.")
        super.onViewCreated(view, savedInstanceState)
        val fileDescriptionDraft = chatController.fileDescriptionDraft
        if (isVoiceMessage(fileDescriptionDraft)) {
            setFileDescription(fileDescriptionDraft)
            mQuoteLayoutHolder?.setVoice()
        }
        val campaignMessage = chatController.campaignMessage
        val arguments = arguments
        if (arguments != null && campaignMessage != null) {
            @OpenWay val from = arguments.getInt(ARG_OPEN_WAY)
            if (from == OpenWay.DEFAULT) {
                return
            }
            val uid = UUID.randomUUID().toString()
            mQuote = Quote(uid, campaignMessage.senderName, campaignMessage.text, null, campaignMessage.receivedDate.time)
            this.campaignMessage = campaignMessage
            mQuoteLayoutHolder?.setContent(
                campaignMessage.senderName,
                campaignMessage.text,
                null,
                false
            )
            chatController.campaignMessage = null
        }
    }

    override fun onStart() {
        super.onStart()
        info(ChatFragment::class.java.simpleName + " onStart.")
        ChatController.getInstance().onViewStart()
        initRecordButtonState()
        chatController.threadId?.let { setCurrentThreadId(it) }
        BaseConfig.instance.transport.setLifecycle(lifecycle)
        ChatController.getInstance().settings
        ChatController.getInstance().loadHistory()
    }

    override fun onStop() {
        info(ChatFragment::class.java.simpleName + " onStop.")
        super.onStop()
        chatController.onViewStop()
        isShown = false
        isInMessageSearchMode = false
        fdMediaPlayer?.clearClickedDownloadPath()
        recorder = null
    }

    override fun onPause() {
        super.onPause()
        info(ChatFragment::class.java.simpleName + " onPause.")
        isResumed = false
        stopRecording()
        val fileDescription = getFileDescription()
        if (fileDescription == null || isVoiceMessage(fileDescription)) {
            chatController.fileDescriptionDraft = fileDescription
        }
        chatController.setActivityIsForeground(false)
        if (isAdded) {
            binding.swipeRefresh.isRefreshing = false
            binding.swipeRefresh.destroyDrawingCache()
            binding.swipeRefresh.clearAnimation()
            chatAdapter?.onPauseView()
        }
    }

    override fun onResume() {
        info(ChatFragment::class.java.simpleName + " onResume.")
        super.onResume()
        chatController.setActivityIsForeground(true)
        scrollToFirstUnreadMessage()
        chatAdapter?.onResumeView()
        isResumed = true
        isShown = true
        afterResume = true
    }

    override fun onDestroyView() {
        info(ChatFragment::class.java.simpleName + " onDestroyView.")
        fdMediaPlayer?.apply {
            release()
            fdMediaPlayer = null
        }
        chatController.unbindFragment()
        val activity: Activity? = activity
        activity?.unregisterReceiver(mChatReceiver)
        isShown = false
        _binding = null
        statuses.clear()
        super.onDestroyView()
    }

    override fun onDestroy() {
        info(ChatFragment::class.java.simpleName + " onDestroy.")
        super.onDestroy()
        BaseConfig.instance.transport.setLifecycle(null)
        chatController.onViewDestroy()
        chatAdapter?.onDestroyView()
    }

    fun setupStartSecondLevelScreen() {
        resumeAfterSecondLevelScreen = true
    }

    fun isStartSecondLevelScreen(): Boolean {
        return resumeAfterSecondLevelScreen
    }
    val lastVisibleItemPosition: Int
        get() = if (isAdded) {
            mLayoutManager?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
        } else {
            RecyclerView.NO_POSITION
        }

    fun scrollToElementByIndex(index: Int) {
        if (isAdded) {
            mLayoutManager?.smoothScrollToPosition(binding.recycler, RecyclerView.State(), index)
        }
    }

    val elements: List<ChatItem>
        get() = if (isAdded) {
            val list: List<ChatItem>? = chatAdapter?.list
            list ?: ArrayList()
        } else {
            ArrayList()
        }

    private fun initController() {
        val activity = activity ?: return
        welcomeScreenVisibility(chatController.isNeedToShowWelcome)
        chatController.bindFragment(this)
        mChatReceiver = ChatReceiver()
        val intentFilter = IntentFilter(ACTION_SEARCH_CHAT_FILES)
        intentFilter.addAction(ACTION_SEARCH)
        intentFilter.addAction(ACTION_SEND_QUICK_MESSAGE)
        activity.registerReceiver(mChatReceiver, intentFilter)
    }

    private fun initViews() {
        val activity: Activity? = activity
        if (activity == null || fdMediaPlayer == null || chatAdapterCallback == null) {
            return
        }
        initInputLayout(activity)
        mQuoteLayoutHolder = QuoteLayoutHolder()
        mLayoutManager = LinearLayoutManager(activity)
        binding.recycler.layoutManager = mLayoutManager
        chatAdapter = ChatAdapter(
            chatAdapterCallback!!,
            fdMediaPlayer!!,
            mediaMetadataRetriever,
            ChatController.getInstance().messageErrorProcessor
        )
        val itemAnimator = binding.recycler.itemAnimator
        if (itemAnimator != null) {
            itemAnimator.changeDuration = 0
        }
        binding.recycler.adapter = chatAdapter
        binding.searchDownIb.alpha = DISABLED_ALPHA
        binding.searchUpIb.alpha = DISABLED_ALPHA
    }

    private fun initInputLayout(activity: Activity) {
        applyTintAndColorState(activity)
        val attachmentVisibility = if (config.getIsAttachmentsEnabled()) View.VISIBLE else View.GONE
        binding.addAttachment.visibility = attachmentVisibility
        binding.addAttachment.setOnClickListener { openBottomSheetAndGallery() }
        binding.sendMessage.setOnClickListener { onSendButtonClick() }
        binding.sendMessage.isEnabled = false
    }

    private fun applyTintAndColorState(activity: Activity) {
        binding.sendMessage.setImageResource(style.sendMessageIconResId)
        binding.addAttachment.setImageResource(style.attachmentIconResId)
        binding.quoteClear.setImageResource(style.quoteClearIconResId)
        val fullColorStateListSize = 3
        if (style.chatBodyIconsColorState != null &&
            style.chatBodyIconsColorState.size >= fullColorStateListSize
        ) {
            val chatImagesColorStateList = ColorsHelper.getColorStateList(
                activity,
                style.chatBodyIconsColorState[0],
                style.chatBodyIconsColorState[1],
                style.chatBodyIconsColorState[2]
            )
            ColorsHelper.setTintColorStateList(binding.sendMessage, chatImagesColorStateList)
            ColorsHelper.setTintColorStateList(binding.addAttachment, chatImagesColorStateList)
            ColorsHelper.setTintColorStateList(binding.quoteClear, chatImagesColorStateList)
        } else {
            val iconTint = if (style.chatBodyIconsTint == 0) style.inputIconTintResId else style.chatBodyIconsTint
            ColorsHelper.setTint(activity, binding.sendMessage, iconTint)
            ColorsHelper.setTint(activity, binding.addAttachment, iconTint)
            val quoteClearIconTintResId = if (style.chatBodyIconsTint == 0) style.quoteClearIconTintResId else style.chatBodyIconsTint
            ColorsHelper.setTint(activity, binding.quoteClear, quoteClearIconTintResId)
        }
    }

    private fun initRecordButtonState() {
        val recordButton = binding.recordButton
        if (!isRecordAudioPermissionGranted(requireContext())) {
            recordButton.isListenForRecord = false
            recordButton.setOnRecordClickListener {
                if (style.arePermissionDescriptionDialogsEnabled) {
                    showSafelyPermissionDescriptionDialog(
                        PermissionDescriptionType.RECORD_AUDIO,
                        REQUEST_PERMISSION_RECORD_AUDIO
                    )
                } else {
                    startRecordAudioPermissionActivity(REQUEST_PERMISSION_RECORD_AUDIO)
                }
            }
        } else {
            recordButton.isListenForRecord = true
            recordButton.setOnRecordClickListener(null)
        }
    }

    private fun initRecording() {
        val recordButton = binding.recordButton
        if (!style.voiceMessageEnabled) {
            recordButton.visibility = View.GONE
            return
        }
        val recordView = binding.recordView
        recordView.setRecordPermissionHandler { isRecordAudioPermissionGranted(requireContext()) }
        recordButton.setRecordView(recordView)
        var drawable = AppCompatResources.getDrawable(
            requireContext(),
            style.threadsRecordButtonBackground
        )
        if (drawable != null) {
            drawable = drawable.mutate()
            ColorsHelper.setDrawableColor(
                requireContext(),
                drawable,
                style.threadsRecordButtonBackgroundColor
            )
            recordButton.background = drawable
        }
        recordButton.setImageResource(style.threadsRecordButtonIcon)
        recordButton.setColorFilter(
            ContextCompat.getColor(requireContext(), style.threadsRecordButtonIconColor),
            PorterDuff.Mode.SRC_ATOP
        )
        recordView.cancelBounds = 8f
        recordView.setSmallMicColor(style.threadsRecordButtonSmallMicColor)
        recordView.setLessThanSecondAllowed(false)
        recordView.setSlideToCancelText(requireContext().getString(R.string.ecc_voice_message_slide_to_cancel))
        recordView.setSoundEnabled(false)
        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                fdMediaPlayer?.reset()
                fdMediaPlayer?.requestAudioFocus()
                recordView.visibility = View.VISIBLE
                startRecorder()
            }

            override fun onCancel() {
                val start = Date()
                debug("RecordView: onCancel")
                subscribe(
                    releaseRecorder()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            {}
                        ) { error: Throwable -> error("initRecording -> onCancel $error") }
                )
                recordButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                info("onStart performance: " + (Date().time - start.time))
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                val start = Date()
                subscribe(
                    releaseRecorder()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                recorder?.let {
                                    if (isResumed) {
                                        val file = File(it.voiceFilePath)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            addVoiceMessagePreview(file)
                                        } else {
                                            audioConverter.convertToWav(file, this@ChatFragment)
                                        }
                                    }
                                } ?: error("error finishing voice message recording")
                            }
                        ) { error: Throwable? -> error("ChatFragment onFinish ", error) }
                )
                recordView.visibility = View.INVISIBLE
                debug("RecordView: onFinish")
                debug("RecordTime: $recordTime")
                recordButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                info("onFinish performance: " + (Date().time - start.time))
            }

            override fun onLessThanSecond() {
                recordView.visibility = View.INVISIBLE
                subscribe(
                    releaseRecorder()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            {}
                        ) { error: Throwable? -> error("initRecording -> onLessThanSecond ", error) }
                )
                show(requireContext(), getString(R.string.ecc_hold_button_to_record_audio))
                debug("RecordView: onLessThanSecond")
                recordButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }

            override fun onLock() {
            }

            private fun startRecorder() {
                subscribe(
                    Completable.fromAction {
                        synchronized(this) {
                            val context = context ?: return@fromAction
                            recorder = AudioRecorder(context)
                            recorder?.prepareWithDefaultConfig(fileNameDateFormat)
                            recorder?.start()
                        }
                    }
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            {}
                        ) { error: Throwable? -> error("initRecording -> startRecorder ", error) }
                )
            }

            private fun releaseRecorder(): Completable {
                fdMediaPlayer?.abandonAudioFocus()
                return Completable.fromAction {
                    synchronized(this) {
                        recorder?.stop()
                    }
                }
            }
        })
        recordView.setOnBasketAnimationEndListener {
            recordView.visibility = View.INVISIBLE
            debug("RecordView: Basket Animation Finished")
        }
    }

    private fun stopRecording() {
        if (recorder != null) {
            val motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_UP, 0f, 0f, 0)
            binding.recordButton.onTouch(binding.recordButton, motionEvent)
            motionEvent.recycle()
        }
    }

    private fun addVoiceMessagePreview(file: File) {
        val context = context ?: return
        val fd = FileDescription(
            requireContext().getString(R.string.ecc_voice_message).lowercase(Locale.getDefault()),
            FileProviderHelper.getUriForFile(context, file),
            file.length(),
            System.currentTimeMillis()
        )
        setFileDescription(fd)
        mQuoteLayoutHolder?.setVoice()
    }

    private fun initUserInputState() {
        subscribe(
            chatUpdateProcessor.processor.userInputEnableProcessor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { enableModel: InputFieldEnableModel -> updateInputEnable(enableModel) }
                ) { error: Throwable? -> error("initUserInputState ", error) }
        )
    }

    private fun initQuickReplies() {
        subscribe(
            chatUpdateProcessor.processor.quickRepliesProcessor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { quickReplies: QuickReplyItem? ->
                        if (quickReplies == null || quickReplies.items.isEmpty()) {
                            hideQuickReplies()
                        } else {
                            showQuickReplies(quickReplies)
                        }
                    }
                ) { error: Throwable? -> error("initQuickReplies ", error) }
        )
    }

    private fun initMediaPlayer() {
        fdMediaPlayer?.let { player ->
            player.updateProcessor
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (fdMediaPlayer == null) {
                            return@subscribe
                        }
                        if (isPreviewPlaying) {
                            if (mQuoteLayoutHolder!!.ignorePlayerUpdates) {
                                return@subscribe
                            }
                            val mediaPlayer = player.mediaPlayer
                            if (mediaPlayer != null) {
                                mQuoteLayoutHolder?.updateProgress(mediaPlayer.currentPosition)
                                mQuoteLayoutHolder?.updateIsPlaying(mediaPlayer.isPlaying)
                            }
                            chatAdapter?.resetPlayingHolder()
                        } else {
                            chatAdapter?.playerUpdate()
                            mQuoteLayoutHolder?.resetProgress()
                        }
                    }
                ) { error: Throwable? -> error("initMediaPlayer ", error) }
        }
    }

    private fun bindViews() {
        binding.swipeRefresh.setSwipeListener {}
        binding.swipeRefresh.setOnRefreshListener { onRefresh() }
        binding.consultName.setOnClickListener {
            if (chatController.isConsultFound) {
                chatAdapterCallback?.onConsultAvatarClick(chatController.currentConsultInfo!!.id)
            }
        }
        binding.consultName.setOnLongClickListener {
            val context = context
            if (context != null) {
                LogZipSender(context).shareLogs()
            }
            true
        }
        binding.subtitle.setOnClickListener {
            if (chatController.isConsultFound) {
                chatAdapterCallback?.onConsultAvatarClick(chatController.currentConsultInfo?.id)
            }
        }
        configureUserTypingSubscription()
        configureRecordButtonVisibility()
        binding.searchUpIb.setOnClickListener {
            if (TextUtils.isEmpty(binding.search.text)) return@setOnClickListener
            doFancySearch(binding.search.text.toString(), false)
        }
        binding.searchDownIb.setOnClickListener {
            if (TextUtils.isEmpty(binding.search.text)) return@setOnClickListener
            doFancySearch(binding.search.text.toString(), true)
        }
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!isInMessageSearchMode) {
                    return
                }
                doFancySearch(s.toString(), true)
            }
        })
        binding.search.setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
            if (isInMessageSearchMode && actionId == EditorInfo.IME_ACTION_SEARCH) {
                doFancySearch(v.text.toString(), false)
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }
        binding.recycler.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                binding.recycler.postDelayed({
                    if (style.scrollChatToEndIfUserTyping) {
                        chatAdapter?.itemCount?.let { scrollToPosition(it - 1, false) }
                    } else {
                        binding.recycler.smoothScrollBy(0, oldBottom - bottom)
                    }
                }, 100)
            }
        }
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.recycler.layoutManager as LinearLayoutManager?
                if (layoutManager != null) {
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val itemCount = chatAdapter?.itemCount
                    if (itemCount != null && itemCount - 1 - lastVisibleItemPosition > INVISIBLE_MESSAGES_COUNT &&
                        binding.scrollDownButtonContainer.isNotVisible()
                    ) {
                        binding.scrollDownButtonContainer.visible()
                        showUnreadMessagesCount(chatAdapter?.unreadCount ?: 0)
                    } else {
                        binding.scrollDownButtonContainer.visibility = View.GONE
                        recyclerView.post { setMessagesAsRead() }
                    }
                    if (firstVisibleItemPosition == 0) {
                        chatController.loadHistory(false)
                    }
                }
            }
        })
        binding.scrollDownButtonContainer.setOnClickListener {
            showUnreadMessagesCount(0)
            val unreadCount = chatAdapter?.unreadCount ?: 0
            if (unreadCount > 0) {
                scrollToNewMessages()
            } else {
                scrollToPosition(chatAdapter!!.itemCount - 1, false)
            }
            setMessagesAsRead()
            binding.scrollDownButtonContainer.visibility = View.GONE
            if (isInMessageSearchMode) {
                hideSearchMode()
            }
        }
    }

    private fun setMessagesAsRead() {
        chatAdapter?.setAllMessagesRead()
        setMessagesAsReadForStorages()
    }

    private fun setMessagesAsReadForStorages() {
        if (previousChatItemsCount == 0 || chatAdapter?.itemCount != null && chatAdapter?.itemCount != previousChatItemsCount) {
            chatController.setMessagesInCurrentThreadAsReadInDB()
            chatController.clearUnreadPushCount()
            previousChatItemsCount = chatAdapter?.itemCount ?: 0
        }
    }

    private fun configureUserTypingSubscription() {
        val userTypingDisposable = RxUtils.toObservable(inputTextObservable)
            .throttleLatest(INPUT_DELAY, TimeUnit.MILLISECONDS)
            .filter { charSequence: String -> charSequence.isNotEmpty() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { input: String -> onInputChanged(input) }
            ) { error: Throwable? -> error("configureInputChangesSubscription ", error) }
        subscribe(userTypingDisposable)
    }

    private fun onInputChanged(input: String) {
        chatController.onUserTyping(input)
        updateLastUserActivityTime()
    }

    private fun updateLastUserActivityTime() {
        val timeCounter = getLastUserActivityTimeCounter()
        timeCounter.updateLastUserActivityTime()
    }

    private fun configureRecordButtonVisibility() {
        val recordButtonVisibilityDisposable = Observable.combineLatest(
            RxUtils.toObservableImmediately(inputTextObservable),
            RxUtils.toObservableImmediately(fileDescription)
        ) { s: String, fileDescriptionOptional: Optional<FileDescription?>? ->
            (
                (TextUtils.isEmpty(s) || s.trim { it <= ' ' }.isEmpty()) &&
                    (fileDescriptionOptional == null || fileDescriptionOptional.isEmpty)
                )
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { isInputEmpty: Boolean -> setRecordButtonVisibility(isInputEmpty) }
            ) { error: Throwable? -> error("configureInputChangesSubscription ", error) }
        subscribe(recordButtonVisibilityDisposable)
    }

    private fun setRecordButtonVisibility(isInputEmpty: Boolean) {
        val isButtonVisible = (isInputEmpty && style.voiceMessageEnabled)
        binding.recordButton.visibility = if (isButtonVisible) View.VISIBLE else View.GONE
    }

    private fun showUnreadMessagesCount(unreadCount: Int) {
        if (binding.scrollDownButtonContainer.visibility == View.VISIBLE) {
            val hasUnreadCount = unreadCount > 0
            binding.unreadMsgCount.text = if (hasUnreadCount) unreadCount.toString() else ""
            binding.unreadMsgCount.visibility = if (hasUnreadCount) View.VISIBLE else View.GONE
            binding.unreadMsgSticker.visibility = if (hasUnreadCount) View.VISIBLE else View.GONE
        }
    }

    private fun onSendButtonClick() {
        val inputText = inputTextObservable.get()
        if (inputText == null || inputText.trim { it <= ' ' }.isEmpty() && getFileDescription() == null) {
            return
        }
        welcomeScreenVisibility(false)
        val input: MutableList<UpcomingUserMessage> = ArrayList()
        val message = UpcomingUserMessage(
            getFileDescription(),
            campaignMessage,
            mQuote,
            inputText.trim { it <= ' ' },
            inputText.isLastCopyText()
        )
        input.add(message)
        sendMessage(input)
    }

    fun getFileDescription(): FileDescription? {
        val fileDescriptionOptional = fileDescription.get()
        return if (fileDescriptionOptional != null && fileDescriptionOptional.isPresent) {
            fileDescriptionOptional.get()
        } else {
            null
        }
    }

    private fun setFileDescription(fileDescription: FileDescription?) {
        this.fileDescription.set(Optional.ofNullable(fileDescription))
    }

    private fun subscribeToFileDescription() {
        subscribe(
            RxUtils.toObservable(fileDescription)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { files: Optional<FileDescription?>? ->
                    val isFilesAvailable = files != null && !files.isEmpty
                    val isInputAvailable = !TextUtils.isEmpty(binding.inputEditView.text)
                    val isEnable = isFilesAvailable || isInputAvailable
                    binding.sendMessage.isEnabled = isEnable
                }
        )
    }

    private fun onRefresh() {
        val disposable = chatController.requestItems(BaseConfig.instance.historyLoadingCount, true)
            ?.delay(500, TimeUnit.MILLISECONDS)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ result: List<ChatItem> -> afterRefresh(result) }) { onError: Throwable? -> error("onRefresh ", onError) }

        if (disposable != null) {
            subscribe(disposable)
        }
    }

    private fun afterRefresh(result: List<ChatItem>) {
        chatAdapter?.let {
            val itemsBefore = it.itemCount
            chatAdapter?.addItems(result)
            scrollToPosition(it.itemCount - itemsBefore, true)
        }

        Runnable {
            binding.swipeRefresh.isRefreshing = false
            binding.swipeRefresh.clearAnimation()
            binding.swipeRefresh.destroyDrawingCache()
            binding.swipeRefresh.invalidate()
        }.runOnUiThread()
    }

    private fun setFragmentStyle() {
        val activity = activity ?: return
        ColorsHelper.setBackgroundColor(activity, binding.chatRoot, style.chatBackgroundColor)
        ColorsHelper.setBackgroundColor(activity, binding.inputLayout, style.chatMessageInputColor)
        ColorsHelper.setBackgroundColor(activity, binding.bottomLayout, style.chatMessageInputColor)
        ColorsHelper.setBackgroundColor(activity, binding.recordView, style.chatMessageInputColor)
        ColorsHelper.setDrawableColor(activity, binding.searchUpIb.drawable, style.chatToolbarTextColorResId)
        ColorsHelper.setDrawableColor(activity, binding.searchDownIb.drawable, style.chatToolbarTextColorResId)
        binding.searchMore.setBackgroundColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor))
        binding.searchMore.setTextColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor))
        binding.swipeRefresh.setColorSchemeColors(*resources.getIntArray(style.threadsSwipeRefreshColors))
        binding.scrollDownButton.setBackgroundResource(style.scrollDownBackgroundResId)
        binding.scrollDownButton.setImageResource(style.scrollDownIconResId)
        val lp = binding.scrollDownButton.layoutParams as MarginLayoutParams
        lp.height = resources.getDimensionPixelSize(style.scrollDownButtonHeight)
        lp.width = resources.getDimensionPixelSize(style.scrollDownButtonWidth)
        ViewCompat.setElevation(binding.scrollDownButton, resources.getDimension(style.scrollDownButtonElevation))
        val lpButtonContainer = binding.scrollDownButtonContainer.layoutParams as MarginLayoutParams
        val margin = resources.getDimensionPixelSize(style.scrollDownButtonMargin)
        lpButtonContainer.setMargins(margin, margin, margin, margin)
        binding.unreadMsgSticker.background.setColorFilter(
            ContextCompat.getColor(activity, style.unreadMsgStickerColorResId),
            PorterDuff.Mode.SRC_ATOP
        )
        ViewCompat.setElevation(binding.unreadMsgSticker, resources.getDimension(style.scrollDownButtonElevation))
        binding.unreadMsgCount.setTextColor(ContextCompat.getColor(activity, style.unreadMsgCountTextColorResId))
        ViewCompat.setElevation(binding.unreadMsgCount, resources.getDimension(style.scrollDownButtonElevation))
        binding.inputEditView.minHeight = activity.resources.getDimension(style.inputHeight).toInt()
        binding.inputEditView.background = AppCompatResources.getDrawable(activity, style.inputBackground)
        binding.inputEditView.setHint(style.inputHint)
        binding.inputEditView.maxLines = INPUT_EDIT_VIEW_MIN_LINES_COUNT
        binding.inputEditView.setPadding(
            resources.getDimensionPixelSize(style.inputFieldPaddingLeft),
            resources.getDimensionPixelSize(style.inputFieldPaddingTop),
            resources.getDimensionPixelSize(style.inputFieldPaddingRight),
            resources.getDimensionPixelSize(style.inputFieldPaddingBottom)
        )
        val params = binding.inputEditView.layoutParams as LinearLayout.LayoutParams
        params.setMargins(
            resources.getDimensionPixelSize(style.inputFieldMarginLeft),
            resources.getDimensionPixelSize(style.inputFieldMarginTop),
            resources.getDimensionPixelSize(style.inputFieldMarginRight),
            resources.getDimensionPixelSize(style.inputFieldMarginBottom)
        )
        binding.inputEditView.layoutParams = params
        binding.inputEditView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(binding.inputEditView.text)) {
                    binding.inputEditView.maxLines = INPUT_EDIT_VIEW_MIN_LINES_COUNT
                } else {
                    binding.inputEditView.maxLines = INPUT_EDIT_VIEW_MAX_LINES_COUNT
                }
                binding.sendMessage.isEnabled = !TextUtils.isEmpty(s) || hasAttachments()
            }
        })
        ColorsHelper.setTextColor(activity, binding.search, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, binding.subtitle, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, binding.consultName, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, binding.subtitle, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, binding.consultName, style.chatToolbarTextColorResId)
        ColorsHelper.setHintTextColor(activity, binding.inputEditView, style.chatMessageInputHintTextColor)
        ColorsHelper.setHintTextColor(activity, binding.search, style.chatToolbarHintTextColor)
        ColorsHelper.setTextColor(activity, binding.inputEditView, style.inputTextColor)
        if (!TextUtils.isEmpty(style.inputTextFont)) {
            try {
                val customFont = Typeface.createFromAsset(activity.assets, style.inputTextFont)
                binding.inputEditView.typeface = customFont
            } catch (e: Exception) {
                error("setFragmentStyle", e)
            }
        }
        binding.flEmpty.setBackgroundColor(ContextCompat.getColor(activity, style.emptyStateBackgroundColorResId))
        val progressDrawable = binding.progressBar.indeterminateDrawable.mutate()
        ColorsHelper.setDrawableColor(activity, progressDrawable, style.emptyStateProgressBarColorResId)
        binding.progressBar.indeterminateDrawable = progressDrawable
        ColorsHelper.setTextColor(activity, binding.tvEmptyStateHint, style.emptyStateHintColorResId)
    }

    override fun onAllowClick(type: PermissionDescriptionType, requestCode: Int) {
        when (type) {
            PermissionDescriptionType.STORAGE -> startStoragePermissionActivity(requestCode)
            PermissionDescriptionType.RECORD_AUDIO -> startRecordAudioPermissionActivity(requestCode)
            PermissionDescriptionType.CAMERA -> startCameraPermissionActivity(requestCode)
        }
    }

    private fun hasAttachments(): Boolean {
        val hasVoice = recorder != null
        val hasFile = getFileDescription() != null
        val hasImages = !(mAttachedImages == null || mAttachedImages.isNullOrEmpty())
        return hasVoice || hasFile || hasImages
    }

    private fun startStoragePermissionActivity(requestCode: Int) {
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL) {
            PermissionsActivity.startActivityForResult(
                this,
                REQUEST_PERMISSION_READ_EXTERNAL,
                R.string.ecc_permissions_read_external_storage_help_text,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else if (requestCode == REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY) {
            PermissionsActivity.startActivityForResult(
                this,
                REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY,
                R.string.ecc_permissions_read_external_storage_help_text,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun startRecordAudioPermissionActivity(requestCode: Int) {
        if (requestCode == REQUEST_PERMISSION_RECORD_AUDIO) {
            PermissionsActivity.startActivityForResult(
                this,
                REQUEST_PERMISSION_RECORD_AUDIO,
                R.string.ecc_permissions_record_audio_help_text,
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startCameraPermissionActivity(requestCode: Int) {
        val permissions = ArrayList(cameraPermissions ?: listOf()).toTypedArray()
        if (requestCode == REQUEST_PERMISSION_CAMERA && permissions.isNotEmpty()) {
            PermissionsActivity.startActivityForResult(
                this,
                REQUEST_PERMISSION_CAMERA,
                R.string.ecc_permissions_camera_and_write_external_storage_help_text,
                *permissions
            )
        }
    }

    override fun onDialogDetached() {
        cameraPermissions = null
        permissionDescriptionAlertDialogFragment = null
    }

    override fun onCameraClick() {
        val activity = activity ?: return
        val isCameraGranted = ThreadsPermissionChecker.isCameraPermissionGranted(activity)
        val isWriteGranted =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ThreadsPermissionChecker.isWriteExternalPermissionGranted(activity)
        info("isCameraGranted = $isCameraGranted isWriteGranted $isWriteGranted")
        if (isCameraGranted && isWriteGranted) {
            if (config.getChatStyle().useExternalCameraApp) {
                try {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    externalCameraPhotoFile = createImageFile(activity)
                    val photoUri = FileProviderHelper.getUriForFile(activity, externalCameraPhotoFile!!)
                    debug("Image File uri resolved: $photoUri")
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    grantPermissionsForImageUri(activity, intent, photoUri)
                    startActivityForResult(intent, REQUEST_EXTERNAL_CAMERA_PHOTO)
                } catch (e: IllegalArgumentException) {
                    error("Could not start external camera", e)
                    show(requireContext(), requireContext().getString(R.string.ecc_camera_could_not_start_error))
                }
            } else {
                setBottomStateDefault()
                startActivityForResult(CameraActivity.getStartIntent(activity), REQUEST_CODE_PHOTO)
            }
        } else {
            val permissions = ArrayList<String>()
            if (!isCameraGranted) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (!isWriteGranted) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (style.arePermissionDescriptionDialogsEnabled) {
                showSafelyCameraPermissionDescriptionDialog(permissions)
            } else {
                cameraPermissions = permissions
                startCameraPermissionActivity(REQUEST_PERMISSION_CAMERA)
            }
        }
    }

    override fun onGalleryClick() {
        startActivityForResult(getStartIntent(activity, REQUEST_CODE_PHOTOS), REQUEST_CODE_PHOTOS)
    }

    override fun onImageSelectionChanged(imageList: List<Uri>) {
        mAttachedImages = ArrayList(imageList)
    }

    override fun onBottomSheetDetached() {
        bottomSheetDialogFragment = null
    }

    private fun showPopup() {
        val activity = activity ?: return
        val popup = PopupMenu(activity, binding.popupMenuButton)
        popup.setOnMenuItemClickListener(this)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.ecc_menu_main, popup.menu)
        val menu = popup.menu
        val searchMenuItem = menu.findItem(R.id.ecc_search)
        if (searchMenuItem != null) {
            val s = SpannableString(searchMenuItem.title)
            s.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity, style.menuItemTextColorResId)), 0, s.length, 0)
            searchMenuItem.title = s
            val searchEnabled = resources.getBoolean(config.getChatStyle().searchEnabled)
            searchMenuItem.isVisible = searchEnabled
        }
        val filesAndMedia = menu.findItem(R.id.ecc_files_and_media)
        if (filesAndMedia != null) {
            val s2 = SpannableString(filesAndMedia.title)
            s2.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity, style.menuItemTextColorResId)), 0, s2.length, 0)
            filesAndMedia.title = s2
        }
        filesAndMedia?.isVisible = config.filesAndMediaMenuItemEnabled
        if (isPopupMenuEnabled) {
            popup.show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val activity = activity ?: return false
        if (item.itemId == R.id.ecc_files_and_media) {
            if (isInMessageSearchMode) {
                onActivityBackPressed()
            }
            startActivity(activity)
            return true
        }
        if (item.itemId == R.id.ecc_search) {
            if (!isInMessageSearchMode) {
                search(false)
                binding.chatBackButton.visibility = View.VISIBLE
            } else {
                return true
            }
        }
        return false
    }

    private fun onReplyClick(cp: ChatPhrase, position: Int) {
        hideCopyControls()
        scrollToPosition(position, true)
        val userPhrase = if (cp is UserPhrase) cp else null
        val consultPhrase = if (cp is ConsultPhrase) cp else null
        val text = cp.phraseText
        if (userPhrase != null) {
            mQuote = Quote(
                userPhrase.id,
                requireContext().getString(R.string.ecc_I),
                userPhrase.phraseText,
                userPhrase.fileDescription,
                userPhrase.timeStamp
            )
            mQuote?.isFromConsult = false
        } else if (consultPhrase != null) {
            mQuote = Quote(
                consultPhrase.id,
                consultPhrase.consultName ?: requireContext().getString(R.string.ecc_consult),
                consultPhrase.phraseText,
                consultPhrase.fileDescription,
                consultPhrase.timeStamp
            )
            mQuote?.isFromConsult = true
            mQuote?.quotedPhraseConsultId = consultPhrase.consultId
        }
        setFileDescription(null)
        if (isImage(cp.fileDescription)) {
            mQuoteLayoutHolder?.setContent(
                if (mQuote?.phraseOwnerTitle.isNullOrEmpty()) "" else mQuote?.phraseOwnerTitle,
                (if (text.isNullOrEmpty()) requireContext().getString(R.string.ecc_image) else text),
                cp.fileDescription?.fileUri,
                false
            )
        } else if (cp.fileDescription != null) {
            var fileName = ""
            try {
                val fileUri = cp.fileDescription?.fileUri
                val incomingNameIsNotEmpty = cp.fileDescription != null && !cp.fileDescription?.incomingName.isNullOrEmpty()
                fileName =
                    if (incomingNameIsNotEmpty) {
                        cp.fileDescription?.incomingName ?: ""
                    } else if (fileUri != null) {
                        getFileName(
                            fileUri
                        )
                    } else {
                        ""
                    }
            } catch (e: Exception) {
                error("onReplyClick", e)
            }
            val phraseOwnerTitleIsNotEmpty = mQuote != null && !mQuote?.phraseOwnerTitle.isNullOrEmpty()
            mQuoteLayoutHolder?.setContent(
                if (phraseOwnerTitleIsNotEmpty) mQuote?.phraseOwnerTitle else "",
                fileName,
                null,
                false
            )
        } else {
            val phraseOwnerTitleIsNotEmpty = mQuote != null && !mQuote?.phraseOwnerTitle.isNullOrEmpty()
            mQuoteLayoutHolder?.setContent(
                if (phraseOwnerTitleIsNotEmpty) mQuote?.phraseOwnerTitle else "",
                (if (text.isNullOrEmpty()) "" else text),
                null,
                false
            )
        }
    }

    private fun onCopyClick(activity: Activity, cp: ChatPhrase) {
        val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val whatToCopy = cp.phraseText ?: return
        cm.copyToBuffer(whatToCopy)
        show(requireContext(), getString(R.string.ecc_message_text_copied))
        unChooseItem()
    }

    override fun onFilePickerClick() {
        val activity = activity ?: return
        setBottomStateDefault()
        if (ThreadsPermissionChecker.isReadExternalPermissionGranted(activity)) {
            openFile()
        } else if (style.arePermissionDescriptionDialogsEnabled) {
            showSafelyPermissionDescriptionDialog(
                PermissionDescriptionType.STORAGE,
                REQUEST_PERMISSION_READ_EXTERNAL
            )
        } else {
            startStoragePermissionActivity(REQUEST_PERMISSION_READ_EXTERNAL)
        }
    }

    override fun onSendClick() {
        if (mAttachedImages == null || mAttachedImages!!.isEmpty()) {
            show(requireContext(), getString(R.string.ecc_failed_to_open_file))
            return
        }
        subscribe(
            Single.fromCallable {
                Stream.of(mAttachedImages)
                    .filter { value: Uri? -> canBeSent(requireContext(), value!!) }
                    .toList()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { filteredPhotos: List<Uri> ->
                        if (filteredPhotos.isEmpty()) {
                            show(requireContext(), getString(R.string.ecc_failed_to_open_file))
                            return@subscribe
                        }
                        val inputText = inputTextObservable.get() ?: return@subscribe
                        val messages = getUpcomingUserMessagesFromSelection(
                            filteredPhotos,
                            inputText,
                            requireContext().getString(R.string.ecc_I),
                            campaignMessage,
                            mQuote
                        )
                        if (isSendBlocked) {
                            clearInput()
                            show(requireContext(), requireContext().getString(R.string.ecc_message_were_unsent))
                        } else {
                            sendMessage(messages)
                        }
                    }
                ) { onError: Throwable? -> error("onSendClick ", onError) }
        )
    }

    fun hideBottomSheet() {
        if (bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment!!.dismiss()
            bottomSheetDialogFragment = null
        }
    }

    fun showBottomSheet() {
        if (bottomSheetDialogFragment == null && isAdded) {
            bottomSheetDialogFragment = AttachmentBottomSheetDialogFragment()
            bottomSheetDialogFragment!!.show(childFragmentManager, AttachmentBottomSheetDialogFragment.TAG)
        }
    }

    private fun doFancySearch(request: String, forward: Boolean) {
        updateLastUserActivityTime()
        if (TextUtils.isEmpty(request)) {
            chatAdapter?.removeHighlight()
            binding.searchUpIb.alpha = DISABLED_ALPHA
            binding.searchDownIb.alpha = DISABLED_ALPHA
            return
        }
        onSearch(request, forward)
    }

    private fun onSearch(request: String, forward: Boolean) {
        chatController.fancySearch(request, forward) { dataPair: Pair<List<ChatItem?>?, ChatItem?>? -> onSearchEnd(dataPair) }
    }

    private fun onSearchEnd(dataPair: Pair<List<ChatItem?>?, ChatItem?>?) {
        var first = -1
        var last = -1
        if (dataPair?.first != null) {
            val data = dataPair.first ?: listOf()
            val highlightedItem = dataPair.second
            // для поиска - ищем индекс первого совпадения
            for (i in data.indices) {
                if (data[i] is ChatPhrase) {
                    if ((data[i] as ChatPhrase?)!!.found) {
                        first = i
                        break
                    }
                }
            }
            // для поиска - ищем индекс последнего совпадения
            for (i in data.indices.reversed()) {
                if (data[i] is ChatPhrase) {
                    if ((data[i] as ChatPhrase?)!!.found) {
                        last = i
                        break
                    }
                }
            }
            for (i in data.indices) {
                if (data[i] is ChatPhrase) {
                    if (data[i] == highlightedItem) {
                        // для поиска - если можно перемещаться, подсвечиваем
                        if (first != -1 && i > first) {
                            binding.searchUpIb.alpha = ENABLED_ALPHA
                        } else {
                            binding.searchUpIb.alpha = DISABLED_ALPHA
                        }
                        // для поиска - если можно перемещаться, подсвечиваем
                        if (last != -1 && i < last) {
                            binding.searchDownIb.alpha = ENABLED_ALPHA
                        } else {
                            binding.searchDownIb.alpha = DISABLED_ALPHA
                        }
                        break
                    }
                }
            }
            chatAdapter?.let {
                it.addItems(data)
                if (highlightedItem != null) {
                    chatAdapter?.removeHighlight()
                    scrollToPosition(it.setItemHighlighted(highlightedItem), true)
                }
            }
        }
    }

    private fun onPhotosResult(data: Intent) {
        val photos = data.getParcelableArrayListExtra<Uri>(GalleryActivity.PHOTOS_TAG)
        hideBottomSheet()
        welcomeScreenVisibility(false)
        val inputText = inputTextObservable.get()
        if (photos == null || photos.size == 0 || inputText == null) {
            return
        }
        subscribe(
            Single.fromCallable {
                Stream.of(photos)
                    .filter { value: Uri? -> canBeSent(requireContext(), value!!) }
                    .toList()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ filteredPhotos: List<Uri> ->
                    if (filteredPhotos.isEmpty()) {
                        show(requireContext(), getString(R.string.ecc_failed_to_open_file))
                        return@subscribe
                    }
                    unChooseItem()
                    var fileUri = filteredPhotos[0]
                    var uum = UpcomingUserMessage(
                        FileDescription(
                            requireContext().getString(R.string.ecc_I),
                            fileUri,
                            getFileSize(fileUri),
                            System.currentTimeMillis()
                        ),
                        campaignMessage,
                        mQuote,
                        inputText.trim { it <= ' ' },
                        inputText.isLastCopyText()
                    )
                    if (isSendBlocked) {
                        show(requireContext(), getString(R.string.ecc_message_were_unsent))
                    } else {
                        chatController.onUserInput(uum)
                    }
                    inputTextObservable.set("")
                    mQuoteLayoutHolder?.clear()
                    for (i in 1 until filteredPhotos.size) {
                        fileUri = filteredPhotos[i]
                        uum = UpcomingUserMessage(
                            FileDescription(
                                requireContext().getString(R.string.ecc_I),
                                fileUri,
                                getFileSize(fileUri),
                                System.currentTimeMillis()
                            ),
                            null,
                            null,
                            null,
                            false
                        )
                        chatController.onUserInput(uum)
                    }
                }) { onError: Throwable? -> error("onPhotosResult ", onError) }
        )
    }

    private fun onExternalCameraPhotoResult() {
        externalCameraPhotoFile?.let { file ->
            setFileDescription(
                FileDescription(
                    requireContext().getString(R.string.ecc_image),
                    FileProviderHelper.getUriForFile(BaseConfig.instance.context, file),
                    file.length(),
                    System.currentTimeMillis()
                )
            )
            val inputText = inputTextObservable.get()
            sendMessage(
                listOf(
                    UpcomingUserMessage(
                        getFileDescription(),
                        campaignMessage,
                        mQuote,
                        inputText?.trim { it <= ' ' },
                        false
                    )
                )
            )
        }
    }

    @SuppressLint("WrongConstant")
    private fun onFileResult(data: Intent) {
        val uri = data.data
        if (uri != null) {
            if (isAllowedFileExtension(getExtensionFromMediaStore(BaseConfig.instance.context, uri))) {
                if (isAllowedFileSize(getFileSizeFromMediaStore(BaseConfig.instance.context, uri))) {
                    try {
                        if (canBeSent(requireContext(), uri)) {
                            onFileResult(uri)
                            val takeFlags = (
                                data.flags
                                    and (
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                        )
                                )
                            requireActivity().contentResolver.takePersistableUriPermission(uri, takeFlags)
                        } else {
                            show(requireContext(), getString(R.string.ecc_failed_to_open_file))
                        }
                    } catch (e: SecurityException) {
                        error("file can't be sent", e)
                        show(requireContext(), getString(R.string.ecc_failed_to_open_file))
                    }
                } else {
                    // Недопустимый размер файла
                    show(
                        requireContext(),
                        getString(
                            R.string.ecc_not_allowed_file_size,
                            maxAllowedFileSize
                        )
                    )
                }
            } else {
                // Недопустимое расширение файла
                show(requireContext(), getString(R.string.ecc_not_allowed_file_extension))
            }
        }
    }

    private fun onFileResult(uri: Uri) {
        info("onFileSelected: $uri")
        setFileDescription(FileDescription(requireContext().getString(R.string.ecc_I), uri, getFileSize(uri), System.currentTimeMillis()))
        mQuoteLayoutHolder!!.setContent(
            requireContext().getString(R.string.ecc_file),
            getFileName(uri),
            null,
            true
        )
    }

    private fun onPhotoResult(data: Intent) {
        val imageExtra = data.getStringExtra(CameraActivity.IMAGE_EXTRA)
        if (imageExtra != null) {
            val file = File(imageExtra)
            val fileDescription = FileDescription(
                requireContext().getString(R.string.ecc_image),
                FileProviderHelper.getUriForFile(requireContext(), file),
                file.length(),
                System.currentTimeMillis()
            )
            setFileDescription(
                fileDescription
            )
            val inputText = inputTextObservable.get()
            val uum = UpcomingUserMessage(
                fileDescription,
                campaignMessage,
                mQuote,
                inputText?.trim { it <= ' ' },
                false
            )
            sendMessage(listOf(uum))
        }
    }

    private fun openBottomSheetAndGallery() {
        val activity = activity ?: return
        if (ThreadsPermissionChecker.isReadExternalPermissionGranted(activity)) {
            setTitleStateCurrentOperatorConnected()
            if (bottomSheetDialogFragment == null) {
                showBottomSheet()
                chatAdapter?.itemCount?.let { scrollToPosition(it - 1, false) }
            } else {
                hideBottomSheet()
            }
        } else if (style.arePermissionDescriptionDialogsEnabled) {
            showSafelyPermissionDescriptionDialog(
                PermissionDescriptionType.STORAGE,
                REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY
            )
        } else {
            startStoragePermissionActivity(REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY)
        }
    }

    private fun sendMessage(messages: List<UpcomingUserMessage>, clearInput: Boolean = true) {
        info("isInMessageSearchMode =$isInMessageSearchMode")
        for (message in messages) {
            chatController.onUserInput(message)
        }
        if (null != chatAdapter) {
            setMessagesAsRead()
        }
        if (clearInput) {
            clearInput()
        }
        chatController.hideQuickReplies()
    }

    private fun clearInput() {
        inputTextObservable.set("")
        mQuoteLayoutHolder?.clear()
        setBottomStateDefault()
        hideCopyControls()
        mAttachedImages?.clear()
        if (isInMessageSearchMode) {
            onActivityBackPressed()
        }
    }

    fun addChatItem(item: ChatItem?) {
        info("addChatItem: $item")
        val layoutManager = binding.recycler.layoutManager as LinearLayoutManager? ?: return
        if (item == null) {
            return
        }
        val isLastMessageVisible = (
            chatAdapter!!.itemCount - 1 - layoutManager.findLastVisibleItemPosition()
                < INVISIBLE_MESSAGES_COUNT
            )
        if (item is ConsultPhrase) {
            item.isRead = isLastMessageVisible && isResumed && !isInMessageSearchMode
            chatAdapter?.setAvatar(item.consultId, item.avatarPath)
        }
        if (needsAddMessage(item)) {
            welcomeScreenVisibility(false)
            chatAdapter?.addItems(listOf(item))
            if (!isLastMessageVisible) {
                binding.scrollDownButtonContainer.visibility = View.VISIBLE
                showUnreadMessagesCount(chatAdapter?.unreadCount ?: 0)
            }
            scrollDelayedOnNewMessageReceived(item is UserPhrase, isLastMessageVisible)
        } else if (needsModifyImage(item)) {
            chatAdapter!!.modifyImageInItem((item as ChatPhrase).fileDescription)
        }
    }

    /**
     * Отлистывает сообщения до последнего в случаях, когда сообщение отправлено пользователем или
     * когда отображается последнее сообщение.
     *
     *
     * Подробнее о логике подкрутки сообщений: https://jira.edna.ru/browse/EC-12190.
     *
     *
     * После отправки сообщения пользователем чат-бот может ответить сразу несколькими
     * сообщениями. Установка флага isNewMessageUpdateTimeoutOn нужна, чтобы handler
     * запустился только 1 раз в течение указанной задержки delayMillis.
     *
     *
     * Значение isLastMessageVisible берётся перед добавлением нового сообщения и используется
     * при срабатывании Runnable хендлера, itemCount используется актуальный на момент
     * срабатывания Runnable.
     *
     * @param isUserPhrase         true, если добавляется сообщение от пользователя
     * @param isLastMessageVisible отображается ли на экране последнее сообщение на момент получения
     * нового сообщения
     */
    private fun scrollDelayedOnNewMessageReceived(
        isUserPhrase: Boolean,
        isLastMessageVisible: Boolean,
    ) {
        if (!isNewMessageUpdateTimeoutOn) {
            isNewMessageUpdateTimeoutOn = true
            handler.postDelayed({
                if (!isInMessageSearchMode) {
                    val itemCount = chatAdapter!!.itemCount
                    if (isLastMessageVisible || isUserPhrase) {
                        scrollToPosition(itemCount - 1, false)
                    }
                }
                isNewMessageUpdateTimeoutOn = false
            }, 100)
        }
    }

    private fun scrollToPosition(itemCount: Int, smooth: Boolean) {
        info("scrollToPosition: $itemCount")
        if (itemCount >= 0) {
            if (smooth) {
                binding.recycler.smoothScrollToPosition(itemCount)
            } else {
                binding.recycler.scrollToPosition(itemCount)
            }
        }
    }

    private fun needsAddMessage(item: ChatItem): Boolean {
        return if (item is ScheduleInfo) {
            // Если сообщение о расписании уже показано, то снова отображать не нужно.
            // Если в сообщении о расписании указано, что сейчас чат работет,
            // то расписание отображать не нужно.
            !item.isChatWorking && chatAdapter?.hasSchedule() != true
        } else {
            val chatPhrase: ChatPhrase
            try {
                chatPhrase = item as ChatPhrase
                chatPhrase.fileDescription == null || TextUtils.isEmpty(chatPhrase.fileDescription?.originalPath)
            } catch (exception: Exception) {
                true
            }
        }
    }

    private fun needsModifyImage(item: ChatItem): Boolean {
        val chatPhrase: ChatPhrase
        return try {
            chatPhrase = item as ChatPhrase
            chatPhrase.fileDescription != null && !TextUtils.isEmpty(chatPhrase.fileDescription?.originalPath)
        } catch (exception: Exception) {
            false
        }
    }

    fun addHistoryChatItems(list: List<ChatItem?>) {
        if (list.filterNotNull().isEmpty()) {
            return
        }
        val layoutManager = binding.recycler.layoutManager as LinearLayoutManager?
        var firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()
        chatAdapter?.let {
            if (it.list[firstVisibleItemPosition] is DateRow) {
                firstVisibleItemPosition += 1
            }
            val timeStamp = it.list[firstVisibleItemPosition].timeStamp
            chatAdapter!!.addItems(list)
            val newPosition = it.getPositionByTimeStamp(timeStamp)
            scrollToPosition(newPosition, false)
        }
    }

    fun addChatItems(list: List<ChatItem?>) {
        if (list.isEmpty()) {
            return
        }
        chatAdapter?.let { chatAdapter ->
            val oldAdapterSize = chatAdapter.list.size
            welcomeScreenVisibility(false)
            chatAdapter.addItems(list)
            val layoutManager = binding.recycler.layoutManager as LinearLayoutManager?
            if (layoutManager == null || list.size == 1 && list[0] is ConsultTyping || isInMessageSearchMode) {
                return
            }
            val newAdapterSize = chatAdapter.list.size
            if (oldAdapterSize == 0) {
                scrollToPosition(chatAdapter.itemCount - 1, false)
            } else if (afterResume) {
                if (!isStartSecondLevelScreen() && newAdapterSize != oldAdapterSize) {
                    scrollToPosition(chatAdapter.itemCount - 1, false)
                }
                afterResume = false
            } else if (newAdapterSize > oldAdapterSize) {
                handler.postDelayed({ scrollToPosition(chatAdapter.itemCount - 1, false) }, 100)
                afterResume = false
            }
            resumeAfterSecondLevelScreen = false
            checkSearch()
        }
    }

    fun setStateConsultConnected(info: ConsultInfo?) {
        if (!isAdded) {
            return
        }
        handler.post {
            val context = context
            if (context != null && isAdded) {
                if (!isInMessageSearchMode) {
                    binding.consultName.visibility = View.VISIBLE
                }
                if (!resources.getBoolean(style.fixedChatTitle)) {
                    if (!isInMessageSearchMode) {
                        binding.subtitle.visibility = View.VISIBLE
                    }
                    if (!TextUtils.isEmpty(info!!.name) && info.name != "null") {
                        binding.consultName.text = info.name
                    } else {
                        binding.consultName.text = context.getString(R.string.ecc_unknown_operator)
                    }
                    setSubtitle(info, context)
                }
                if (!resources.getBoolean(style.isChatSubtitleVisible)) {
                    binding.subtitle.visibility = View.GONE
                }
                chatAdapter?.removeConsultSearching()
                showOverflowMenu()
            }
        }
    }

    private fun setSubtitle(info: ConsultInfo, context: Context) {
        val subtitle: String = if (style.chatSubtitleShowOrgUnit && !info.organizationUnit.isNullOrEmpty()) {
            info.organizationUnit
        } else if (resources.getBoolean(style.fixedChatSubtitle) || info.role.isNullOrEmpty()) {
            context.getString(style.chatSubtitleTextResId)
        } else {
            val role = consultRoleFromString(info.role)
            if (ConsultRole.BOT === role ||
                ConsultRole.EXTERNAL_BOT === role
            ) {
                context.getString(R.string.ecc_bot)
            } else {
                context.getString(R.string.ecc_operator)
            }
        }
        binding.subtitle.text = subtitle
    }

    fun setTitleStateDefault() {
        handler.post {
            if (!isInMessageSearchMode && isAdded) {
                binding.subtitle.visibility = View.GONE
                binding.consultName.visibility = View.VISIBLE
                binding.searchLo.visibility = View.GONE
                binding.search.setText("")
                binding.consultName.setText(style.chatTitleTextResId)
            }
        }
    }

    fun setMessageState(correlationId: String?, backendMessageId: String?, state: MessageStatus?) {
        chatAdapter?.changeStateOfMessageByMessageId(correlationId, backendMessageId, state)
    }

    fun setSurveySentStatus(uuid: Long, sentState: MessageStatus?) {
        chatAdapter?.changeStateOfSurvey(uuid, sentState)
    }

    private fun hideCopyControls() {
        val activity = activity ?: return
        setTitleStateCurrentOperatorConnected()
        ColorsHelper.setTint(activity, binding.chatBackButton, style.chatToolbarTextColorResId)
        ColorsHelper.setTint(activity, binding.popupMenuButton, style.chatToolbarTextColorResId)
        ColorsHelper.setBackgroundColor(activity, binding.toolbar, style.chatToolbarColorResId)
        binding.copyControls.visibility = View.GONE
        if (!isInMessageSearchMode) {
            binding.consultName.visibility = View.VISIBLE
        }
        val isFixedChatTitle = resources.getBoolean(style.fixedChatTitle)
        val isVisibleSubtitle = resources.getBoolean(style.isChatSubtitleVisible)
        if (chatController.isConsultFound && !isInMessageSearchMode && !isFixedChatTitle && isVisibleSubtitle) {
            binding.subtitle.visibility = View.VISIBLE
        }
    }

    private fun checkSearch() {
        if (!TextUtils.isEmpty(binding.search.text)) {
            doFancySearch(binding.search.text.toString(), false)
        }
    }

    private fun setBottomStateDefault() {
        hideBottomSheet()
        if (!isInMessageSearchMode) {
            binding.searchLo.visibility = View.GONE
            binding.search.setText("")
        }
    }

    private fun setTitleStateCurrentOperatorConnected() {
        if (isInMessageSearchMode) return
        if (chatController.isConsultFound) {
            if (!resources.getBoolean(style.fixedChatTitle)) {
                binding.subtitle.visibility = View.VISIBLE
            }
            binding.consultName.visibility = View.VISIBLE
            binding.searchLo.visibility = View.GONE
            binding.search.setText("")
        }
        if (!resources.getBoolean(style.isChatSubtitleVisible)) {
            binding.subtitle.visibility = View.GONE
        }
    }

    fun cleanChat() {
        val activity: Activity? = activity
        if (!isAdded || activity == null) {
            return
        }
        handler.post {
            if (fdMediaPlayer == null || chatAdapterCallback == null) { return@post }
            chatAdapter = ChatAdapter(
                chatAdapterCallback!!,
                fdMediaPlayer!!,
                mediaMetadataRetriever,
                ChatController.getInstance().messageErrorProcessor
            )
            binding.recycler.adapter = chatAdapter
            setTitleStateDefault()
            welcomeScreenVisibility(false)
            binding.inputEditView.clearFocus()
            welcomeScreenVisibility(true)
        }
    }

    private fun welcomeScreenVisibility(show: Boolean) {
        binding.welcome.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Remove close request from the thread history
     *
     * @return true - if deletion occurred, false - if there was no resolve request in the history
     */
    fun removeResolveRequest(): Boolean {
        return chatAdapter?.removeResolveRequest() ?: false
    }

    /**
     * Remove survey from the thread history
     *
     * @return true - if deletion occurred, false - if there was no survey in the history
     */
    fun removeSurvey(sendingId: Long): Boolean {
        return chatAdapter?.removeSurvey(sendingId) ?: false
    }

    fun setAllMessagesWereRead() {
        if (null != chatAdapter) {
            setMessagesAsRead()
        }
    }

    override fun updateProgress(fileDescription: FileDescription?) {
        chatAdapter?.updateProgress(fileDescription)
    }

    override fun onDownloadError(fileDescription: FileDescription?, throwable: Throwable?) {
        if (isAdded) {
            val activity: Activity? = activity
            if (activity != null) {
                updateProgress(fileDescription)
                if (throwable is FileNotFoundException) {
                    show(requireContext(), getString(R.string.ecc_error_no_file))
                    chatAdapter?.onDownloadError(fileDescription)
                }
                if (throwable is UnknownHostException) {
                    show(requireContext(), getString(R.string.ecc_check_connection))
                    chatAdapter?.onDownloadError(fileDescription)
                }
            }
        }
    }

    fun notifyConsultAvatarChanged(newAvatarUrl: String?, consultId: String?) {
        handler.post {
            if (chatAdapter != null) {
                chatAdapter?.notifyAvatarChanged(newAvatarUrl, consultId)
            }
        }
    }

    private fun setTitleStateSearchingConsult() {
        if (isInMessageSearchMode) {
            return
        }
        binding.subtitle.visibility = View.GONE
        binding.consultName.visibility = View.VISIBLE
        binding.searchLo.visibility = View.GONE
        binding.search.setText("")
        if (isAdded && !resources.getBoolean(style.fixedChatTitle)) {
            binding.consultName.text = requireContext().getString(R.string.ecc_searching_operator)
        }
    }

    fun setTitleStateSearchingMessage() {
        binding.subtitle.visibility = View.GONE
        binding.consultName.visibility = View.GONE
        binding.searchLo.visibility = View.VISIBLE
        binding.search.setText("")
    }

    fun setStateSearchingConsult() {
        handler.post {
            setTitleStateSearchingConsult()
            chatAdapter?.setSearchingConsult()
        }
    }

    fun removeSearching() {
        chatAdapter?.also {
            it.removeConsultSearching()
            showOverflowMenu()
        }
    }

    private fun unChooseItem() {
        hideCopyControls()
        chatAdapter?.removeHighlight()
    }

    fun removeSchedule(checkSchedule: Boolean) {
        chatAdapter?.removeSchedule(checkSchedule)
    }

    override fun setMenuVisibility(isVisible: Boolean) {
        if (isVisible) {
            showOverflowMenu()
        } else {
            hideOverflowMenu()
        }
    }

    private fun updateInputEnable(enableModel: InputFieldEnableModel) {
        isSendBlocked = !enableModel.isEnabledSendButton
        binding.sendMessage.isEnabled = enableModel.isEnabledSendButton &&
            (!TextUtils.isEmpty(binding.inputEditView.text) || hasAttachments())
        binding.inputEditView.isEnabled = enableModel.isEnabledInputField
        binding.addAttachment.isEnabled = enableModel.isEnabledInputField
        if (!enableModel.isEnabledInputField) {
            binding.inputEditView.hideKeyboard(100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_PHOTOS -> if (resultCode == Activity.RESULT_OK && data != null) {
                onPhotosResult(data)
            }
            REQUEST_EXTERNAL_CAMERA_PHOTO -> {
                if (resultCode == Activity.RESULT_OK && externalCameraPhotoFile != null) {
                    onExternalCameraPhotoResult()
                }
                externalCameraPhotoFile = null
            }
            REQUEST_CODE_FILE -> if (resultCode == Activity.RESULT_OK && data != null) {
                onFileResult(data)
            }
            REQUEST_CODE_PHOTO -> if (resultCode == Activity.RESULT_OK && data != null) {
                onPhotoResult(data)
            }
            REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY -> if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                openBottomSheetAndGallery()
            }
            REQUEST_PERMISSION_CAMERA -> if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                onCameraClick()
            }
            REQUEST_PERMISSION_READ_EXTERNAL -> if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                openFile()
            }
            REQUEST_PERMISSION_RECORD_AUDIO -> if (resultCode == PermissionsActivity.RESPONSE_GRANTED) {
                binding.recordButton.isListenForRecord = true
                show(requireContext(), requireContext().getString(R.string.ecc_hold_button_to_record_audio))
            }
        }
    }

    private fun scrollToNewMessages() {
        val layoutManager = binding.recycler.layoutManager as LinearLayoutManager? ?: return
        val list: List<ChatItem> = chatAdapter!!.list
        for (i in 1 until list.size) {
            val currentItem = list[i]
            if (currentItem is UnreadMessages || currentItem is ConsultPhrase &&
                !currentItem.isRead || currentItem is Survey && !currentItem.isRead
            ) {
                layoutManager.scrollToPositionWithOffset(i - 1, 0)
                break
            }
        }
    }

    private fun scrollToFirstUnreadMessage() {
        val layoutManager = binding.recycler.layoutManager as LinearLayoutManager? ?: return
        val list: List<ChatItem> = chatAdapter?.list ?: listOf()
        val firstUnreadUuid = chatController.firstUnreadUuidId
        if (list.isNotEmpty() && firstUnreadUuid != null) {
            for (i in 1 until list.size) {
                if (list[i] is ConsultPhrase) {
                    val cp = list[i] as ConsultPhrase
                    if (firstUnreadUuid.equals(cp.id, ignoreCase = true)) {
                        handler.post {
                            if (!isInMessageSearchMode) {
                                binding.recycler.post { layoutManager.scrollToPositionWithOffset(i - 1, 0) }
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    private val isPopupMenuEnabled: Boolean
        get() = (resources.getBoolean(config.getChatStyle().searchEnabled) || config.filesAndMediaMenuItemEnabled)

    private fun initToolbar() {
        val activity = activity ?: return
        binding.toolbar.title = ""
        ColorsHelper.setBackgroundColor(activity, binding.toolbar, style.chatToolbarColorResId)
        initToolbarShadow()
        if (activity is ChatActivity) {
            binding.chatBackButton.visibility = View.VISIBLE
        } else {
            binding.chatBackButton.visibility = if (style.showBackButton) View.VISIBLE else View.GONE
        }
        binding.chatBackButton.setOnClickListener { onActivityBackPressed() }
        binding.chatBackButton.setImageResource(style.chatToolbarBackIconResId)
        ColorsHelper.setTint(activity, binding.chatBackButton, style.chatToolbarTextColorResId)
        binding.popupMenuButton.setImageResource(style.chatToolbarPopUpMenuIconResId)
        ColorsHelper.setTint(activity, binding.popupMenuButton, style.chatToolbarTextColorResId)
        binding.popupMenuButton.setOnClickListener { showPopup() }
        binding.popupMenuButton.visibility = if (isPopupMenuEnabled) View.VISIBLE else View.GONE
        showOverflowMenu()
        binding.contentCopy.setImageResource(style.chatToolbarContentCopyIconResId)
        binding.reply.setImageResource(style.chatToolbarReplyIconResId)
        setContextIconDefaultTint(binding.contentCopy, binding.reply)
        if (resources.getBoolean(style.fixedChatTitle)) {
            setTitleStateDefault()
        }
        initToolbarTextPosition()
    }

    private fun setContextIconDefaultTint(vararg imageButtons: ImageButton) {
        val toolbarInverseIconTint = if (style.chatBodyIconsTint == 0) {
            style.chatToolbarInverseIconTintResId
        } else {
            style.chatBodyIconsTint
        }
        imageButtons.forEach { ColorsHelper.setTint(context, it, toolbarInverseIconTint) }
    }

    private fun initToolbarShadow() {
        val isShadowVisible = resources.getBoolean(style.isChatTitleShadowVisible)
        binding.toolbarShadow.visibility = if (isShadowVisible) View.VISIBLE else View.INVISIBLE
        if (!isShadowVisible) {
            binding.toolbar.elevation = 0f
        }
    }

    private fun initToolbarTextPosition() {
        val isToolbarTextCentered = Config.getInstance().getChatStyle().isToolbarTextCentered
        val gravity = if (isToolbarTextCentered) Gravity.CENTER else Gravity.CENTER_VERTICAL
        if (isToolbarTextCentered) {
            val paddingTopBottom = 0
            var paddingLeft = 0
            var paddingRight = 0
            if (binding.chatBackButton.isVisible() &&
                !binding.popupMenuButton.isVisible()
            ) {
                paddingRight = resources.getDimensionPixelSize(R.dimen.ecc_toolbar_button_width)
            } else if (!binding.chatBackButton.isVisible() &&
                binding.popupMenuButton.isVisible()
            ) {
                paddingLeft = resources.getDimensionPixelSize(R.dimen.ecc_toolbar_button_width)
            }
            binding.consultTitle.setPadding(paddingLeft, paddingTopBottom, paddingRight, paddingTopBottom)
        }
        binding.consultName.gravity = gravity
        binding.subtitle.gravity = gravity
    }

    private fun showOverflowMenu() {
        if (isPopupMenuEnabled) {
            binding.popupMenuButton.visibility = View.VISIBLE
        }
    }

    private fun hideOverflowMenu() {
        binding.popupMenuButton.visibility = View.GONE
    }

    private fun onActivityBackPressed() {
        if (isAdded) {
            val activity: Activity? = activity
            activity?.onBackPressed()
        }
    }

    /**
     * @return true, if chat should be closed
     */
    fun onBackPressed(): Boolean {
        val activity: Activity? = activity
        if (activity == null && !isAdded) {
            return true
        }
        chatAdapter?.removeHighlight()
        var isNeedToClose = true
        if (bottomSheetDialogFragment != null) {
            hideBottomSheet()
            return false
        }
        if (binding.copyControls.visibility == View.VISIBLE &&
            binding.searchLo.visibility == View.VISIBLE
        ) {
            unChooseItem()
            binding.search.requestFocus()
            binding.search.showKeyboard(100)
            return false
        }
        if (binding.copyControls.visibility == View.VISIBLE) {
            unChooseItem()
            hideBackButton()
            isNeedToClose = false
        }
        if (binding.searchLo.visibility == View.VISIBLE) {
            isNeedToClose = false
            hideSearchMode()
            if (chatAdapter != null) {
                scrollToPosition(chatAdapter!!.itemCount - 1, false)
            }
        }
        if (mQuoteLayoutHolder?.isVisible == true) {
            mQuoteLayoutHolder?.clear()
            return false
        }
        return isNeedToClose
    }

    private fun hideSearchMode() {
        activity ?: return
        binding.searchLo.visibility = View.GONE
        setMenuVisibility(true)
        isInMessageSearchMode = false
        binding.search.setText("")
        binding.search.hideKeyboard(100)
        binding.searchMore.visibility = View.GONE
        binding.swipeRefresh.isEnabled = true
        when (chatController.stateOfConsult) {
            ChatController.CONSULT_STATE_DEFAULT -> setTitleStateDefault()
            ChatController.CONSULT_STATE_FOUND -> setStateConsultConnected(chatController.currentConsultInfo)
            ChatController.CONSULT_STATE_SEARCHING -> setTitleStateSearchingConsult()
        }
        hideBackButton()
    }

    private fun hideBackButton() {
        val activity: Activity? = activity
        if (activity !is ChatActivity) {
            if (!style.showBackButton) {
                binding.chatBackButton.visibility = View.GONE
            }
        }
    }

    private fun search(searchInFiles: Boolean) {
        activity ?: return
        info("searchInFiles: $searchInFiles")
        isInMessageSearchMode = true
        setBottomStateDefault()
        setTitleStateSearchingMessage()
        binding.search.requestFocus()
        hideOverflowMenu()
        setMenuVisibility(false)
        binding.search.showKeyboard(100)
        binding.swipeRefresh.isEnabled = false
        binding.searchMore.visibility = View.GONE
    }

    private fun updateUIonPhraseLongClick(chatPhrase: ChatPhrase, position: Int) {
        unChooseItem()
        val activity = activity ?: return
        val toolbarInverseIconTint =
            if (style.chatBodyIconsTint == 0) style.chatToolbarInverseIconTintResId else style.chatBodyIconsTint
        // для случая, если popupMenuButton отображается при выделении сообщения
        ColorsHelper.setTint(activity, binding.popupMenuButton, toolbarInverseIconTint)
        ColorsHelper.setTint(activity, binding.chatBackButton, toolbarInverseIconTint)
        ColorsHelper.setBackgroundColor(
            activity,
            binding.toolbar,
            style.chatToolbarContextMenuColorResId
        )
        binding.toolbar.elevation = 0f
        binding.copyControls.visibility = View.VISIBLE
        binding.consultName.visibility = View.GONE
        binding.subtitle.visibility = View.GONE
        if (binding.chatBackButton.visibility == View.GONE) {
            binding.chatBackButton.visibility = View.VISIBLE
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (chatController.isMessageSent(chatPhrase.id)) {
                withMainContext {
                    setContextIconDefaultTint(binding.reply)
                    binding.reply.isEnabled = true
                    binding.reply.setOnClickListener {
                        onReplyClick(chatPhrase, position)
                        hideBackButton()
                    }
                }
            } else {
                withMainContext {
                    try {
                        style.chatBodyIconsColorState[0]
                    } catch (exc: Exception) {
                        R.color.disabled_icons_color
                    }.also { color ->
                        ColorsHelper.setTint(context, binding.reply, color)
                    }
                    binding.reply.isEnabled = false
                }
            }
        }

        binding.contentCopy.setOnClickListener {
            onCopyClick(activity, chatPhrase)
            hideBackButton()
        }

        chatAdapter?.setItemHighlighted(chatPhrase)
    }

    fun showQuickReplies(quickReplies: QuickReplyItem?) {
        quickReplyItem = quickReplies
        addChatItem(quickReplyItem)

        chatAdapter?.itemCount?.let { scrollToPosition(it - 1, false) }
        hideBottomSheet()
    }

    fun hideQuickReplies() {
        if (quickReplyItem != null) {
            chatAdapter?.removeItem(quickReplyItem)
        }
    }

    private fun openFile() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*"),
            REQUEST_CODE_FILE
        )
    }

    override fun onFileSelected(file: File?) {
        val uri = if (file != null) FileProviderHelper.getUriForFile(requireContext(), file) else null
        if (uri != null && canBeSent(requireContext(), uri)) {
            onFileResult(uri)
        } else {
            show(requireContext(), getString(R.string.ecc_failed_to_open_file))
        }
    }

    fun setClientNotificationDisplayType(type: ClientNotificationDisplayType?) {
        chatAdapter?.setClientNotificationDisplayType(type)
    }

    fun setCurrentThreadId(threadId: Long) {
        chatAdapter?.setCurrentThreadId(threadId)
    }

    private val isPreviewPlaying: Boolean
        get() = if (fdMediaPlayer != null) {
            ObjectsCompat.equals(fdMediaPlayer!!.fileDescription, getFileDescription())
        } else {
            false
        }

    fun showEmptyState() {
        binding.flEmpty.visibility = View.VISIBLE
        binding.tvEmptyStateHint.setText(R.string.ecc_empty_state_hint)
    }

    fun hideEmptyState() {
        binding.flEmpty.visibility = View.GONE
    }

    fun showProgressBar() {
        binding.welcome.visibility = View.GONE
        binding.flEmpty.visibility = View.VISIBLE
        binding.tvEmptyStateHint.setText(style.loaderTextResId)
    }

    fun hideProgressBar() {
        welcomeScreenVisibility(chatController.isNeedToShowWelcome)
        binding.flEmpty.visibility = View.GONE
    }

    fun showBalloon(message: String?) {
        show(requireContext(), message!!)
    }

    override fun acceptConvertedFile(convertedFile: File) {
        addVoiceMessagePreview(convertedFile)
    }

    private fun showSafelyCameraPermissionDescriptionDialog(
        cameraPermissions: List<String>,
    ) {
        if (permissionDescriptionAlertDialogFragment == null) {
            this.cameraPermissions = cameraPermissions
            showPermissionDescriptionDialog(PermissionDescriptionType.CAMERA, REQUEST_PERMISSION_CAMERA)
        }
    }

    private fun showSafelyPermissionDescriptionDialog(
        type: PermissionDescriptionType,
        requestCode: Int,
    ) {
        if (permissionDescriptionAlertDialogFragment == null) {
            showPermissionDescriptionDialog(type, requestCode)
        }
    }

    private fun showPermissionDescriptionDialog(
        type: PermissionDescriptionType,
        requestCode: Int,
    ) {
        permissionDescriptionAlertDialogFragment = newInstance(type, requestCode)
        permissionDescriptionAlertDialogFragment?.show(
            childFragmentManager,
            PermissionDescriptionAlertFragment.TAG
        )
    }

    @SuppressLint("RestrictedApi")
    private inner class QuoteLayoutHolder {
        var ignorePlayerUpdates = false
        private var formattedDuration = ""

        init {
            activity?.let { activity ->
                binding.quoteButtonPlayPause.setColorFilter(
                    ContextCompat.getColor(requireContext(), style.previewPlayPauseButtonColor),
                    PorterDuff.Mode.SRC_ATOP
                )
                binding.quoteHeader.setTextColor(ContextCompat.getColor(activity, style.incomingMessageTextColor))
                binding.quoteClear.setOnClickListener { clear() }
                binding.quoteButtonPlayPause.setOnClickListener {
                    if (fdMediaPlayer == null) {
                        return@setOnClickListener
                    }
                    val fileDescription = getFileDescription()
                    if (fileDescription != null && isVoiceMessage(fileDescription)) {
                        fdMediaPlayer?.processPlayPause(fileDescription)
                        fdMediaPlayer?.mediaPlayer?.let { init(it.duration, it.currentPosition, it.isPlaying) }
                    }
                }
                binding.quoteSlider.addOnChangeListener(
                    Slider.OnChangeListener { _: Slider?, value: Float, fromUser: Boolean ->
                        if (fromUser) {
                            fdMediaPlayer?.mediaPlayer?.seekTo(value.toInt())
                        }
                    }
                )
                binding.quoteSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                        ignorePlayerUpdates = true
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        ignorePlayerUpdates = false
                    }
                })
                binding.quoteSlider.setLabelFormatter(VoiceTimeLabelFormatter())
            }
        }

        var isVisible: Boolean
            get() = binding.quoteLayout.visibility == View.VISIBLE
            private set(isVisible) {
                if (isVisible) {
                    binding.quoteLayout.visibility = View.VISIBLE
                    binding.delimeter.visibility = View.VISIBLE
                } else {
                    binding.quoteLayout.visibility = View.GONE
                    binding.delimeter.visibility = View.GONE
                }
            }

        fun clear() {
            binding.quoteHeader.text = ""
            binding.quoteText.text = ""
            isVisible = false
            mQuote = null
            campaignMessage = null
            setFileDescription(null)
            resetProgress()
            if (isPreviewPlaying) {
                fdMediaPlayer?.reset()
            }
            unChooseItem()
            chatUpdateProcessor.processor.postAttachAudioFile(false)
        }

        fun setContent(header: String?, text: String, imagePath: Uri?, isFromFilePicker: Boolean) {
            isVisible = true
            setQuotePast(isFromFilePicker)
            if (header == null || header == "null") {
                binding.quoteHeader.visibility = View.INVISIBLE
            } else {
                binding.quoteHeader.visibility = View.VISIBLE
                binding.quoteHeader.text = header
            }
            binding.quoteText.visibility = View.VISIBLE
            binding.quoteButtonPlayPause.visibility = View.GONE
            binding.quoteSlider.visibility = View.GONE
            binding.quoteDuration.visibility = View.GONE
            binding.quoteText.text = text
            if (imagePath != null) {
                binding.quoteImage.visibility = View.VISIBLE
                get()
                    .load(imagePath.toString())
                    .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP)
                    .into(binding.quoteImage)
            } else {
                binding.quoteImage.visibility = View.GONE
            }
        }

        private fun setQuotePast(isFromFilePicker: Boolean) {
            if (isFromFilePicker) {
                binding.quotePast.visibility = View.GONE
            } else {
                binding.quotePast.visibility = View.VISIBLE
                binding.quotePast.setImageResource(style.quoteAttachmentIconResId)
            }
        }

        fun setVoice() {
            isVisible = true
            binding.quoteButtonPlayPause.visibility = View.VISIBLE
            binding.quoteSlider.visibility = View.VISIBLE
            binding.quoteDuration.visibility = View.VISIBLE
            binding.quoteHeader.visibility = View.GONE
            binding.quoteText.visibility = View.GONE
            binding.quotePast.visibility = View.GONE
            formattedDuration = getFormattedDuration(getFileDescription())
            binding.quoteDuration.text = formattedDuration
            chatUpdateProcessor.processor.postAttachAudioFile(true)
        }

        private fun init(maxValue: Int, progress: Int, isPlaying: Boolean) {
            val effectiveProgress = progress.coerceAtMost(maxValue)
            binding.quoteDuration.text = effectiveProgress.formatAsDuration()
            binding.quoteSlider.isEnabled = true
            binding.quoteSlider.valueTo = maxValue.toFloat()
            binding.quoteSlider.value = effectiveProgress.toFloat()
            binding.quoteButtonPlayPause.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
        }

        fun updateProgress(progress: Int) {
            info("updateProgress: $progress")
            binding.quoteDuration.text = progress.formatAsDuration()
            binding.quoteSlider.value = progress.toFloat().coerceAtMost(binding.quoteSlider.valueTo)
        }

        fun updateIsPlaying(isPlaying: Boolean) {
            binding.quoteButtonPlayPause.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
        }

        fun resetProgress() {
            binding.quoteDuration.text = formattedDuration
            ignorePlayerUpdates = false
            binding.quoteSlider.isEnabled = false
            binding.quoteSlider.value = 0f
            binding.quoteButtonPlayPause.setImageResource(style.voiceMessagePlayButton)
        }

        private fun getFormattedDuration(fileDescription: FileDescription?): String {
            var duration = 0L
            if (fileDescription != null && fileDescription.fileUri != null) {
                duration = mediaMetadataRetriever.getDuration(fileDescription.fileUri!!)
            }
            return duration.formatAsDuration()
        }
    }

    private inner class AdapterCallback : ChatAdapter.Callback {
        override fun onFileClick(filedescription: FileDescription) {
            chatController.onFileClick(filedescription)
        }

        override fun onPhraseLongClick(chatPhrase: ChatPhrase, position: Int) {
            updateUIonPhraseLongClick(chatPhrase, position)
        }

        override fun onUserPhraseClick(userPhrase: UserPhrase, position: Int, view: View) {
            if (userPhrase.sentState.ordinal >= MessageStatus.SENT.ordinal) {
                chatAdapter?.let {
                    val item = it.list[position]
                    if (item is UserPhrase) {
                        it.list[position] = userPhrase
                        it.notifyItemChanged(position)
                    }
                }
            } else {
                PopupMenu(view.context, view, Gravity.END, 0, R.style.PopupMenu).apply {
                    inflate(R.menu.ecc_menu_user_phrase_sent_error)
                    setOnMenuItemClickListener {
                        return@setOnMenuItemClickListener when (it.itemId) {
                            R.id.resend -> {
                                chatController.forceResend(userPhrase)
                                true
                            }
                            R.id.delete -> {
                                chatAdapter?.removeItem(position)
                                chatController.removeUserPhraseFromDatabaseAsync(userPhrase)
                                true
                            }
                            else -> false
                        }
                    }
                    show()
                }
            }
        }

        override fun onQuoteClick(quote: Quote) {
            subscribe(
                chatController.downloadMessagesTillEnd()
                    .observeOn(AndroidSchedulers.mainThread())
                    .map<List<ChatItem?>?> { list: List<ChatItem?>? ->
                        if (list != null) chatAdapter?.addItems(list)
                        val itemHighlightedIndex = chatAdapter?.setItemHighlighted(quote.uuid) ?: -1
                        if (itemHighlightedIndex != -1) scrollToPosition(itemHighlightedIndex, true)
                        list
                    }
                    .delay(1500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { list: List<ChatItem?>? ->
                            chatAdapter?.removeHighlight()
                            if (list != null) chatAdapter!!.addItems(list)
                        }
                    ) { obj: Throwable -> obj.message }
            )
        }

        override fun onConsultAvatarClick(consultId: String) {
            if (config.getChatStyle().canShowSpecialistInfo) {
                val activity: Activity? = activity
                if (activity != null) {
                    chatController.onConsultChoose(activity, consultId)
                    setupStartSecondLevelScreen()
                }
            }
        }

        override fun onImageClick(chatPhrase: ChatPhrase) {
            if (chatPhrase.fileDescription?.fileUri == null) {
                return
            }
            if (chatPhrase is UserPhrase) {
                if (chatPhrase.sentState !== MessageStatus.READ) {
                    chatController.forceResend(chatPhrase)
                }
                if (chatPhrase.sentState !== MessageStatus.FAILED) {
                    val activity: Activity? = activity
                    activity?.startActivity(getStartIntent(activity, chatPhrase.fileDescription))
                    setupStartSecondLevelScreen()
                }
            } else if (chatPhrase is ConsultPhrase) {
                val activity: Activity? = activity
                activity?.startActivity(getStartIntent(activity, chatPhrase.fileDescription))
                setupStartSecondLevelScreen()
            }
        }

        override fun onFileDownloadRequest(fileDescription: FileDescription) {
            chatController.onFileDownloadRequest(fileDescription)
        }

        override fun onSystemMessageClick(systemMessage: SystemMessage) {}
        override fun onRatingClick(survey: Survey, rating: Int) {
            val activity: Activity? = activity
            if (activity != null) {
                survey.questions[0].rate = rating
                chatController.onRatingClick(survey)
            }
        }

        override fun onResolveThreadClick(approveResolve: Boolean) {
            val activity: Activity? = activity
            if (activity != null) {
                chatController.onResolveThreadClick(approveResolve)
            }
        }

        override fun onQuickReplyClick(quickReply: QuickReply) {
            hideQuickReplies()
            sendMessage(
                listOf(
                    UpcomingUserMessage(
                        null,
                        null,
                        null,
                        quickReply.text.trim { it <= ' ' },
                        quickReply.text.isLastCopyText()
                    )
                ),
                false
            )
        }
    }

    private inner class ChatReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == ACTION_SEARCH_CHAT_FILES) {
                search(true)
            } else if (intent.action != null && intent.action == ACTION_SEARCH) {
                search(false)
            }
        }
    }

    companion object {
        const val REQUEST_CODE_PHOTOS = 100
        const val REQUEST_CODE_PHOTO = 101
        const val REQUEST_EXTERNAL_CAMERA_PHOTO = 102
        const val REQUEST_CODE_FILE = 103
        const val REQUEST_PERMISSION_BOTTOM_GALLERY_GALLERY = 200
        const val REQUEST_PERMISSION_CAMERA = 201
        const val REQUEST_PERMISSION_READ_EXTERNAL = 202
        const val ACTION_SEARCH_CHAT_FILES = "ACTION_SEARCH_CHAT_FILES"
        const val ACTION_SEARCH = "ACTION_SEARCH"
        const val ACTION_SEND_QUICK_MESSAGE = "ACTION_SEND_QUICK_MESSAGE"
        private const val REQUEST_PERMISSION_RECORD_AUDIO = 204
        private const val ARG_OPEN_WAY = "arg_open_way"
        private const val DISABLED_ALPHA = 0.5f
        private const val ENABLED_ALPHA = 1.0f
        private const val INVISIBLE_MESSAGES_COUNT = 3
        private const val INPUT_DELAY: Long = 3000
        private const val INPUT_EDIT_VIEW_MIN_LINES_COUNT = 1
        private const val INPUT_EDIT_VIEW_MAX_LINES_COUNT = 7
        var isShown = false
            private set
        private var afterResume = false
        private var resumeAfterSecondLevelScreen: Boolean = false

        @JvmStatic
        fun newInstance(@OpenWay from: Int): ChatFragment {
            val arguments = Bundle()
            arguments.putInt(ARG_OPEN_WAY, from)
            val chatFragment = ChatFragment()
            chatFragment.arguments = arguments
            return chatFragment
        }
    }
}
