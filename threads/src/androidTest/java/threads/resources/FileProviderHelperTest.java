package threads.resources;

import android.content.Context;
import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import androidx.test.InstrumentationRegistry;
import im.threads.internal.helpers.FileProviderHelper;
import im.threads.internal.utils.FileDownloader;

public class FileProviderHelperTest {

    private Context appContext;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @Test
    public void checkDownloadDirAccess() throws IOException {

        File outputFile = new File(FileDownloader.getDownloadDir(appContext), "testFile");
        outputFile.createNewFile();
        Uri uri = FileProviderHelper.getUriForFile(appContext, outputFile);
        Assert.assertNotNull(uri);
        Assert.assertNotEquals("File uri is empty", uri, Uri.EMPTY);
    }
}
