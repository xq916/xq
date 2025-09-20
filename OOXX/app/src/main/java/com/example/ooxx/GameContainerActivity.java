package com.example.ooxx;

import static com.example.ooxx.tools.UserDbHelper.COLUMN_LEVEL;
import static com.example.ooxx.tools.UserDbHelper.COLUMN_USERNAME;
import static com.example.ooxx.tools.UserDbHelper.TABLE_USERS;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.ooxx.BroadcastReceiver.GameStatusReceiver;
import com.example.ooxx.BroadcastReceiver.NetworkReceiver;
import com.example.ooxx.Service.BluetoothService;
import com.example.ooxx.Service.MusicService;
import com.example.ooxx.Service.TimerService;
import com.example.ooxx.databinding.ActivityGameContainerBinding;
import com.example.ooxx.manager.GameManager;
import com.example.ooxx.model.BaseResponse;
import com.example.ooxx.model.GameRecord;
import com.example.ooxx.model.GameTopic;
import com.example.ooxx.Retrofit.RetrofitClient;
import com.example.ooxx.room.AppDatabase;
import com.example.ooxx.tools.UserDbHelper;

import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameContainerActivity extends AppCompatActivity implements GameManager.OnGameEventListener {
    private String savedUsername="1";
    private UserDbHelper dbHelper; // 声明你的数据库帮助类实例

    private ActivityGameContainerBinding binding;
    private GameManager gameManager;
    private GameTopic currentGame;
    private final String userName = "WeChat_User123";
    private int boardSize = 6; // 改为6x6棋盘

    // 服务相关
    private TimerService timerService;
    private MusicService musicService;
    private boolean isTimerBound = false;
    private boolean isMusicBound = false;

    // 广播接收器
    private NetworkReceiver networkReceiver;
    private GameStatusReceiver gameStatusReceiver;

    // 传感器相关
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;

    private AppDatabase db;

    // TimerService 连接
    private final ServiceConnection timerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isTimerBound = true;

            timerService.setTimerCallback(time -> {
                long seconds = time / 1000;
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
                binding.tvTimer.setText(timeStr);
            });

            timerService.startTimer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isTimerBound = false;
        }
    };

    // MusicService 连接
    private final ServiceConnection musicConnection = new ServiceConnection() {
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

    // 传感器监听器
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > 12) {
                restartGame();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取游戏模式
        String gameMode = getIntent().getStringExtra("GAME_MODE");

        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE); // "LoginInfo" 是保存登录信息时使用的文件名，请确保与登录时使用的文件名一致
        savedUsername = sharedPreferences.getString("statususername", ""); // "username" 是存储用户名的键，第二个参数是默认值（当找不到"username"键时返回）


        dbHelper = new UserDbHelper(this);

        db = AppDatabase.getInstance(this);
        setupGameManager();
        setupListeners();
        bindServices();
        registerReceivers();
        initShakeSensor();

        // 更新界面显示
        binding.tvGameMode.setText(String.format("模式: %s", getGameModeText(gameMode)));

        // 生成初始谜题
        generateInitialPuzzle();
    }

    private String getGameModeText(String gameMode) {
        switch (gameMode) {
            case "single": return "单人模式";
            case "vs": return "对战模式";
            case "bluetooth": return "蓝牙对战";
            case "online": return "网络对战";
            default: return "未知模式";
        }
    }

    private void setupGameManager() {
        gameManager = new GameManager(this,
                binding.gridBoard,
                binding.tvCurrentPlayer,
                binding.tvResult,
                this,
                boardSize);
    }

    private void setupListeners() {
        binding.btnRestart.setOnClickListener(v -> resetGame());
        binding.btnQuit.setOnClickListener(v -> finish());

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

    private void bindServices() {
        bindTimerService();
        bindMusicService();
    }

    private void bindTimerService() {
        Intent timerIntent = new Intent(this, TimerService.class);
        bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
    }

    private void bindMusicService() {
        Intent musicIntent = new Intent(this, MusicService.class);
        bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerReceivers() {
        // 注册网络状态接收器
        networkReceiver = new NetworkReceiver();
        IntentFilter networkFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        ContextCompat.registerReceiver(
                this,
                networkReceiver,
                networkFilter,
                ContextCompat.RECEIVER_EXPORTED
        );

        networkReceiver.setNetworkCallback(new NetworkReceiver.NetworkCallback() {
            @Override
            public void onNetworkAvailable() {
                Toast.makeText(GameContainerActivity.this, "网络已恢复", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNetworkUnavailable() {
                Toast.makeText(GameContainerActivity.this, "网络已断开", Toast.LENGTH_SHORT).show();
            }
        });

        // 注册游戏状态接收器
        gameStatusReceiver = new GameStatusReceiver();
        IntentFilter gameStatusFilter = new IntentFilter(GameStatusReceiver.ACTION_GAME_STATUS);
        ContextCompat.registerReceiver(
                this,
                gameStatusReceiver,
                gameStatusFilter,
                ContextCompat.RECEIVER_EXPORTED
        );

        gameStatusReceiver.setGameStatusCallback(new GameStatusReceiver.GameStatusCallback() {
            @Override
            public void onGameStatusChanged(String status, String result) {
                if ("开始".equals(status)) {
                    binding.tvResult.setText("游戏开始");
                } else if ("结束".equals(status)) {
                    binding.tvResult.setText(String.format(Locale.getDefault(), "游戏结束：%s", result));
                }
            }
        });
    }

    private void initShakeSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 悔棋功能
     */
    private void undoLastMove() {
        if (gameManager.canUndo()) {
            gameManager.undoLastMove();
            Toast.makeText(this, "已撤销上一步操作", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "没有可撤销的操作", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetGame() {
        gameManager.resetGame();
        if (isTimerBound) {
            timerService.resetTimer();
            timerService.startTimer();
        }
        sendGameStatusBroadcast("开始", null);

        // 重新生成谜题
        generateInitialPuzzle();
    }

    private void restartGame() {
        resetGame();
        Toast.makeText(this, "摇一摇重新开始游戏", Toast.LENGTH_SHORT).show();
    }

    /**
     * 生成初始谜题
     */
    private void generateInitialPuzzle() {
        // 创建6x6的初始谜题，预先放置一些X和O
        String initialBoard =
                "211221" +
                "121221" +
                "212112" +
                "212121" +
                "121212" +
                "022112";

        gameManager.initBoardFromServer(initialBoard);
    }

    // 实现 GameManager.OnGameEventListener 接口方法
    @Override
    public void onGameWin(String result, int score) {
        handleGameEnd("胜利", score + 100);
    }

    @Override
    public void onGameDraw(String result, int score) {
        handleGameEnd("平局", score);
    }

    @Override
    public void onUndoAvailable(boolean available) {

    }

    public boolean increaseUserLevel(Context context, String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabaseWithRef(); // 使用新的方法
        int currentLevel = dbHelper.getUserLevelByUsername(username);

        if (currentLevel == -1) {
            Log.e("UserDbHelper", "用户不存在: " + username);
            dbHelper.closeDatabaseWithRef(db); // 使用新的方法关闭
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_LEVEL, currentLevel + 150);

        String whereClause = COLUMN_USERNAME + " = ?";
        String[] whereArgs = {username};

        try {
            int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
            dbHelper.closeDatabaseWithRef(db); // 使用新的方法关闭
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDbHelper", "更新用户等级时出错: " + e.getMessage());
            dbHelper.closeDatabaseWithRef(db); // 即使出错也要确保关闭
            return false;
        }
    }


    private void handleGameEnd(String result, int finalScore) {
        binding.tvResult.setText(String.format(Locale.getDefault(), "游戏结束：%s！得分：%d", result, finalScore));
        if (!(savedUsername=="")){
            boolean updateSuccess = increaseUserLevel(this,savedUsername);

            if (updateSuccess) {
                Log.d("Game", "用户 " + savedUsername + " 等级提升成功！");
            } else {
                Log.e("Game", "用户等级更新失败");
            }
        }
        sendGameStatusBroadcast("结束", result);
        saveGameRecord(result, finalScore);

        // 停止计时器
        if (isTimerBound) {
            timerService.pauseTimer();
        }
    }

    @Override
    public void onPlayerChanged(int player) {
        // 玩家切换处理
    }

    @Override
    public void onCellClicked(int row, int col, int player) {
        // 检查游戏规则
        if (checkGameRules()) {
            // 如果符合所有规则，游戏胜利
            onGameWin("恭喜完成谜题", gameManager.getScore());
        }
    }

    /**
     * 检查游戏规则
     */
    private boolean checkGameRules() {
        int[][] board = gameManager.getBoard();

        // 检查每一行和每一列
        for (int i = 0; i < boardSize; i++) {
            if (!checkRow(i, board) || !checkColumn(i, board)) {
                return false;
            }
        }

        // 检查所有行和列是否唯一
        if (!checkAllRowsUnique(board) || !checkAllColumnsUnique(board)) {
            return false;
        }

        return true;
    }

    /**
     * 检查单行是否符合规则
     */
    private boolean checkRow(int row, int[][] board) {
        return checkLine(board[row]);
    }

    /**
     * 检查单列是否符合规则
     */
    private boolean checkColumn(int col, int[][] board) {
        int[] column = new int[boardSize];
        for (int i = 0; i < boardSize; i++) {
            column[i] = board[i][col];
        }
        return checkLine(column);
    }

    /**
     * 检查单行/列是否符合规则
     */
    private boolean checkLine(int[] line) {
        int xCount = 0;
        int oCount = 0;

        // 检查连续三个相同符号
        for (int i = 0; i < boardSize - 2; i++) {
            if (line[i] != 0 && line[i] == line[i + 1] && line[i] == line[i + 2]) {
                return false;
            }
        }

        // 统计X和O的数量
        for (int cell : line) {
            if (cell == 1) xCount++;
            else if (cell == 2) oCount++;
        }

        // 检查数量是否相同且没有空单元格
        return xCount == oCount && xCount + oCount == boardSize;
    }

    /**
     * 检查所有行是否唯一
     */
    private boolean checkAllRowsUnique(int[][] board) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = i + 1; j < boardSize; j++) {
                if (areLinesEqual(board[i], board[j])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查所有列是否唯一
     */
    private boolean checkAllColumnsUnique(int[][] board) {
        for (int i = 0; i < boardSize; i++) {
            int[] col1 = getColumn(board, i);
            for (int j = i + 1; j < boardSize; j++) {
                int[] col2 = getColumn(board, j);
                if (areLinesEqual(col1, col2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取指定列
     */
    private int[] getColumn(int[][] board, int col) {
        int[] column = new int[boardSize];
        for (int i = 0; i < boardSize; i++) {
            column[i] = board[i][col];
        }
        return column;
    }

    /**
     * 比较两行/列是否相同
     */
    private boolean areLinesEqual(int[] line1, int[] line2) {
        for (int i = 0; i < boardSize; i++) {
            if (line1[i] != line2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 发送游戏状态广播
     */
    private void sendGameStatusBroadcast(String status, String result) {
        Intent intent = new Intent(GameStatusReceiver.ACTION_GAME_STATUS);
        intent.putExtra("status", status);
        intent.putExtra("result", result);
        sendBroadcast(intent);
    }

    /**
     * 保存游戏记录
     */
    private void saveGameRecord(String result, int score) {
        new Thread(() -> {
            // 1. 获取游戏时长（从TimerService中获取，需确保TimerService已绑定）
            long playTime = isTimerBound ? timerService.getElapsedTime() : 0;
            // 2. 定义gameId（根据实际需求修改，此处用常量举例）
            int gameId = 1;
            // 3. 使用带参构造方法实例化GameRecord（传入5个必要参数）
            GameRecord record = new GameRecord(
                    userName,       // 参数1：用户名（已定义）
                    gameId,         // 参数2：gameId（补充）
                    result,         // 参数3：游戏结果（已传入）
                    score,          // 参数4：得分（已传入）
                    playTime        // 参数5：游戏时长（补充）
            );
            // 4. 补充其他字段（gameMode）
            record.gameMode = getIntent().getStringExtra("GAME_MODE");
            record.createTime = System.currentTimeMillis();
            // 5. 插入数据库
            db.gameRecordDao().insertRecord(record);
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 解绑服务
        if (isTimerBound) {
            unbindService(timerConnection);
        }
        if (isMusicBound) {
            unbindService(musicConnection);
        }

        // 注销广播接收器
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        if (gameStatusReceiver != null) {
            unregisterReceiver(gameStatusReceiver);
        }

        // 注销传感器监听
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }

        // 停止服务
        stopService(new Intent(this, TimerService.class));
        stopService(new Intent(this, MusicService.class));
    }
}
