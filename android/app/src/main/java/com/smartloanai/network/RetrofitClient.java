package com.smartloanai.network;

import com.smartloanai.SmartLoanApp;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Singleton Retrofit client with JWT auth interceptor.
 */
public class RetrofitClient {

    private static final String BASE_URL = com.smartloanai.BuildConfig.BASE_URL;
    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient() {
        // Logging interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor - adds JWT token to requests
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                String token = SmartLoanApp.getInstance().getAuthToken();
                if (token != null) {
                    builder.header("Authorization", "Bearer " + token);
                }

                builder.header("Content-Type", "application/json");
                return chain.proceed(builder.build());
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }
}
