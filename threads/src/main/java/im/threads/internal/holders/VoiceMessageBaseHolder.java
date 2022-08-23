package im.threads.internal.holders;

import android.view.View;

import androidx.annotation.Nullable;

import im.threads.business.models.FileDescription;

public abstract class VoiceMessageBaseHolder extends BaseHolder {

    VoiceMessageBaseHolder(View itemView) {
        super(itemView);
    }

    @Nullable
    public abstract FileDescription getFileDescription();

    public abstract void init(int maxValue, int progress, boolean isPlaying);

    public abstract void updateProgress(int progress);

    public abstract void updateIsPlaying(boolean isPlaying);

    public abstract void resetProgress();
}
