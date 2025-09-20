package com.example.ooxx.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import com.example.ooxx.R;

public class MusicService extends Service {
    private static MusicService instance;
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;

    public static MusicService getInstance() {
        return instance;
    }

    // Binder内部类
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // 初始化播放器
    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.game_music);
            mediaPlayer.setLooping(true);  // 循环播放
        }
    }

    // 播放音乐
    public void playMusic() {
        initMediaPlayer();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    // 暂停音乐
    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    // 停止音乐
    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 检查是否正在播放
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        stopMusic();
    }
}
