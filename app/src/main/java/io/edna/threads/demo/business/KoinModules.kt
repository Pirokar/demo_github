package io.edna.threads.demo.business

import io.edna.threads.demo.ui.fragments.launch.LaunchViewModel
import io.edna.threads.demo.ui.fragments.server.ServerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LaunchViewModel() }
    viewModel { ServerViewModel() }
}
