package com.example.quanlykhohang;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Màn hình nhập và thêm sản phẩm mới vào cơ sở dữ liệu
 */
public class AddProductActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etPrice;
    private EditText etQuantity;
    private Spinner spCategory;
    private SwitchCompat swActive;
    private EditText etDescription;
    private Button btnSave;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ các thành phần giao diện
        initViews();

        // Cài đặt dữ liệu cho Spinner loại sản phẩm
        setupCategorySpinner();

        // Cài đặt sự kiện bấm nút Lưu
        initEvents();
    }

    /**
     * Ánh xạ các view qua findViewById
     */
    private void initViews() {
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        spCategory = findViewById(R.id.spCategory);
        swActive = findViewById(R.id.swActive);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
    }

    /**
     * Cài đặt dữ liệu cho Spinner từ string-array categories_array trong strings.xml
     */
    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.categories_array,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(spinnerAdapter);
    }

    /**
     * Cài đặt sự kiện cho nút Lưu sản phẩm
     */
    private void initEvents() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu và lưu vào CSDL SQLite
     */
    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        String category = spCategory.getSelectedItem() != null
                ? spCategory.getSelectedItem().toString()
                : "";
        boolean active = swActive.isChecked();

        // 1. Kiểm tra tên sản phẩm
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_empty_name));
            etName.requestFocus();
            return;
        }

        // 2. Kiểm tra giá bán (không rỗng, parse được số, > 0)
        if (priceStr.isEmpty()) {
            etPrice.setError(getString(R.string.error_invalid_price));
            etPrice.requestFocus();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
            if (price <= 0) {
                etPrice.setError(getString(R.string.error_invalid_price));
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError(getString(R.string.error_invalid_price));
            etPrice.requestFocus();
            return;
        }

        // 3. Kiểm tra số lượng (không rỗng, parse được số nguyên, >= 0)
        if (quantityStr.isEmpty()) {
            etQuantity.setError(getString(R.string.error_invalid_quantity));
            etQuantity.requestFocus();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                etQuantity.setError(getString(R.string.error_invalid_quantity));
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.error_invalid_quantity));
            etQuantity.requestFocus();
            return;
        }

        // 4. Dữ liệu hợp lệ -> Khởi tạo đối tượng Product (không cần id)
        Product newProduct = new Product(name, price, quantity, category, active, description);

        // 5. Thêm vào CSDL SQLite
        long resultId = dbHelper.insertProduct(newProduct);

        if (resultId > 0) {
            Toast.makeText(this, R.string.toast_add_success, Toast.LENGTH_SHORT).show();
            // Đóng màn hình để quay lại MainActivity (MainActivity.onResume sẽ tự tải lại danh sách và thống kê)
            finish();
        } else {
            Toast.makeText(this, R.string.toast_add_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
