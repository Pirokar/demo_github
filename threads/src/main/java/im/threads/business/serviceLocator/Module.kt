package im.threads.business.serviceLocator

import im.threads.business.config.BaseConfig
import im.threads.business.preferences.Preferences
import im.threads.business.preferences.PreferencesImpl
import im.threads.business.serviceLocator.core.module

val serviceLocatorModule = module {
    factory { BaseConfig.instance.context }
    factory<Preferences> { PreferencesImpl(get()) }
}
