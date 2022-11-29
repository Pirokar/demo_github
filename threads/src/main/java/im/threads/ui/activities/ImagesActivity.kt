package im.threads.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
import im.threads.ui.fragments.PermissionDescriptionAlertFragment
import im.threads.ui.fragments.PermissionDescriptionAlertFragment.Companion.newInstance
import im.threads.ui.fragments.PermissionDescriptionAlertFragment.OnAllowPermissionClickListener
import im.threads.ui.permissions.PermissionsActivity
import im.threads.ui.styles.permissions.PermissionDescriptionType
import im.threads.business.utils.Balloon
import im.threads.ui.utils.ColorsHelper
import im.threads.ui.utils.ColorsHelper.setDrawableColor
import im.threads.ui.utils.invisible
import im.threads.ui.utils.runOnUiThread
import im.threads.ui.utils.visible
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.Collections

class ImagesActivity : BaseActivity(), OnPageChangeListener, OnAllowPermissionClickListener {
    private lateinit var mViewPager: ViewPager
    private var style = Config.getInstance().getChatStyle()
    private var collectionSize = 0
    private var files: ArrayList<FileDescription> = ArrayList()
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()
    private var permissionDescrAlertFragment: PermissionDescriptionAlertFragment? = null
    private val config: Config by lazy { Config.getInstance() }
    private lateinit var titleTextView: TextView
    private val database: DatabaseHolder by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        initToolbar(findViewById(R.id.toolbar), findViewById(R.id.toolbar_shadow))
        mViewPager = findViewById(R.id.pager)
        mViewPager.addOnPageChangeListener(this)
        compositeDisposable?.add(
            database.allFileDescriptions
                .doOnSuccess { data: List<FileDescription?>? ->
                    data?.forEach {
                        if (isImage(it)) {
                            files.add(it!!)
                        }
                    }
                    sortByTimeStamp(files)
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

    private fun sortByTimeStamp(items: List<FileDescription>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(items, Comparator.comparingLong(FileDescription::getTimeStamp))
        } else {
            Collections.sort(
                items
            ) { lhs: FileDescription, rhs: FileDescription ->
                lhs.timeStamp.compareTo(rhs.timeStamp)
            }
        }
    }

    private fun initToolbar(toolbar: Toolbar, toolbarShadow: View) {
        setSupportActionBar(toolbar)
        val statusBarColor = ContextCompat.getColor(this, style.chatStatusBarColorResId)
        val toolBarColor = ContextCompat.getColor(this, style.chatToolbarColorResId)
        val isStatusBarLight = resources.getBoolean(style.windowLightStatusBarResId)
        val backBtn = toolbar.findViewById<ImageButton>(R.id.back_button)
        titleTextView = toolbar.findViewById<TextView>(R.id.title)

        super.setStatusBarColor(isStatusBarLight, statusBarColor)
        toolbar.setBackgroundColor(toolBarColor)
        ColorsHelper.setTint(this, backBtn, config.getChatStyle().chatToolbarTextColorResId)
        if (resources.getBoolean(style.isChatTitleShadowVisible)) {
            toolbarShadow.visible()
        } else {
            toolbarShadow.invisible()
            toolbar.elevation = 0f
        }
        initToolbarTextPosition()
        setClickForBackBtn(backBtn)
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

    private fun initToolbarTextPosition() {
        val isToolbarTextCentered =
            im.threads.ui.config.Config.getInstance().getChatStyle().isToolbarTextCentered
        val gravity =
            if (isToolbarTextCentered) android.view.Gravity.CENTER else android.view.Gravity.CENTER_VERTICAL
        titleTextView.gravity = gravity
    }

    private fun setClickForBackBtn(backButton: ImageButton) {
        backButton.setOnClickListener {
            onBackPressed()
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
                            Balloon.show(
                                this,
                                getString(R.string.threads_saved_to_downloads)
                            )
                        }
                    ) { throwable: Throwable? ->
                        Balloon.show(this, getString(R.string.threads_unable_to_save))
                        error("downloadImage", throwable)
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
        if (permissionDescrAlertFragment == null) {
            permissionDescrAlertFragment = newInstance(
                PermissionDescriptionType.STORAGE,
                CODE_REQUEST_DOWNLOAD
            )
        }
        permissionDescrAlertFragment?.show(
            supportFragmentManager,
            PermissionDescriptionAlertFragment.TAG
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
        permissionDescrAlertFragment = null
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        Runnable {
            val title =
                "${mViewPager.currentItem + 1} ${getString(R.string.threads_from)} $collectionSize"
            setTitle(title, titleTextView)
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
