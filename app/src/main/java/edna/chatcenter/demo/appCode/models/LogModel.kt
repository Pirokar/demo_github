package edna.chatcenter.demo.appCode.models

import com.huawei.hms.support.log.LogLevel
import edna.chatcenter.ui.core.logger.ChatLogLevel

data class LogModel(
    var timeText: String,
    var logLevel: ChatLogLevel,
    var logText: String
) : LogLevel
