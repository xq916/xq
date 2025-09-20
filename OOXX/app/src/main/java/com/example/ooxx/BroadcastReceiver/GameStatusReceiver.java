package com.example.ooxx.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GameStatusReceiver extends BroadcastReceiver {
    // 广播常量
    public static final String ACTION_GAME_STATUS = "com.example.ooxxgame.ACTION_GAME_STATUS";
    public static final String EXTRA_STATUS = "extra_status";  // 状态（开始/结束/暂停）
    public static final String EXTRA_RESULT = "extra_result";  // 结果（胜利/失败/平局）

    private GameStatusCallback gameStatusCallback;

    // 游戏状态回调接口
    public interface GameStatusCallback {
        void onGameStatusChanged(String status, String result);
    }

    // 设置回调
    public void setGameStatusCallback(GameStatusCallback callback) {
        this.gameStatusCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_GAME_STATUS.equals(intent.getAction())) {
            String status = intent.getStringExtra(EXTRA_STATUS);
            String result = intent.getStringExtra(EXTRA_RESULT);
            if (gameStatusCallback != null) {
                gameStatusCallback.onGameStatusChanged(status, result);
            }
        }
    }
}
