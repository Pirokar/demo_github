package im.threads.internal.holders;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.model.FileDescription;
import im.threads.internal.model.MessageState;
import im.threads.internal.views.VoiceTimeLabelFormatter;
import im.threads.internal.views.VoiceTimeLabelFormatterKt;

public final class UserVoiceMessageViewHolder extends VoiceMessageBaseHolder {

    private final Slider slider;
    private final ImageView buttonPlayPause;
    private final TextView fileSizeTextView;
    private final TextView mTimeStampTextView;
    private final View mFilterView;
    private final View mFilterSecond;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private FileDescription fileDescription = null;
    @NonNull
    private String formattedDuration = "";
    private final ChatStyle style;

    public UserVoiceMessageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_voice_message, parent, false));
        buttonPlayPause = itemView.findViewById(R.id.voice_message_user_button_play_pause);
        slider = itemView.findViewById(R.id.voice_message_user_slider);
        fileSizeTextView = itemView.findViewById(R.id.file_size);
        mTimeStampTextView = itemView.findViewById(R.id.timestamp);
        mFilterView = itemView.findViewById(R.id.filter);
        mFilterSecond = itemView.findViewById(R.id.filter_second);
        View mBubble = itemView.findViewById(R.id.bubble);
        style = Config.instance.getChatStyle();
        setTextColorToViews(new TextView[]{fileSizeTextView}, style.outgoingMessageTextColor);
        mTimeStampTextView.setTextColor(getColorInt(style.outgoingMessageTimeColor));
        mBubble.setBackground(AppCompatResources.getDrawable(itemView.getContext(), style.outgoingMessageBubbleBackground));
        mBubble.getBackground().setColorFilter(getColorInt(style.outgoingMessageBubbleColor), PorterDuff.Mode.SRC_ATOP);
        mFilterView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        mFilterSecond.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), style.chatHighlightingColor));
        buttonPlayPause.setColorFilter(getColorInt(style.outgoingPlayPauseButtonColor), PorterDuff.Mode.SRC_ATOP);
    }

    public void onBind(
            long timeStamp,
            String formattedDuration,
            FileDescription fileDescription,
            View.OnClickListener buttonClickListener,
            View.OnClickListener rowClickListener,
            View.OnLongClickListener onLongClick,
            Slider.OnChangeListener onChangeListener,
            Slider.OnSliderTouchListener onSliderTouchListener,
            boolean isFilterVisible,
            MessageState sentState) {
        this.fileDescription = fileDescription;
        if (fileDescription == null) return;
        this.formattedDuration = formattedDuration;
        fileSizeTextView.setText(formattedDuration);
        mTimeStampTextView.setText(sdf.format(new Date(timeStamp)));
        ViewGroup vg = (ViewGroup) itemView;
        for (int i = 0; i < vg.getChildCount(); i++) {
            vg.getChildAt(i).setOnLongClickListener(onLongClick);
            vg.getChildAt(i).setOnClickListener(rowClickListener);
        }
        buttonPlayPause.setOnClickListener(buttonClickListener);
        if (isFilterVisible) {
            mFilterView.setVisibility(View.VISIBLE);
            mFilterSecond.setVisibility(View.VISIBLE);
        } else {
            mFilterView.setVisibility(View.INVISIBLE);
            mFilterSecond.setVisibility(View.INVISIBLE);
        }
        Drawable d;
        switch (sentState) {
            case STATE_WAS_READ:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_received);
                if (d != null) {
                    d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_received_icon), PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_sent);
                if (d != null) {
                    d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_sent_icon), PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_NOT_SENT:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.threads_message_waiting);
                if (d != null) {
                    d.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.threads_outgoing_message_not_send_icon), PorterDuff.Mode.SRC_ATOP);
                }
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
            case STATE_SENDING:
                d = AppCompatResources.getDrawable(itemView.getContext(), R.drawable.empty_space_24dp);
                mTimeStampTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                break;
        }
        slider.addOnChangeListener(onChangeListener);
        slider.addOnSliderTouchListener(onSliderTouchListener);
        slider.setLabelFormatter(new VoiceTimeLabelFormatter());
    }

    @Nullable
    @Override
    public FileDescription getFileDescription() {
        return fileDescription;
    }

    @Override
    public void init(int maxValue, int progress, boolean isPlaying) {
        int effectiveProgress = Math.min(progress, maxValue);
        fileSizeTextView.setText(VoiceTimeLabelFormatterKt.formatAsDuration(effectiveProgress));
        slider.setEnabled(true);
        slider.setValueTo(maxValue);
        slider.setValue(effectiveProgress);
        buttonPlayPause.setImageResource(isPlaying ? style.voiceMessagePauseButton : style.voiceMessagePlayButton);
    }

    @Override
    public void updateProgress(int progress) {
        fileSizeTextView.setText(VoiceTimeLabelFormatterKt.formatAsDuration(progress));
        slider.setValue(Math.min(progress, slider.getValueTo()));
    }

    @Override
    public void updateIsPlaying(boolean isPlaying) {
        buttonPlayPause.setImageResource(isPlaying ? style.voiceMessagePauseButton : style.voiceMessagePlayButton);
    }

    @Override
    public void resetProgress() {
        fileSizeTextView.setText(formattedDuration);
        slider.setEnabled(false);
        slider.setValue(0);
        buttonPlayPause.setImageResource(style.voiceMessagePlayButton);
    }
}
