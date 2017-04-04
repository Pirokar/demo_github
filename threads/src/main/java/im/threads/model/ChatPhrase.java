package im.threads.model;

/**
 * Created by yuri on 14.06.2016.
 */
public interface ChatPhrase extends ChatItem {
    String getId();

    String getPhraseText();

    Quote getQuote();

    FileDescription getFileDescription();

    boolean isHighlight();

    void setHighLighted(boolean isHighlight);

    boolean isFound();

    void setFound(boolean found);
}
