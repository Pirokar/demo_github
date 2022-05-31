package im.threads.android.di

import im.threads.android.ui.developer_options.DeveloperOptionsVM
import im.threads.android.use_cases.developer_options.DeveloperOptionsInteractor
import im.threads.android.use_cases.developer_options.DeveloperOptionsUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory<DeveloperOptionsUseCase> { DeveloperOptionsInteractor(androidContext()) }
    viewModel { DeveloperOptionsVM() }
}
