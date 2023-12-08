package im.threads.business.models;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import im.threads.business.models.Client;

@RunWith(Parameterized.class)
public class ClientContentTest {

    private String name;

    public ClientContentTest(String name) {
        this.name = name;
    }

    @Test
    public void setGetName() {
        Client client = new Client();
        client.setName(name);
        Assert.assertEquals(name, client.getName());
    }

    @Parameterized.Parameters(name = "name{index}")
    public static Iterable<String> dataForNameTest() {
        return Arrays.asList(
                null,
                "",
                "Name",
                "Name Surname",
                "owjhrvgopse ngpr5y4w h95uv aioejpg9584vu no;isrnuyr5984wb");
    }

}