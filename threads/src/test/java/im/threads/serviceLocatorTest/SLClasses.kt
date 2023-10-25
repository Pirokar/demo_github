package im.threads.serviceLocatorTest

class SLTestClass1()

class SLTestClass2(private val class1: SLTestClass1)

class SLTestClass3(
    private val class1: SLTestClass1,
    private val class2: SLTestClass2
)
