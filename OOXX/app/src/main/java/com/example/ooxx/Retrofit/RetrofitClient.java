package com.example.ooxx.Retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    // 服务器基础地址（需替换为实际地址）
    private static final String BASE_URL = "http://192.168.1.100:8080/";
    private static RetrofitClient instance;
    private final GameApiService apiService;

    // 私有构造（初始化Retrofit）
    private RetrofitClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(GameApiService.class);
    }

    // 单例获取
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // 获取API接口
    public GameApiService getApiService() {
        return apiService;
    }
}
