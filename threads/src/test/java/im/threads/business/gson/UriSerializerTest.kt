package im.threads.business.gson

import android.net.Uri
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import im.threads.business.utils.gson.UriSerializer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class UriSerializerTest {
    private lateinit var context: JsonSerializationContext
    private lateinit var uri: Uri
    private val uriSerializer = UriSerializer()

    @Before
    fun before() {
        context = mock(JsonSerializationContext::class.java)
        uri = mock(Uri::class.java)
        `when`(uri.toString()).thenReturn("https://www.example.com")
    }

    @Test
    fun whenSerialize_thenJsonElementIsReturned() {
        val uriString = "https://www.example.com"
        val result = uriSerializer.serialize(uri, Uri::class.java, context)
        assertEquals(JsonPrimitive(uriString), result)
    }

    @Test
    fun whenSerializeEmptyUri_thenEmptyJsonElementIsReturned() {
        val uriString = ""
        `when`(uri.toString()).thenReturn(uriString)
        val result = uriSerializer.serialize(uri, Uri::class.java, context)
        assertEquals(JsonPrimitive(uriString), result)
    }
}
