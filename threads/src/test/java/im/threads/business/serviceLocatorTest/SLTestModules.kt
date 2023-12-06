package im.threads.business.serviceLocatorTest

import im.threads.business.serviceLocator.core.module

val mainTestSLModule = module {
    factory { SLTestClass1() }
    factory { SLTestClass2(get()) }
}

val supplementaryTestSLModule = module {
    factory { SLTestClass3(get(), get()) }
}
