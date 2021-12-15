package org.freeeed.ai.inabia;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InabiaOkHttpClient {

    private int timeout = 60;
    private ConnectionPool connectionPool;
    private static InabiaOkHttpClient okHttpClientInstance;
    private OkHttpClient client;

    private InabiaOkHttpClient() {
        connectionPool = new ConnectionPool(20, 2, TimeUnit.MINUTES);

        client = new OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(connectionPool)
                .addInterceptor(new ConnectionInterceptor())
                .build();
    }

    public static InabiaOkHttpClient getInstance() {
        if (okHttpClientInstance == null) {
            okHttpClientInstance = new InabiaOkHttpClient();
        }
        return okHttpClientInstance;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public class ConnectionInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            if (response.code() == 200) {
                return response;
            } else if (response.code() == 503) {
                connectionPool.evictAll();
                throw new IOException("Server Error: " + response.code());
            } else {
                throw new IOException("Server Error: " + response.code());
            }
        }
    }

}
