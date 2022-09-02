package im.threads.internal.holders;

import android.view.View;

import androidx.annotation.Nullable;

import im.threads.business.models.ChatItem;
import im.threads.business.models.FileDescription;
import io.reactivex.subjects.PublishSubject;

public abstract class VoiceMessageBaseHolder extends BaseHolder {

    VoiceMessageBaseHolder(View itemView, PublishSubject<ChatItem> highlightStream) {
        super(itemView, highlightStream);
    }

    @Nullable
    public abstract FileDescription getFileDescription();

    public abstract void init(int maxValue, int progress, boolean isPlaying);

    public abstract void updateProgress(int progress);

    public abstract void updateIsPlaying(boolean isPlaying);

    public abstract void resetProgress();
}
