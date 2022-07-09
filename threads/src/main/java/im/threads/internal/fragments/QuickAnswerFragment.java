package im.threads.internal.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import im.threads.ChatStyle;
import im.threads.R;
import im.threads.internal.Config;
import im.threads.internal.activities.QuickAnswerActivity;
import im.threads.internal.chat_updates.ChatUpdateProcessor;
import im.threads.internal.image_loading.ImageLoader;
import im.threads.internal.image_loading.ImageModifications;
import im.threads.internal.model.InputFieldEnableModel;
import im.threads.internal.useractivity.LastUserActivityTimeCounter;
import im.threads.internal.useractivity.LastUserActivityTimeCounterSingletonProvider;
import im.threads.internal.utils.ColorsHelper;
import im.threads.internal.utils.FileUtils;
import im.threads.internal.utils.ThreadsLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;

public final class QuickAnswerFragment extends BaseDialogFragment {
    public static final String TAG = QuickAnswerFragment.class.getCanonicalName();
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
        initSendButton(style, v);
        v.findViewById(R.id.close_button).setOnClickListener(v1 -> {
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(new Intent(QuickAnswerActivity.ACTION_CANCEL));
            dismiss();
        });
        Bundle arguments = getArguments();
        if (null != arguments) {
            String avatarPath = arguments.getString("avatarPath");
            String consultName = arguments.getString("consultName");
            String consultPhrase = arguments.getString("consultPhrase");
            if (null != avatarPath && !avatarPath.equals("null")) {
                avatarPath = FileUtils.convertRelativeUrlToAbsolute(avatarPath);
                ImageLoader
                        .get()
                        .load(avatarPath)
                        .scales(ImageView.ScaleType.FIT_XY)
                        .modifications(ImageModifications.CircleCropModification.INSTANCE)
                        .into(imageView);
            }
            if (null != consultName && !consultName.equals("null"))
                consultNameTextView.setText(consultName);
            if (null != consultPhrase && !consultPhrase.equals("null"))
                textView.setText(consultPhrase);

        }
        v.findViewById(R.id.layout_root).setBackgroundColor(getColorInt(style.chatBackgroundColor));
        v.findViewById(R.id.header).setBackgroundColor(getColorInt(style.chatToolbarColorResId));

        consultNameTextView.setTextColor(getColorInt(style.chatToolbarTextColorResId));
        textView.setTextColor(getColorInt(style.notificationQuickReplyMessageTextColor));
        textView.setBackgroundColor(getColorInt(style.notificationQuickReplyMessageBackgroundColor));
        mEditText.setTextColor(getColorInt(style.incomingMessageTextColor));
        v.findViewById(R.id.answer_layout).setBackgroundColor(getColorInt(style.chatMessageInputColor));

        mEditText.setHintTextColor(getColorInt(style.chatMessageInputHintTextColor));
        mEditText.getLayoutParams().height = (int) requireContext().getResources().getDimension(style.inputHeight);
        mEditText.setBackground(AppCompatResources.getDrawable(requireContext(), style.inputBackground));
        initUserInputState();
        return v;
    }

    private void initSendButton(@NonNull ChatStyle style, @NonNull View view) {
        ImageButton sendButton = view.findViewById(R.id.send);
        sendButton.setImageResource(style.sendMessageIconResId);
        int iconTint = style.chatBodyIconsTint == 0
                ? style.inputIconTintResId : style.chatBodyIconsTint;
        ColorsHelper.setTint(requireContext(), sendButton, iconTint);

        sendButton.setOnClickListener(v12 -> {
            if (mEditText.getText().toString().trim().length() == 0) {
                return;
            }
            Intent intent = new Intent(QuickAnswerActivity.ACTION_ANSWER)
                    .putExtra(QuickAnswerActivity.ACTION_ANSWER, mEditText.getText().toString());
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
            mEditText.setText("");
            dismiss();
        });
    }

    @ColorInt
    private int getColorInt(@ColorRes int colorResId) {
        return ContextCompat.getColor(requireContext(), colorResId);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), getTheme()) {
            @Override
            public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
                LastUserActivityTimeCounter timeCounter =
                        LastUserActivityTimeCounterSingletonProvider.INSTANCE
                                .getLastUserActivityTimeCounter();
                if (MotionEvent.ACTION_DOWN == ev.getAction()) {
                    timeCounter.updateLastUserActivityTime();
                }
                return super.dispatchTouchEvent(ev);
            }
        };
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        dialog.getWindow().setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Config.instance.transport.setLifecycle(getLifecycle());
        Dialog d = getDialog();
        if (null != d) {
            int width = getResources().getDisplayMetrics().widthPixels;
            d.getWindow().setLayout(width, FrameLayout.LayoutParams.WRAP_CONTENT);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Config.instance.transport.setLifecycle(null);
    }

    private void initUserInputState() {
        subscribe(ChatUpdateProcessor.getInstance().getUserInputEnableProcessor()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateInputEnable,
                        error -> ThreadsLogger.e(TAG, "initUserInputState " + error.getMessage())
                ));
    }

    private void updateInputEnable(InputFieldEnableModel enableModel) {
        mEditText.setEnabled(enableModel.isEnabledInputField());
        if (!enableModel.isEnabledInputField()) {
            Toast.makeText(requireContext(), R.string.threads_message_sending_is_unavailable, Toast.LENGTH_LONG)
                    .show();
        }
    }
}
