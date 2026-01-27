package org.freeeed.ai;

import okhttp3.OkHttpClient;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AIUtilLongTimeoutTest {

    @Test
    public void longIndexingClientHasVeryLongTimeouts() throws Exception {
        Field f = AIUtil.class.getDeclaredField("LONG_INDEXING_CLIENT");
        f.setAccessible(true);
        OkHttpClient client = (OkHttpClient) f.get(null);

        assertNotNull(client);
        assertTrue("connectTimeout should be >= 30s", client.connectTimeoutMillis() >= (int) TimeUnit.SECONDS.toMillis(30));
        assertTrue("readTimeout should be >= 30min", client.readTimeoutMillis() >= (int) TimeUnit.MINUTES.toMillis(30));
        assertTrue("writeTimeout should be >= 30min", client.writeTimeoutMillis() >= (int) TimeUnit.MINUTES.toMillis(30));
        assertTrue("callTimeout should be >= 30min", client.callTimeoutMillis() >= (int) TimeUnit.MINUTES.toMillis(30));
    }
}
