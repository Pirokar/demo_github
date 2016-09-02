package com.sequenia.threads.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sequenia.threads.R;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.CircleTransform;

/**
 * Created by yuri on 02.09.2016.
 */
public class QuickAnswerFragment extends DialogFragment {
    private static final String TAG = "QuickAnswerFragment ";
    private OnQuickAnswer mOnQuickAnswer;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fast_answer, container, false);
        TextView consultNameTextView = (TextView) v.findViewById(R.id.consult_name);
        TextView textView = (TextView) v.findViewById(R.id.question);
        final EditText editText = (EditText) v.findViewById(R.id.answer);
        ImageView imageView = (ImageView) v.findViewById(R.id.consult_image);
        ImageButton imageButton = (ImageButton) v.findViewById(R.id.send);
        mOnQuickAnswer = null;
        try {
            mOnQuickAnswer = (OnQuickAnswer) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "activity must implement OnQuickAnswer interface to catch result from this dialog");
        }
        v.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Bundle arguments = getArguments();
        if (null != arguments) {
            String avatarPath = arguments.getString("avatarPath");
            String consultName = arguments.getString("consultName");
            String consultPhrase = arguments.getString("consultPhrase");
            if (null != avatarPath && !avatarPath.equals("null")) {
                Picasso
                        .with(getActivity())
                        .load(avatarPath)
                        .fit()
                        .transform(new CircleTransform())
                        .into(imageView);

            }
            if (null != consultName && !consultName.equals("null"))
                consultNameTextView.setText(consultName);
            if (null != consultPhrase && !consultPhrase.equals("null"))
                textView.setText(consultPhrase);

        }
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnQuickAnswer) {
                    mOnQuickAnswer.onQuickAnswer(editText.getText().toString());
                }
                editText.setText("");
                dismiss();
            }
        });
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        d.getWindow().setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT);
        return d;
    }

    public interface OnQuickAnswer {
        void onQuickAnswer(String answer);
    }
}
