package im.threads.internal.utils

import im.threads.R

private const val KEY_FILE_RESTRICTED_TYPE = "file.restricted.type"
private const val KEY_FILE_RESTRICTED_SIZE = "file.restricted.size"
private const val KEY_FILE_NOT_FOUND = "file.not-found"

fun getErrorStringResByCode(code: String): Int {
    return when (code.lowercase()) {
        KEY_FILE_RESTRICTED_TYPE -> R.string.threads_file_restricted_type_error_during_load_file
        KEY_FILE_RESTRICTED_SIZE -> R.string.threads_file_restricted_size_error_during_load_file
        KEY_FILE_NOT_FOUND -> R.string.threads_file_not_found_error_during_load_file
        else -> R.string.threads_some_error_during_load_file
    }
}
