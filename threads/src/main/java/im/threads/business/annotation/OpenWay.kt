package im.threads.business.annotation

import androidx.annotation.IntDef

@IntDef(OpenWay.DEFAULT, OpenWay.FROM_PUSH)
@Retention(AnnotationRetention.SOURCE)
annotation class OpenWay {
    companion object {
        const val DEFAULT = 0
        const val FROM_PUSH = 1
    }
}
