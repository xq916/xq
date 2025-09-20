package com.example.ooxx.tools;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // 表名和列名
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password"; // 存储的是哈希后的密码
    public static final String COLUMN_LEVEL = "level"; // 新增的字段

    private SQLiteDatabase mDatabase;
    private AtomicInteger mOpenCounter = new AtomicInteger(); // 引用计数器

    // 获取可写数据库（带引用计数）
    public synchronized SQLiteDatabase getWritableDatabaseWithRef() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // 如果计数器从0变为1，说明需要打开新的数据库连接
            mDatabase = super.getWritableDatabase();
        }
        return mDatabase;
    }

    // 获取可读数据库（带引用计数）
    public synchronized SQLiteDatabase getReadableDatabaseWithRef() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // 如果计数器从0变为1，说明需要打开新的数据库连接
            mDatabase = super.getReadableDatabase();
        }
        return mDatabase;
    }

    // 关闭数据库（带引用计数）
    public synchronized void closeDatabaseWithRef(SQLiteDatabase db) {
        if (mOpenCounter.decrementAndGet() == 0) {
            // 如果计数器减到0，说明没有其他操作在使用数据库，可以关闭
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // 创建表的SQL语句
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL"+
                    ")";

    public UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_LEVEL + " INTEGER DEFAULT 0");

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 这是一个简单的升级策略：丢弃旧表，创建新表。
        // 在实际应用中，你需要在这里实现数据迁移逻辑，否则用户数据会丢失。
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_LEVEL + " INTEGER DEFAULT 0");
    }

    /**
     * 根据用户名获取其等级（level）
     * @param username 要查询的用户名
     * @return 对应用户的等级，如果用户不存在则返回 -1 或你认为合适的默认值（如0）
     */
    public int getUserLevelByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase(); // 获取可读数据库
        int level = -1; // 使用-1表示用户不存在，这是一个通用的约定
        Cursor cursor = null;

        // 定义要查询的列
        String[] projection = {COLUMN_LEVEL}; // 使用常量，避免硬编码
        // 定义查询条件
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        try {
            // 执行查询
            cursor = db.query(
                    TABLE_USERS,         // 表名
                    projection,          // 要返回的列
                    selection,           // WHERE 条件
                    selectionArgs,       // WHERE 条件的参数
                    null,
                    null,
                    null                 // Group by, having, order by
            );

            // 🔴 关键修改：正确处理游标
            // 1. 检查游标不为null
            // 2. 检查游标中有数据（moveToFirst()返回true）
            // 3. 检查列索引有效
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_LEVEL);
                if (columnIndex != -1) {
                    level = cursor.getInt(columnIndex);
                } else {
                    Log.e("UserDbHelper", "Column 'level' not found in the result set.");
                    // 列不存在，可以根据需要处理，这里保持level为-1（用户不存在）
                }
            } else {
                Log.d("UserDbHelper", "No user found with username: " + username);
                // 游标为空或没有数据，level保持为-1（用户不存在）
            }
        } catch (Exception e) {
            Log.e("UserDbHelper", "Error querying user level for username: " + username, e);
            level = -1; // 确保异常时返回默认值
        } finally {
            // 非常重要：关闭cursor以释放资源
            if (cursor != null) {
                cursor.close();
            }
            // 🔴 关键修改：移除了 db.close();
            // 让 SQLiteOpenHelper 管理数据库连接的生命周期
            // 避免其他地方出现 "attempt to re-open an already-closed object" 错误
        }
        return level;
    }

    public Cursor getUsersRankedByLevel() {
        SQLiteDatabase db = this.getReadableDatabase();
        // 按等级降序排序，等级相同则按用户名排序，并限制返回前10条记录[1,2](@ref)
        String query = "SELECT * FROM " + TABLE_USERS +
                " ORDER BY " + COLUMN_LEVEL + " DESC, " +
                COLUMN_USERNAME + " COLLATE NOCASE ASC " +
                "LIMIT 10"; // 添加LIMIT 10
        return db.rawQuery(query, null);
    }
}