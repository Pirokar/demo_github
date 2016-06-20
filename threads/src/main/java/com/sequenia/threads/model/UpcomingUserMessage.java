package com.sequenia.threads.model;

import java.util.List;

/**
 * Created by yuri on 10.06.2016.
 */
public class UpcomingUserMessage {
    private final String text;
    private final Quote quote;
    private final FileDescription fileDescription;
    private final List<String> attachments;

    public UpcomingUserMessage(String text, Quote quote, FileDescription fileDescription, List<String> attachments) {
        this.text = text;
        this.quote = quote;
        this.fileDescription = fileDescription;
        this.attachments = attachments;
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

    public List<String> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return "UpcomingUserMessage{" +
                "text='" + text + '\'' +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                ", attachments=" + attachments +
                '}';
    }
}
