package com.cuelogic.android.nfc.api;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private static final String APP_ID = "e1477e44";
    private static final String APP_KEY = "84b1ef7d35856376bf460d274b81e60d";
    private static ApiManager apiManager;
    private final WebApi service;

    private ApiManager() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://od-api.oxforddictionaries.com/api/v1/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        service = retrofit.create(WebApi.class);


        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
//                        .addHeader("", APP_ID)
//                        .addHeader("app_key", APP_KEY)
                        .build();
                return chain.proceed(request);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://connect-live.blacklinesafety.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(WebApi.class);

    }

    public static ApiManager getInstance() {
        if (apiManager == null) {
            apiManager = new ApiManager();
        }
        return apiManager;
    }

    public void assignDevice(String token, RequestInfo info, Callback<Object> callback) {
        Call<Object> dictionaryEntries = service.assignDevice(token, info);
        dictionaryEntries.enqueue(callback);
    }

}
