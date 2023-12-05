package im.threads.business.models

import android.net.Uri
import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileDescriptionTest {

    private val fileDescription = FileDescription(
        from = "Test From",
        fileUri = Uri.parse("content://test"),
        size = 123L,
        timeStamp = 456L
    )

    @Test
    fun whenHasSameContentWithSameObject_thenReturnsTrue() {
        assertTrue(fileDescription.hasSameContent(fileDescription))
    }

    @Test
    fun whenHasSameContentWithDifferentObject_thenReturnsFalse() {
        val otherFileDescription = FileDescription(
            from = "Other From",
            fileUri = Uri.parse("content://other"),
            size = 789L,
            timeStamp = 101112L
        )
        assertFalse(fileDescription.hasSameContent(otherFileDescription))
    }

    @Test
    fun whenHasSameContentWithNull_thenReturnsFalse() {
        assertFalse(fileDescription.hasSameContent(null))
    }

    @Test
    fun whenIsFromAssetsWithAssetUri_thenReturnsTrue() {
        val assetFileDescription = FileDescription(
            from = "Test From",
            null,
            size = 123L,
            timeStamp = 456L
        ).apply {
            downloadPath = "file:///android_asset/test.jpg"
        }
        assert(assetFileDescription.isFromAssets())
    }

    @Test
    fun whenIsFromAssetsWithNonAssetUri_thenReturnsFalse() {
        assertFalse(fileDescription.isFromAssets())
    }

    @Test
    fun whenDescribeContents_thenReturnsCorrectValue() {
        assertEquals(fileDescription.hashCode(), fileDescription.describeContents())
    }

    @Test
    fun whenWriteToParcel_thenParcelContainsCorrectData() {
        val parcel = Parcel.obtain()
        fileDescription.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        assertEquals("ANY", parcel.readString())
        assertEquals("ANY", parcel.readString())
        assertEquals("", parcel.readString())
        assertEquals("Test From", parcel.readString())
        assertEquals(Uri.parse("content://test"), parcel.readParcelable(Uri::class.java.classLoader))
        assertEquals(null, parcel.readString())
        assertEquals(null, parcel.readString())
        assertEquals(null, parcel.readString())
        assertEquals(null, parcel.readString())
        assertEquals(123L, parcel.readLong())
        assertEquals(456L, parcel.readLong())
        assertEquals(0, parcel.readInt())
    }

    @Test
    fun whenToString_thenReturnsCorrectString() {
        val expectedString = "FileDescription{from='Test From', fileUri='content://test', downloadPath='null'," +
            " incomingName='null', size=123, timeStamp=456, downloadProgress=0, state=ANY, errorCode=ANY, errorMessage=}"
        assertEquals(expectedString, fileDescription.toString())
    }

    @Test
    fun whenCreatorCreatesFromParcel_thenReturnsCorrectFileDescription() {
        val parcel = Parcel.obtain()
        fileDescription.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val createdFileDescription = FileDescription.CREATOR.createFromParcel(parcel)
        assertEquals("Test From", createdFileDescription.from)
        assertEquals(Uri.parse("content://test"), createdFileDescription.fileUri)
        assertEquals(123L, createdFileDescription.size)
        assertEquals(456L, createdFileDescription.timeStamp)
    }
}
