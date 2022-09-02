package im.threads.ui.markdown

import android.text.Spanned

interface MarkdownProcessor {
    fun parseClientMessage(text: String): Spanned
    fun parseOperatorMessage(text: String): Spanned

    companion object {
        val instance: MarkwonMarkdownProcessor by lazy {
            MarkwonMarkdownProcessor()
        }
    }
}
