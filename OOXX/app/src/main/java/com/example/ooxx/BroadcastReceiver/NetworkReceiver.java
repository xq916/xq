package com.example.ooxx.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class NetworkReceiver extends BroadcastReceiver {
    private NetworkCallback networkCallback;

    // 网络状态回调接口
    public interface NetworkCallback {
        void onNetworkAvailable();  // 网络可用
        void onNetworkUnavailable();// 网络不可用
    }

    // 设置回调
    public void setNetworkCallback(NetworkCallback callback) {
        this.networkCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 检查网络状态
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());

        boolean isAvailable = nc != null &&
                nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        if (networkCallback != null) {
            if (isAvailable) {
                networkCallback.onNetworkAvailable();
            } else {
                networkCallback.onNetworkUnavailable();
            }
        }
    }
}
