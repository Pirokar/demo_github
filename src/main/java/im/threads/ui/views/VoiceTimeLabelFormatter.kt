package im.threads.ui.views

import com.google.android.material.slider.LabelFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class VoiceTimeLabelFormatter : LabelFormatter {
    override fun getFormattedValue(value: Float): String {
        return value.toLong().formatAsDuration()
    }
}

fun Long.formatAsDuration(): String {
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(this) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun Int.formatAsDuration(): String {
    return this.toLong().formatAsDuration()
}
