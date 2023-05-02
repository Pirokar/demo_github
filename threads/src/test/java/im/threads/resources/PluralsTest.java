package im.threads.resources;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collection;

import im.threads.R;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PluralsTest {

    private Context appContext;
    private final int quantity;

    public PluralsTest(int quantity) {
        this.quantity = quantity;
    }

    @Before
    public void setup() {
        appContext = RuntimeEnvironment.application.getApplicationContext();
    }

    @Test
    public void checkPluralUnreadMessages() {
        String pluralString = appContext.getResources().getQuantityString(R.plurals.ecc_unread_messages, quantity, quantity);
        Assert.assertNotNull(pluralString);
    }

    @Config(qualifiers="en")
    @Test
    public void checkENPluralUnreadMessages() {
        String pluralEnString = appContext.getResources().getQuantityString(R.plurals.ecc_unread_messages, quantity, quantity);
        Assert.assertNotNull(pluralEnString);
    }

    @Test
    public void checkPluralNewMessages() {
        String pluralString = appContext.getResources().getQuantityString(R.plurals.ecc_new_messages, quantity, quantity);
        Assert.assertNotNull(pluralString);
    }

    @Config(qualifiers="en")
    @Test
    public void checkENPluralNewMessages() {
        String pluralEnString = appContext.getResources().getQuantityString(R.plurals.ecc_new_messages, quantity, quantity);
        Assert.assertNotNull(pluralEnString);
    }

    @Test
    public void checkPluralImages() {
        String pluralString = appContext.getResources().getQuantityString(R.plurals.ecc_images, quantity, quantity);
        Assert.assertNotNull(pluralString);
    }

    @Config(qualifiers="en")
    @Test
    public void checkENPluralImages() {
        String pluralEnString = appContext.getResources().getQuantityString(R.plurals.ecc_images, quantity, quantity);
        Assert.assertNotNull(pluralEnString);
    }

    @Test
    public void checkPluralFiles() {
        String pluralString = appContext.getResources().getQuantityString(R.plurals.ecc_files, quantity, quantity);
        Assert.assertNotNull(pluralString);
    }

    @Config(qualifiers="en")
    @Test
    public void checkENPluralFiles() {
        String pluralEnString = appContext.getResources().getQuantityString(R.plurals.ecc_files, quantity, quantity);
        Assert.assertNotNull(pluralEnString);
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "quantity = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {0},
                {1},
                {2},
                {3},
                {4},
                {5},
                {6},
                {7},
                {8},
                {9},
                {10},
                {11},
                {22},
                {37},
                {59},
                {103}
        });
    }

}
