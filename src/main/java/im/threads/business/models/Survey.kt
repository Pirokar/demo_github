package im.threads.business.models

import androidx.core.util.ObjectsCompat

class Survey : ChatItem, Hidable {
    var uuid: String?
    override val modified = null
    var sendingId: Long
        private set
    var questions: ArrayList<QuestionDTO>? = null
    override var hideAfter: Long?
        private set
    override var timeStamp: Long
    var isDisplayMessage: Boolean
    var sentState: MessageStatus
    var isRead: Boolean

    override fun toString(): String {
        return "Survey. uuid:\n = $uuid, " +
            "\nsendingId = $sendingId, " +
            "\nhideAfter = $hideAfter, " +
            "\nisDisplayMessage = $isDisplayMessage, " +
            "\nsentState = $sentState, " +
            "\nisRead = $isRead, " +
            "\ntimeStamp = $timeStamp, " +
            "\nthreadId = $threadId, " +
            "\nquestions = ${questions?.toString()}"
    }

    constructor(
        uuid: String?,
        surveySendingId: Long,
        hideAfter: Long?,
        phraseTimeStamp: Long,
        messageState: MessageStatus,
        read: Boolean,
        displayMessage: Boolean
    ) {
        this.uuid = uuid
        sendingId = surveySendingId
        this.hideAfter = hideAfter
        timeStamp = phraseTimeStamp
        sentState = messageState
        isRead = read
        isDisplayMessage = displayMessage
    }

    constructor(
        uuid: String?,
        sendingId: Long,
        questions: ArrayList<QuestionDTO>?,
        hideAfter: Long?,
        phraseTimeStamp: Long,
        displayMessage: Boolean,
        sentState: MessageStatus,
        read: Boolean
    ) {
        this.uuid = uuid
        this.sendingId = sendingId
        this.questions = questions
        this.hideAfter = hideAfter
        timeStamp = phraseTimeStamp
        isDisplayMessage = displayMessage
        this.sentState = sentState
        isRead = read
    }

    constructor(
        uuid: String?,
        surveySendingId: Long,
        phraseTimeStamp: Long,
        messageState: MessageStatus,
        read: Boolean,
        displayMessage: Boolean
    ) : this(uuid, surveySendingId, null, phraseTimeStamp, messageState, read, displayMessage)

    override fun isTheSameItem(otherItem: ChatItem?): Boolean {
        if (otherItem is Survey) {
            return ObjectsCompat.equals(sendingId, otherItem.sendingId) && isQuestionsEquals(
                questions,
                otherItem.questions
            )
        }
        return false
    }

    val isCompleted: Boolean
        get() = (sentState === MessageStatus.SENT || sentState === MessageStatus.READ) && allQuestionsHasRate()
    override val threadId: Long?
        get() = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val survey = other as Survey
        return sendingId == survey.sendingId && timeStamp == survey.timeStamp &&
            ObjectsCompat.equals(questions, survey.questions) &&
            ObjectsCompat.equals(hideAfter, survey.hideAfter) && sentState === survey.sentState
    }

    override fun hashCode(): Int {
        return ObjectsCompat.hash(sendingId, questions, hideAfter, timeStamp, sentState)
    }

    fun copy(): Survey {
        return Survey(
            uuid,
            sendingId,
            questions,
            hideAfter,
            timeStamp,
            isDisplayMessage,
            sentState,
            isRead
        )
    }

    private fun isQuestionsEquals(
        collection1: List<QuestionDTO>?,
        collection2: List<QuestionDTO>?
    ): Boolean {
        var result = false
        if (collection1!!.size == collection2!!.size) {
            for (coll1Element in collection1) {
                result = false
                for (coll2Element in collection2) {
                    val ratesEquals = coll2Element.rate === coll1Element.rate
                    if (ratesEquals && coll2Element.text == coll1Element.text) {
                        result = true
                        break
                    }
                }
                if (!result) {
                    break
                }
            }
        }
        return result
    }

    private fun allQuestionsHasRate(): Boolean {
        var result = true
        val questionsSize = questions!!.size
        var i = 0
        while (i < questionsSize && result) {
            result = questions!![i].hasRate()
            i++
        }
        return result
    }
}
