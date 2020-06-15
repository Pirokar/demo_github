package im.threads.internal.opengraph;

import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import java.util.Arrays;
import java.util.Collection;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class OGDataProviderTest {
    private static final String TAG = OGDataProviderTest.class.getSimpleName();
    private String url;
    private final boolean mustSucceed;
    boolean testFinished = false;

    public OGDataProviderTest(String url, boolean mustSucceed) {
        this.url = url;
        this.mustSucceed = mustSucceed;
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
