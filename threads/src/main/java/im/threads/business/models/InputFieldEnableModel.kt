package im.threads.business.models

data class InputFieldEnableModel(val isEnabledInputField: Boolean, val isEnabledSendButton: Boolean) {
    override fun toString(): String {
        return "isEnabledInputField: $isEnabledInputField, isEnabledSendButton: $isEnabledSendButton"
    }
}
