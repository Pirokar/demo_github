package io.edna.threads.demo.appCode.business

import io.edna.threads.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import io.edna.threads.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import io.edna.threads.demo.appCode.fragments.demoSamplesFragment.DemoSamplesViewModel
import io.edna.threads.demo.appCode.fragments.demoSamplesList.DemoSamplesListViewModel
import io.edna.threads.demo.appCode.fragments.launch.LaunchViewModel
import io.edna.threads.demo.appCode.fragments.server.ServerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CurrentJsonProvider(get()) }
    single { SamplesJsonProvider() }
    single { StringsProvider(get()) }
    viewModel { LaunchViewModel() }
    viewModel { ServerViewModel() }
    viewModel { DemoSamplesViewModel(get()) }
    viewModel { DemoSamplesListViewModel(get(), get(), get()) }
}
