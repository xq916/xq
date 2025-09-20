package com.example.ooxx.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.lifecycle.LiveData;
import java.util.List;

import com.example.ooxx.model.RankItem;
import com.example.ooxx.model.GameRecord;

@Dao
public interface GameRecordDao {
    // 插入战绩
    @Insert
    void insertRecord(GameRecord record);

    // 查询用户所有战绩（LiveData自动更新UI）
    @Query("SELECT * FROM game_records WHERE userName = :userName ORDER BY createTime DESC")
    LiveData<List<GameRecord>> getUserRecords(String userName);

    // 查询排行榜数据（总得分降序）
    @Query("SELECT userName, SUM(score) as totalScore, COUNT(CASE WHEN result = '胜利' THEN 1 END) as winCount " +
            "FROM game_records GROUP BY userName ORDER BY totalScore DESC")
    LiveData<List<RankItem>> getRankList();

    // 清空用户战绩
    @Query("DELETE FROM game_records WHERE userName = :userName")
    void clearUserRecords(String userName);
}
