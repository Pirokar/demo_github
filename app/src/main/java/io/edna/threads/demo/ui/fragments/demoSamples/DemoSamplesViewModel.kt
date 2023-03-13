package io.edna.threads.demo.ui.fragments.demoSamples

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.edna.threads.demo.ui.models.DemoSamplesListItem
import io.edna.threads.demo.ui.models.DemoSamplesListItem.DIVIDER
import io.edna.threads.demo.ui.models.DemoSamplesListItem.TEXT
import io.edna.threads.demo.ui.models.DemoSamplesListItem.TITLE

class DemoSamplesViewModel : ViewModel() {
    private val mutableDemoSamplesData = MutableLiveData<List<DemoSamplesListItem>>()
    val demoSamplesData: LiveData<List<DemoSamplesListItem>> = mutableDemoSamplesData

    fun start() {
        createData()
    }

    private fun createData() {
        mutableDemoSamplesData.postValue(
            listOf(
                TITLE("ПРИМЕРЫ ОТДЕЛЬНЫХ ЭЛЕМЕНТОВ"),
                TEXT("Цитаты"),
                TEXT("Картинки"),
                TEXT("Голосовые сообщения"),
                TEXT("Текстовые сообщения"),
                TEXT("Видео"),
                TEXT("Системные сообщения"),
                DIVIDER,
                TITLE("ПРИМЕРЫ ДИАЛОГОВ"),
                TEXT("Чат с оператором"),
                TEXT("Ошибки подключения"),
                TEXT("Голосовые сообщения"),
                TEXT("Текстовые сообщения"),
                TEXT("Видео"),
                TEXT("Системные сообщения"),
            )
        )
    }
}
