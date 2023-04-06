package io.edna.threads.demo.business

import io.edna.threads.demo.ui.fragments.launch.LaunchViewModel
import io.edna.threads.demo.ui.fragments.server.AddServerViewModel
import io.edna.threads.demo.ui.fragments.server.ServersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { LaunchViewModel() }
    viewModel { ServersViewModel() }
    viewModel { AddServerViewModel() }
}
