package com.example.quanlykhohang;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Lớp đại diện cho một sản phẩm trong kho hàng
 */
public class Product {
    private int id;
    private String name;
    private int price;          // Giá bán (VNĐ)
    private int quantity;       // Số lượng tồn kho
    private String category;    // Loại sản phẩm (Điện tử, Thực phẩm, Văn phòng phẩm, Gia dụng, Khác)
    private boolean active;     // Trạng thái: true = đang kinh doanh, false = ngừng kinh doanh
    private String description; // Mô tả sản phẩm (có thể rỗng)

    // Constructor rỗng
    public Product() {
    }

    // Constructor không có id (sử dụng khi thêm sản phẩm mới vào CSDL)
    public Product(String name, int price, int quantity, String category, boolean active, String description) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.active = active;
        this.description = description;
    }

    // Constructor đầy đủ tham số (sử dụng khi đọc từ CSDL)
    public Product(int id, String name, int price, int quantity, String category, boolean active, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.active = active;
        this.description = description;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Kiểm tra sản phẩm có sắp hết hàng hay không (dưới 5 sản phẩm)
     */
    public boolean isLowStock() {
        return quantity < 5;
    }

    /**
     * Tính tổng giá trị tồn kho của sản phẩm (giá bán * số lượng)
     */
    public long getStockValue() {
        return (long) price * quantity;
    }

    /**
     * Hàm tiện ích định dạng số tiền sang định dạng tiền tệ Việt Nam (ví dụ: 150.000)
     */
    public static String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}
