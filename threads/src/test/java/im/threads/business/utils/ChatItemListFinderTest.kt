package im.threads.business.utils

import android.os.Build
import im.threads.business.models.ChatItem
import im.threads.business.models.ConsultPhrase
import im.threads.business.models.UserPhrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ChatItemListFinderTest {
    private lateinit var userPhrase: UserPhrase
    private lateinit var consultPhrase: ConsultPhrase
    private lateinit var chatItemList: List<ChatItem?>

    @Before
    fun before() {
        userPhrase = mock(UserPhrase::class.java)
        consultPhrase = mock(ConsultPhrase::class.java)
        chatItemList = listOf(userPhrase, consultPhrase, userPhrase, consultPhrase)
    }

    @Test
    fun whenUserPhraseIsSearched_thenIndexOfReturnsFirstIndex() {
        `when`(userPhrase.isTheSameItem(userPhrase)).thenReturn(true)
        val index = ChatItemListFinder.indexOf(chatItemList, userPhrase)
        assert(index == 0)
    }

    @Test
    fun whenConsultPhraseIsSearched_thenIndexOfReturnsFirstIndex() {
        `when`(consultPhrase.isTheSameItem(consultPhrase)).thenReturn(true)
        val index = ChatItemListFinder.indexOf(chatItemList, consultPhrase)
        assert(index == 1)
    }

    @Test
    fun whenUserPhraseIsSearched_thenLastIndexOfReturnsLastIndex() {
        `when`(userPhrase.isTheSameItem(userPhrase)).thenReturn(true)
        val index = ChatItemListFinder.lastIndexOf(chatItemList, userPhrase)
        assert(index == 2)
    }

    @Test
    fun whenConsultPhraseIsSearched_thenLastIndexOfReturnsLastIndex() {
        `when`(consultPhrase.isTheSameItem(consultPhrase)).thenReturn(true)
        val index = ChatItemListFinder.lastIndexOf(chatItemList, consultPhrase)
        assert(index == 3)
    }
}
