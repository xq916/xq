package com.example.puzzle_server.entity;

public class GameTopic {
    private int id;
    private String initialBoard;  // 棋盘初始状态字符串
    private String target;        // 胜利条件
    private String difficulty;    // 难度级别
    
    // 构造函数
    public GameTopic(int id, String initialBoard, String target, String difficulty) {
        this.id = id;
        this.initialBoard = initialBoard;
        this.target = target;
        this.difficulty = difficulty;
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getInitialBoard() {
        return initialBoard;
    }
    
    public void setInitialBoard(String initialBoard) {
        this.initialBoard = initialBoard;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}