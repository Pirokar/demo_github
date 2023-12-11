package im.threads.business.utils

import android.os.Parcel
import android.os.Parcelable
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WorkerUtilsTest {

    @Test
    fun whenMarshall_thenBytesAreReturned() {
        val parcelable = object : Parcelable {
            override fun describeContents() = 0
            override fun writeToParcel(dest: Parcel, flags: Int) {
                dest.writeInt(123)
            }
        }

        val bytes = WorkerUtils.marshall(parcelable)
        val expectedBytes = Parcel.obtain().apply {
            writeInt(123)
        }.marshall()

        assertArrayEquals(expectedBytes, bytes)
    }

    @Test
    fun whenUnmarshall_thenParcelIsReturned() {
        val expectedParcel = Parcel.obtain().apply {
            writeInt(123)
            setDataPosition(0)
        }
        val bytes = expectedParcel.marshall()
        val parcel = WorkerUtils.unmarshall(bytes)

        assertEquals(expectedParcel.readInt(), parcel.readInt())
    }

    @Test
    fun whenUnmarshallEmpty_thenParcelIsEmpty() {
        val parcel = WorkerUtils.unmarshall(byteArrayOf())
        assertEquals(0, parcel.dataSize())
    }

    @Test
    fun whenMarshallWithDifferentParcelable_thenBytesAreReturned() {
        val parcelable = object : Parcelable {
            override fun describeContents() = 0

            override fun writeToParcel(dest: Parcel, flags: Int) {
                dest.writeString("Hello World")
            }
        }

        val bytes = WorkerUtils.marshall(parcelable)
        val expectedBytes = Parcel.obtain().apply {
            writeString("Hello World")
        }.marshall()

        assertArrayEquals(expectedBytes, bytes)
    }

    @Test
    fun whenUnmarshallWithDifferentBytes_thenParcelIsReturned() {
        val expectedParcel = Parcel.obtain().apply {
            writeString("Hello World")
            setDataPosition(0)
        }
        val bytes = expectedParcel.marshall()
        val parcel = WorkerUtils.unmarshall(bytes)

        assertEquals(expectedParcel.readString(), parcel.readString())
    }

    @Test
    fun whenUnmarshallWithEmptyBytes_thenParcelIsEmpty() {
        val parcel = WorkerUtils.unmarshall(byteArrayOf())
        assertEquals(0, parcel.dataSize())
    }
}
