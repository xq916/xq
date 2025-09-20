package com.example.ooxx;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.content.Context;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.ooxx.Service.MusicService;
import com.example.ooxx.databinding.ActivityGameBinding;

public class GameActivity extends AppCompatActivity {
    private String savedUsername="1";

    private ActivityGameBinding binding;
    private MusicService musicService;
    private boolean isMusicBound = false;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isMusicBound = true;

            // 同步音乐开关状态
            binding.switchMusic.setChecked(musicService.isPlaying());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定音乐服务
        bindMusicService();
        initView();

        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE); // "LoginInfo" 是保存登录信息时使用的文件名，请确保与登录时使用的文件名一致
        savedUsername = sharedPreferences.getString("statususername", ""); // "username" 是存储用户名的键，第二个参数是默认值（当找不到"username"键时返回）


        TextView tvNickname = findViewById(R.id.tv_nickname); // 请确保布局文件中有此ID的TextView

        if (!savedUsername.isEmpty()) {
            tvNickname.setText(savedUsername); // 用户名存在，设置TextView的文本
        }

        tvNickname.setOnClickListener(v -> {
            if (!savedUsername.isEmpty()) {
                // 显示对话框
                new AlertDialog.Builder(this)
                        .setTitle("要退出登录吗？")
                        .setNegativeButton("确定", (dialog, which) -> {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("statususername");
                            savedUsername="";
                            editor.apply();
                            tvNickname.setText("未登录");
                        })
                        .setPositiveButton("取消", (dialog, which) -> {

                        })
                        .show();
            } else {
                Intent intent = new Intent(GameActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        try {
            // 设置背景图片
            Glide.with(this)
                    .load(R.drawable.game_background)
                    .centerCrop()
                    .into(binding.ivBackground);
        } catch (Exception e) {
            binding.ivBackground.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        // 单人模式按钮点击事件
        binding.btnSingleMode.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, GameContainerActivity.class);
            intent.putExtra("GAME_MODE", "single");
            startActivity(intent);
        });

        // 对战模式按钮点击事件
        binding.btnVsMode.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, MultiplayerLobbyActivity.class);
            startActivity(intent);
        });



        // 排行榜按钮点击事件
        binding.btnRank.setOnClickListener(v -> {
            startActivity(new Intent(GameActivity.this, RankActivity.class));
        });

        // 规则按钮点击事件
        binding.btnRules.setOnClickListener(v -> {
            showRulesDialog();
        });

        // 音乐开关监听
        binding.switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isMusicBound) {
                if (isChecked) {
                    musicService.playMusic();
                    Toast.makeText(this, "音乐已开启", Toast.LENGTH_SHORT).show();
                } else {
                    musicService.pauseMusic();
                    Toast.makeText(this, "音乐已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 显示游戏规则对话框
     */
    private void showRulesDialog() {
        // 加载自定义布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_rules, null);

        // 创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // 设置对话框背景为圆角
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);

        // 设置对话框尺寸
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }

        // 设置确定按钮点击事件
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回主菜单时更新界面状态
        updateUI();
    }

    /**
     * 更新界面状态
     */
    private void updateUI() {
        // 例如：binding.tvUserInfo.setText("欢迎，" + getUserName());
    }

    /**
     * 获取用户名（示例）
     */
    private String getUserName() {
        return "WeChat_User123"; // 实际应用中应从SharedPreferences或数据库获取
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑音乐服务
        if (isMusicBound) {
            unbindService(musicConnection);
        }
    }
}