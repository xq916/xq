package com.example.ooxx.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;

public class TimerService extends Service {
    private final IBinder binder = new TimerBinder();
    private long totalTime = 0L;  // 总时长（毫秒）
    private boolean isRunning = false;
    private TimerThread timerThread;
    private TimerCallback timerCallback;

    // 回调接口（通知时间更新）
    public interface TimerCallback {
        void onTimeUpdate(long time);
    }

    // Binder内部类（供Activity绑定）
    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // 设置回调
    public void setTimerCallback(TimerCallback callback) {
        this.timerCallback = callback;
    }

    // 开始计时
    public void startTimer() {
        if (isRunning) return;
        isRunning = true;
        final long startTime = SystemClock.elapsedRealtime() - totalTime;

        timerThread = new TimerThread(() -> {
            while (isRunning) {
                totalTime = SystemClock.elapsedRealtime() - startTime;
                // 主线程更新UI
                if (timerCallback != null) {
                    runOnUiThread(() -> timerCallback.onTimeUpdate(totalTime));
                }
                try {
                    Thread.sleep(1000);  // 每秒更新一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        timerThread.start();
    }

    // 暂停计时
    public void pauseTimer() {
        isRunning = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    // 重置计时
    public void resetTimer() {
        pauseTimer();
        totalTime = 0L;
        if (timerCallback != null) {
            runOnUiThread(() -> timerCallback.onTimeUpdate(totalTime));
        }
    }

    // 获取总时长
    public long getTotalTime() {
        return totalTime;
    }

    // 获取已流逝时间（与getTotalTime功能一致，作为补充方法）
    public long getElapsedTime() {
        return totalTime;
    }

    // 主线程执行工具
    private void runOnUiThread(Runnable runnable) {
        new android.os.Handler(getMainLooper()).post(runnable);
    }

    // 计时线程
    private static class TimerThread extends Thread {
        private final Runnable task;

        public TimerThread(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            super.run();
            if (task != null) {
                task.run();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pauseTimer();
    }
}
