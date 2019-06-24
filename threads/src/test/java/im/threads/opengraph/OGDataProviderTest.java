package im.threads.opengraph;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import im.threads.model.ChatStyle;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class OGDataProviderTest {

    private Context appContext;
    private String url;
    private final boolean mustSucceed;
    boolean testFinished = false;

    public OGDataProviderTest(String url, boolean mustSucceed) {
        this.url = url;
        this.mustSucceed = mustSucceed;
    }

    @Before
    public void setup() {
        appContext = RuntimeEnvironment.application.getApplicationContext();
    }

    @Test
    public void getOGData() {

        ChatStyle.updateContext(appContext);

        final CountDownLatch signal = new CountDownLatch(1);

        OGDataProvider.getOGData(url, new OGDataProvider.Callback<OGData>() {
            @Override
            public void onSuccess(OGData data) {
                Assert.assertTrue(mustSucceed);
                testFinished = true;
                signal.countDown();
            }

            @Override
            public void onError(Throwable error) {
                Assert.assertFalse(mustSucceed);
                testFinished = true;
                signal.countDown();
            }
        });

        try {
            signal.await(2000, TimeUnit.MILLISECONDS);
            Assert.assertTrue("Some exception occured, or test timed out", testFinished);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "url = {0}, mustSucceed = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Пожалуйста.\uD83D\uDE0A\uD83C\uDF3A", false},//from THREADS-5329
                {"mistyped.dot", false},
                {"threads.im", true},
        });
    }

}
