package im.threads.internal.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.squareup.picasso.Picasso;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.activities.QuickAnswerActivity;
import im.threads.internal.utils.CircleTransformation;
import im.threads.internal.utils.FileUtils;

public final class QuickAnswerFragment extends DialogFragment {
    private EditText mEditText;

    public static QuickAnswerFragment getInstance(
            String avatarPath,
            String consultName,
            String consultPhrase) {
        QuickAnswerFragment frag = new QuickAnswerFragment();
        Bundle b = new Bundle();
        b.putString("avatarPath", avatarPath);
        b.putString("consultName", consultName);
        b.putString("consultPhrase", consultPhrase);
        frag.setArguments(b);
        return frag;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ChatStyle style = Config.instance.getChatStyle();
        View v = inflater.inflate(R.layout.dialog_fast_answer, container, false);
        TextView consultNameTextView = v.findViewById(R.id.consult_name);
        TextView textView = v.findViewById(R.id.question);
        mEditText = v.findViewById(R.id.answer);
        ImageView imageView = v.findViewById(R.id.consult_image);
        ImageButton imageButton = v.findViewById(R.id.send);
        v.findViewById(R.id.close_button).setOnClickListener(v1 -> {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            Intent answerIntent = new Intent(QuickAnswerActivity.ACTION_CANCEL);
            manager.sendBroadcast(answerIntent);
            dismiss();
        });
        Bundle arguments = getArguments();
        if (null != arguments) {
            String avatarPath = arguments.getString("avatarPath");
            String consultName = arguments.getString("consultName");
            String consultPhrase = arguments.getString("consultPhrase");
            if (null != avatarPath && !avatarPath.equals("null")) {
                avatarPath = FileUtils.convertRelativeUrlToAbsolute(avatarPath);
                Picasso.get()
                        .load(avatarPath)
                        .fit()
                        .transform(new CircleTransformation())
                        .into(imageView);
            }
            if (null != consultName && !consultName.equals("null"))
                consultNameTextView.setText(consultName);
            if (null != consultPhrase && !consultPhrase.equals("null"))
                textView.setText(consultPhrase);

        }
        imageButton.setOnClickListener(v12 -> {
            if (mEditText.getText().toString().trim().length() == 0) {
                return;
            }
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            Intent answerIntent = new Intent(QuickAnswerActivity.ACTION_ANSWER);
            answerIntent.putExtra(QuickAnswerActivity.ACTION_ANSWER, mEditText.getText().toString());
            manager.sendBroadcast(answerIntent);
            mEditText.setText("");
            dismiss();
        });
        v.findViewById(R.id.layout_root).setBackgroundColor(getColorInt(style.chatBackgroundColor));
        v.findViewById(R.id.header).setBackgroundColor(getColorInt(style.chatToolbarColorResId));

        consultNameTextView.setTextColor(getColorInt(style.chatToolbarTextColorResId));
        textView.setTextColor(getColorInt(style.notificationQuickReplyMessageTextColor));
        textView.setBackgroundColor(getColorInt(style.notificationQuickReplyMessageBackgroundColor));
        mEditText.setTextColor(getColorInt(style.incomingMessageTextColor));
        v.findViewById(R.id.answer_layout).setBackgroundColor(getColorInt(style.chatMessageInputColor));

        Drawable d = imageButton.getDrawable();
        d.setColorFilter(getColorInt(style.chatBodyIconsTint), PorterDuff.Mode.SRC_ATOP);
        imageButton.setImageDrawable(d);

        mEditText.setHintTextColor(getColorInt(style.chatMessageInputHintTextColor));
        mEditText.getLayoutParams().height = (int) getActivity().getResources().getDimension(style.inputHeight);
        mEditText.setBackground(AppCompatResources.getDrawable(getActivity(), style.inputBackground));
        return v;
    }

    @ColorInt
    private int getColorInt(@ColorRes int colorResId) {
        return ContextCompat.getColor(getActivity(), colorResId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        d.getWindow().setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT);
        d.setCancelable(false);
        return d;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (null != d) {
            int width = getResources().getDisplayMetrics().widthPixels;
            d.getWindow().setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT);
            d.setCancelable(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mEditText) {
            mEditText.requestFocus();
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
