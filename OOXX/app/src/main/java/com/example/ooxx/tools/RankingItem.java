package com.example.ooxx.tools;

public class RankingItem {
    public int rank;
    public String name;
    public int level;
    public RankingItem(int rank, String name, int level) {
        this.rank = rank;
        this.name = name;
        this.level = level;
    }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getPlayerName() { return name; }
    public void setPlayerName(String playerName) { this.name = playerName; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

}
