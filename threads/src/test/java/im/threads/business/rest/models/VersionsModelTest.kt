package im.threads.business.rest.models

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VersionsModelTest {

    private lateinit var versionsModel: VersionsModel

    @Before
    fun setUp() {
        val versionItemModel = VersionItemModel("group", "artifact", "name", "1.0.0", "time")
        versionsModel = VersionsModel(versionItemModel, versionItemModel, versionItemModel)
    }

    @Test
    fun whenToTableString_thenTableStringReturned() {
        val result = versionsModel.toTableString()
        assert(result.contains("1.0.0"))
    }

    @Test
    fun whenToTableStringWithNullVersion_thenDefaultValueReturned() {
        versionsModel = VersionsModel()
        val result = versionsModel.toTableString()
        assert(result.contains("unavailable"))
    }
}
