package im.threads.model;


import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.Collection;

import im.threads.utils.PrefUtils;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PrefUtilsSignatureTest {

    private Context appContext;
    private String inputSignature;
    private String expectedSignature;

    @Before
    public void setup() {
        appContext = RuntimeEnvironment.application.getApplicationContext();
    }

    public PrefUtilsSignatureTest(String inputSignature, String expectedSignature){
        this.inputSignature = inputSignature;
        this.expectedSignature = expectedSignature;
    }

    @Test
    public void saveAndGetSignature() {
        PrefUtils.setClientIdSignature(appContext, inputSignature);
        String signature = PrefUtils.getClientIdSignature(appContext);

        Assert.assertEquals(expectedSignature, signature);
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "inputSignature = {0}, expectedResult = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, ""},
                {"", ""},
                {"inputSignature", "inputSignature"},
                {"eroigjvmp458uyvb6", "eroigjvmp458uyvb6"}
        });
    }

}
