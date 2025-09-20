package com.example.ooxx;

import static com.example.ooxx.tools.UserDbHelper.COLUMN_LEVEL;
import static com.example.ooxx.tools.UserDbHelper.COLUMN_USERNAME;

import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.example.ooxx.databinding.ActivityRankBinding;
import com.example.ooxx.databinding.ItemRankBinding;

import com.example.ooxx.tools.RankingAdapter;
import com.example.ooxx.tools.RankingItem;
import com.example.ooxx.tools.UserDbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankActivity extends AppCompatActivity {
    private Button binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);//全屏
        setContentView(R.layout.activity_rank);
        ListView listView = findViewById(R.id.ranking_list);
        List<RankingItem> rankingData = new ArrayList<>();
        UserDbHelper dbHelper = new UserDbHelper(this); // this 是你的 Context
        Cursor cursor = dbHelper.getUsersRankedByLevel(); // 执行查询
        binding = findViewById(R.id.btn_back);
//        setContentView(binding.getRoot());


        binding.setOnClickListener(v -> finish());



        if (cursor != null && cursor.moveToFirst()) {
            int rankNumber = 1; // 排名从1开始
            do {
                // 从cursor中获取用户名和等级
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEVEL));
                // 创建 RankingItem 并添加到列表
                rankingData.add(new RankingItem(rankNumber, username, level));
                rankNumber++; // 排名递增
            } while (cursor.moveToNext());
            cursor.close(); // 关闭cursor
        }
        dbHelper.close(); // 关闭数据库连接

        for (int i = 0; i < rankingData.size(); i++) {
            rankingData.get(i).rank = i + 1;
        }
        RankingAdapter adapter = new RankingAdapter(this, rankingData);
        Log.d("mydata", String.valueOf(rankingData.get(0).name));
        try {
            listView.setAdapter(adapter);
        }catch (Exception e){
            Log.d("myerror", String.valueOf(e));
        }
    }
}
