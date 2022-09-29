package im.threads.business.logger

import im.threads.BuildConfig
import im.threads.business.logger.core.LoggerEdna
import im.threads.business.rest.queries.BackendApi
import im.threads.business.utils.paddingStartEnd

class VersionLogger {
    /**
     * Запрашивает версии backend, threads gate, data store из api (синхронно)
     * и выводит их в консоль в виде таблицы
     */
    fun logVersions() {
        val defaultValue = "unavailable"
        val chatCenterSDK = BuildConfig.VERSION_NAME
        var backend = defaultValue
        var threadsGate = defaultValue
        var datastore = defaultValue

        val response = BackendApi.get().versions()?.execute()
        if (response?.isSuccessful == true) {
            response.body()?.build?.version?.let { backend = it }
            response.body()?.gate?.version?.let { threadsGate = it }
            response.body()?.datastore?.version?.let { datastore = it }
        }

        val firstAndLastLine = "+-------------------------+----------------------+\n"
        val secondLine = "|        Component        |        Version       |\n"
        val stringBuilder = StringBuilder().apply {
            append("Versions:\n")
            append(firstAndLastLine)
            append(secondLine)
            append(firstAndLastLine)
            append(getVersionDataLine("ChatCenter SDK", chatCenterSDK))
            append(getVersionDataLine("Backend", backend))
            append(getVersionDataLine("ThreadsGate", threadsGate))
            append(getVersionDataLine("DataStore", datastore))
            append(firstAndLastLine)
        }

        LoggerEdna.info(stringBuilder.toString())
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
