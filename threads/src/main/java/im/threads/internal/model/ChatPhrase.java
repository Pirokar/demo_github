package im.threads.internal.model;

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
