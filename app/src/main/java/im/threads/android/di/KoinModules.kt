package im.threads.android.di

import im.threads.android.ui.developer_options.DeveloperOptionsViewModel
import im.threads.android.use_cases.developer_options.DevOptionsInteractor
import im.threads.android.use_cases.developer_options.DevOptionsUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory<DevOptionsUseCase> { DevOptionsInteractor(androidContext()) }
    viewModel { DeveloperOptionsViewModel(get()) }
}
