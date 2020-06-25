package im.threads.internal.model;

import androidx.annotation.Nullable;

public final class OutgoingUserMessage {
    @Nullable
    public final String text;
    @Nullable
    public final Quote quote;
    @Nullable
    public final FileDescription fileDescription;

    public OutgoingUserMessage(@Nullable FileDescription fileDescription, @Nullable Quote quote, @Nullable String text) {
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.text = text;
    }

    @Override
    public String toString() {
        return "OutgoingUserMessage{" +
                "text='" + text + '\'' +
                ", quote=" + quote +
                ", fileDescription=" + fileDescription +
                '}';
    }
}
