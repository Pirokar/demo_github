package im.threads.ui.holders.helper

import im.threads.business.models.ChatItem
import im.threads.business.models.Survey

fun List<ChatItem>.splitSurveyQuestions(): List<ChatItem> {
    val newItems = mutableListOf<ChatItem>()
    this.forEach { item ->
        if (item !is Survey) {
            newItems.add(item)
        } else {
            item.questions.forEach { question ->
                val newItem = item.copy()
                newItem.questions = listOf(question)
                newItems.add(newItem)
            }
        }
    }
    return newItems
}
