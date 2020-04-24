package im.threads.internal.utils;

import com.yydcdut.markdown.MarkdownProcessor;

public enum MarkdownParser {

    INSTANCE;

    public CharSequence parse(String markdownStr, MarkdownProcessor markdownProcessor) {
        return markdownProcessor.parse(markdownStr);
    }

}
