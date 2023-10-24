package im.threads.ui.models

import android.net.Uri

internal class BottomGalleryItem(@JvmField var isChosen: Boolean, @JvmField var imagePath: Uri?) {
    var isSendAllowed = false
    override fun toString(): String {
        return "BottomGalleryItem{" +
            "isChosen=" + isChosen +
            ", imagePath='" + imagePath + '\'' +
            '}'
    }
}
