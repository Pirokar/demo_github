package im.threads.business.gson

import android.net.Uri
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonPrimitive
import im.threads.business.utils.gson.UriDeserializer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriDeserializerTest {
    private lateinit var context: JsonDeserializationContext
    private lateinit var uriDeserializer: UriDeserializer

    @Before
    fun setup() {
        context = Mockito.mock(JsonDeserializationContext::class.java)
        uriDeserializer = UriDeserializer()
    }

    @Test
    fun whenDeserialize_thenUriIsReturned() {
        val uriString = "https://www.example.com"
        val jsonElement = JsonPrimitive(uriString)
        val uri = Uri.parse(uriString)
        `when`<Uri>(context.deserialize(jsonElement, Uri::class.java)).thenReturn(uri)
        val result = uriDeserializer.deserialize(jsonElement, Uri::class.java, context)
        assertEquals(uri, result)
    }

    @Test
    fun whenDeserializeEmptyString_thenEmptyUriIsReturned() {
        val uriString = ""
        val jsonElement = JsonPrimitive(uriString)
        val uri = Uri.parse(uriString)
        `when`<Uri>(context.deserialize(jsonElement, Uri::class.java)).thenReturn(uri)
        val result = uriDeserializer.deserialize(jsonElement, Uri::class.java, context)
        assertEquals(uri, result)
    }
}
