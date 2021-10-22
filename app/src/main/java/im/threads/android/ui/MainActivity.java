package im.threads.android.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.threads.ThreadsLib;
import im.threads.UserInfoBuilder;
import im.threads.android.R;
import im.threads.android.data.Card;
import im.threads.android.databinding.ActivityMainBinding;
import im.threads.android.utils.CardsLinearLayoutManager;
import im.threads.android.utils.CardsSnapHelper;
import im.threads.android.utils.ChatStyleBuilderHelper;
import im.threads.android.utils.PrefUtils;
import im.threads.view.ChatActivity;

/**
 * Активность с примерами открытия чата:
 * - в виде новой Активности
 * - в виде активности, где чат выступает в качестве фрагмента
 */
public class MainActivity extends AppCompatActivity implements EditCardDialog.EditCardDialogActionsListener, YesNoDialog.YesNoDialogActionListener {

    private static final int YES_NO_DIALOG_REQUEST_CODE = 323;
    ActivityMainBinding binding;
    private CardsAdapter cardsAdapter;
    private CardsSnapHelper cardsSnapHelper;
    private Card cardForDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(this);

        binding.designSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String theme = binding.designSpinner.getSelectedItem().toString();
                ChatStyleBuilderHelper.ChatDesign.setTheme(MainActivity.this, ChatStyleBuilderHelper.ChatDesign.enumOf(MainActivity.this, theme));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }

        });

        TextView versionView = findViewById(R.id.version_name);
        versionView.setText(getString(R.string.lib_version, ThreadsLib.getLibVersion(), getString(R.string.transport_type)));
        cardsSnapHelper = new CardsSnapHelper();
        cardsSnapHelper.attachToRecyclerView(binding.cardsView);
        binding.cardsView.setLayoutManager(new CardsLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.cardsView.setHasFixedSize(true);
        cardsAdapter = new CardsAdapter();
        cardsAdapter.setCardActionListener(new CardsAdapter.CardActionListener() {
            @Override
            public void onDelete(Card card) {
                cardForDelete = card;
                YesNoDialog.open(MainActivity.this, getString(R.string.card_delete_text),
                        getString(R.string.card_delete_yes),
                        getString(R.string.card_delete_no),
                        YES_NO_DIALOG_REQUEST_CODE);
            }

            @Override
            public void onEdit(Card card) {
                showEditCardDialog(card);
            }
        });
        binding.cardsView.setAdapter(cardsAdapter);
        showCards(PrefUtils.getCards(this));
    }

    private void showCards(List<Card> cards) {

        boolean hasCards = cards != null && !cards.isEmpty();

        binding.addCard.setVisibility(!hasCards ? View.VISIBLE : View.GONE);
        binding.addCardHint.setVisibility(!hasCards ? View.VISIBLE : View.GONE);

        binding.cardsView.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.chatActivityButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.chatFragmentButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        binding.sendMessageButton.setVisibility(hasCards ? View.VISIBLE : View.GONE);
        cardsAdapter.setCards(hasCards ? cards : new ArrayList<>());
    }

    /**
     * Пример открытия чата в виде Активности
     */
    public void navigateToChatActivity() {
        Card currentCard = getCurrentCard();
        if (currentCard == null) {
            displayError(R.string.error_empty_user);
            return;
        }
        currentCard.getUserId();
        ThreadsLib.getInstance().initUser(
                new UserInfoBuilder(currentCard.getUserId())
                        .setAuthData(currentCard.getAuthToken(), currentCard.getAuthSchema())
                        .setClientData(currentCard.getClientData())
                        .setClientIdSignature(currentCard.getClientIdSignature())
                        .setAppMarker(currentCard.getAppMarker())
        );
        ThreadsLib.getInstance().applyChatStyle(ChatStyleBuilderHelper.getChatStyle(PrefUtils.getTheme(this)));
        startActivity(new Intent(this, ChatActivity.class));
    }

    /**
     * Пример открытия чата в виде фрагмента
     */
    public void navigateToBottomNavigationActivity() {
        Card currentCard = getCurrentCard();
        if (currentCard == null) {
            displayError(R.string.error_empty_user);
            return;
        }
        currentCard.getUserId();
        if (ThreadsLib.getInstance().isUserInitialized()) {
            ThreadsLib.getInstance().initUser(
                    new UserInfoBuilder(currentCard.getUserId())
                            .setAuthData(currentCard.getAuthToken(), currentCard.getAuthSchema())
                            .setClientData(currentCard.getClientData())
                            .setClientIdSignature(currentCard.getClientIdSignature())
                            .setAppMarker(currentCard.getAppMarker())
            );
        }
        startActivity(BottomNavigationActivity.createIntent(
                this,
                currentCard.getAppMarker(),
                currentCard.getUserId(),
                currentCard.getClientData(),
                currentCard.getClientIdSignature(),
                currentCard.getAuthToken(),
                currentCard.getAuthSchema(),
                PrefUtils.getTheme(this))
        );
    }

    public void showEditCardDialog() {
        EditCardDialog.open(this);
    }

    public void showEditCardDialog(Card card) {
        EditCardDialog.open(this, card);
    }

    public void showEditTransportConfigDialog() {
        EditTransportConfigDialog.Companion.open(this);
    }

    public void sendExampleMessage() {
        View view = findViewById(android.R.id.content);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap icon = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        File imageFile = new File(getFilesDir(), "screenshot.jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            icon.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
        Card currentCard = getCurrentCard();
        if (currentCard == null) {
            displayError(R.string.error_empty_user);
            return;
        }
        if (currentCard.getUserId() == null) {
            displayError(R.string.error_empty_userid);
            return;
        }
        UserInfoBuilder userInfoBuilder =
                new UserInfoBuilder(currentCard.getUserId())
                        .setAuthData(currentCard.getAuthToken(), currentCard.getAuthSchema())
                        .setClientData(currentCard.getClientData())
                        .setClientIdSignature(currentCard.getClientIdSignature())
                        .setAppMarker(currentCard.getAppMarker());
        ThreadsLib.getInstance().initUser(userInfoBuilder);
        boolean messageSent = ThreadsLib.getInstance().sendMessage(getString(R.string.test_message), imageFile);
        if (messageSent) {
            Toast.makeText(this, R.string.send_text_message_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.send_text_message_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Card getCurrentCard() {
        RecyclerView.LayoutManager layoutManager = binding.cardsView.getLayoutManager();
        if (layoutManager != null) {
            View centerView = cardsSnapHelper.findSnapView(layoutManager);
            if (centerView != null) {
                return cardsAdapter.getCard(layoutManager.getPosition(centerView));
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_card) {
            showEditCardDialog();
            return true;
        }
        if (id == R.id.edit_transport_config) {
            showEditTransportConfigDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCardSaved(Card newCard) {
        List<Card> cards = cardsAdapter.getCards();
        final int indexOf = cards.indexOf(newCard);
        if (indexOf != -1) {
            cards.set(indexOf, newCard);
            Toast.makeText(this, R.string.client_info_updated, Toast.LENGTH_LONG).show();
        } else {
            cards.add(newCard);
        }
        PrefUtils.storeCards(this, cards);
        showCards(cards);
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onOkClicked(final int requestCode) {
        List<Card> cards = cardsAdapter.getCards();
        if (cards.contains(cardForDelete)) {
            cards.remove(cardForDelete);
            showCards(cards);
            PrefUtils.storeCards(this, cards);
            ThreadsLib.getInstance().logoutClient(cardForDelete.getUserId());
        }
        cardForDelete = null;
    }

    @Override
    public void onCancelClicked(final int requestCode) {
        cardForDelete = null;
    }


    private void displayError(final @StringRes int errorTextRes) {
        displayError(getString(errorTextRes));
    }

    private void displayError(final @NonNull String errorText) {
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
    }
}
