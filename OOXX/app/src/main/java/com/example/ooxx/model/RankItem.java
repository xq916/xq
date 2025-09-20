package com.example.ooxx.model;

public class RankItem {
    private String userName;  // 用户名
    private int totalScore;   // 总得分
    private int winCount;     // 胜利次数

    // 构造
    public RankItem(String userName, int totalScore, int winCount) {
        this.userName = userName;
        this.totalScore = totalScore;
        this.winCount = winCount;
    }

    // Getter和Setter
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    public int getWinCount() { return winCount; }
    public void setWinCount(int winCount) { this.winCount = winCount; }
}
