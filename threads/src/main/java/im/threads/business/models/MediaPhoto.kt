package im.threads.business.models

import android.net.Uri

class MediaPhoto(val imageUri: Uri, val displayName: String, val bucketName: String) {
    var isChecked = false

    override fun toString(): String {
        return "MediaPhoto{" +
            "imagePath='" + imageUri + '\'' +
            ", bucketName='" + bucketName + '\'' +
            ", isChecked=" + isChecked +
            '}'
    }
}
