package edna.chatcenter.demo.appCode.business

import android.content.Context
import edna.chatcenter.ui.visual.extensions.isDarkThemeOn

class UiThemeProvider(private val context: Context) {
    fun isDarkThemeOn() = context.isDarkThemeOn()
}
