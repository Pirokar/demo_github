package im.threads.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import im.threads.R;
import im.threads.activities.TranslucentActivity;
import im.threads.model.ChatStyle;
import im.threads.picasso_url_connection_only.Picasso;
import im.threads.utils.CircleTransform;
import im.threads.utils.PrefUtils;

/**
 * Created by yuri on 02.09.2016.
 */
public class QuickAnswerFragment extends DialogFragment {
    private EditText mEditText;
    private static final String TAG = "QuickAnswerFragment ";
    private ChatStyle style;

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
        style = PrefUtils.getIncomingStyle(getActivity());
        View v = inflater.inflate(R.layout.dialog_fast_answer, container, false);
        TextView consultNameTextView = (TextView) v.findViewById(R.id.consult_name);
        TextView textView = (TextView) v.findViewById(R.id.question);
        mEditText = (EditText) v.findViewById(R.id.answer);
        ImageView imageView = (ImageView) v.findViewById(R.id.consult_image);
        ImageButton imageButton = (ImageButton) v.findViewById(R.id.send);
        v.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
                Intent answerIntent = new Intent(TranslucentActivity.ACTION_CANCEL);
                manager.sendBroadcast(answerIntent);
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
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
                Intent answerIntent = new Intent(TranslucentActivity.ACTION_ANSWER);
                answerIntent.putExtra(TranslucentActivity.ACTION_ANSWER, mEditText.getText().toString());
                manager.sendBroadcast(answerIntent);
                mEditText.setText("");
                dismiss();
            }
        });
        if (style!=null){
            if (style.chatBackgroundColor!= ChatStyle.INVALID){
                v.findViewById(R.id.layout_root).setBackgroundColor(getColorInt(style.chatBackgroundColor));
            }
            if (style.chatToolbarColorResId!= ChatStyle.INVALID){
                v.findViewById(R.id.header).setBackgroundColor(getColorInt(style.chatToolbarColorResId));
            }
            if (style.chatToolbarTextColorResId!= ChatStyle.INVALID){
                consultNameTextView.setTextColor(getColorInt(style.chatToolbarTextColorResId));
            }
            if (style.incomingMessageTextColor!= ChatStyle.INVALID){
                ((TextView) v.findViewById(R.id.question)).setTextColor(getColorInt(style.incomingMessageTextColor));
                ((TextView) v.findViewById(R.id.close_button)).setTextColor(getColorInt(style.incomingMessageTextColor));
                mEditText.setTextColor(getColorInt(style.incomingMessageTextColor));
            }
            if (style.chatMessageInputColor!= ChatStyle.INVALID){
                v.findViewById(R.id.answer_layout).setBackgroundColor(getColorInt(style.chatMessageInputColor));
            }
            if (style.chatBodyIconsTint!= ChatStyle.INVALID){
                Drawable d =imageButton.getDrawable();
                d.setColorFilter(getColorInt(style.chatBodyIconsTint), PorterDuff.Mode.SRC_ATOP);
                imageButton.setImageDrawable(d);
            }
            if (style.chatMessageInputHintTextColor!= ChatStyle.INVALID){
                mEditText.setHintTextColor(getColorInt(style.chatMessageInputHintTextColor));
            }
        }
        return v;
    }
    private @ColorInt int  getColorInt(@ColorRes int colorResId){
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
            int width = (int) (getResources().getDisplayMetrics().widthPixels);
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
