package im.threads.ui.activities

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import im.threads.R
import im.threads.business.extensions.withMainContext
import im.threads.business.logger.LoggerEdna
import im.threads.business.models.MediaPhoto
import im.threads.business.models.PhotoBucketItem
import im.threads.business.utils.MediaHelper
import im.threads.databinding.EccActivityGalleryBinding
import im.threads.ui.adapters.GalleryAdapter
import im.threads.ui.adapters.GalleryAdapter.OnGalleryItemClick
import im.threads.ui.adapters.PhotoBucketsGalleryAdapter
import im.threads.ui.adapters.PhotoBucketsGalleryAdapter.OnItemClick
import im.threads.ui.config.Config.Companion.getInstance
import im.threads.ui.utils.BucketsGalleryDecorator
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.ColorsHelper.setDrawableColor
import im.threads.ui.utils.GalleryDecorator
import im.threads.ui.utils.gone
import im.threads.ui.utils.visible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

class GalleryActivity : BaseActivity(), OnItemClick, OnGalleryItemClick {

    private val screenState = BehaviorSubject.createDefault(ScreenState.BUCKET_LIST)
    private val dataIsEmpty = BehaviorSubject.createDefault(false)

    private var photosMap: MutableMap<String, MutableList<MediaPhoto>> = HashMap()
    private val bucketItems: MutableList<PhotoBucketItem> = ArrayList()
    private val chosenItems: MutableList<MediaPhoto> = ArrayList()
    private val bucketsGalleryDecorator = BucketsGalleryDecorator(4)
    private val galleryDecorator = GalleryDecorator(4)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val compositeDisposable = CompositeDisposable()

    private lateinit var binding: EccActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EccActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        subscribeToScreenState()
        subscribeToDataIsEmpty()
        initStatusBar()
        coroutineScope.launch {
            initData()
            withMainContext {
                showBucketListState()
                setButtonsClickListeners()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun initStatusBar() {
        val style = getInstance().chatStyle
        val statusBarColor = ContextCompat.getColor(this, style.chatStatusBarColorResId)
        val toolBarColor = ContextCompat.getColor(this, style.chatToolbarColorResId)
        val isStatusBarLight =
            resources.getBoolean(getInstance().chatStyle.windowLightStatusBarResId)
        super.setStatusBarColor(isStatusBarLight, statusBarColor)
        val backButtonDrawable =
            AppCompatResources.getDrawable(this, R.drawable.ecc_ic_arrow_back_white_24dp)
        setDrawableColor(this, backButtonDrawable, style.chatToolbarTextColorResId)
        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(toolBarColor))
            setHomeAsUpIndicator(style.chatToolbarBackIconResId)
            setHomeAsUpIndicator(backButtonDrawable)
        }
        val textColor = ContextCompat.getColor(this, style.chatToolbarTextColorResId)
        val hintTextColor = ContextCompat.getColor(this, style.chatToolbarHintTextColor)
        binding.toolbar.title
        binding.searchEditText.setTextColor(textColor)
        binding.searchEditText.setHintTextColor(hintTextColor)
        binding.clearSearchButton.setImageResource(R.drawable.ecc_ic_clear_gray_30dp)
        binding.title.setTextColor(textColor)
        ColorsHelper.setTint(this, binding.clearSearchButton, style.chatToolbarTextColorResId)
        ColorsHelper.setTint(this, binding.backButton, style.chatToolbarTextColorResId)

        initToolbarTextPosition()
    }

    private fun initToolbarTextPosition() {
        val isToolbarTextCentered = getInstance().chatStyle.isToolbarTextCentered
        val gravity = if (isToolbarTextCentered) Gravity.CENTER else Gravity.CENTER_VERTICAL
        binding.title.gravity = gravity
    }

