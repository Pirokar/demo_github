package im.threads.business.markdown

import android.widget.TextView

interface LinksHighlighter {
    /**
     * Подсвечивает ссылки, email, номера телефонов
     * @param textView вью, где должны быть подсвечены типы ссылок
     * @param url url, содержащийся в сообщении (если известен)
     * @param isUnderlined если false, ссылки не будут подчеркнуты
     */
    fun highlightAllTypeOfLinks(textView: TextView, url: String?, isUnderlined: Boolean)
}
