package im.threads.internal.markdown

import android.text.Spanned

interface MarkdownProcessor {
    fun parseClientMessage(text: String): Spanned
    fun parseOperatorMessage(text: String): Spanned

    companion object {
        val instance: MarkdownProcessorImpl by lazy {
            MarkdownProcessorImpl(null, null)
        }
    }
}
