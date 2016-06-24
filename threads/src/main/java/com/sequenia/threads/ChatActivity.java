package com.sequenia.threads;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.model.SearchingConsult;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.views.BottomSheetView;
import com.sequenia.threads.views.SwipeAwareView;
import com.sequenia.threads.views.WelcomeScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class ChatActivity extends AppCompatActivity
        implements BottomSheetView.ButtonsListener, ChatAdapter.AdapterInterface {
    private static final String TAG = "ChatActivity ";
    private ChatController mChatController;
    private WelcomeScreen mWelcomeScreen;
    private EditText mInputEditText;
    private BottomSheetView mBottomSheetView;
    private ChatAdapter mChatAdapter;
    private TextView mTitleView;
    private TextView mSubTitleView;
    private boolean isConsultTyping = false;
    private boolean isSearchingConsult = false;
    private View mCopyControls;
    private Toolbar mToolbar;
    private QuoteLayoutHolder mQuoteLayoutHolder;
    private RecyclerView mRecyclerView;
    private Quote mQuote = null;
    private FileDescription mFileDescription = null;
    private ArrayList<String> mAttachments = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        if (null != t) setUpToolbar(t);
        initViews();
        if (null != getFragmentManager().findFragmentByTag(ChatController.TAG)) {
            mChatController = (ChatController) getFragmentManager().findFragmentByTag(ChatController.TAG);
        } else {
            mChatController = new ChatController();
            getFragmentManager().beginTransaction().add(android.R.id.content, mChatController).commit();
        }
        mChatController.bindActivity(this);
        if (!mChatController.isWelcomeScreenShowed()) {
            ViewGroup vg = (ViewGroup) findViewById(R.id.chat_content);
            vg.addView((mWelcomeScreen = new WelcomeScreen(this)));

        }
    }

    public static Intent getStartIntent(Context ctx) {
        Intent i = new Intent(ctx, ChatActivity.class);
        return i;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //initialy  sets title,click listeners on toolbar.
    private void setUpToolbar(@NonNull Toolbar t) {
        t.setTitle("");
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCopyControls.getVisibility() == View.VISIBLE) {
                    hideCopyControls();
                    return;
                }
                finish();
            }
        });
        t.showOverflowMenu();
        Drawable overflowDrawable = t.getOverflowIcon();
        try {
            overflowDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("all")
    private void initViews() {
        mQuoteLayoutHolder = new QuoteLayoutHolder();
        mBottomSheetView = (BottomSheetView) findViewById(R.id.file_input_sheet);
        mBottomSheetView.setButtonsListener(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        SwipeAwareView sav = (SwipeAwareView) findViewById(R.id.swipe_view);
        mInputEditText = (EditText) findViewById(R.id.input);
        ImageButton SendButton = (ImageButton) findViewById(R.id.send_message);
        final Context c = this;
        ImageButton AddAttachmentButton = (ImageButton) findViewById(R.id.add_attachment);
        AddAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBottomSheetView.getVisibility() == View.GONE) {
                    mBottomSheetView.setVisibility(View.VISIBLE);
                    mBottomSheetView.setAlpha(0.0f);
                    mBottomSheetView.animate().alpha(1.0f).setDuration(300).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mBottomSheetView.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mBottomSheetView.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
        final Context ctx = this;
        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputEditText.getText().length() == 0 && ((mQuote == null) && (mFileDescription == null)))
                    return;

                mChatController.onUserInput(new UpcomingUserMessage(mInputEditText.getText().toString(), mQuote, mFileDescription, mAttachments));
               /* PushController.getInstance(ctx).sendMessageAsync(mInputEditText.getText().toString(), false, new RequestCallback<Void, PushServerErrorException>() {
                    @Override
                    public void onResult(Void aVoid) {
                        Log.e(TAG, "onResult");
                    }

                    @Override
                    public void onError(PushServerErrorException e) {
                        Log.e(TAG, "onError " + e);
                    }
                });*/// TODO: 24.06.2016  
                mInputEditText.setText("");
                mQuoteLayoutHolder.setIsVisible(false);
                mQuote = null;
                mFileDescription = null;
                mAttachments = null;
            }
        });

        if (sav != null) {
            sav.setSwipeListener(new SwipeAwareView.SwipeListener() {
                @Override
                public void onRightSwipe() {
                    finish();
                }
            });
        }
        mTitleView = (TextView) findViewById(R.id.title);
        mSubTitleView = (TextView) findViewById(R.id.subtitle);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter((mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), this)));
        mChatAdapter.setAdapterInterface(this);
        mCopyControls = findViewById(R.id.copy_controls);
        mInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && (mWelcomeScreen != null)) {
                    mWelcomeScreen.removeViewWithAnimation(500, null);
                    mWelcomeScreen = null;
                }
            }
        });
    }

    @Override
    public void onCameraClick() {
        mFileDescription = new FileDescription(mChatController.getConsultName(), UUID.randomUUID().toString() + ".jpg", System.currentTimeMillis());
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
            }
        });
        mQuoteLayoutHolder.setText(mFileDescription.getHeader(), mFileDescription.getText());
        if (mAttachments == null) mAttachments = new ArrayList<>();
        mAttachments.add(UUID.randomUUID().toString());
    }

    @Override
    public void onGalleyClick() {
        mFileDescription = new FileDescription(mChatController.getConsultName(), UUID.randomUUID().toString() + ".jpg", System.currentTimeMillis());
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
            }
        });
        mQuoteLayoutHolder.setText(mFileDescription.getHeader(), mFileDescription.getText());
        if (mAttachments == null) mAttachments = new ArrayList<>();
        mAttachments.add(UUID.randomUUID().toString());
    }

    @Override
    public void onFileClick() {
        mFileDescription = new FileDescription(mChatController.getConsultName(), UUID.randomUUID().toString() + ".pdf", System.currentTimeMillis());
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
            }
        });
        mQuoteLayoutHolder.setText(mFileDescription.getHeader(), mFileDescription.getText());
        if (mAttachments == null) mAttachments = new ArrayList<>();
        mAttachments.add(UUID.randomUUID().toString());
    }

    @Override
    public void onSendClick() {
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
            }
        });
    }

    public void addMessage(ChatItem item) {
        if (item instanceof SearchingConsult) {
            mChatAdapter.addConsultSearching((SearchingConsult) item);
            isSearchingConsult = true;
        } else if (item instanceof ConsultTyping) {
            mChatAdapter.addConsultTyping((ConsultTyping) item);
            isConsultTyping = true;
        } else {
            if (isConsultTyping) {
                mChatAdapter.removeConsultIsTyping();
                isConsultTyping = false;
            }
            if (isSearchingConsult) {
                mChatAdapter.removeConsultSearching();
                isSearchingConsult = false;
            }
            mChatAdapter.addItem(item);
        }
        mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    public void addMessages(List<ChatItem> list) {
        mChatAdapter.addItems(list);
        mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    public void changeStateOfMessage(String messageId, MessageState state) {
        mChatAdapter.changeStateOfMessage(messageId, state);
    }

    public void setTitleStateDefault() {
        mSubTitleView.setVisibility(View.GONE);
        mTitleView.setVisibility(View.VISIBLE);
        mTitleView.setText(getResources().getString(R.string.contact_center));
    }

    public void setTitleStateSearching() {
        mSubTitleView.setVisibility(View.GONE);
        mTitleView.setVisibility(View.VISIBLE);
        mTitleView.setText(getResources().getString(R.string.searching_operator));
    }

    public void setTitleStateOperatorConnected(String title, String subtitle) {
        mSubTitleView.setVisibility(View.VISIBLE);
        mTitleView.setVisibility(View.VISIBLE);
        mTitleView.setText(title);
        mSubTitleView.setText(subtitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatController.unbindActivity();
    }

    @Override
    public void onFileClick(String path) {
        mChatController.onDownloadRequest(path);
    }

    @Override
    public void onPhraseLongClick(final ChatPhrase cp, final int position) {
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        mToolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
        mCopyControls.setVisibility(View.VISIBLE);
        mTitleView.setVisibility(View.GONE);
        mSubTitleView.setVisibility(View.GONE);
        ImageButton reply = (ImageButton) mCopyControls.findViewById(R.id.reply);
        ImageButton copy = (ImageButton) mCopyControls.findViewById(R.id.content_copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(new ClipData("", new String[]{"text/plain"}, new ClipData.Item(cp.getPhraseText())));
                hideCopyControls();
            }
        });
        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String headerText;
                if (cp instanceof UserPhrase) {
                    headerText = "Я";
                } else {
                    headerText = mChatController.getConsultName();
                }
                mQuoteLayoutHolder.setText(headerText, cp.getPhraseText());
                hideCopyControls();
                mRecyclerView.scrollToPosition(position);
                mQuote = new Quote(headerText, cp.getPhraseText(), cp.getTimeStamp());
            }
        });
    }

    private void hideCopyControls() {
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.green_light));
        mCopyControls.setVisibility(View.GONE);
        mTitleView.setVisibility(View.VISIBLE);
        mSubTitleView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (mCopyControls.getVisibility() == View.VISIBLE) {
            hideCopyControls();
            return;
        }
        super.onBackPressed();
    }

    public void changeChatPhraseStatus(String id, MessageState messageState) {
        mChatAdapter.changeStateOfMessage(id, messageState);
    }

    public void updateProgress(String path, int progress) {
        mChatAdapter.updateProgress(path, progress);
    }

    private class QuoteLayoutHolder {
        private View view;
        private TextView mHeader;
        private TextView mText;

        public QuoteLayoutHolder() {
            view = findViewById(R.id.quote_layout);
            mHeader = (TextView) view.findViewById(R.id.quote_header);
            mText = (TextView) view.findViewById(R.id.quote_text);
            View clear = view.findViewById(R.id.quote_clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHeader.setText("");
                    mText.setText("");
                    view.setVisibility(View.GONE);
                    mQuote = null;
                    mFileDescription = null;
                }
            });
        }

        public boolean isVisible() {
            return view.getVisibility() == View.VISIBLE;
        }

        public void setIsVisible(boolean isVisible) {
            if (isVisible) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }

        public void setText(String header, String text) {
            setIsVisible(true);
            mHeader.setText(header);
            mText.setText(text);
        }
    }

    @Override
    public void onUserPhraseClick(UserPhrase userPhrase, int position) {
        mChatController.checkAndResendPhrase(userPhrase);
    }
}
