package com.example.quanlykhohang;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình xem chi tiết và thực hiện các thao tác: Nhập thêm, Bán, Xóa sản phẩm
 */
public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvDetailName;
    private TextView tvDetailCategory;
    private TextView tvDetailPrice;
    private TextView tvDetailQuantity;
    private TextView tvDetailStockValue;
    private TextView tvDetailWarning;
    private TextView tvDetailDescription;

    private Button btnImport;
    private Button btnSell;
    private Button btnBack;
    private Button btnDelete;

    private DatabaseHelper dbHelper;
    private int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Nhận ID sản phẩm từ Intent (nếu mở trực tiếp để kiểm thử thì mặc định ID = 2: Bàn phím cơ)
        productId = getIntent().getIntExtra("product_id", 2);
        if (productId <= 0) {
            productId = 2;
        }

        // Ánh xạ các thành phần giao diện
        initViews();

        // Cài đặt các sự kiện nút bấm
        initEvents();

        // Hiển thị thông tin sản phẩm từ CSDL
        showProduct();
    }

    /**
     * Ánh xạ các view qua findViewById
     */
    private void initViews() {
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvDetailStockValue = findViewById(R.id.tvDetailStockValue);
        tvDetailWarning = findViewById(R.id.tvDetailWarning);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);

        btnImport = findViewById(R.id.btnImport);
        btnSell = findViewById(R.id.btnSell);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
    }

    /**
     * Cài đặt các sự kiện bấm nút
     */
    private void initEvents() {
        // Nhấn nút Nhập thêm 1
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImport();
            }
        });

        // Nhấn nút Bán 1
        btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSell();
            }
        });

        // Nhấn nút Quay lại
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Đóng màn hình hiện tại để quay về MainActivity
                finish();
            }
        });

        // Nhấn nút Xóa sản phẩm
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    /**
     * Truy vấn lại CSDL theo id và cập nhật toàn bộ TextView
     */
    private void showProduct() {
        Product product = dbHelper.getProductById(productId);

        if (product == null) {
            Toast.makeText(this, R.string.toast_product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 1. Tên sản phẩm
        tvDetailName.setText(product.getName());

        // Loại sản phẩm và Trạng thái kinh doanh
        String statusText = product.isActive()
                ? getString(R.string.status_active)
                : getString(R.string.status_inactive);
        String categoryAndStatus = product.getCategory() + "  •  " + statusText;
        tvDetailCategory.setText(categoryAndStatus);

        // 2. Giá bán
        String formattedPrice = Product.formatCurrency(product.getPrice());
        tvDetailPrice.setText(getString(R.string.format_detail_price, formattedPrice));

        // 3. Số lượng tồn kho và Tổng giá trị tồn kho
        tvDetailQuantity.setText(getString(R.string.format_detail_quantity, product.getQuantity()));

        String formattedStockValue = Product.formatCurrency(product.getStockValue());
        tvDetailStockValue.setText(getString(R.string.format_detail_stock_value, formattedStockValue));

        // Cảnh báo nếu số lượng tồn < 5
        if (product.isLowStock()) {
            tvDetailWarning.setVisibility(View.VISIBLE);
        } else {
            tvDetailWarning.setVisibility(View.GONE);
        }

        // 4. Mô tả sản phẩm
        if (product.getDescription().trim().isEmpty()) {
            tvDetailDescription.setText(getString(R.string.no_description));
        } else {
            tvDetailDescription.setText(product.getDescription());
        }
    }

    /**
     * Xử lý nhập thêm 1 sản phẩm vào kho
     */
    private void handleImport() {
        Product product = dbHelper.getProductById(productId);
        if (product != null) {
            int newQuantity = product.getQuantity() + 1;
            dbHelper.updateQuantity(productId, newQuantity);
            showProduct();
        }
    }

    /**
     * Xử lý bán 1 sản phẩm
     */
    private void handleSell() {
        Product product = dbHelper.getProductById(productId);
        if (product != null) {
            if (product.getQuantity() == 0) {
                // Nếu đã hết hàng thì báo lỗi và KHÔNG giảm tiếp (số lượng không bao giờ âm)
                Toast.makeText(this, R.string.toast_out_of_stock, Toast.LENGTH_SHORT).show();
            } else {
                int newQuantity = product.getQuantity() - 1;
                dbHelper.updateQuantity(productId, newQuantity);
                showProduct();
            }
        }
    }

    /**
     * Hiển thị hộp thoại AlertDialog xác nhận trước khi xóa
     */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_confirm_delete_title)
                .setMessage(R.string.dialog_confirm_delete_message)
                .setPositiveButton(R.string.dialog_action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteProduct(productId);
                        Toast.makeText(ProductDetailActivity.this, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
                        // Đóng màn hình chi tiết để quay lại danh sách
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_action_cancel, null)
                .show();
    }
}
