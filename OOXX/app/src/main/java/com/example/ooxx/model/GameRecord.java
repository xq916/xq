package com.example.ooxx.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_records")
public class GameRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userName;       // 用户名（微信登录获取）
    public String result;         // 结果（胜利/失败/平局）
    public int score;             // 得分
    public long playTime;         // 游戏时长（毫秒）
    public long createTime;       // 记录时间

    public String gameMode;

    public GameRecord() {
    }

    // 构造（不含自增ID）
    public GameRecord(String userName, int gameId, String result, int score, long playTime) {
        this.userName = userName;
        this.result = result;
        this.score = score;
        this.playTime = playTime;
        this.createTime = System.currentTimeMillis();
    }

    // Getter和Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public long getPlayTime() { return playTime; }
    public void setPlayTime(long playTime) { this.playTime = playTime; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}
