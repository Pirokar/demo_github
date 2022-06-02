package im.threads.android.di

import im.threads.android.use_cases.developer_options.ServersSelectionInteractor
import im.threads.android.use_cases.developer_options.ServersSelectionUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<ServersSelectionUseCase> { ServersSelectionInteractor(androidContext()) }
}
