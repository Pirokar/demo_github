package com.sequenia.threads.model;

import java.util.List;

/**
 * Created by yuri on 10.06.2016.
 */
public class UpcomingUserMessage {
    private final String text;
    private final Quote quote;
    private final FileDescription fileDescription;
    private boolean copyied;

    public UpcomingUserMessage(FileDescription fileDescription, Quote quote, String text, boolean copyied) {
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.text = text;
        this.copyied = copyied;
    }

    public boolean isCopyied() {
        return copyied;
    }

    public String getText() {
        return text;

    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public Quote getQuote() {
        return quote;
    }

    @Override
    public String toString() {
        return "UpcomingUserMessage{" +
                "text='" + text + '\'' +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                ", copyied=" + copyied +
                '}';
    }
}
