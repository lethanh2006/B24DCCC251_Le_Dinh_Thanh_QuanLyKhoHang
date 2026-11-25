package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình đăng nhập của ứng dụng (màn hình khởi động đầu tiên - LAUNCHER)
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private TextView tvError;
    private Button btnLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo đối tượng thao tác cơ sở dữ liệu
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ các thành phần giao diện từ layout XML
        initViews();

        // Cài đặt sự kiện lắng nghe
        initEvents();
    }

    /**
     * Ánh xạ các view qua findViewById
     */
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);
        btnLogin = findViewById(R.id.btnLogin);

        // Tự động điền sẵn tài khoản mẫu admin / 123456 theo đề bài để người dùng bấm Đăng nhập là vào ngay
        etUsername.setText("admin");
        etPassword.setText("123456");
    }

    /**
     * Cài đặt sự kiện cho nút bấm và các ô nhập liệu
     */
    private void initEvents() {
        // Sự kiện khi nhấn nút Đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Tự động ẩn thông báo lỗi khi người dùng bắt đầu nhập lại vào ô tài khoản
        TextWatcher hideErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvError.getVisibility() == View.VISIBLE) {
                    tvError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etUsername.addTextChangedListener(hideErrorWatcher);
        etPassword.addTextChangedListener(hideErrorWatcher);
    }

    /**
     * Xử lý kiểm tra tài khoản và mật khẩu
     */
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Ẩn lỗi cũ trước khi kiểm tra
        tvError.setVisibility(View.GONE);

        // Kiểm tra tính hợp lệ của dữ liệu nhập vào (không được để trống)
        if (username.isEmpty() || password.isEmpty()) {
            tvError.setText(R.string.error_empty_login);
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        // Truy vấn kiểm tra đăng nhập từ cơ sở dữ liệu SQLite
        boolean isValid = dbHelper.checkLogin(username, password);

        if (isValid) {
            // Đăng nhập thành công -> chuyển sang MainActivity (Danh sách sản phẩm & Thống kê)
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

            // Đóng màn hình đăng nhập để khi bấm Back không bị quay lại đây
            finish();
        } else {
            // Đăng nhập thất bại -> hiển thị lỗi chính xác và giữ nguyên màn hình
            tvError.setText(R.string.error_login_failed);
            tvError.setVisibility(View.VISIBLE);
        }
    }
}
