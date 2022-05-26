package im.threads.di

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

interface CustomKoinComponent : KoinComponent {
    override fun getKoin(): Koin = ThreadsKoinContext.koinApp.koin
}
