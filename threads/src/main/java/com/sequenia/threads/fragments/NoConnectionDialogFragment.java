package com.sequenia.threads.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sequenia.threads.R;
import com.sequenia.threads.model.ChatStyle;
import com.sequenia.threads.utils.PrefUtils;

import static com.sequenia.threads.model.ChatStyle.INVALID;

/**
 * Created by yuri on 28.06.2016.
 */
public class NoConnectionDialogFragment extends DialogFragment {
    private static final String TAG = "NoConnectionDialogFragment ";
    private Button mCloseButton;
    private OnCancelListener mOnCancelListener;

    public static NoConnectionDialogFragment getInstance(OnCancelListener listener) {
        NoConnectionDialogFragment d = new NoConnectionDialogFragment();
        d.setOnCancelListener(listener);
        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_no_connection, container, false);
        mCloseButton = (Button) v.findViewById(R.id.close);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    public void setOnCancelListener(OnCancelListener mOnCancelListener) {
        this.mOnCancelListener = mOnCancelListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(STYLE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    public interface OnCancelListener {
        void onCancel();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != mOnCancelListener) mOnCancelListener.onCancel();
    }
}
