package io.edna.threads.demo.appCode.business

import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import io.edna.threads.demo.appCode.fragments.demoSamplesFragment.DemoSamplesViewModel
import io.edna.threads.demo.appCode.fragments.demoSamplesList.DemoSamplesListViewModel
import io.edna.threads.demo.appCode.fragments.launch.LaunchViewModel
import io.edna.threads.demo.appCode.fragments.server.AddServerViewModel
import io.edna.threads.demo.appCode.fragments.server.ServersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CurrentJsonProvider(get()) }
    single { SamplesJsonProvider(get()) }
    single { StringsProvider(get()) }
    single { PreferencesProvider(get()) }
    viewModel { LaunchViewModel(get()) }
    viewModel { ServersViewModel() }
    viewModel { AddServerViewModel() }
    viewModel { DemoSamplesViewModel(get(), get()) }
    viewModel { DemoSamplesListViewModel(get(), get(), get()) }
}
