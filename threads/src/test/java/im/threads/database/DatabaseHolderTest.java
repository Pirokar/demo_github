package im.threads.database;

import android.content.Context;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import im.threads.model.ChatItem;
import im.threads.model.ConsultPhrase;
import im.threads.model.FileDescription;
import im.threads.model.MessageState;
import im.threads.model.Quote;
import im.threads.model.UserPhrase;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class DatabaseHolderTest {

    private Context appContext;
    private ChatItem chatItem;
    private DatabaseHolder dbHolder;

    @Before
    public void setup() {
        appContext = RuntimeEnvironment.application.getApplicationContext();
        dbHolder = DatabaseHolder.getInstance(appContext);
    }

    public DatabaseHolderTest(ChatItem chatItem) {
        this.chatItem = chatItem;
    }

    @Test
    public void saveAndGetChatItem() {
        String assertErrorMessage = "Chat item not equal after set and get from DB. ";

        dbHolder.putChatItem(this.chatItem);
        ChatItem itemFromDb = dbHolder.getChatItems(0, 1).get(0);
        Assert.assertEquals(assertErrorMessage, this.chatItem, itemFromDb);

        UserPhrase userPhrase = chatItem instanceof UserPhrase ? (UserPhrase) chatItem : null;
        ConsultPhrase consultPhrase = chatItem instanceof ConsultPhrase ? (ConsultPhrase) chatItem : null;

        if (userPhrase != null) {
            UserPhrase userPhraseFromDB = (UserPhrase) itemFromDb;
            Assert.assertTrue(assertErrorMessage, userPhrase.hasSameContent(userPhraseFromDB));

        } else if (consultPhrase != null) {
            ConsultPhrase consultPhraseFromDB = (ConsultPhrase) itemFromDb;
            Assert.assertTrue(assertErrorMessage, consultPhrase.hasSameContent(consultPhraseFromDB));
        }
    }

    @Test
    public void duplicateMessagesNotSaved() {
        dbHolder.putChatItem(this.chatItem);
        dbHolder.putChatItem(this.chatItem);
        dbHolder.putChatItem(this.chatItem);
        int count = dbHolder.getChatItems(0, 100).size();
        Assert.assertEquals("Duplicate messages in DB.", 1, count);
    }

    @Test
    public void duplicateFileDescriptionsNotSaved() {

        UserPhrase userPhrase = chatItem instanceof UserPhrase ? (UserPhrase) chatItem : null;
        ConsultPhrase conPhrase = chatItem instanceof ConsultPhrase ? (ConsultPhrase) chatItem : null;

        boolean isUserFile = userPhrase != null && userPhrase.getFileDescription() != null;
        boolean isConFile = conPhrase != null && conPhrase.getFileDescription() != null;

        if (isUserFile || isConFile) {

            dbHolder.putChatItem(this.chatItem);
            dbHolder.putChatItem(this.chatItem);
            dbHolder.putChatItem(this.chatItem);

            List<FileDescription> fileDescriptions = dbHolder.getMyOpenHelper().getFd();

            Assert.assertEquals("Duplicate file descriptions in DB. ", 1, fileDescriptions.size());
        }
    }

    @Test
    public void duplicateQuotesNotSaved() {

        UserPhrase userPhrase = chatItem instanceof UserPhrase ? (UserPhrase) chatItem : null;
        ConsultPhrase conPhrase = chatItem instanceof ConsultPhrase ? (ConsultPhrase) chatItem : null;

        boolean isUserFile = userPhrase != null && userPhrase.getQuote() != null;
        boolean isConFile = conPhrase != null && conPhrase.getQuote() != null;

        if (isUserFile || isConFile) {

            dbHolder.putChatItem(this.chatItem);
            dbHolder.putChatItem(this.chatItem);
            dbHolder.putChatItem(this.chatItem);

            List<Quote> quotes = dbHolder.getMyOpenHelper().getQuotes();
            Assert.assertEquals("Duplicate quotes in DB. ", 1, quotes.size());
        }
    }

    @After
    public void finishComponentTesting() {
        // Workaround on static variables cleared by Robolectric
        // https://github.com/robolectric/robolectric/issues/1890#issuecomment-111726535
        // DatabaseHolder.instance setting to null
        dbHolder.getMyOpenHelper().close();
        DatabaseHolder.eraseInstance();
    }


    @ParameterizedRobolectricTestRunner.Parameters(name = "chatItem = {0}")
    public static Collection<Object[]> getTestsParams() {
        return Arrays.asList(new Object[][]{
                {getConsultPhrase()},
                {getConsultPhraseWithFile()},
                {getConsultPhraseWithQuote()},
                {getConsultPhraseWithQuoteOfFile()},
                {getUserPhrase()},
                {getUserPhraseWithFile()},
                {getUserPhraseWithQuote()},
                {getUserPhraseWithQuoteOfFile()}

        });
    }

    private static ConsultPhrase getConsultPhrase() {

        return new ConsultPhrase(
                "15a20b85-a6de-4ac5-98ab-8700d3f40724",
                "3:1699351548172962899",
                null,
                null,
                "Оператор4 Андреевич",
                "dt6nu6i",
                1559814333687L,
                "7",
                "055b27bc-7455-4d42-a043-70bdbfa09aa6",
                false,
                null,
                true
        );
    }

    private static ConsultPhrase getConsultPhraseWithFile() {

        FileDescription fileDesc = new FileDescription(
                "Оператор4 Андреевич",
                null,
                7945,
                1559810598957L);

        fileDesc.setDownloadProgress(0);
        fileDesc.setDownloadPath("https://polarbearstore.threads.im/files/c485bfc9-0c6a-4747-97e5-45ee3b73ec43");
        fileDesc.setIncomingName("pdf-sample.pdf");

        return new ConsultPhrase(
                "b613fe15-b920-4586-8e90-0909274eec3b",
                "3:1699335510765079574",
                fileDesc,
                null,
                "Оператор4 Андреевич",
                "",
                1559810598957L,
                "7",
                "055b27bc-7455-4d42-a043-70bdbfa09aa6",
                false,
                null,
                true
        );
    }

    private static ConsultPhrase getConsultPhraseWithQuote() {

        Quote quote = new Quote("a433a7d8-6af4-4c21-b6ca-915a301e2f4a",
                "I",
                "f67in",
                null,
                1559810522556L);

        return new ConsultPhrase(
                "0bf12ad6-6267-47ed-b01d-6e6b3030ce58",
                "3:1699335781348019232",
                null,
                quote,
                "Оператор4 Андреевич",
                "e56un67",
                1559810662251L,
                "7",
                "055b27bc-7455-4d42-a043-70bdbfa09aa6",
                false,
                null,
                true);
    }

    private static ConsultPhrase getConsultPhraseWithQuoteOfFile() {

        FileDescription fileDesc = new FileDescription(
                "I",
                null,
                7945,
                1559897546447L);

        fileDesc.setDownloadProgress(0);
        fileDesc.setDownloadPath("https://polarbearstore.threads.im/files/ecc3654d-de6f-4ccb-8c0e-32d953bc0eb1");
        fileDesc.setIncomingName("pdf-sample.pdf");

        Quote quote = new Quote("9058775a-99ea-40a3-bc49-f0bf3e68790b",
                "I",
                "",
                fileDesc,
                1559812894748L);

        return new ConsultPhrase(
                "d8bcbe1f-6d6f-48b8-bf73-02e915def139",
                "38aa703d-93b3-4ab1-96b7-edcb23457ec5",
                null,
                quote,
                "Оператор4 Андреевич",
                "d6n7dtm6iu0[e95",
                1559897546447L,
                "7",
                "055b27bc-7455-4d42-a043-70bdbfa09aa6",
                true,
                null,
                false);
    }

    private static UserPhrase getUserPhrase() {

        UserPhrase phrase = new UserPhrase(
                "e68c0567-6181-45bf-aed2-1ffd583b1b6e",
                "tempProviderId: be907fd6-3c01-4b9a-b618-5ca44f37fd38",
                "dtmutmi78om8",
                null,
                1559814297374L,
                null);
        phrase.setSentState(MessageState.STATE_SENDING);

        return phrase;
    }

    private static UserPhrase getUserPhraseWithFile() {

        FileDescription fileDesc = new FileDescription(
                "I",
                "file:///storage/emulated/0/Download/pdf-sample.pdf",
                7945,
                1559811192436L);

        fileDesc.setDownloadProgress(0);
        fileDesc.setDownloadPath(null);
        fileDesc.setIncomingName(null);

        UserPhrase phrase = new UserPhrase(
                "9058775a-99ea-40a3-bc49-f0bf3e68790b",
                "tempProviderId: ef78a30a-6d96-46d2-ac42-af7f9fc11b72",
                "",
                null,
                1559811194722L,
                fileDesc);
        phrase.setSentState(MessageState.STATE_SENDING);

        return phrase;
    }

    private static UserPhrase getUserPhraseWithQuote() {

        Quote quote = new Quote("1a4e698f-4e18-47cc-8b19-5e87c7ce7c69",
                "I",
                "7685n7",
                null,
                1559803597204L);

        UserPhrase phrase = new UserPhrase(
                "14195234-f1a4-41e9-9c59-8b54cb31a890",
                "1699305502774881052",
                "dtm6i7",
                quote,
                1559803612915L,
                null);
        phrase.setSentState(MessageState.STATE_WAS_READ);
        return phrase;
    }

    private static UserPhrase getUserPhraseWithQuoteOfFile() {

        FileDescription fileDesc = new FileDescription(
                "Оператор4 Андреевич",
                null,
                7945,
                1559810598957L);

        fileDesc.setDownloadProgress(0);
        fileDesc.setDownloadPath("https://polarbearstore.threads.im/files/c485bfc9-0c6a-4747-97e5-45ee3b73ec43");
        fileDesc.setIncomingName("pdf-sample.pdf");

        Quote quote = new Quote("b613fe15-b920-4586-8e90-0909274eec3b",
                "Оператор4 Андреевич",
                "",
                fileDesc,
                1559899069192L);

        UserPhrase phrase = new UserPhrase(
                "31887680-4baa-47b1-bf23-ad560c1c2535",
                "tempProviderId: fe27e6ec-8bb4-440c-9bdd-c3e4c34ff175",
                "dt6nut7i8",
                quote,
                1559899078246L,
                null);
        phrase.setSentState(MessageState.STATE_SENDING);

        return phrase;
    }

}
