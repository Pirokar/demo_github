package im.threads.ui.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.threads.R
import im.threads.ui.adapters.BottomGalleryAdapter
import im.threads.ui.adapters.BottomGalleryAdapter.OnChooseItemsListener
import im.threads.ui.models.BottomGalleryItem

internal class BottomGallery : FrameLayout {
    private var recyclerView: RecyclerView? = null

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
        recyclerView?.adapter = BottomGalleryAdapter(items, listener)
    }
}