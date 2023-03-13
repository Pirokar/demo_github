package io.edna.threads.demo.ui.models

sealed class DemoSamplesListItem {
    object DIVIDER : DemoSamplesListItem()

    data class TITLE(val text: String) : DemoSamplesListItem() {
        override fun toString() = text
        companion object
    }

    data class TEXT(val text: String) : DemoSamplesListItem() {
        override fun toString() = text
        companion object
    }
}
