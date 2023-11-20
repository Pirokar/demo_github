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
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.util.ObjectsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.annimon.stream.Optional
import com.annimon.stream.Stream
import com.devlomi.record_view.OnRecordListener
import com.google.android.material.slider.Slider
import im.threads.BuildConfig
import im.threads.R
import im.threads.business.annotation.OpenWay
import im.threads.business.audio.audioRecorder.AudioRecorder
import im.threads.business.broadcastReceivers.ProgressReceiver
import im.threads.business.chatUpdates.ChatUpdateProcessor
import im.threads.business.config.BaseConfig
import im.threads.business.extensions.withMainContext
import im.threads.business.imageLoading.ImageLoader.Companion.get
import im.threads.business.logger.LogZipSender
import im.threads.business.logger.LoggerEdna
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
import im.threads.business.models.enums.ModificationStateEnum
import im.threads.business.serviceLocator.core.inject
import im.threads.business.useractivity.UserActivityTimeProvider.getLastUserActivityTimeCounter
import im.threads.business.utils.Balloon.show
import im.threads.business.utils.FileProvider
import im.threads.business.utils.FileUtils
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
import im.threads.business.utils.ThreadsPermissionChecker
import im.threads.business.utils.ThreadsPermissionChecker.isRecordAudioPermissionGranted
import im.threads.business.utils.copyToBuffer
import im.threads.business.utils.getDuration
import im.threads.business.utils.isLastCopyText
import im.threads.databinding.EccFragmentChatBinding
import im.threads.ui.ChatStyle
import im.threads.ui.activities.ChatActivity
import im.threads.ui.activities.GalleryActivity
import im.threads.ui.activities.GalleryActivity.Companion.getStartIntent
import im.threads.ui.activities.ImagesActivity.Companion.getStartIntent
import im.threads.ui.adapters.ChatAdapter
import im.threads.ui.config.Config
import im.threads.ui.controllers.ChatController
import im.threads.ui.fragments.PermissionDescriptionAlertFragment.Companion.newInstance
import im.threads.ui.fragments.PermissionDescriptionAlertFragment.OnAllowPermissionClickListener
import im.threads.ui.holders.BaseHolder.Companion.statuses
import im.threads.ui.permissions.PermissionsActivity
import im.threads.ui.styles.permissions.PermissionDescriptionType
import im.threads.ui.utils.CameraConstants
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.FileHelper.isAllowedFileExtension
import im.threads.ui.utils.FileHelper.isAllowedFileSize
import im.threads.ui.utils.FileHelper.isFileExtensionsEmpty
import im.threads.ui.utils.FileHelper.maxAllowedFileSize
import im.threads.ui.utils.gone
import im.threads.ui.utils.hideKeyboard
import im.threads.ui.utils.invisible
import im.threads.ui.utils.isNotVisible
import im.threads.ui.utils.isVisible
import im.threads.ui.utils.visible
import im.threads.ui.views.VoiceTimeLabelFormatter
import im.threads.ui.views.formatAsDuration
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
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
 * чтобы чат можно было встроить в приложение с навигацией на фрагментах
 */
