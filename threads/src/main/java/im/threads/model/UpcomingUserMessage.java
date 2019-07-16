package im.threads.model;

import androidx.annotation.Nullable;

/**
 * Created by yuri on 10.06.2016.
 */
public class UpcomingUserMessage {
    @Nullable
    public final String text;
    @Nullable
    public final Quote quote;
    @Nullable
    public final FileDescription fileDescription;
    public final boolean copyied;

    public UpcomingUserMessage(@Nullable FileDescription fileDescription, @Nullable Quote quote, @Nullable String text, boolean copyied) {
        this.fileDescription = fileDescription;
        this.quote = quote;
        this.text = text;
        this.copyied = copyied;
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
