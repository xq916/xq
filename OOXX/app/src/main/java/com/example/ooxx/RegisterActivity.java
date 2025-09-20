package com.example.ooxx;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.mindrot.jbcrypt.BCrypt;

import com.example.ooxx.tools.UserDbHelper;

import java.io.File;
import java.io.UnsupportedEncodingException;


public class RegisterActivity extends AppCompatActivity {

    private EditText mUsernameEditText, mPasswordEditText;
    private Button mRegisterButton;
    private UserDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mDbHelper = new UserDbHelper(this);
        mUsernameEditText = findViewById(R.id.et_username);
        mPasswordEditText = findViewById(R.id.et_password);
        mRegisterButton = findViewById(R.id.btn_register);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = mUsernameEditText.getText().toString().trim();
        String plainPassword = mPasswordEditText.getText().toString().trim();

        if (username.isEmpty() || plainPassword.isEmpty()) {
            Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 检查用户名是否已存在
        if (isUsernameExists(username)) {
            Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 对明文密码进行哈希加密
        String hashedPassword = hashPassword(plainPassword);

        // 3. 将用户名和哈希密码存入数据库
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
//        mDbHelper.onUpgrade(db,9,10);
        ContentValues values = new ContentValues();
        values.put(UserDbHelper.COLUMN_USERNAME, username);
        values.put(UserDbHelper.COLUMN_PASSWORD, hashedPassword);

        long newRowId = db.insert(UserDbHelper.TABLE_USERS, null, values);
        db.close();

        if (newRowId == -1) {
            Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
            try {
                String dbPath = getDatabasePath("UserDatabase.db").getAbsolutePath();
                // 将原始字符串按正确编码获取字节（通常UTF-8是Android默认）
                byte[] bytes = dbPath.getBytes("UTF-8");
                // 再按UTF-8编码重新构造字符串，确保数据一致性
                String decodedPath = new String(bytes, "UTF-8");
                Log.d("DatabaseDebug", "解码后的数据库路径: " + decodedPath);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            finish(); // 关闭注册页面
        }
    }

    // 检查用户名是否已存在
    private boolean isUsernameExists(String username) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {UserDbHelper.COLUMN_ID};
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
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // 使用BCrypt哈希密码
    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }
}