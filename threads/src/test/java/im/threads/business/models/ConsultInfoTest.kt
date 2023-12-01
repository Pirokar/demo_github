package im.threads.business.models

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsultInfoTest {

    private val consultInfo = ConsultInfo(
        name = "Test Name",
        id = "Test ID",
        status = "Test Status",
        organizationUnit = "Test Organization Unit",
        photoUrl = "Test Photo URL",
        role = "Test Role"
    )

    @Test
    fun whenToString_thenReturnsCorrectString() {
        val expectedString = """ConsultInfo{
            name='Test Name',
            id=Test ID,
            status='Test Status',
            organizationUnit='Test Organization Unit',
            photoUrl='Test Photo URL',
            role='Test Role'
            }
        """.trim()
        assertEquals(expectedString, consultInfo.toString())
    }

    @Test
    fun whenToJson_thenReturnsCorrectJsonObject() {
        val expectedJsonObject = JsonObject().apply {
            addProperty("name", "Test Name")
            addProperty("status", "Test Status")
            addProperty("id", "Test ID")
            addProperty("photoUrl", "Test Photo URL")
            addProperty("role", "Test Role")
        }
        assertEquals(expectedJsonObject, consultInfo.toJson())
    }
}
