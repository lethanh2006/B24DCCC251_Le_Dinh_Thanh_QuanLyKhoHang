package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Màn hình chính: Thống kê kho hàng và hiển thị danh sách sản phẩm
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvTotalProducts;
    private TextView tvTotalValue;
    private TextView tvLowStockCount;
    private ListView lvProducts;
    private TextView tvEmpty;
    private Button btnAddProduct;

    private DatabaseHelper dbHelper;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo đối tượng CSDL
        dbHelper = new DatabaseHelper(this);

        // Ánh xạ View từ file giao diện XML
        initViews();

        // Cài đặt sự kiện click
        initEvents();

        // Tải dữ liệu ban đầu
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tự động tải lại danh sách và cập nhật số liệu thống kê mỗi khi quay lại màn hình này
        loadData();
    }

    /**
     * Ánh xạ các view qua findViewById
     */
    private void initViews() {
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvLowStockCount = findViewById(R.id.tvLowStockCount);
        lvProducts = findViewById(R.id.lvProducts);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        // Gán view hiển thị khi ListView rỗng
        lvProducts.setEmptyView(tvEmpty);
    }

    /**
     * Cài đặt các sự kiện tương tác
     */
    private void initEvents() {
        // Nhấn vào một sản phẩm trong danh sách -> mở màn hình Chi tiết sản phẩm
        lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = (Product) parent.getItemAtPosition(position);
                if (selectedProduct != null) {
                    Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                    // Chỉ truyền id sản phẩm qua Intent theo yêu cầu
                    intent.putExtra("product_id", selectedProduct.getId());
                    startActivity(intent);
                }
            }
        });

        // Nhấn nút Thêm sản phẩm -> mở màn hình Thêm sản phẩm
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Truy vấn lại CSDL và cập nhật giao diện danh sách cùng khối thống kê
     */
    private void loadData() {
        // 1. Cập nhật danh sách sản phẩm
        List<Product> productList = dbHelper.getAllProducts();
        adapter = new ProductAdapter(this, R.layout.item_product, productList);
        lvProducts.setAdapter(adapter);

        // 2. Cập nhật khối thống kê từ SQLite
        int totalProducts = dbHelper.countProducts();
        long totalValue = dbHelper.getTotalValue();
        int lowStockCount = dbHelper.countLowStock();

        // Định dạng chuỗi hiển thị
        tvTotalProducts.setText(getString(R.string.format_total_products, totalProducts));

        String formattedTotalValue = Product.formatCurrency(totalValue);
        tvTotalValue.setText(getString(R.string.format_total_value, formattedTotalValue));

        tvLowStockCount.setText(getString(R.string.format_low_stock_count, lowStockCount));

        // Đổi màu cảnh báo đỏ nếu có sản phẩm sắp hết hàng
        if (lowStockCount > 0) {
            tvLowStockCount.setTextColor(ContextCompat.getColor(this, R.color.danger));
        } else {
            tvLowStockCount.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }
}
