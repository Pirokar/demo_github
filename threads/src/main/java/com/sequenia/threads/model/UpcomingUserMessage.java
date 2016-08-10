package com.sequenia.threads.model;

import java.util.List;

/**
 * Created by yuri on 10.06.2016.
 */
public class UpcomingUserMessage {
    private String id;
    private final String text;
    private final Quote quote;
    private final FileDescription fileDescription;

    public UpcomingUserMessage(String text, Quote quote, FileDescription fileDescription) {
        this.text = text;
        this.quote = quote;
        this.fileDescription = fileDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                '}';
    }
}
