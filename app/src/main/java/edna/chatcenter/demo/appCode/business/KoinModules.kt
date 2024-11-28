package edna.chatcenter.demo.appCode.business

import edna.chatcenter.demo.appCode.business.mockJsonProvider.CurrentJsonProvider
import edna.chatcenter.demo.appCode.business.mockJsonProvider.SamplesJsonProvider
import edna.chatcenter.demo.appCode.fragments.demoSamplesFragment.DemoSamplesViewModel
import edna.chatcenter.demo.appCode.fragments.demoSamplesList.DemoSamplesListViewModel
import edna.chatcenter.demo.appCode.fragments.log.LogViewModel
import edna.chatcenter.demo.appCode.fragments.server.AddServerViewModel
import edna.chatcenter.demo.appCode.fragments.server.ServerListViewModel
import edna.chatcenter.demo.appCode.fragments.user.AddUserViewModel
import edna.chatcenter.demo.appCode.fragments.user.UserListViewModel
import edna.chatcenter.demo.integrationCode.fragments.launch.LaunchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CurrentJsonProvider(get()) }
    single { SamplesJsonProvider(get()) }
    single { StringsProvider(get()) }
    single { PreferencesProvider(get()) }
    single { UiThemeProvider(get()) }
    factory { ServersProvider(get(), get()) }
    viewModel { LaunchViewModel(get(), get()) }
    viewModel { UserListViewModel(get()) }
    viewModel { AddUserViewModel(get()) }
    viewModel { ServerListViewModel(get(), get()) }
    viewModel { AddServerViewModel(get()) }
    viewModel { DemoSamplesViewModel(get(), get()) }
    viewModel { DemoSamplesListViewModel(get(), get(), get()) }
    viewModel { LogViewModel() }
}
