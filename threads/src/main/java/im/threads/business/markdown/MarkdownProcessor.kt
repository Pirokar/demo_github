package im.threads.business.markdown

import android.text.Spanned

interface MarkdownProcessor {
    fun parseClientMessage(text: String): Spanned
    fun parseOperatorMessage(text: String): Spanned
}
