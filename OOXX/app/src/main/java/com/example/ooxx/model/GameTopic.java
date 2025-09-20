package com.example.ooxx.model;

public class GameTopic {
    private int id;               // 题目ID
    private String initialBoard;  // 初始棋盘（9个字符，0=空，1=X，2=O）
    private String target;        // 目标结果（胜利条件）
    private String difficulty;    // 难度（简单/中等/困难）

    // 空构造（Gson解析必需）
    public GameTopic() {}

    // 全参构造
    public GameTopic(int id, String initialBoard, String target, String difficulty) {
        this.id = id;
        this.initialBoard = initialBoard;
        this.target = target;
        this.difficulty = difficulty;
    }

    // Getter和Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getInitialBoard() { return initialBoard; }
    public void setInitialBoard(String initialBoard) { this.initialBoard = initialBoard; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
