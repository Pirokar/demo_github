package im.threads.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import im.threads.R
import im.threads.business.logger.LoggerEdna.error
import im.threads.business.models.FileDescription
import im.threads.business.secureDatabase.DatabaseHolder
import im.threads.business.serviceLocator.core.inject
import im.threads.business.utils.FileUtils.isImage
import im.threads.business.utils.FileUtils.saveToDownloads
import im.threads.business.utils.ThreadsPermissionChecker
import im.threads.ui.adapters.ImagesAdapter
import im.threads.ui.config.Config
import im.threads.ui.fragments.PermissionDescriptionAlertDialogFragment
import im.threads.ui.fragments.PermissionDescriptionAlertDialogFragment.Companion.newInstance
import im.threads.ui.fragments.PermissionDescriptionAlertDialogFragment.OnAllowPermissionClickListener
import im.threads.ui.permissions.PermissionsActivity
import im.threads.ui.styles.permissions.PermissionDescriptionType
import im.threads.ui.utils.ColorsHelper.setDrawableColor
import im.threads.ui.utils.invisible
import im.threads.ui.utils.runOnUiThread
import im.threads.ui.utils.visible
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ImagesActivity : BaseActivity(), OnPageChangeListener, OnAllowPermissionClickListener {
    private lateinit var mViewPager: ViewPager
    private var style = Config.getInstance().getChatStyle()
    private var collectionSize = 0
    private var files: ArrayList<FileDescription> = ArrayList()
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private var permissionDescriptionAlertDialogFragment: PermissionDescriptionAlertDialogFragment? = null
    private val database: DatabaseHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        mViewPager = findViewById(R.id.pager)
        mViewPager.addOnPageChangeListener(this)
        initToolbar(findViewById(R.id.toolbar), findViewById(R.id.toolbar_shadow))
        compositeDisposable?.add(
            database.allFileDescriptions
                .doOnSuccess { data: List<FileDescription?>? ->
                    data?.forEach {
                        if (isImage(it)) {
                            files.add(it!!)
                        }
                    }
                    collectionSize = files.size
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        mViewPager.adapter = ImagesAdapter(files, supportFragmentManager)
                        val fd = intent.getParcelableExtra<FileDescription>("FileDescription")
                        if (fd != null) {
                            val page = files.indexOf(fd)
                            if (page != -1) {
                                mViewPager.currentItem = page
                                onPageSelected(page)
                            }
                        }
                        onPageSelected(0)
                    }
                ) { e: Throwable? ->
                    error("getAllFileDescriptions error: ", e)
                }
        )
    }

    private fun initToolbar(toolbar: Toolbar, toolbarShadow: View) {
        setSupportActionBar(toolbar)
        val statusBarColor = ContextCompat.getColor(this, style.chatStatusBarColorResId)
        val toolBarColor = ContextCompat.getColor(this, style.chatToolbarColorResId)
        val isStatusBarLight = resources.getBoolean(style.windowLightStatusBarResId)
        super.setStatusBarColor(isStatusBarLight, statusBarColor)
        val backButtonDrawable =
            AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        setDrawableColor(this, backButtonDrawable, style.chatToolbarTextColorResId)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(toolBarColor))
            setHomeAsUpIndicator(style.chatToolbarBackIconResId)
            setHomeAsUpIndicator(backButtonDrawable)
        }
        if (resources.getBoolean(style.isChatTitleShadowVisible)) {
            toolbarShadow.visible()
        } else {
            toolbarShadow.invisible()
            toolbar.elevation = 0f
        }
    }

    private fun setTitle(text: String) {
        val font = Typeface.createFromAsset(assets, style.defaultFontRegular)
        val typeface = Typeface.create(font, Typeface.NORMAL)
        val textColor = ContextCompat.getColor(this, style.chatToolbarTextColorResId)
        val fontSize = resources.getDimensionPixelSize(R.dimen.text_big)
        supportActionBar?.apply {
            val titleText = SpannableString(text)
            applyToolbarTextStyle(textColor, fontSize, typeface, titleText)
            title = titleText
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable?.dispose()
        compositeDisposable = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_gallery, menu)
        if (menu.size() > 0) {
            setDrawableColor(
                this,
                menu.getItem(0).icon,
                style.chatToolbarTextColorResId
            )
            menu.getItem(0).icon
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.download -> {
                downloadImage()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_REQUEST_DOWNLOAD && resultCode == PermissionsActivity.RESPONSE_GRANTED) {
            downloadImage()
        }
    }

    private fun downloadImage() {
        files[mViewPager.currentItem].fileUri?.let {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !ThreadsPermissionChecker.isWriteExternalPermissionGranted(this)
            ) {
                requestPermission()
                return
            }

            compositeDisposable?.add(
                Completable.fromAction {
                    saveToDownloads(files[mViewPager.currentItem])
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Toast.makeText(
                                this@ImagesActivity,
                                getString(R.string.threads_saved_to_downloads),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) { throwable: Throwable? ->
                        error("downloadImage", throwable)
                        Toast.makeText(
                            this@ImagesActivity,
                            R.string.threads_unable_to_save,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            )
        }
    }

    private fun requestPermission() {
        if (style.arePermissionDescriptionDialogsEnabled) {
            showStoragePermissionDescriptionDialog()
        } else {
            startStoragePermissionActivity(CODE_REQUEST_DOWNLOAD)
        }
    }

    private fun showStoragePermissionDescriptionDialog() {
        if (permissionDescriptionAlertDialogFragment == null) {
            permissionDescriptionAlertDialogFragment = newInstance(
                PermissionDescriptionType.STORAGE,
                CODE_REQUEST_DOWNLOAD
            )
        }
        permissionDescriptionAlertDialogFragment?.show(
            supportFragmentManager,
            PermissionDescriptionAlertDialogFragment.TAG
        )
    }

    override fun onAllowClick(type: PermissionDescriptionType, requestCode: Int) {
        if (PermissionDescriptionType.STORAGE == type) {
            startStoragePermissionActivity(requestCode)
        }
    }

    private fun startStoragePermissionActivity(requestCode: Int) {
        if (requestCode == CODE_REQUEST_DOWNLOAD) {
            PermissionsActivity.startActivityForResult(
                this,
                CODE_REQUEST_DOWNLOAD,
                R.string.threads_permissions_write_external_storage_help_text,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onDialogDetached() {
        permissionDescriptionAlertDialogFragment = null
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        Runnable {
            val title = "${mViewPager.currentItem + 1} ${getString(R.string.threads_from)} $collectionSize"
            setTitle(title)
        }.runOnUiThread()
    }

    override fun onPageScrollStateChanged(state: Int) {}

    companion object {
        private const val CODE_REQUEST_DOWNLOAD = 1

        @JvmStatic
        fun getStartIntent(context: Context?, fileDescription: FileDescription?): Intent {
            return Intent(context, ImagesActivity::class.java).putExtra(
                "FileDescription",
                fileDescription
            )
        }
    }
}
