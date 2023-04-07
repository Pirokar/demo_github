package im.threads.business.rest.models

import im.threads.BuildConfig
import im.threads.business.utils.paddingStartEnd

data class VersionsModel(
    val api: VersionItemModel? = null,
    val datastore: VersionItemModel? = null,
    val gate: VersionItemModel? = null
) {
    /**
     * Выводит в консоль версии chat center, backend, threads gate, data store
     */
    fun toTableString(): String {
        val defaultValue = "unavailable"
        val chatCenterSDK = BuildConfig.VERSION_NAME
        var backendVersion = defaultValue
        var threadsGateVersion = defaultValue
        var datastoreVersion = defaultValue

        api?.version?.let { backendVersion = it }
        gate?.version?.let { threadsGateVersion = it }
        datastore?.version?.let { datastoreVersion = it }

        val firstAndLastLine = "+-------------------------+----------------------+\n"
        val secondLine = "|        Component        |        Version       |\n"
        val stringBuilder = StringBuilder().apply {
            append("Versions:\n")
            append(firstAndLastLine)
            append(secondLine)
            append(firstAndLastLine)
            append(getVersionDataLine("ChatCenter SDK", chatCenterSDK))
            append(getVersionDataLine("Backend", backendVersion))
            append(getVersionDataLine("ThreadsGate", threadsGateVersion))
            append(getVersionDataLine("DataStore", datastoreVersion))
            append(firstAndLastLine)
        }

        return stringBuilder.toString()
    }

    private fun getVersionDataLine(name: String, version: String): String {
        val nameSpaceLength = 25
        val versionSpaceLength = 22
        val nameSpace = getSpacing(name, nameSpaceLength)
        val versionSpace = getSpacing(version, versionSpaceLength)

        val nameString = name.paddingStartEnd(nameSpace.first, nameSpace.second)
        val versionString = version.paddingStartEnd(versionSpace.first, versionSpace.second)

        return "|$nameString|$versionString|\n"
    }

    private fun getSpacing(text: String, spaceSize: Int): Pair<Int, Int> {
        val textSpace = spaceSize - text.length
        val textTrailSpacing = textSpace / 2
        val endTextSpace = if (textSpace % 2 == 0) textTrailSpacing else textTrailSpacing + 1

        return Pair(textTrailSpacing, endTextSpace)
    }
}
