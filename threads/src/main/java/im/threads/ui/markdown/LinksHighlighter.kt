package im.threads.ui.markdown

import android.widget.TextView

interface LinksHighlighter {
    /**
     * Подсвечивает ссылки, email, номера телефонов
     * @param textView вью, где должны быть подсвечены типы ссылок
     * @param isUnderlined если false, ссылки не будут подчеркнуты
     */
    fun highlightAllTypeOfLinks(textView: TextView, isUnderlined: Boolean)
}
