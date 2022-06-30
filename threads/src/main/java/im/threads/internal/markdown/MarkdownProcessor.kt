package im.threads.internal.markdown

import android.widget.TextView

interface MarkdownProcessor {
    fun parseClientMessage(textView: TextView, text: String)
    fun parseOperatorMessage(textView: TextView, text: String)

    companion object {
        val instance: MarkwonMarkdownProcessor by lazy {
            MarkwonMarkdownProcessor()
        }
    }
}