class ChatFragment :
    BaseFragment(),
    AttachmentBottomSheetDialogFragment.Callback,
    ProgressReceiver.Callback,
    ChatCenterAudioConverterCallback,
    OnAllowPermissionClickListener {

    private val handler = Handler(Looper.getMainLooper())
    private val fileNameDateFormat = SimpleDateFormat("dd.MM.yyyy.HH:mm:ss.S", Locale.getDefault())
    private val inputTextObservable = BehaviorSubject.createDefault("")
    internal val fileDescription = BehaviorSubject.createDefault(Optional.empty<FileDescription?>())
    private val mediaMetadataRetriever = MediaMetadataRetriever()
    private val audioConverter = ChatCenterAudioConverter()
    private val chatUpdateProcessor: ChatUpdateProcessor by inject()
    private val fileProvider: FileProvider by inject()
    private var fdMediaPlayer: FileDescriptionMediaPlayer? = null
    private val chatController: ChatController by lazy { ChatController.getInstance() }
    private var chatAdapter: ChatAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var chatAdapterCallback: ChatAdapter.Callback? = null
    private var quoteLayoutHolder: QuoteLayoutHolder? = null
    private var mQuote: Quote? = null
    private var campaignMessage: CampaignMessage? = null
    private var mChatReceiver: ChatReceiver? = null
    private var isInMessageSearchMode = false
    internal var isResumed = false
    private var isSendBlocked = false
    private var binding: EccFragmentChatBinding? = null
    private var externalCameraPhotoFile: File? = null
    private var bottomSheetDialogFragment: AttachmentBottomSheetDialogFragment? = null
    private var permissionDescriptionAlertDialogFragment: PermissionDescriptionAlertFragment? = null
    private var cameraPermissions: List<String>? = null
    private var mAttachedImages: MutableList<Uri>? = ArrayList()
    private var recorder: AudioRecorder? = null
    private var isNewMessageUpdateTimeoutOn = false
    var quickReplyItem: QuickReplyItem? = null
    private var previousChatItemsCount = 0
    private val config = Config.getInstance()
    private val coroutineScope = lifecycle.coroutineScope
    var style: ChatStyle = config.chatStyle
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        info(ChatFragment::class.java.simpleName + " onCreateView.")
        val activity: Activity? = activity

        // Статус бар подкрашивается только при использовании чата в стандартном Activity.
        if (activity is ChatActivity) {
            ColorsHelper.setStatusBarColor(WeakReference(activity), style.chatStatusBarColorResId, style.windowLightStatusBarResId)
        }
        binding = EccFragmentChatBinding.inflate(inflater, container, false)
        chatAdapterCallback = AdapterCallback()
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        fdMediaPlayer = FileDescriptionMediaPlayer(audioManager)
        initViews()
        initRecording()
        bindViews()
        setHasOptionsMenu(true)
        initController()
        setFragmentStyle()
        initMediaPlayer()
        subscribeToFileDescription()
        subscribeToInputText()
        isShown = true
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        info(ChatFragment::class.java.simpleName + " onViewCreated.")
        super.onViewCreated(view, savedInstanceState)
        val fileDescriptionDraft = chatController.fileDescriptionDraft
        if (isVoiceMessage(fileDescriptionDraft)) {
            setFileDescription(fileDescriptionDraft)
            quoteLayoutHolder?.setVoice()
        }
        val campaignMessage = chatController.campaignMessage
        val arguments = arguments
        if (arguments != null && campaignMessage != null) {
            @OpenWay val from = arguments.getInt(ARG_OPEN_WAY)
            if (from == OpenWay.DEFAULT) {
                return
            }
            val uid = UUID.randomUUID().toString()
            mQuote = Quote(
                uid,
                campaignMessage.senderName,
                campaignMessage.text,
                null,
                campaignMessage.receivedDate.time,
                ModificationStateEnum.NONE
            )
            this.campaignMessage = campaignMessage
            quoteLayoutHolder?.setContent(
                campaignMessage.senderName,
                campaignMessage.text,
                null,
                false
            )
            chatController.campaignMessage = null
        }
        initToolbar()
        initSearch()
    }

    override fun onStart() {
        super.onStart()
        info(ChatFragment::class.java.simpleName + " onStart.")
        ChatController.getInstance().onViewStart()
        initRecordButtonState()
        chatController.threadId?.let { setCurrentThreadId(it) }
        BaseConfig.getInstance().transport.setLifecycle(lifecycle)
        checkScrollDownButtonVisibility()
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
            binding?.swipeRefresh?.isRefreshing = false
            binding?.swipeRefresh?.destroyDrawingCache()
            binding?.swipeRefresh?.clearAnimation()
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
        updateToolBar()
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
        statuses.clear()
        BaseConfig.getInstance().transport.setLifecycle(null)
        chatController.onViewDestroy()
        chatAdapter?.onDestroyView()
        binding = null
        super.onDestroyView()
    }

    fun setupStartSecondLevelScreen() {
        resumeAfterSecondLevelScreen = true
    }

    fun isStartSecondLevelScreen(): Boolean {
        return resumeAfterSecondLevelScreen
    }

    internal val lastVisibleItemPosition: Int
        get() = if (isAdded) {
            mLayoutManager?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
        } else {
            RecyclerView.NO_POSITION
        }

    fun scrollToElementByIndex(index: Int) {
        if (isAdded) {
            mLayoutManager?.smoothScrollToPosition(binding?.chatItemsRecycler, RecyclerView.State(), index)
        }
    }

    val elements: List<ChatItem>
        get() = if (isAdded) {
            val list: List<ChatItem>? = chatAdapter?.list
            list ?: ArrayList()
        } else {
            ArrayList()
        }

    internal fun showErrorView(message: String?) = binding?.apply {
        if (chatErrorLayout.errorLayout.isNotVisible()) {
            showWelcomeScreen(false)
            hideProgressBar()
            chatItemsRecycler.invisible()
            bottomLayout.invisible()
            chatErrorLayout.errorLayout.visible()
            initErrorViewStyles()
            chatErrorLayout.errorMessage.text = message
            popupMenuButton.visibility = View.GONE
        }
    }

    internal fun hideErrorView(showList: Boolean = true) = binding?.apply {
        chatErrorLayout.errorLayout.gone()
        bottomLayout.visible()
        isNeedToShowWelcome {
            if (it && showList) {
                showWelcomeScreen(chatController.isChatReady())
            } else if (showList) {
                chatItemsRecycler.visible()
            }
        }
        popupMenuButton.visibility = if (isPopupMenuEnabled) View.VISIBLE else View.GONE
    }

    internal fun isErrorViewNotVisible(): Boolean {
        return binding?.chatErrorLayout?.errorLayout.isNotVisible()
    }

    private fun initErrorViewStyles() = binding?.chatErrorLayout?.apply {
        errorImage.setImageResource(style.chatErrorScreenImageResId)
        context?.let {
            errorMessage.setTextColor(ContextCompat.getColor(it, style.chatErrorScreenMessageTextColorResId))
            retryInitChatBtn.setTextColor(ContextCompat.getColor(it, style.chatErrorScreenButtonTextColorResId))
            retryInitChatBtn.backgroundTintList = if (style.chatErrorScreenButtonTintColorList != null) {
                style.chatErrorScreenButtonTintColorList
            } else if (style.chatBodyIconsColorState != null && style.chatBodyIconsColorState.size > 2) {
                ColorsHelper.getColorStateList(
                    it,
                    style.chatBodyIconsColorState[0],
                    style.chatBodyIconsColorState[1],
                    style.chatBodyIconsColorState[2]
                )
            } else {
                ColorsHelper.getColorStateList(
                    it,
                    style.chatDisabledTextColor,
                    style.chatToolbarColorResId,
                    style.chatToolbarColorResId
                )
            }
        }
        errorMessage.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(style.chatErrorScreenMessageTextSizeResId)
        )
        retryInitChatBtn.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(style.chatErrorScreenButtonTextSizeResId)
        )
        retryInitChatBtn.setOnClickListener {
            chatController.onRetryInitChatClick()
        }
    }

    private fun isNeedToShowWelcome(callback: (Boolean) -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val isNeedToShowWelcome = chatController.isNeedToShowWelcome()
            withMainContext { callback(isNeedToShowWelcome) }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun initController() {
        val activity = activity ?: return

        isNeedToShowWelcome { showWelcomeScreen(it) }
        chatController.bindFragment(this)
        mChatReceiver = ChatReceiver()
        val intentFilter = IntentFilter(ACTION_SEARCH_CHAT_FILES)
        intentFilter.addAction(ACTION_SEARCH)
        intentFilter.addAction(ACTION_SEND_QUICK_MESSAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.registerReceiver(mChatReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            activity.registerReceiver(mChatReceiver, intentFilter)
        }
    }

    private fun initViews() = binding?.apply {
        val activity: Activity? = activity
        if (activity == null || fdMediaPlayer == null || chatAdapterCallback == null) {
            return@apply
        }
        initInputLayout(activity)
        quoteLayoutHolder = QuoteLayoutHolder()
        mLayoutManager = LinearLayoutManager(activity)
        chatItemsRecycler.layoutManager = mLayoutManager
        chatAdapter = ChatAdapter(
            chatAdapterCallback!!,
            fdMediaPlayer!!,
            mediaMetadataRetriever,
            ChatController.getInstance().messageErrorProcessor
        )
        val itemAnimator = chatItemsRecycler.itemAnimator
        if (itemAnimator != null) {
            itemAnimator.changeDuration = 0
        }
        chatItemsRecycler.adapter = chatAdapter
    }

    private fun initInputLayout(activity: Activity) = binding?.apply {
        applyTintAndColorState(activity)
        val attachmentVisibility = if (config.getIsAttachmentsEnabled()) View.VISIBLE else View.GONE
        addAttachment.visibility = attachmentVisibility
        addAttachment.setOnClickListener { openBottomSheetAndGallery() }
        sendMessage.setOnClickListener { onSendButtonClick() }
        sendMessage.isEnabled = false
    }

    private fun applyTintAndColorState(activity: Activity) = binding?.apply {
        sendMessage.setImageResource(style.sendMessageIconResId)
        addAttachment.setImageResource(style.attachmentIconResId)
        quoteClear.setImageResource(style.quoteClearIconResId)
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
            ColorsHelper.setTintColorStateList(sendMessage, chatImagesColorStateList)
            ColorsHelper.setTintColorStateList(addAttachment, chatImagesColorStateList)
            ColorsHelper.setTintColorStateList(quoteClear, chatImagesColorStateList)
        } else {
            val iconTint = if (style.chatBodyIconsTint == 0) style.inputIconTintResId else style.chatBodyIconsTint
            ColorsHelper.setTint(activity, sendMessage, iconTint)
            ColorsHelper.setTint(activity, addAttachment, iconTint)
            val quoteClearIconTintResId = if (style.chatBodyIconsTint == 0) style.quoteClearIconTintResId else style.chatBodyIconsTint
            ColorsHelper.setTint(activity, quoteClear, quoteClearIconTintResId)
        }
    }

    private fun initRecordButtonState() {
        val recordButton = binding?.recordButton
        if (!isRecordAudioPermissionGranted(requireContext())) {
            recordButton?.isListenForRecord = false
            recordButton?.setOnRecordClickListener {
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
            recordButton?.isListenForRecord = true
            recordButton?.setOnRecordClickListener(null)
        }
    }

    private fun initRecording() {
        val recordButton = binding?.recordButton
        if (!style.voiceMessageEnabled) {
            binding?.recordLayout?.visibility = View.GONE
            return
        }
        val recordView = binding?.recordView
        recordView?.setRecordPermissionHandler { isRecordAudioPermissionGranted(requireContext()) }
        recordButton?.setRecordView(recordView)
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
            recordButton?.background = drawable
        }
        recordButton?.setImageResource(style.threadsRecordButtonIcon)
        recordButton?.setColorFilter(
            ContextCompat.getColor(requireContext(), style.threadsRecordButtonIconColor),
            PorterDuff.Mode.SRC_ATOP
        )
        recordView?.cancelBounds = 8f
        recordView?.setSmallMicColor(style.threadsRecordButtonSmallMicColor)
        recordView?.setLessThanSecondAllowed(false)
        recordView?.setSlideToCancelText(requireContext().getString(R.string.ecc_voice_message_slide_to_cancel))
        recordView?.setSoundEnabled(false)
        recordView?.setOnRecordListener(object : OnRecordListener {
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
                recordButton?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
                recordButton?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
                recordButton?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
        recordView?.setOnBasketAnimationEndListener {
            recordView.visibility = View.INVISIBLE
            debug("RecordView: Basket Animation Finished")
        }
    }

    private fun stopRecording() = binding?.apply {
        if (recorder != null) {
            val motionEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_UP, 0f, 0f, 0)
            recordButton.onTouch(recordButton, motionEvent)
            motionEvent.recycle()
        }
    }

    private fun addVoiceMessagePreview(file: File) {
        val context = context ?: return
        coroutineScope.launch(Dispatchers.IO) {
            val fd = FileDescription(
                requireContext().getString(R.string.ecc_voice_message).lowercase(Locale.getDefault()),
                fileProvider.getUriForFile(context, file),
                file.length(),
                System.currentTimeMillis()
            )
            withMainContext {
                setFileDescription(fd)
                quoteLayoutHolder?.setVoice()
            }
        }
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
                            if (quoteLayoutHolder!!.ignorePlayerUpdates) {
                                return@subscribe
                            }
                            val mediaPlayer = player.mediaPlayer
                            if (mediaPlayer != null) {
                                quoteLayoutHolder?.updateProgress(mediaPlayer.currentPosition)
                                quoteLayoutHolder?.updateIsPlaying(mediaPlayer.isPlaying)
                            }
                            chatAdapter?.resetPlayingHolder()
                        } else {
                            chatAdapter?.playerUpdate()
                            quoteLayoutHolder?.resetProgress()
                        }
                    }
                ) { error: Throwable? -> error("initMediaPlayer ", error) }
        }
    }

    private fun bindViews() = binding?.apply {
        swipeRefresh.setSwipeListener {}
        swipeRefresh.setOnRefreshListener {
            if (chatController.isChatReady()) {
                onRefresh()
            } else {
                swipeRefresh.isRefreshing = false
            }
        }
        consultName.setOnClickListener {
            if (chatController.isConsultFound()) {
                chatAdapterCallback?.onConsultAvatarClick(chatController.currentConsultInfo!!.id)
            }
        }
        consultName.setOnLongClickListener {
            val context = context
            if (context != null) {
                LogZipSender(context, fileProvider).shareLogs()
            }
            true
        }
        subtitle.setOnClickListener {
            if (chatController.isConsultFound()) {
                chatAdapterCallback?.onConsultAvatarClick(chatController.currentConsultInfo?.id)
            }
        }
        configureUserTypingSubscription()
        configureRecordButtonVisibility()
        chatItemsRecycler.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                chatItemsRecycler.postDelayed({
                    try {
                        if (style.scrollChatToEndIfUserTyping) {
                            chatAdapter?.itemCount?.let { scrollToPosition(it - 1, false) }
                        } else {
                            if (isAdded) {
                                chatItemsRecycler.smoothScrollBy(0, oldBottom - bottom)
                            }
                        }
                    } catch (exc: NullPointerException) {
                        LoggerEdna.error("Handling exception when scrolling after delay", exc)
                    }
                }, 100)
            }
        }

        chatItemsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = chatItemsRecycler.layoutManager as LinearLayoutManager?
                if (layoutManager != null) {
                    checkScrollDownButtonVisibility()
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val itemCount = chatAdapter?.itemCount
                    if (firstVisibleItemPosition == 0 &&
                        !chatController.isAllMessagesDownloaded &&
                        itemCount != null &&
                        itemCount > BaseConfig.getInstance().historyLoadingCount / 2 &&
                        chatController.isChatReady()
                    ) {
                        swipeRefresh.isRefreshing = true
                        chatController.loadHistory(isAfterAnchor = false) // before
                    }
                }
            }
        })
        binding?.scrollDownButtonContainer?.setOnClickListener {
            showUnreadMessagesCount(0)
            val unreadCount = chatController.getUnreadMessagesCount()
            if (unreadCount > 0) {
                scrollToNewMessages()
            } else {
                scrollToPosition(chatAdapter!!.itemCount - 1, false)
            }
            setMessagesAsRead()
            binding?.scrollDownButtonContainer?.visibility = View.GONE
            if (isInMessageSearchMode) {
                hideSearchMode()
            }
        }
    }

    private fun checkScrollDownButtonVisibility() {
        mLayoutManager?.let { layoutManager ->
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val itemCount = chatAdapter?.itemCount
            if (itemCount != null &&
                itemCount - 1 - lastVisibleItemPosition > INVISIBLE_MESSAGES_COUNT
            ) {
                binding?.scrollDownButtonContainer?.visible()
                showUnreadMessagesCount(chatController.getUnreadMessagesCount())
            } else {
                binding?.scrollDownButtonContainer?.visibility = View.GONE
                activity?.runOnUiThread { setMessagesAsRead() }
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
        subscribe(
            inputTextObservable
                .throttleLatest(INPUT_DELAY, TimeUnit.MILLISECONDS)
                .filter { charSequence: String -> charSequence.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ input: String? ->
                    onInputChanged(input)
                }) { error: Throwable? -> error("configureInputChangesSubscription ", error) }
        )
    }

    private fun onInputChanged(input: String?) {
        chatController.onUserTyping(input)
        updateLastUserActivityTime()
    }

    private fun updateLastUserActivityTime() {
        val timeCounter = getLastUserActivityTimeCounter()
        timeCounter.updateLastUserActivityTime()
    }

    private fun configureRecordButtonVisibility() {
        val recordButtonVisibilityDisposable = Observable.combineLatest(
            inputTextObservable,
            fileDescription
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
        binding?.recordButton?.visibility = if (isButtonVisible) View.VISIBLE else View.GONE
    }

    private fun showUnreadMessagesCount(unreadCount: Int) = binding?.apply {
        if (scrollDownButtonContainer.visibility == View.VISIBLE) {
            val hasUnreadCount = unreadCount > 0
            unreadMsgCount.text = if (hasUnreadCount) unreadCount.toString() else ""
            unreadMsgCount.visibility = if (hasUnreadCount) View.VISIBLE else View.GONE
            unreadMsgSticker.visibility = if (hasUnreadCount) View.VISIBLE else View.GONE
        }
    }

    private fun onSendButtonClick() {
        if (isSendBlocked) {
            show(requireContext(), requireContext().getString(R.string.ecc_message_were_unsent))
            return
        }

        val inputText = inputTextObservable.value
        if (inputText == null || inputText.trim { it <= ' ' }.isEmpty() && getFileDescription() == null) {
            return
        }
        showWelcomeScreen(false)
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
        updateLastUserActivityTime()
        clearInput()
    }

    fun getFileDescription(): FileDescription? {
        val fileDescriptionOptional = fileDescription.value
        return if (fileDescriptionOptional != null && fileDescriptionOptional.isPresent) {
            fileDescriptionOptional.get()
        } else {
            null
        }
    }

    private fun setFileDescription(fileDescription: FileDescription?) {
        this.fileDescription.onNext(Optional.ofNullable(fileDescription))
    }

    private fun subscribeToFileDescription() {
        subscribe(
            fileDescription
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { files: Optional<FileDescription?>? ->
                    val isFilesAvailable = files != null && !files.isEmpty
                    val isInputAvailable = binding?.inputEditView?.text?.isNotBlank()
                    val isEnable = isFilesAvailable || isInputAvailable == true
                    binding?.sendMessage?.isEnabled = isEnable
                }
        )
    }

    private fun subscribeToInputText() {
        subscribe(
            inputTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onInputChanged(it)
                }, {
                    error(it)
                })
        )
    }

    private fun onRefresh() {
        if (chatController.isChatReady()) {
            chatController.loadHistory(forceLoad = true)
        }
    }

    private fun setFragmentStyle() = binding?.apply {
        val activity = activity ?: return@apply
        ColorsHelper.setBackgroundColor(activity, chatRoot, style.chatBackgroundColor)
        ColorsHelper.setBackgroundColor(activity, inputLayout, style.chatMessageInputColor)
        ColorsHelper.setBackgroundColor(activity, bottomLayout, style.chatMessageInputColor)
        ColorsHelper.setBackgroundColor(activity, recordView, style.chatMessageInputColor)
        searchMore.setBackgroundColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor))
        searchMore.setTextColor(ContextCompat.getColor(activity, style.iconsAndSeparatorsColor))
        swipeRefresh.setColorSchemeColors(*resources.getIntArray(style.threadsSwipeRefreshColors))
        scrollDownButton.setBackgroundResource(style.scrollDownBackgroundResId)
        scrollDownButton.setImageResource(style.scrollDownIconResId)
        val lp = scrollDownButton.layoutParams as MarginLayoutParams
        lp.height = resources.getDimensionPixelSize(style.scrollDownButtonHeight)
        lp.width = resources.getDimensionPixelSize(style.scrollDownButtonWidth)
        ViewCompat.setElevation(scrollDownButton, resources.getDimension(style.scrollDownButtonElevation))
        val lpButtonContainer = scrollDownButtonContainer.layoutParams as MarginLayoutParams
        val margin = resources.getDimensionPixelSize(style.scrollDownButtonMargin)
        lpButtonContainer.setMargins(margin, margin, margin, margin)
        unreadMsgSticker.background.setColorFilter(
            ContextCompat.getColor(activity, style.unreadMsgStickerColorResId),
            PorterDuff.Mode.SRC_ATOP
        )
        ViewCompat.setElevation(unreadMsgSticker, resources.getDimension(style.scrollDownButtonElevation))
        unreadMsgCount.setTextColor(ContextCompat.getColor(activity, style.unreadMsgCountTextColorResId))
        ViewCompat.setElevation(unreadMsgCount, resources.getDimension(style.scrollDownButtonElevation))
        inputEditView.minHeight = activity.resources.getDimension(style.inputHeight).toInt()
        inputEditView.background = AppCompatResources.getDrawable(activity, style.inputBackground)
        inputEditView.setHint(style.inputHint)
        inputEditView.maxLines = INPUT_EDIT_VIEW_MIN_LINES_COUNT
        inputEditView.setPadding(
            resources.getDimensionPixelSize(style.inputFieldPaddingLeft),
            resources.getDimensionPixelSize(style.inputFieldPaddingTop),
            resources.getDimensionPixelSize(style.inputFieldPaddingRight),
            resources.getDimensionPixelSize(style.inputFieldPaddingBottom)
        )
        val params = inputEditView.layoutParams as LinearLayout.LayoutParams
        params.setMargins(
            resources.getDimensionPixelSize(style.inputFieldMarginLeft),
            resources.getDimensionPixelSize(style.inputFieldMarginTop),
            resources.getDimensionPixelSize(style.inputFieldMarginRight),
            resources.getDimensionPixelSize(style.inputFieldMarginBottom)
        )
        inputEditView.layoutParams = params
        inputEditView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(inputEditView.text)) {
                    inputEditView.maxLines = INPUT_EDIT_VIEW_MIN_LINES_COUNT
                } else {
                    inputEditView.maxLines = INPUT_EDIT_VIEW_MAX_LINES_COUNT
                }
                inputTextObservable.onNext(s.toString())
                sendMessage.isEnabled = !TextUtils.isEmpty(s) || hasAttachments()
            }
        })
        ColorsHelper.setTextColor(activity, subtitle, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, consultName, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, subtitle, style.chatToolbarTextColorResId)
        ColorsHelper.setTextColor(activity, consultName, style.chatToolbarTextColorResId)
        ColorsHelper.setHintTextColor(activity, inputEditView, style.chatMessageInputHintTextColor)
        ColorsHelper.setTextColor(activity, inputEditView, style.inputTextColor)
        if (!TextUtils.isEmpty(style.inputTextFont)) {
            try {
                val customFont = Typeface.createFromAsset(activity.assets, style.inputTextFont)
                inputEditView.typeface = customFont
            } catch (e: Exception) {
                error("setFragmentStyle", e)
            }
        }
        flEmpty.setBackgroundColor(ContextCompat.getColor(activity, style.emptyStateBackgroundColorResId))
        if (!BuildConfig.IS_ANIMATIONS_DISABLED.get()) {
            val progressDrawable = progressBar.indeterminateDrawable.mutate()
            ColorsHelper.setDrawableColor(
                activity,
                progressDrawable,
                style.emptyStateProgressBarColorResId
            )
            progressBar.indeterminateDrawable = progressDrawable
            ColorsHelper.setTextColor(activity, tvEmptyStateHint, style.emptyStateHintColorResId)
        } else {
            progressBar.invisible()
        }
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
            coroutineScope.launch {
                try {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val externalCameraPhotoFileDef = async(Dispatchers.IO) { createImageFile(activity.applicationContext) }
                    externalCameraPhotoFile = externalCameraPhotoFileDef.await()
                    val photoUri = fileProvider.getUriForFile(activity, externalCameraPhotoFile!!)
                    debug("Image File uri resolved: $photoUri")
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    grantPermissionsForImageUri(activity, intent, photoUri)
                    startActivityForResult(intent, REQUEST_EXTERNAL_CAMERA_PHOTO)
                } catch (e: IllegalArgumentException) {
                    error("Could not start external camera", e)
                    show(requireContext(), requireContext().getString(R.string.ecc_camera_could_not_start_error))
                }
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

    override fun onImageSelectionChanged(imageList: List<Uri>?) {
        mAttachedImages = if (imageList != null) {
            ArrayList(imageList)
        } else {
            arrayListOf()
        }
    }

    override fun onBottomSheetDetached() {
        bottomSheetDialogFragment = null
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
                consultPhrase.timeStamp,
                consultPhrase.modified
            )
            mQuote?.isFromConsult = true
            mQuote?.quotedPhraseConsultId = consultPhrase.consultId
        }
        setFileDescription(null)
        if (isImage(cp.fileDescription)) {
            quoteLayoutHolder?.setContent(
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
            quoteLayoutHolder?.setContent(
                if (phraseOwnerTitleIsNotEmpty) mQuote?.phraseOwnerTitle else "",
                fileName,
                null,
                false
            )
        } else {
            val phraseOwnerTitleIsNotEmpty = mQuote != null && !mQuote?.phraseOwnerTitle.isNullOrEmpty()
            quoteLayoutHolder?.setContent(
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
                        val inputText = inputTextObservable.value ?: return@subscribe
                        coroutineScope.launch {
                            val messagesDef = async(Dispatchers.IO) {
                                getUpcomingUserMessagesFromSelection(
                                    filteredPhotos,
                                    inputText,
                                    requireContext().getString(R.string.ecc_I),
                                    campaignMessage,
                                    mQuote
                                )
                            }
                            val messages = messagesDef.await()
                            if (isSendBlocked) {
                                clearInput()
                                show(requireContext(), requireContext().getString(R.string.ecc_message_were_unsent))
                            } else {
                                sendMessage(messages)
                            }
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

    private fun onPhotosResult(data: Intent) {
        val photos = data.getParcelableArrayListExtra<Uri>(GalleryActivity.PHOTOS_TAG)
        hideBottomSheet()
        showWelcomeScreen(false)
        val inputText = inputTextObservable.value
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
                    coroutineScope.launch(Dispatchers.IO) {
                        val fileSizes = ArrayList<Long>(filteredPhotos.size)
                        for (i in filteredPhotos.indices) {
                            fileSizes.add(getFileSize(filteredPhotos[i]))
                        }
                        withMainContext {
                            var fileUri = filteredPhotos[0]
                            var fileSize = fileSizes[0]
                            var uum = UpcomingUserMessage(
                                FileDescription(
                                    requireContext().getString(R.string.ecc_I),
                                    fileUri,
                                    fileSize,
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
                            inputTextObservable.onNext("")
                            quoteLayoutHolder?.clear()
                            for (i in 1 until filteredPhotos.size) {
                                fileUri = filteredPhotos[i]
                                fileSize = fileSizes[i]
                                uum = UpcomingUserMessage(
                                    FileDescription(
                                        requireContext().getString(R.string.ecc_I),
                                        fileUri,
                                        fileSize,
                                        System.currentTimeMillis()
                                    ),
                                    null,
                                    null,
                                    null,
                                    false
                                )
                                chatController.onUserInput(uum)
                            }
                        }
                    }
                }) { onError: Throwable? -> error("onPhotosResult ", onError) }
        )
    }

    private fun onExternalCameraPhotoResult() {
        externalCameraPhotoFile?.let { file ->
            coroutineScope.launch {
                val fileLengthDef = async(Dispatchers.IO) { file.length() }
                setFileDescription(
                    FileDescription(
                        requireContext().getString(R.string.ecc_image),
                        fileProvider.getUriForFile(BaseConfig.getInstance().context, file),
                        fileLengthDef.await(),
                        System.currentTimeMillis()
                    )
                )
                val inputText = inputTextObservable.value
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
    }

    @SuppressLint("WrongConstant")
    private fun onFileResult(data: Intent) {
        val uri = data.data
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val isAllowedFileExtension = isAllowedFileExtension(getExtensionFromMediaStore(BaseConfig.getInstance().context, uri))
                val isAllowedFileSize = isAllowedFileSize(getFileSizeFromMediaStore(BaseConfig.getInstance().context, uri))
                val isCanBeSent = canBeSent(requireContext(), uri)

                withMainContext {
                    if (isAllowedFileExtension) {
                        if (isAllowedFileSize) {
                            try {
                                if (isCanBeSent) {
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
        }
    }

    private fun onFileResult(uri: Uri) {
        info("onFileSelected: $uri")
        coroutineScope.launch(Dispatchers.IO) {
            setFileDescription(FileDescription(requireContext().getString(R.string.ecc_I), uri, getFileSize(uri), System.currentTimeMillis()))
            val fileName = getFileName(uri)
            withMainContext {
                quoteLayoutHolder?.setContent(
                    requireContext().getString(R.string.ecc_file),
                    fileName,
                    null,
                    true
                )
            }
        }
    }

    private fun onPhotoResult(data: Intent) {
        val imageExtra = data.getStringExtra(CameraConstants.IMAGE_EXTRA)
        if (imageExtra != null) {
            val file = File(imageExtra)
            val fileDescription = FileDescription(
                requireContext().getString(R.string.ecc_image),
                fileProvider.getUriForFile(requireContext(), file),
                file.length(),
                System.currentTimeMillis()
            )
            setFileDescription(
                fileDescription
            )
            val inputText = inputTextObservable.value
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
        if (isFileExtensionsEmpty()) {
            activity?.let { show(it, getString(R.string.ecc_sending_files_not_allowed)) }
            return
        }
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
        binding?.inputEditView?.setText("")
        quoteLayoutHolder?.clear()
        setBottomStateDefault()
        hideCopyControls()
        mAttachedImages?.clear()
        if (isInMessageSearchMode) {
            onActivityBackPressed()
        }
    }

    fun addChatItem(item: ChatItem?) = binding?.apply {
        val lastMessageVisible = isLastMessageVisible()
        if (item == null) {
            return@apply
        }
        if (item is ConsultPhrase) {
            val previouslyRead = item.read
            item.read = (lastMessageVisible && isResumed && !isInMessageSearchMode)
            if (item.read) {
                if (!previouslyRead && !item.id.isNullOrBlank()) {
                    BaseConfig.getInstance().transport.markMessagesAsRead(listOf(item.id))
                }
                chatController.setMessageAsRead(item)
            }
            chatAdapter?.setAvatar(item.consultId, item.avatarPath)
        }
        if (needsAddMessage(item)) {
            showWelcomeScreen(false)
            addItemsToChat(listOf(item))
            if (!isLastMessageVisible()) {
                scrollDownButtonContainer.visibility = View.VISIBLE
                showUnreadMessagesCount(chatController.getUnreadMessagesCount())
            }
            scrollDelayedOnNewMessageReceived(item is UserPhrase, lastMessageVisible)
        } else if (needsModifyImage(item)) {
            chatAdapter?.modifyImageInItem((item as ChatPhrase).fileDescription)
        }
    }

    private fun isLastMessageVisible(): Boolean {
        val layoutManager = (binding?.chatItemsRecycler?.layoutManager as LinearLayoutManager?) ?: return false
        return try {
            chatAdapter!!.itemCount - 1 - layoutManager.findLastVisibleItemPosition() < INVISIBLE_MESSAGES_COUNT
        } catch (exc: Exception) {
            false
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
        isLastMessageVisible: Boolean
    ) {
        if (!isNewMessageUpdateTimeoutOn) {
            isNewMessageUpdateTimeoutOn = true
            handler.postDelayed({
                if (!isInMessageSearchMode && isAdded && chatAdapter != null) {
                    val itemCount = chatAdapter!!.itemCount
                    if (isLastMessageVisible || isUserPhrase) {
                        scrollToPosition(itemCount - 1, false)
                    }
                }
                isNewMessageUpdateTimeoutOn = false
            }, 100)
        }
    }

    internal fun scrollToPosition(itemPosition: Int, smooth: Boolean) = binding?.apply {
        if (itemPosition >= 0 && isAdded) {
            if (smooth) {
                chatItemsRecycler.smoothScrollToPosition(itemPosition)
            } else {
                chatItemsRecycler.scrollToPosition(itemPosition)
            }
        }
    }

    private fun needsAddMessage(item: ChatItem): Boolean {
        return when (item) {
            is ScheduleInfo -> {
                // Если сообщение о расписании уже показано, то снова отображать не нужно.
                // Если в сообщении о расписании указано, что сейчас чат работет,
                // то расписание отображать не нужно.
                !item.isChatWorking && chatAdapter?.hasSchedule() != true
            }
            is QuickReplyItem -> {
                chatController.isChatWorking()
            }
            else -> {
                val chatPhrase: ChatPhrase
                try {
                    chatPhrase = item as ChatPhrase
                    chatPhrase.fileDescription == null || TextUtils.isEmpty(chatPhrase.fileDescription?.originalPath)
                } catch (exception: Exception) {
                    true
                }
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

    /**
     * Добавляет список элементов в чат и осуществляет прокрутку по необходимости
     *
     * @param list - список элементов для добавления
     * @param forceScrollToTheEnd -  Принудительный скролл в конец чата.
     * При возврате с дополнительного экрана (галереи, например) данный флаг не имеет силы.
     * По умолчанию - false
     */
    fun addChatItems(
        list: List<ChatItem?>,
        forceScrollToTheEnd: Boolean = false
    ) {
        if (list.isEmpty()) {
            return
        }
        chatAdapter?.let { chatAdapter ->
            val oldAdapterSize = chatAdapter.list.size
            val layoutManager = binding?.chatItemsRecycler?.layoutManager as LinearLayoutManager?
            val isBottomItemsVisible = chatAdapter.itemCount - 1 - lastVisibleItemPosition < INVISIBLE_MESSAGES_COUNT
            if (layoutManager == null || list.size == 1 && list[0] is ConsultTyping || isInMessageSearchMode) {
                return
            }
            showWelcomeScreen(false)
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val lastVisibleItemTimestamp = if (lastVisibleItemPosition >= 0 && lastVisibleItemPosition < chatAdapter.list.size) {
                chatAdapter.list[lastVisibleItemPosition].timeStamp
            } else {
                null
            }
            val listUpdatedCallback = object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {
                    updateChatAdapter(
                        chatAdapter,
                        layoutManager,
                        list,
                        oldAdapterSize,
                        forceScrollToTheEnd,
                        isBottomItemsVisible,
                        lastVisibleItemTimestamp
                    )
                }

                override fun onRemoved(position: Int, count: Int) {}
                override fun onMoved(fromPosition: Int, toPosition: Int) {}
                override fun onChanged(position: Int, count: Int, payload: Any?) {}
            }
            addItemsToChat(list, listUpdatedCallback, isBottomItemsVisible)
        }
    }

    private fun updateChatAdapter(
        chatAdapter: ChatAdapter,
        layoutManager: LinearLayoutManager,
        list: List<ChatItem?>,
        oldAdapterSize: Int,
        forceScrollToTheEnd: Boolean,
        isBottomItemsVisible: Boolean,
        lastVisibleItemTimestamp: Long?
    ) {
        val newAdapterSize = chatAdapter.list.size
        if (oldAdapterSize == 0 || (forceScrollToTheEnd && !isStartSecondLevelScreen())) {
            scrollToPosition(chatAdapter.itemCount - 1, false)
            afterResume = false
            resumeAfterSecondLevelScreen = false
            return
        }

        var needScrollDown = false
        if (list.isNotEmpty()) {
            needScrollDown = (
                oldAdapterSize < newAdapterSize ||
                    (list.last()?.timeStamp ?: 0) > chatAdapter.list.last().timeStamp &&
                    !isStartSecondLevelScreen()
                ) && isLastMessageVisible()
        }
        if (afterResume) {
            if ((!isStartSecondLevelScreen() && isBottomItemsVisible && newAdapterSize != oldAdapterSize) || needScrollDown) {
                scrollToPosition(chatAdapter.itemCount - 1, false)
            } else if (lastVisibleItemTimestamp != null) {
                layoutManager.scrollToPosition(chatAdapter.getPositionByTimeStamp(lastVisibleItemTimestamp))
            }
        } else if ((isBottomItemsVisible && newAdapterSize > oldAdapterSize) || needScrollDown) {
            scrollToPosition(chatAdapter.itemCount - 1, false)
        } else if (lastVisibleItemTimestamp != null) {
            layoutManager.scrollToPosition(chatAdapter.getPositionByTimeStamp(lastVisibleItemTimestamp))
        }
        afterResume = false
        resumeAfterSecondLevelScreen = false
    }

    fun setStateConsultConnected(info: ConsultInfo?) = binding?.apply {
        if (!isAdded) {
            return@apply
        }
        handler.post {
            val context = context
            if (context != null && isAdded) {
                if (!isInMessageSearchMode) {
                    consultName.visibility = View.VISIBLE
                }
                if (!resources.getBoolean(style.fixedChatTitle)) {
                    if (!isInMessageSearchMode) {
                        subtitle.visibility = View.VISIBLE
                    }
                    if (info != null) {
                        if (!TextUtils.isEmpty(info.name) && info.name != "null") {
                            consultName.text = info.name
                        } else {
                            consultName.text = context.getString(R.string.ecc_unknown_operator)
                        }
                        setSubtitle(info, context)
                    } else {
                        consultName.text = context.getString(R.string.ecc_unknown_operator)
                    }
                }
                if (!resources.getBoolean(style.isChatSubtitleVisible)) {
                    subtitle.visibility = View.GONE
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
        binding?.subtitle?.text = subtitle
    }

    fun setTitleStateDefault() = binding?.apply {
        handler.post {
            if (!isInMessageSearchMode && isAdded) {
                subtitle.visibility = View.GONE
                consultName.visibility = View.VISIBLE
                searchBar.gone()
                searchBar.clearSearch()
                searchListView.gone()
                consultName.setText(style.chatTitleTextResId)
            }
        }
    }

    fun setMessageState(correlationId: String?, backendMessageId: String?, state: MessageStatus?) {
        chatAdapter?.changeStateOfMessageByMessageId(correlationId, backendMessageId, state)
    }

    fun setSurveySentStatus(survey: Survey) {
        chatAdapter?.changeStateOfSurvey(survey)
    }

    private fun hideCopyControls() = binding?.apply {
        val activity = activity ?: return@apply
        setTitleStateCurrentOperatorConnected()
        ColorsHelper.setTint(activity, chatBackButton, style.chatToolbarTextColorResId)
        ColorsHelper.setTint(activity, popupMenuButton, style.chatToolbarTextColorResId)
        ColorsHelper.setBackgroundColor(activity, toolbar, style.chatToolbarColorResId)
        copyControls.visibility = View.GONE
        if (!isInMessageSearchMode) {
            consultName.visibility = View.VISIBLE
        }
        val isFixedChatTitle = resources.getBoolean(style.fixedChatTitle)
        val isVisibleSubtitle = resources.getBoolean(style.isChatSubtitleVisible)
        if (chatController.isConsultFound() && !isInMessageSearchMode && !isFixedChatTitle && isVisibleSubtitle) {
            subtitle.visibility = View.VISIBLE
        }
    }

    private fun setBottomStateDefault() {
        hideBottomSheet()
        if (!isInMessageSearchMode) {
            binding?.searchBar.gone()
            binding?.searchListView.gone()
            binding?.searchBar?.clearSearch()
        }
    }

    private fun setTitleStateCurrentOperatorConnected() = binding?.apply {
        if (isInMessageSearchMode) return@apply
        if (chatController.isConsultFound()) {
            if (!resources.getBoolean(style.fixedChatTitle)) {
                subtitle.visibility = View.VISIBLE
            }
            consultName.visibility = View.VISIBLE
            binding?.searchBar.gone()
            binding?.searchListView.gone()
            binding?.searchBar?.clearSearch()
        }
        if (!resources.getBoolean(style.isChatSubtitleVisible)) {
            subtitle.visibility = View.GONE
        }
    }

    fun cleanChat() {
        val activity: Activity? = activity
        if (!isAdded || activity == null) {
            return
        }
        handler.post {
            if (!isAdded || fdMediaPlayer == null || chatAdapterCallback == null) { return@post }
            chatAdapter = ChatAdapter(
                chatAdapterCallback!!,
                fdMediaPlayer!!,
                mediaMetadataRetriever,
                ChatController.getInstance().messageErrorProcessor
            )
            binding?.chatItemsRecycler?.adapter = chatAdapter
            setTitleStateDefault()
            showWelcomeScreen(false)
            binding?.inputEditView?.clearFocus()
            showWelcomeScreen(chatController.isChatReady())
        }
    }

    internal fun showWelcomeScreen(show: Boolean) {
        binding?.welcome?.visibility = if (show && binding?.chatErrorLayout?.errorLayout.isNotVisible()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    internal fun showWelcomeScreenIfNeed() {
        isNeedToShowWelcome { showWelcomeScreen(it) }
    }

    internal fun showBottomBar() {
        binding?.bottomLayout.visible()
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

    private fun setTitleStateSearchingConsult() = binding?.apply {
        if (isInMessageSearchMode || !isAdded) {
            return@apply
        }
        subtitle.visibility = View.GONE
        consultName.visibility = View.VISIBLE
        binding?.searchBar.gone()
        binding?.searchBar?.clearSearch()
        binding?.searchListView.gone()
        if (!resources.getBoolean(style.fixedChatTitle)) {
            consultName.text = requireContext().getString(R.string.ecc_searching_operator)
        }
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

    internal fun updateInputEnable(enableModel: InputFieldEnableModel) = binding?.apply {
        isSendBlocked = !enableModel.isEnabledSendButton
        val isChatWorking = chatController.isChatWorking() || chatController.isSendDuringInactive()
        sendMessage.isEnabled = enableModel.isEnabledSendButton && isChatWorking &&
            (!TextUtils.isEmpty(inputEditView.text) || hasAttachments())
        inputEditView.isEnabled = enableModel.isEnabledInputField && isChatWorking
        addAttachment.isEnabled = enableModel.isEnabledInputField && isChatWorking
        if (!enableModel.isEnabledInputField) {
            inputEditView.hideKeyboard(100)
        }
    }

    internal fun updateChatAvailabilityMessage(enableModel: InputFieldEnableModel) {
        if (enableModel.isEnabledSendButton &&
            enableModel.isEnabledInputField &&
            chatController.isChatWorking()
        ) {
            chatAdapter?.removeSchedule(false)
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
                binding?.recordButton?.isListenForRecord = true
                show(requireContext(), requireContext().getString(R.string.ecc_hold_button_to_record_audio))
            }
        }
    }

    private fun scrollToNewMessages() {
        val layoutManager = binding?.chatItemsRecycler?.layoutManager as LinearLayoutManager? ?: return
        val list: List<ChatItem> = chatAdapter!!.list
        for (i in 1 until list.size) {
            val currentItem = list[i]
            if (currentItem is UnreadMessages || currentItem is ConsultPhrase &&
                !currentItem.read || currentItem is Survey && !currentItem.isRead
            ) {
                layoutManager.scrollToPositionWithOffset(i - 1, 0)
                break
            }
        }
    }

    private fun scrollToFirstUnreadMessage() = binding?.apply {
        val layoutManager = chatItemsRecycler.layoutManager as LinearLayoutManager? ?: return@apply
        val list: List<ChatItem> = chatAdapter?.list ?: listOf()
        val firstUnreadUuid = chatController.firstUnreadUuidId
        if (list.isNotEmpty() && firstUnreadUuid != null) {
            for (i in 1 until list.size) {
                if (list[i] is ConsultPhrase) {
                    val cp = list[i] as ConsultPhrase
                    if (firstUnreadUuid.equals(cp.id, ignoreCase = true)) {
                        handler.post {
                            if (isAdded && !isInMessageSearchMode) {
                                chatItemsRecycler.post {
                                    layoutManager.scrollToPositionWithOffset(i - 1, 0)
                                }
                            }
                        }
                        break
                    }
                }
            }
        }
    }

    private val isPopupMenuEnabled: Boolean
        get() = (resources.getBoolean(config.chatStyle.searchEnabled))

    private fun initToolbar() = binding?.apply {
        val activity = activity ?: return@apply
        toolbar.title = ""
        ColorsHelper.setBackgroundColor(activity, toolbar, style.chatToolbarColorResId)
        initToolbarShadow()
        checkBackButtonVisibility()
        chatBackButton.setOnClickListener { onActivityBackPressed() }
        chatBackButton.setImageResource(style.chatToolbarBackIconResId)
        ColorsHelper.setTint(activity, chatBackButton, style.chatToolbarTextColorResId)
        popupMenuButton.setImageResource(style.searchIconResId)
        ColorsHelper.setTint(activity, popupMenuButton, style.chatToolbarTextColorResId)
        popupMenuButton.setOnClickListener {
            if (!isInMessageSearchMode) {
                search()
                binding?.chatBackButton.visible()
            }
        }
        popupMenuButton.visibility = if (isPopupMenuEnabled) View.VISIBLE else View.GONE
        showOverflowMenu()
        contentCopy.setImageResource(style.chatToolbarContentCopyIconResId)
        reply.setImageResource(style.chatToolbarReplyIconResId)
        setContextIconDefaultTint(contentCopy, reply)
        if (resources.getBoolean(style.fixedChatTitle)) {
            setTitleStateDefault()
        }
        initToolbarTextPosition()
    }

    private fun updateToolBar() {
        binding?.popupMenuButton?.visibility = if (isPopupMenuEnabled) View.VISIBLE else View.GONE
        checkBackButtonVisibility()
    }

    private fun initSearch() = binding?.apply {
        val searchQueryChannel: MutableStateFlow<String?> = MutableStateFlow("")
        val loadingChannel: MutableStateFlow<Boolean> = MutableStateFlow(false)

        searchBar.setSearchChannels(searchQueryChannel, loadingChannel)
        searchListView.setSearchChannels(searchQueryChannel, loadingChannel)

        coroutineScope.launch {
            searchQueryChannel.collect {
                if (!it.isNullOrBlank() && it.length > 2) {
                    searchListView.visible()
                } else {
                    searchListView.gone()
                }
            }
        }

        searchListView.setOnClickListener { uuid, date ->
            hideSearchMode()
            chatController.onSearchResultsClick(uuid, date)
        }
    }

    private fun setContextIconDefaultTint(vararg imageButtons: ImageButton) {
        val toolbarInverseIconTint = if (style.chatBodyIconsTint == 0) {
            style.chatToolbarInverseIconTintResId
        } else {
            style.chatBodyIconsTint
        }
        imageButtons.forEach { ColorsHelper.setTint(context, it, toolbarInverseIconTint) }
    }

    private fun initToolbarShadow() = binding?.apply {
        val isShadowVisible = resources.getBoolean(style.isChatTitleShadowVisible)
        toolbarShadow.visibility = if (isShadowVisible) View.VISIBLE else View.INVISIBLE
        if (!isShadowVisible) {
            toolbar.elevation = 0f
        }
    }

    private fun initToolbarTextPosition() = binding?.apply {
        val isToolbarTextCentered = Config.getInstance().chatStyle.isToolbarTextCentered
        val gravity = if (isToolbarTextCentered) Gravity.CENTER else Gravity.CENTER_VERTICAL
        if (isToolbarTextCentered) {
            val paddingTopBottom = 0
            var paddingLeft = 0
            var paddingRight = 0
            if (chatBackButton.isVisible() &&
                !popupMenuButton.isVisible()
            ) {
                paddingRight = resources.getDimensionPixelSize(R.dimen.ecc_toolbar_button_width)
            } else if (!chatBackButton.isVisible() &&
                popupMenuButton.isVisible()
            ) {
                paddingLeft = resources.getDimensionPixelSize(R.dimen.ecc_toolbar_button_width)
            }
            consultTitle.setPadding(paddingLeft, paddingTopBottom, paddingRight, paddingTopBottom)
        }
        consultName.gravity = gravity
        subtitle.gravity = gravity
    }

    private fun showOverflowMenu() {
        if (isPopupMenuEnabled) {
            binding?.popupMenuButton?.visibility = View.VISIBLE
        }
    }

    private fun hideOverflowMenu() {
        binding?.popupMenuButton?.visibility = View.GONE
    }

    private fun onActivityBackPressed() {
        if (isAdded) {
            if (isInMessageSearchMode && binding?.copyControls?.visibility != View.VISIBLE) {
                hideSearchMode()
            } else {
                val activity: Activity? = activity
                activity?.onBackPressed()
            }
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
        return if (bottomSheetDialogFragment != null) {
            hideBottomSheet()
            false
        } else if (binding?.copyControls.isVisible() && binding?.searchBar.isVisible()) {
            unChooseItem()
            binding?.searchBar?.requestFocus()
            binding?.searchBar?.showKeyboard(100)
            false
        } else if (binding?.copyControls.isVisible()) {
            unChooseItem()
            checkBackButtonVisibility()
            false
        } else if (binding?.searchBar.isVisible()) {
            hideSearchMode()
            if (chatAdapter != null) {
                scrollToPosition(chatAdapter!!.itemCount - 1, false)
            }
            false
        } else if (quoteLayoutHolder?.isVisible == true) {
            quoteLayoutHolder?.clear()
            false
        } else if (isInMessageSearchMode) {
            hideSearchMode()
            false
        } else {
            true
        }
    }

    private fun hideSearchMode() = binding?.apply {
        activity ?: return@apply
        searchBar.gone()
        searchListView.gone()
        setMenuVisibility(true)
        isInMessageSearchMode = false
        searchBar.clearSearch()
        searchBar.hideKeyboard(100)
        searchMore.gone()
        swipeRefresh.isEnabled = true
        when (chatController.stateOfConsult) {
            ChatController.CONSULT_STATE_DEFAULT -> setTitleStateDefault()
            ChatController.CONSULT_STATE_FOUND -> setStateConsultConnected(chatController.currentConsultInfo)
            ChatController.CONSULT_STATE_SEARCHING -> setTitleStateSearchingConsult()
        }
        checkBackButtonVisibility()
    }

    private fun checkBackButtonVisibility() {
        if (!style.showBackButton) {
            info("Back button is disabled in the style")
            binding?.chatBackButton.gone()
        } else {
            info("Back button is enabled in the style")
            binding?.chatBackButton.visible()
        }
    }

    private fun search() = binding?.apply {
        activity ?: return@apply
        info("starting search")
        isInMessageSearchMode = true
        consultName.gone()
        subtitle.gone()
        setBottomStateDefault()
        searchBar.visible()
        searchBar.requestFocus()
        hideOverflowMenu()
        setMenuVisibility(false)
        searchBar.showKeyboard(100)
        swipeRefresh.isEnabled = false
        searchMore.visibility = View.GONE
    }

    private fun updateUIonPhraseLongClick(chatPhrase: ChatPhrase, position: Int) = binding?.apply {
        unChooseItem()
        val activity = activity ?: return@apply
        val toolbarInverseIconTint =
            if (style.chatBodyIconsTint == 0) style.chatToolbarInverseIconTintResId else style.chatBodyIconsTint
        // для случая, если popupMenuButton отображается при выделении сообщения
        ColorsHelper.setTint(activity, popupMenuButton, toolbarInverseIconTint)
        ColorsHelper.setTint(activity, chatBackButton, toolbarInverseIconTint)
        ColorsHelper.setBackgroundColor(
            activity,
            toolbar,
            style.chatToolbarContextMenuColorResId
        )
        toolbar.elevation = 0f
        copyControls.visibility = View.VISIBLE
        consultName.visibility = View.GONE
        subtitle.visibility = View.GONE
        if (chatPhrase.phraseText.isNullOrEmpty()) {
            contentCopy.visibility = View.GONE
        } else {
            contentCopy.visibility = View.VISIBLE
        }
        if (chatBackButton.isNotVisible()) {
            chatBackButton.visible()
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (chatController.isMessageSent(chatPhrase.id)) {
                withMainContext {
                    setContextIconDefaultTint(reply)
                    reply.isEnabled = true
                    reply.setOnClickListener {
                        onReplyClick(chatPhrase, position)
                        checkBackButtonVisibility()
                        unChooseItem()
                    }
                }
            } else {
                withMainContext {
                    try {
                        style.chatBodyIconsColorState[0]
                    } catch (exc: Exception) {
                        R.color.disabled_icons_color
                    }.also { color ->
                        ColorsHelper.setTint(context, reply, color)
                    }
                    reply.isEnabled = false
                }
            }
        }

        contentCopy.setOnClickListener {
            onCopyClick(activity, chatPhrase)
            checkBackButtonVisibility()
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

    fun showEmptyState() = binding?.apply {
        if (chatController.isChatReady()) {
            flEmpty.visibility = View.VISIBLE
            tvEmptyStateHint.setText(R.string.ecc_empty_state_hint)
        }
    }

    fun hideEmptyState() {
        binding?.flEmpty.gone()
    }

    fun showProgressBar() {
        binding?.welcome.gone()
        binding?.flEmpty.visible()
        binding?.tvEmptyStateHint?.setText(style.loaderTextResId)
    }

    fun hideProgressBar() {
        binding?.flEmpty.gone()
        binding?.swipeRefresh?.isRefreshing = false
    }

    fun showBalloon(message: String?) {
        show(requireContext(), message!!)
    }

    internal fun showBalloon(messageResId: Int) {
        show(requireContext(), getString(messageResId))
    }

    internal fun getDisplayedMessagesCount() = chatAdapter?.itemCount ?: 0

    override fun acceptConvertedFile(convertedFile: File) {
        addVoiceMessagePreview(convertedFile)
    }

    private fun showSafelyCameraPermissionDescriptionDialog(
        cameraPermissions: List<String>
    ) {
        if (permissionDescriptionAlertDialogFragment == null) {
            this.cameraPermissions = cameraPermissions
            showPermissionDescriptionDialog(PermissionDescriptionType.CAMERA, REQUEST_PERMISSION_CAMERA)
        }
    }

    private fun showSafelyPermissionDescriptionDialog(
        type: PermissionDescriptionType,
        requestCode: Int
    ) {
        if (permissionDescriptionAlertDialogFragment == null) {
            showPermissionDescriptionDialog(type, requestCode)
        }
    }

    private fun showPermissionDescriptionDialog(
        type: PermissionDescriptionType,
        requestCode: Int
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
                binding?.quoteButtonPlayPause?.setColorFilter(
                    ContextCompat.getColor(requireContext(), style.previewPlayPauseButtonColor),
                    PorterDuff.Mode.SRC_ATOP
                )
                binding?.quoteHeader?.setTextColor(ContextCompat.getColor(activity, style.quoteHeaderTextColor))
                binding?.quoteText?.setTextColor(ContextCompat.getColor(activity, style.quoteTextTextColor))
                binding?.quoteClear?.setOnClickListener { clear() }
                binding?.quoteButtonPlayPause?.setOnClickListener {
                    if (fdMediaPlayer == null) {
                        return@setOnClickListener
                    }
                    val fileDescription = getFileDescription()
                    if (fileDescription != null && isVoiceMessage(fileDescription)) {
                        fdMediaPlayer?.processPlayPause(fileDescription)
                        fdMediaPlayer?.mediaPlayer?.let { init(it.duration, it.currentPosition, it.isPlaying) }
                    }
                }
                binding?.quoteSlider?.addOnChangeListener(
                    Slider.OnChangeListener { _: Slider?, value: Float, fromUser: Boolean ->
                        if (fromUser) {
                            fdMediaPlayer?.mediaPlayer?.seekTo(value.toInt())
                        }
                    }
                )
                binding?.quoteSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                        ignorePlayerUpdates = true
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        ignorePlayerUpdates = false
                    }
                })
                binding?.quoteSlider?.setLabelFormatter(VoiceTimeLabelFormatter())
            }
        }

        var isVisible: Boolean
            get() = binding?.quoteLayout.isVisible()
            private set(isVisible) {
                if (isVisible) {
                    binding?.quoteLayout.visible()
                    binding?.delimeter.visible()
                } else {
                    binding?.quoteLayout.gone()
                    binding?.delimeter.gone()
                }
            }

        fun clear() = binding?.apply {
            quoteHeader.text = ""
            quoteText.text = ""
            isVisible = false
            mQuote = null
            campaignMessage = null
            setFileDescription(null)
            resetProgress()
            if (isPreviewPlaying) {
                fdMediaPlayer?.reset()
            }
            unChooseItem()
            chatUpdateProcessor.postAttachAudioFile(false)
        }

        fun setContent(header: String?, text: String, imagePath: Uri?, isFromFilePicker: Boolean) = binding?.apply {
            isVisible = true
            setQuotePast(isFromFilePicker)
            if (header == null || header == "null") {
                quoteHeader.visibility = View.INVISIBLE
            } else {
                quoteHeader.visibility = View.VISIBLE
                quoteHeader.text = header
            }
            quoteText.visibility = View.VISIBLE
            quoteButtonPlayPause.visibility = View.GONE
            quoteSlider.visibility = View.GONE
            quoteDuration.visibility = View.GONE
            quoteText.text = text
            if (imagePath != null) {
                quoteImage.visibility = View.VISIBLE
                get()
                    .load(imagePath.toString())
                    .scales(ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_CROP)
                    .into(quoteImage)
            } else {
                quoteImage.visibility = View.GONE
            }
        }

        private fun setQuotePast(isFromFilePicker: Boolean) = binding?.apply {
            if (isFromFilePicker) {
                quotePast.visibility = View.GONE
            } else {
                quotePast.visibility = View.VISIBLE
                quotePast.setImageResource(style.quoteAttachmentIconResId)
            }
        }

        fun setVoice() = binding?.apply {
            isVisible = true
            quoteButtonPlayPause.visibility = View.VISIBLE
            quoteSlider.visibility = View.VISIBLE
            quoteDuration.visibility = View.VISIBLE
            quoteHeader.visibility = View.GONE
            quoteText.visibility = View.GONE
            quotePast.visibility = View.GONE

            coroutineScope.launch(Dispatchers.IO) {
                formattedDuration = getFormattedDuration(getFileDescription())
                withMainContext {
                    quoteDuration.text = formattedDuration
                    chatUpdateProcessor.postAttachAudioFile(true)
                }
            }
        }

        private fun init(maxValue: Int, progress: Int, isPlaying: Boolean) = binding?.apply {
            val effectiveProgress = progress.coerceAtMost(maxValue)
            quoteDuration.text = effectiveProgress.formatAsDuration()
            quoteSlider.isEnabled = true
            quoteSlider.valueTo = maxValue.toFloat()
            quoteSlider.value = effectiveProgress.toFloat()
            quoteButtonPlayPause.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
        }

        fun updateProgress(progress: Int) = binding?.apply {
            info("updateProgress: $progress")
            quoteDuration.text = progress.formatAsDuration()
            quoteSlider.value = progress.toFloat().coerceAtMost(quoteSlider.valueTo)
        }

        fun updateIsPlaying(isPlaying: Boolean) {
            binding?.quoteButtonPlayPause?.setImageResource(if (isPlaying) style.voiceMessagePauseButton else style.voiceMessagePlayButton)
        }

        fun resetProgress() = binding?.apply {
            quoteDuration.text = formattedDuration
            ignorePlayerUpdates = false
            quoteSlider.isEnabled = false
            quoteSlider.value = 0f
            quoteButtonPlayPause.setImageResource(style.voiceMessagePlayButton)
        }

        private fun getFormattedDuration(fileDescription: FileDescription?): String {
            var duration = 0L
            if (fileDescription?.fileUri != null) {
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
                        addItemsToChat(list)
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
                            addItemsToChat(list)
                        }
                    ) { obj: Throwable -> obj.message }
            )
        }

        override fun onConsultAvatarClick(consultId: String) {
            if (config.chatStyle.canShowSpecialistInfo) {
                val activity: Activity? = activity
                if (activity != null) {
                    chatController.onConsultChoose(activity, consultId)
                    setupStartSecondLevelScreen()
                }
            }
        }

        override fun onImageClick(chatPhrase: ChatPhrase) {
            if (!isPreviewFileExist(chatPhrase.fileDescription?.getPreviewFileDescription()) &&
                chatPhrase.fileDescription?.fileUri == null
            ) {
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

        fun isPreviewFileExist(fileDescription: FileDescription?): Boolean {
            fileDescription?.let {
                return FileUtils.isPreviewFileExist(requireContext(), it)
            }
            return false
        }

        override fun onFileDownloadRequest(fileDescription: FileDescription?, isPreview: Boolean) {
            chatController.onFileDownloadRequest(fileDescription, isPreview)
        }

        override fun onSystemMessageClick(systemMessage: SystemMessage) {}

        override fun onRatingClick(survey: Survey, rating: Int) {
            val activity: Activity? = activity
            if (activity != null && !survey.questions.isNullOrEmpty()) {
                survey.questions!![0].rate = rating
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
            if (chatController.isChatWorking()) {
                hideQuickReplies()
                sendMessage(
                    listOf(
                        UpcomingUserMessage(
                            null,
                            null,
                            null,
                            quickReply.text?.trim { it <= ' ' },
                            quickReply.text?.isLastCopyText() ?: false
                        )
                    ),
                    false
                )
            }
        }
    }

    private fun addItemsToChat(
        list: List<ChatItem?>?,
        listUpdatedCallback: ListUpdateCallback? = null,
        withAnimation: Boolean = true
    ) {
        list?.filterNotNull()?.let {
            if (it.isNotEmpty()) {
                val isChatReady = chatController.isChatReady()
                val isAnimationEnabled = withAnimation && isChatReady
                chatController.removeCorruptedFiles(it) {
                    chatController.setFormattedDurations(it, mediaMetadataRetriever) {
                        chatAdapter?.addItems(it, listUpdatedCallback, isAnimationEnabled)
                        if (isChatReady) {
                            hideErrorView()
                            hideProgressBar()
                        }
                    }
                }
            }
        }
    }

    private inner class ChatReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == ACTION_SEARCH) {
                search()
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
