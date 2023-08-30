package im.threads.ui.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.business.config.BaseConfig
import im.threads.business.utils.Balloon
import im.threads.business.utils.FileUtils
import im.threads.ui.adapters.BottomGalleryAdapter
import im.threads.ui.adapters.BottomGalleryAdapter.OnChooseItemsListener
import im.threads.ui.models.BottomGalleryItem
import im.threads.ui.utils.FileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class BottomGallery : FrameLayout {
    private var recyclerView: RecyclerView? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.ecc_view_bottom_gallery, this, true)
        recyclerView = findViewById(R.id.bottom_gallery_recycler)
        recyclerView?.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    fun setImages(images: List<Uri?>, listener: OnChooseItemsListener?) {
        val items: MutableList<BottomGalleryItem> = ArrayList()
        for (str in images) {
            val item = BottomGalleryItem(false, str)
            items.add(item)
        }
        coroutineScope.launch {
            items.forEach { it.isSendAllowed = isSendingAllowed(it) }
            withContext(Dispatchers.Main) {
                recyclerView?.adapter = BottomGalleryAdapter(items, listener)
            }
        }
    }

    private fun isSendingAllowed(item: BottomGalleryItem): Boolean {
        val uri = item.imagePath
        return if (uri != null) {
            if (FileHelper.isAllowedFileExtension(
                    FileUtils.getExtensionFromMediaStore(BaseConfig.getInstance().context, uri)
                )
            ) {
                if (FileHelper.isAllowedFileSize(
                        FileUtils.getFileSizeFromMediaStore(BaseConfig.getInstance().context, uri)
                    )
                ) {
                    true
                } else {
                    // Недопустимый размер файла
                    Balloon.show(
                        context,
                        context.getString(
                            R.string.ecc_not_allowed_file_size,
                            FileHelper.maxAllowedFileSize
                        )
                    )
                    false
                }
            } else {
                // Недопустимое расширение файла
                Balloon.show(context, context.getString(R.string.ecc_not_allowed_file_extension))
                false
            }
        } else {
            false
        }
    }
}
