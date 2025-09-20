package com.example.ooxx.room;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.ooxx.model.GameRecord;

@Database(entities = {GameRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // 单例模式
    private static volatile AppDatabase INSTANCE;

    // 获取Dao接口
    public abstract GameRecordDao gameRecordDao();

    // 获取数据库实例
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ooxx_game_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
