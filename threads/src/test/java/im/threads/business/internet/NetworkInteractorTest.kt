package im.threads.business.internet

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import im.threads.business.utils.internet.NetworkInteractorImpl
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NetworkInteractorTest {
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkInteractor: NetworkInteractorImpl

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        connectivityManager = Mockito.mock(ConnectivityManager::class.java)
        networkInteractor = NetworkInteractorImpl()
        `when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
    }

    @Test
    fun whenWifiConnected_thenHasNoInternetReturnsFalse() {
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        assert(!networkInteractor.hasNoInternet(context))
    }

    @Test
    fun whenCellularConnected_thenHasNoInternetReturnsFalse() {
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        assert(!networkInteractor.hasNoInternet(context))
    }

    @Test
    fun whenEthernetConnected_thenHasNoInternetReturnsFalse() {
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).thenReturn(true)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        assert(!networkInteractor.hasNoInternet(context))
    }

    @Test
    fun whenNoConnection_thenHasNoInternetReturnsTrue() {
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(false)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(false)
        `when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).thenReturn(false)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        assert(networkInteractor.hasNoInternet(context))
    }
}
