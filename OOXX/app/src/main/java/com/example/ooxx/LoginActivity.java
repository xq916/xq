package com.example.ooxx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ooxx.tools.UserDbHelper;

import org.mindrot.jbcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        TextView people = findViewById(R.id.register);
        people.setOnClickListener(v -> {
            // 创建 Intent 跳转到 SecondActivity
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        });

        // 初始化视图
        etUsername = findViewById(R.id.user_username);
        etPassword = findViewById(R.id.user_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);

        // 获取SharedPreferences实例，文件名为"user_info"，模式为私有模式
        sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);

        // 检查是否有保存的凭据
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");
        String statusUsername = sharedPreferences.getString("statususername", "");
        if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
            etUsername.setText(savedUsername);
            etPassword.setText(savedPassword);
            cbRemember.setChecked(true);
        }

        // 登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 模拟登录验证
                if (login(username, password)) {
                    // 如果勾选了"记住密码"
                    if (cbRemember.isChecked()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.putString("statususername", username);
                        editor.putString("password", password);
                        editor.apply(); // 异步提交
                    } else {
                        // 如果不记住，则清除已保存的密码
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("password");
                        editor.apply();
                    }
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    // 跳转到主界面...
                    // 创建 Intent 跳转到 SecondActivity
                    Intent intent = new Intent(LoginActivity.this,GameActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 模拟登录验证
    private boolean login(String username, String password) {
        // 这里应该是你的实际登录逻辑，比如与服务器验证
        UserDbHelper dbHelper = new UserDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 查询对应用户名的记录
        String[] projection = {UserDbHelper.COLUMN_PASSWORD};
        String selection = UserDbHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                UserDbHelper.TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean loginSuccessful = false;
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String storedHashedPassword = cursor.getString(cursor.getColumnIndex(UserDbHelper.COLUMN_PASSWORD));
            // 使用 BCrypt 检查输入的密码是否与存储的哈希值匹配
            loginSuccessful = BCrypt.checkpw(password, storedHashedPassword);
        }
        cursor.close();
        db.close();
        return loginSuccessful;
    }
}
