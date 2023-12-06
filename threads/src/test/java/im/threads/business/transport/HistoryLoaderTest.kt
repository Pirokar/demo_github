package im.threads.business.transport

import im.threads.business.utils.AppInfo
import im.threads.business.utils.DemoModeProvider
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class HistoryLoaderTest {

    private val demoModeProvider = mock(DemoModeProvider::class.java)
    private val appInfo = mock(AppInfo::class.java)
    private val historyLoader = HistoryLoader(demoModeProvider, appInfo)

    @Test
    fun whenGetCookiesStringIsCalledWithDemoModeEnabled_thenReturnsMockHistory() {
        `when`(demoModeProvider.isDemoModeEnabled()).thenReturn(true)
        `when`(demoModeProvider.getHistoryMock()).thenReturn("{}")
        val result = historyLoader.getHistorySync(null, null)
        Assert.assertNotNull(result)
    }

    @Test(expected = Exception::class)
    fun whenGetCookiesStringIsCalledWithEmptyToken_thenThrowsException() {
        `when`(demoModeProvider.isDemoModeEnabled()).thenReturn(false)
        historyLoader.getHistorySync(null, null)
    }
}
