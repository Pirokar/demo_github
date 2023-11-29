package im.threads.business.formatters

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class JsonFormatterTest {

    @Test
    fun whenCreatingJson_thenPrettyFormatIsCorrect() {
        val jsonFormatter = JsonFormatter()
        val json = "{\"name\":\"Vladimir\",\"age\":30,\"city\":\"Rostov On Don\"}"
        val prettyJson = jsonFormatter.jsonToPrettyFormat(json)

        val expectedPrettyJson = "{\n  \"name\": \"Vladimir\",\n  \"age\": 30,\n  \"city\": \"Rostov On Don\"\n}"
        assertEquals(expectedPrettyJson, prettyJson)

        val invalidJson = "{name:Vladimir,age:30,city:Rostov On Don}"
        val result = jsonFormatter.jsonToPrettyFormat(invalidJson)

        val expectedMessage = "Cannot create PrettyJson. Input json: $invalidJson"
        assertEquals(expectedMessage, result)
    }
}
