package com.sequenia.threads.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sequenia.threads.R;
import com.sequenia.threads.adapters.BottomGalleryAdapter;
import com.sequenia.threads.adapters.ChatAdapter;
import com.sequenia.threads.controllers.ChatController;
import com.sequenia.threads.fragments.NoConnectionDialogFragment;
import com.sequenia.threads.model.ChatItem;
import com.sequenia.threads.model.ChatPhrase;
import com.sequenia.threads.model.ConsultPhrase;
import com.sequenia.threads.model.ConsultTyping;
import com.sequenia.threads.model.FileDescription;
import com.sequenia.threads.model.MessageState;
import com.sequenia.threads.model.Quote;
import com.sequenia.threads.model.SearchingConsult;
import com.sequenia.threads.model.UpcomingUserMessage;
import com.sequenia.threads.model.UserPhrase;
import com.sequenia.threads.picasso_url_connection_only.Picasso;
import com.sequenia.threads.utils.PermissionChecker;
import com.sequenia.threads.views.BottomGallery;
import com.sequenia.threads.views.BottomSheetView;
import com.sequenia.threads.views.SwipeAwareView;
import com.sequenia.threads.views.WelcomeScreen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.text.TextUtils.isEmpty;

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
    private BottomGallery mBottomGallery;
    private ChatAdapter mChatAdapter;
    private TextView mConsultNameView;
    private TextView mConsultTitle;
    private boolean isConsultTyping = false;
    private boolean isSearchingConsult = false;
    private View mCopyControls;
    private Toolbar mToolbar;
    private QuoteLayoutHolder mQuoteLayoutHolder;
    private RecyclerView mRecyclerView;
    private Quote mQuote = null;
    private FileDescription mFileDescription = null;
    private ChatPhrase mChosenPhrase = null;
    private List<String> mAttachedImages = new ArrayList<>();
    private AppCompatEditText mSearchMessageEditText;
    public static final int REQUEST_CODE_PHOTOS = 100;
    public static final int REQUEST_CODE_PHOTO = 101;
    public static final int REQUEST_PERMISSION_GALLERY = 102;
    public static final int REQUEST_PERMISSION_CAMERA = 103;
    public String connectedConsultId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        if (null != t) initToolbar(t);
        initViews();
        if (null != getFragmentManager().findFragmentByTag(ChatController.TAG)) {//mb, someday, we will support orientation change
            mChatController = (ChatController) getFragmentManager().findFragmentByTag(ChatController.TAG);
        } else {
            mChatController = ChatController.getInstance(this);
            getFragmentManager().beginTransaction().add(mChatController, ChatController.TAG).commit();
        }
        mChatController.bindActivity(this);
        mWelcomeScreen = (WelcomeScreen) findViewById(R.id.welcome);
        if (!mChatController.isNeedToShowWelcome() && mWelcomeScreen != null) {
            mWelcomeScreen.removeViewWithAnimation(0, null);
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
    private void initToolbar(@NonNull Toolbar t) {
        t.setTitle("");
        setSupportActionBar(t);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        mSearchMessageEditText = (AppCompatEditText) findViewById(R.id.search);
        mBottomGallery = (BottomGallery) findViewById(R.id.bottom_gallery);
        SwipeAwareView sav = (SwipeAwareView) findViewById(R.id.swipe_view);
        mInputEditText = (EditText) findViewById(R.id.input);
        ImageButton SendButton = (ImageButton) findViewById(R.id.send_message);
        final Context c = this;
        ImageButton AddAttachmentButton = (ImageButton) findViewById(R.id.add_attachment);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), this, this);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setAdapter(mChatAdapter);
        final View inputLayout = findViewById(R.id.input_layout);
        AddAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitleStateCurrentOperatorConnected();
                if (mBottomSheetView.getVisibility() == View.GONE) {
                    mBottomSheetView.setVisibility(View.VISIBLE);
                    mBottomSheetView.setAlpha(0.0f);
                    mBottomSheetView.animate().alpha(1.0f).setDuration(300).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mBottomSheetView.setVisibility(View.VISIBLE);
                        }
                    });
                    inputLayout.setVisibility(View.GONE);
                    mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
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
                if (mWelcomeScreen != null) {
                    mWelcomeScreen.removeViewWithAnimation(500, null);
                    mWelcomeScreen = null;
                }
                ;
                unChooseItem(mChosenPhrase);
                UpcomingUserMessage uum = new UpcomingUserMessage(mInputEditText.getText().toString().trim(), mQuote, mFileDescription);
                mChatController.onUserInput(uum);
                mInputEditText.setText("");
                mQuoteLayoutHolder.setIsVisible(false);
                mQuote = null;
                mFileDescription = null;
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
        mConsultNameView = (TextView) findViewById(R.id.consult_name);
        mConsultTitle = (TextView) findViewById(R.id.subtitle);

        mConsultNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatController.isConsultFound())
                    onConsultAvatarClick(connectedConsultId);
            }
        });

        mConsultTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatController.isConsultFound())
                    onConsultAvatarClick(connectedConsultId);
            }
        });

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
        boolean isCameraGranted = PermissionChecker.isCameraPermissionGranted(this);
        boolean isWriteGranted = PermissionChecker.isWriteExternalPermissionGranted(this);
        if (isCameraGranted && isWriteGranted) {
            setBottomStateDefault();
            mBottomGallery.setVisibility(View.GONE);
            startActivityForResult(new Intent(this, CameraActivity.class), REQUEST_CODE_PHOTO);
        } else {
            ArrayList<String> permissions = new ArrayList<>();
            if (!isCameraGranted) permissions.add(android.Manifest.permission.CAMERA);
            if (!isWriteGranted)
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[]{}), REQUEST_PERMISSION_CAMERA);
        }
    }

    @Override
    public void onGalleryClick() {
        if (PermissionChecker.isReadExternalPermissionGranted(this)) {
            if (mBottomGallery.getVisibility() == View.VISIBLE) {
                mBottomGallery.setVisibility(View.GONE);
                mAttachedImages.clear();
            } else {
                mQuoteLayoutHolder.setIsVisible(false);
                startActivityForResult(GalleryActivity.getStartIntent(this, REQUEST_CODE_PHOTOS), REQUEST_CODE_PHOTOS);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTOS && resultCode == RESULT_OK) {
            if (data.getStringArrayListExtra(GalleryActivity.PHOTOS_TAG) != null && data.getStringArrayListExtra(GalleryActivity.PHOTOS_TAG).size() > 0) {
                mBottomGallery.setVisibility(View.VISIBLE);
                mBottomGallery.setAlpha(0.0f);
                mBottomGallery.animate().alpha(1.0f).setDuration(200).start();
                ArrayList<String> paths = data.getStringArrayListExtra(GalleryActivity.PHOTOS_TAG);
                for (int i = 0; i < paths.size(); i++) {
                    paths.set(i, "file://" + paths.get(i));
                }
                mBottomGallery.setImages(paths, new BottomGalleryAdapter.OnChooseItemsListener() {
                    @Override
                    public void onChosenItems(List<String> items) {
                        mAttachedImages = new ArrayList<>(items);
                        if (mAttachedImages.size() > 0) {
                            mBottomSheetView.setSelectedState(true);
                        } else {
                            mBottomSheetView.setSelectedState(false);
                        }
                    }
                });
                mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
            }
        } else if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            mFileDescription = new FileDescription(getResources().getString(R.string.image), data.getStringExtra(CameraActivity.IMAGE_EXTRA), new File(data.getStringExtra(CameraActivity.IMAGE_EXTRA).replace("file://", "")).length(), System.currentTimeMillis());
            mQuoteLayoutHolder.setText(mChatController.getCurrentConsultName().split("%%")[0], getResources().getString(R.string.image), "file://" + data.getStringExtra(CameraActivity.IMAGE_EXTRA));
            mQuote = null;
        }
    }

    @Override
    public void onFileClick() {
        setBottomStateDefault();
        mFileDescription = new FileDescription("someFile.pdf", UUID.randomUUID().toString() + ".pdf", 100500, System.currentTimeMillis());
        mQuoteLayoutHolder.setText(mChatController.getCurrentConsultName().split("%%")[0], getResources().getString(R.string.file_pdf), null);
        mQuote = null;
    }

    @Override
    public void onHideClick() {
        final View input = findViewById(R.id.input_layout);
        mBottomGallery.setVisibility(View.GONE);
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
                input.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onSendClick() {
        setTitleStateCurrentOperatorConnected();
        if (mAttachedImages == null || mAttachedImages.size() == 0) {
            mBottomGallery.setVisibility(View.GONE);
        } else {
            UpcomingUserMessage uum = new UpcomingUserMessage(mInputEditText.getText().toString().trim(), mQuote, new FileDescription(getString(R.string.I), mAttachedImages.get(0), new File(mAttachedImages.get(0).replaceAll("file://", "")).length(), System.currentTimeMillis()));
            mChatController.onUserInput(uum);
            for (int i = 1; i < mAttachedImages.size(); i++) {
                uum = new UpcomingUserMessage(null, null, new FileDescription(getString(R.string.I), mAttachedImages.get(i), new File(mAttachedImages.get(i).replaceAll("file://", "")).length(), System.currentTimeMillis()));
                mChatController.onUserInput(uum);
            }
        }
        mBottomSheetView.setSelectedState(false);
        mInputEditText.setText("");
        mQuoteLayoutHolder.setIsVisible(false);
        mQuote = null;
        mFileDescription = null;
        setBottomStateDefault();
        hideCopyControls();
        mAttachedImages.clear();
        mBottomGallery.setVisibility(View.GONE);
    }

    private void setBottomStateDefault() {
        final View input = findViewById(R.id.input_layout);
        mBottomSheetView.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                mBottomSheetView.setVisibility(View.GONE);
                input.setVisibility(View.VISIBLE);
            }
        });
        mSearchMessageEditText.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        mBottomGallery.setVisibility(View.GONE);
    }

    public void addMessage(ChatItem item) {
        if (null != mWelcomeScreen) {
            mWelcomeScreen.removeViewWithAnimation(30, null);
            mWelcomeScreen = null;
        }
        if (item instanceof SearchingConsult) {
            mChatAdapter.addConsultSearching((SearchingConsult) item);
            isSearchingConsult = true;
        } else if (item instanceof ConsultTyping) {
            mChatAdapter.removeConsultIsTyping();
            mChatAdapter.addConsultTyping((ConsultTyping) item);
            isConsultTyping = true;
        } else {
            if (mChatAdapter.isConsultTyping()) {
                mChatAdapter.removeConsultIsTyping();
                isConsultTyping = false;
            }
            if (isSearchingConsult) {
                mChatAdapter.removeConsultSearching();
                isSearchingConsult = false;
            }

            mChatAdapter.addItem(item, false);
        }
        mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    public void addMessages(List<ChatItem> list) {
        if (mWelcomeScreen != null) {
            mWelcomeScreen.removeViewWithAnimation(300, null);
            mWelcomeScreen = null;
        }
        mChatAdapter.addItems(list);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }, 300);
    }

    public void changeStateOfMessage(String messageId, MessageState state) {
        mChatAdapter.changeStateOfMessage(messageId, state);
    }

    public void setTitleStateDefault() {
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.VISIBLE);
        mSearchMessageEditText.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        mConsultNameView.setText(getResources().getString(R.string.contact_center));
    }

    public void setTitleStateSearchingConsult() {
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.VISIBLE);
        mSearchMessageEditText.setVisibility(View.GONE);
        mSearchMessageEditText.setText("");
        mConsultNameView.setText(getResources().getString(R.string.searching_operator));
    }

    public void setTitleStateSearchingMessage() {
        mConsultTitle.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.GONE);
        mSearchMessageEditText.setVisibility(View.VISIBLE);
        mSearchMessageEditText.setText("");
        mSearchMessageEditText.requestFocus();
    }


    public void setTitleStateOperatorConnected(String connectedConsultId, String ConsultName, String consultTitle) {
        mConsultTitle.setVisibility(View.VISIBLE);
        mConsultNameView.setVisibility(View.VISIBLE);
        mConsultNameView.setText(ConsultName);
        mConsultTitle.setText(consultTitle);
        this.connectedConsultId = connectedConsultId;
    }

    public void setTitleStateCurrentOperatorConnected() {
        if (mChatController.isConsultFound()) {
            mConsultTitle.setVisibility(View.VISIBLE);
            mConsultNameView.setVisibility(View.VISIBLE);
            mSearchMessageEditText.setVisibility(View.GONE);
            mSearchMessageEditText.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatController.unbindActivity();
    }

    @Override
    public void onFileClick(FileDescription filedescription) {
        mChatController.onDownloadRequest(filedescription);
    }

    @Override
    public void onPhraseLongClick(final ChatPhrase cp, final int position) {
        if (cp == mChosenPhrase) {
            unChooseItem(cp);
            return;
        }
        unChooseItem(cp);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        mToolbar.setBackgroundColor(getResources().getColor(android.R.color.white));
        mCopyControls.setVisibility(View.VISIBLE);
        mConsultNameView.setVisibility(View.GONE);
        mConsultTitle.setVisibility(View.GONE);
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
                String headerText = "";
                if (cp instanceof UserPhrase) {
                    headerText = getString(R.string.I);

                } else if (cp instanceof ConsultPhrase) {
                    headerText = mChatController.getConsultNameById(((ConsultPhrase) cp).getConsultId());
                    if (headerText == null) {
                        headerText = getString(R.string.consult);
                    }
                }
                if (isEmpty(cp.getPhraseText())) {
                    mQuote = new Quote(headerText, cp.getPhraseText(), null, System.currentTimeMillis());
                }
                String text = cp.getPhraseText();
                mQuoteLayoutHolder.setText(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? "" : text, null);
                hideCopyControls();
                mRecyclerView.scrollToPosition(position);
                FileDescription quoteFileDescription = cp.getFileDescription();
                if (quoteFileDescription==null && cp.getQuote()!=null){
                    quoteFileDescription=cp.getQuote().getFileDescription();
                }
                mQuote = new Quote(isEmpty(headerText) ? "" : headerText, isEmpty(text) ? "" : text, quoteFileDescription, cp.getTimeStamp());
                mFileDescription = null;

            }
        });
        mChosenPhrase = cp;
        mChatAdapter.setItemChosen(true, cp);
    }

    @Override
    public void onConsultAvatarClick(String consultId) {
        mChatController.onConsultChoose(this, consultId);
    }

    private void hideCopyControls() {
        setTitleStateCurrentOperatorConnected();
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.green_light));
        mCopyControls.setVisibility(View.GONE);
        mConsultNameView.setVisibility(View.VISIBLE);
        if (mChatController != null && mChatController.isConsultFound()) {
            mConsultTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        boolean isNeedToClose = true;
        if (mCopyControls.getVisibility() == View.VISIBLE) {
            unChooseItem(mChosenPhrase);
            isNeedToClose = false;
        }
        if (mSearchMessageEditText.getVisibility() == View.VISIBLE) {
            mSearchMessageEditText.setVisibility(View.GONE);
            mSearchMessageEditText.setText("");
            if (mChatController != null && mChatController.isConsultFound()) {
                setTitleStateOperatorConnected(connectedConsultId
                        , mChatController.getCurrentConsultName().split("%%")[0]==null?"":mChatController.getCurrentConsultName().split("%%")[0]
                        , mChatController.getCurrentConsultName().split("%%")[1]==null||mChatController.getCurrentConsultName().split("%%")[1].equals("null")?"":mChatController.getCurrentConsultName().split("%%")[1]);
            } else {
                setTitleStateDefault();
            }
            isNeedToClose = false;
            if (mRecyclerView != null && mChatAdapter != null) {
                mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        }
        if (isNeedToClose) {
            super.onBackPressed();
        }

    }

    public void setPhraseSentStatus(String id, MessageState messageState) {
        mChatAdapter.changeStateOfMessage(id, messageState);
    }

    private void unChooseItem(ChatPhrase cp) {
        hideCopyControls();
        mChatAdapter.setItemChosen(false, mChosenPhrase);
        mChosenPhrase = null;
    }

    public void updateProgress(FileDescription filedescription) {
        mChatAdapter.updateProgress(filedescription);
    }

    private class QuoteLayoutHolder {
        private View view;
        private TextView mHeader;
        private TextView mText;
        private ImageView mQuoteImage;

        public QuoteLayoutHolder() {
            view = findViewById(R.id.quote_layout);
            mHeader = (TextView) view.findViewById(R.id.quote_header);
            mText = (TextView) view.findViewById(R.id.quote_text);
            mQuoteImage = (ImageView) findViewById(R.id.quote_image);
            View clear = view.findViewById(R.id.quote_clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHeader.setText("");
                    mText.setText("");
                    view.setVisibility(View.GONE);
                    mQuote = null;
                    mFileDescription = null;
                    unChooseItem(mChosenPhrase);
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

        private void setImage(String path) {
            mQuoteImage.setVisibility(View.VISIBLE);
            Picasso
                    .with(getApplicationContext())
                    .load(path)
                    .fit()
                    .centerInside()
                    .into(mQuoteImage);
        }

        private void removeImage() {
            mQuoteImage.setVisibility(View.GONE);
        }

        void setText(String header, String text, String imagePath) {
            setIsVisible(true);
            mHeader.setText(header);
            mText.setText(text);
            if (imagePath != null) {
                setImage(imagePath);
            } else {
                removeImage();
            }
        }
    }

    @Override
    public void onUserPhraseClick(final UserPhrase userPhrase, int position) {
        if (userPhrase.getSentState() == MessageState.STATE_NOT_SENT) {
            final NoConnectionDialogFragment ncdf = NoConnectionDialogFragment.getInstance(new NoConnectionDialogFragment.OnCancelListener() {
                @Override
                public void onCancel() {
                    mChatController.checkAndResendPhrase(userPhrase);
                }
            });
            ncdf.setCancelable(true);
            ncdf.show(getFragmentManager(), null);
        }
    }

    public void cleanChat() {
        mChatAdapter = new ChatAdapter(new ArrayList<ChatItem>(), this, this);
        mRecyclerView.setAdapter(mChatAdapter);
        setTitleStateDefault();
      /*  mWelcomeScreen = new WelcomeScreen(this);
        ((ViewGroup) findViewById(R.id.chat_content)).addView(mWelcomeScreen);*/
        mInputEditText.clearFocus();
        isConsultTyping = false;
        isSearchingConsult = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.files_and_media) {
            startActivity(FilesActivity.getStartIntetent(this));
            return true;
        }
        if (item.getItemId() == R.id.search) {
            setBottomStateDefault();
            setTitleStateSearchingMessage();
            mSearchMessageEditText.setVisibility(View.VISIBLE);
            mSearchMessageEditText.requestFocus();
            mSearchMessageEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mChatAdapter.filterItems(s.toString());
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onGalleryClick();
            } else {

            }
        }
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            int granted = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted++;
                }
            }
            if (granted == grantResults.length) {
                onCameraClick();
            } else {
                Toast.makeText(this, getResources().getString(R.string.unavailible), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
