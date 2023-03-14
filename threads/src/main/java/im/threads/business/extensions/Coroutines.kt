package im.threads.business.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> CoroutineScope.withMainContext(block: CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)
