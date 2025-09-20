package com.example.ooxx;

import android.app.Application;
import android.content.Intent;
import com.example.ooxx.Service.MusicService;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 启动音乐服务
        startService(new Intent(this, MusicService.class));
    }
}



