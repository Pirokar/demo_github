package im.threads.internal.domain.audioConverter.model

import java.util.Locale

enum class AudioFormat {
    AAC, MP3, M4A, WMA, WAV, FLAC;

    val format: String get() = name.lowercase(Locale.getDefault())
}
