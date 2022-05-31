package im.threads.android.di

import im.threads.android.ui.developer_options.DeveloperOptionsVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { DeveloperOptionsVM() }
}