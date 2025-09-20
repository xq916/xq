package com.example.ooxx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ooxx.databinding.ActivityMultiplayerLobbyBinding;

public class MultiplayerLobbyActivity extends AppCompatActivity {

    private ActivityMultiplayerLobbyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMultiplayerLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
    }

    private void setupUI() {
        // 网络对战区域点击事件

        // 刷新按钮点击事件


        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());


        // 设备列表点击事件

    }
}