    private fun setButtonsClickListeners() = with(binding) {
        backButton.setOnClickListener { onBackPressed() }
        clearSearchButton.setOnClickListener { clearSearch() }
        searchPhoto.setOnClickListener { showSearch() }
        cancel.setOnClickListener { onBackPressed() }
        send.setOnClickListener { send() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (ScreenState.BUCKET_LIST != screenState.value) {
            binding.searchEditText.setText("")
            showBucketListState()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPhotoBucketClick(item: PhotoBucketItem) {
        showPhotoListState(item.bucketName, item)
    }

    override fun onGalleryItemsChosen(chosenItems: List<MediaPhoto>) {
        this.chosenItems.clear()
        this.chosenItems.addAll(chosenItems)
        syncSendButtonState()
    }

    fun clearSearch() {
        binding.searchEditText.setText("")
    }

    fun showSearch() {
        showSearchState()
        search("")
    }

    fun send() {
        val list = ArrayList<Uri>()
        chosenItems.forEach {
            list.add(it.imageUri)
        }
        val intent = Intent()
        intent.putParcelableArrayListExtra(PHOTOS_TAG, list)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    search(s.toString())
                } else {
                    search("")
                }
            }
        })
        binding.searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.text.toString())
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }
    }

    private fun initData() {
        MediaHelper.getAllPhotos(this).use { cursor ->
            cursor?.let {
                if (it.count > 0) {
                    val idKey = it.getColumnIndex(MediaStore.Images.Media._ID)
                    val displayNameKey = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val bucketDisplayNameKey =
                        it.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    photosMap = HashMap()
                    it.moveToFirst()
                    while (!it.isAfterLast) {
                        val imageUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(idKey)
                        )
                        val bucketName = it.getString(bucketDisplayNameKey)
                        val displayName = it.getString(displayNameKey)
                        if (!bucketName.isNullOrEmpty()) {
                            var mediaPhotos: MutableList<MediaPhoto>?
                            if (photosMap.containsKey(bucketName)) {
                                mediaPhotos = photosMap[bucketName]
                                if (mediaPhotos == null) {
                                    mediaPhotos = ArrayList()
                                }
                            } else {
                                mediaPhotos = ArrayList()
                            }
                            if (!displayName.isNullOrEmpty()) {
                                mediaPhotos.add(
                                    MediaPhoto(
                                        imageUri,
                                        displayName,
                                        bucketName
                                    )
                                )
                            }
                            if (mediaPhotos.size > 0) {
                                photosMap[bucketName] = mediaPhotos
                            }
                        }
                        it.moveToNext()
                    }
                }
            }

            for ((key, value) in photosMap) {
                if (value.size > 0) {
                    bucketItems.add(
                        PhotoBucketItem(
                            key,
                            value.size.toString(),
                            value[0].imageUri
                        )
                    )
                }
            }
        }
    }

    private fun showBucketListState() {
        screenState.onNext(ScreenState.BUCKET_LIST)
        chosenItems.clear()
        setTitle(resources.getString(R.string.ecc_photos))
        binding.recycler.removeItemDecoration(galleryDecorator)
        binding.recycler.addItemDecoration(bucketsGalleryDecorator)
        binding.recycler.layoutManager = GridLayoutManager(this, 2)
        binding.recycler.adapter = PhotoBucketsGalleryAdapter(bucketItems, this)
        dataIsEmpty.onNext(bucketItems.isEmpty())
    }

    private fun showPhotoListState(title: String, item: PhotoBucketItem) {
        screenState.onNext(ScreenState.PHOTO_LIST)
        chosenItems.clear()
        syncSendButtonState()
        setTitle(title)
        binding.recycler.removeItemDecoration(bucketsGalleryDecorator)
        binding.recycler.addItemDecoration(galleryDecorator)
        binding.recycler.layoutManager = GridLayoutManager(this, 3)
        var photos: List<MediaPhoto>? = null
        for (list in photosMap.values) {
            if (list[0].imageUri == item.imagePath) {
                photos = list
                break
            }
        }
        photos?.forEach {
            it.isChecked = false
        }
        binding.recycler.adapter = GalleryAdapter(photos, this)
        dataIsEmpty.onNext(photos.isNullOrEmpty())
    }

    private fun showSearchState() {
        screenState.onNext(ScreenState.SEARCH)
        chosenItems.clear()
        syncSendButtonState()
        binding.searchEditText.requestFocus()
        binding.recycler.removeItemDecoration(bucketsGalleryDecorator)
        binding.recycler.addItemDecoration(galleryDecorator)
        binding.recycler.layoutManager = GridLayoutManager(this, 3)
        binding.recycler.adapter = null
        dataIsEmpty.onNext(true)
    }

    private fun search(searchString: String) {
        clearCheckedStateOfItems()
        chosenItems.clear()
        syncSendButtonState()
        val list: MutableList<MediaPhoto> = ArrayList()
        photosMap.values.forEach {
            it.forEach { photo ->
                if (photo.imageUri.toString().lowercase(Locale.getDefault()).contains(
                        searchString.lowercase(Locale.getDefault())
                    ) ||
                    photo.displayName.contains(searchString.lowercase(Locale.getDefault()))
                ) {
                    list.add(photo)
                }
            }
        }
        binding.recycler.adapter = GalleryAdapter(list, this)
        dataIsEmpty.onNext(list.isEmpty())
    }

    private fun clearCheckedStateOfItems() {
        photosMap.values.forEach {
            it.forEach { photo ->
                photo.isChecked = false
            }
        }
    }

    private fun syncSendButtonState() {
        if (chosenItems.size > 0) {
            binding.send.isEnabled = true
            binding.send.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            binding.send.isEnabled = false
            binding.send.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.ecc_disabled_text_color
                )
            )
        }
    }

    private fun subscribeToScreenState() {
        compositeDisposable.add(
            screenState
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it == ScreenState.SEARCH) {
                        binding.searchLayout.visible()
                    } else {
                        binding.searchLayout.gone()
                    }

                    if (it == ScreenState.BUCKET_LIST) {
                        binding.searchLabelLayout.visible()
                    } else {
                        binding.searchLabelLayout.gone()
                    }

                    checkBottomButtons()
                }, {
                    LoggerEdna.error(it)
                })
        )
    }

    private fun subscribeToDataIsEmpty() {
        compositeDisposable.add(
            dataIsEmpty
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isDataEmpty ->
                    if (isDataEmpty) {
                        binding.nothingFoundLabel.visible()
                    } else {
                        binding.nothingFoundLabel.gone()
                    }
                    checkBottomButtons()
                }, {
                    LoggerEdna.error(it)
                })
        )
    }

    private fun checkBottomButtons() {
        if (dataIsEmpty.value != true && screenState.value != ScreenState.BUCKET_LIST) {
            binding.bottomButtons.visible()
        } else {
            binding.bottomButtons.gone()
        }
    }

    enum class ScreenState {
        BUCKET_LIST, PHOTO_LIST, SEARCH
    }

    companion object {
        const val PHOTOS_TAG = "PHOTOS_TAG"
        private const val PHOTOS_REQUEST_CODE_TAG = "PHOTOS_REQUEST_CODE_TAG"

        @JvmStatic
        fun getStartIntent(ctx: Context?, requestCode: Int): Intent {
            val intent = Intent(ctx, GalleryActivity::class.java)
            intent.putExtra(PHOTOS_REQUEST_CODE_TAG, requestCode)
            return intent
        }
    }
}
