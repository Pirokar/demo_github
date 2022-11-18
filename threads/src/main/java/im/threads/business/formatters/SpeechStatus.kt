package im.threads.business.formatters

enum class SpeechStatus {
    PROCESSING, SUCCESS, MAXSPEECH, UNKNOWN, NO_SPEECH_STATUS;

    companion object {
        fun fromString(name: String?): SpeechStatus {
            if (name != null) {
                return try {
                    valueOf(name.uppercase())
                } catch (ex: IllegalArgumentException) {
                    UNKNOWN
                }
            }
            return NO_SPEECH_STATUS
        }
    }
}
