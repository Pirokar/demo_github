package im.threads.business.models

import android.net.Uri

class PhotoBucketItem(val bucketName: String, val bucketSize: String, val imagePath: Uri) {
    override fun toString(): String {
        return "PhotoBucketItem{" +
            "bucketName='" + bucketName + '\'' +
            ", bucketSize='" + bucketSize + '\'' +
            ", imagePath='" + imagePath + '\'' +
            '}'
    }
}
