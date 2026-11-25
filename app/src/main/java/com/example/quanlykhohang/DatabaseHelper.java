package com.example.quanlykhohang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp quản lý cơ sở dữ liệu SQLite cho kho hàng
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên và phiên bản cơ sở dữ liệu
    private static final String DATABASE_NAME = "warehouse.db";
    private static final int DATABASE_VERSION = 2; // Nâng lên version 2 để tự động cập nhật

    // Bảng users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Bảng products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "id";
    public static final String COLUMN_PRODUCT_NAME = "name";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
    public static final String COLUMN_PRODUCT_CATEGORY = "category";
    public static final String COLUMN_PRODUCT_ACTIVE = "active";
    public static final String COLUMN_PRODUCT_DESCRIPTION = "description";

    // Câu lệnh tạo bảng users
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL"
            + ");";

    // Câu lệnh tạo bảng products
    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + " ("
            + COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
            + COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
            + COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
            + COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL, "
            + COLUMN_PRODUCT_ACTIVE + " INTEGER NOT NULL, "
            + COLUMN_PRODUCT_DESCRIPTION + " TEXT"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Tạo bảng users và bảng products
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PRODUCTS);

        // 2. Chèn tài khoản mẫu (admin / 123456)
        insertUserInternal(db, "admin", "123456");

        // 3. Chèn 3 sản phẩm mẫu ban đầu (dùng db truyền vào, không gọi getWritableDatabase)
        insertProductInternal(db, new Product("Chuột không dây", 150000, 20, "Điện tử", true, "Chuột quang không dây 2.4GHz"));
        insertProductInternal(db, new Product("Bàn phím cơ", 450000, 3, "Điện tử", true, "Bàn phím cơ Blue Switch chống nước"));
        insertProductInternal(db, new Product("Sổ tay A5", 25000, 40, "Văn phòng phẩm", true, "Sổ tay ghi chú bìa da 200 trang"));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ và tạo lại khi nâng cấp phiên bản CSDL
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Đảm bảo luôn có tài khoản mẫu và 3 sản phẩm mẫu kể cả khi DB cũ chưa có
     */
    private void ensureDefaultData(SQLiteDatabase db) {
        Cursor uCursor = null;
        try {
            uCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
            if (uCursor != null && uCursor.moveToFirst() && uCursor.getInt(0) == 0) {
                insertUserInternal(db, "admin", "123456");
            }
        } catch (Exception ignored) {
        } finally {
            if (uCursor != null) uCursor.close();
        }

        Cursor pCursor = null;
        try {
            pCursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
            if (pCursor != null && pCursor.moveToFirst() && pCursor.getInt(0) == 0) {
                insertProductInternal(db, new Product("Chuột không dây", 150000, 20, "Điện tử", true, "Chuột quang không dây 2.4GHz"));
                insertProductInternal(db, new Product("Bàn phím cơ", 450000, 3, "Điện tử", true, "Bàn phím cơ Blue Switch chống nước"));
                insertProductInternal(db, new Product("Sổ tay A5", 25000, 40, "Văn phòng phẩm", true, "Sổ tay ghi chú bìa da 200 trang"));
            }
        } catch (Exception ignored) {
        } finally {
            if (pCursor != null) pCursor.close();
        }
    }

    // =========================================================================
    // CÁC HÀM NỘI BỘ DÙNG CHO ONCREATE (TRÁNH LỖI ĐỆ QUY KHI KHỞI TẠO CSDL)
    // =========================================================================

    private long insertUserInternal(SQLiteDatabase db, String username, String password) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    private long insertProductInternal(SQLiteDatabase db, Product p) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, p.getName());
        values.put(COLUMN_PRODUCT_PRICE, p.getPrice());
        values.put(COLUMN_PRODUCT_QUANTITY, p.getQuantity());
        values.put(COLUMN_PRODUCT_CATEGORY, p.getCategory());
        values.put(COLUMN_PRODUCT_ACTIVE, p.isActive() ? 1 : 0);
        values.put(COLUMN_PRODUCT_DESCRIPTION, p.getDescription());
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC QUẢN LÝ NGƯỜI DÙNG
    // =========================================================================

    /**
     * Kiểm tra thông tin đăng nhập từ bảng users
     * @return true nếu tài khoản và mật khẩu khớp, ngược lại false
     */
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureDefaultData(db);

        String selection = COLUMN_USER_USERNAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = new String[]{username, password};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID}, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Thêm người dùng mới
     */
    public long insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertUserInternal(db, username, password);
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC QUẢN LÝ SẢN PHẨM
    // =========================================================================

    /**
     * Thêm sản phẩm mới vào cơ sở dữ liệu
     * @return id của sản phẩm vừa thêm, hoặc -1 nếu thất bại
     */
    public long insertProduct(Product p) {
        SQLiteDatabase db = this.getWritableDatabase();
        return insertProductInternal(db, p);
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm, sắp xếp theo id giảm dần (sản phẩm mới nhất lên đầu)
     * @return Danh sách sản phẩm (không bao giờ null)
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        ensureDefaultData(db);

        String orderBy = COLUMN_PRODUCT_ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, orderBy);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product p = extractProductFromCursor(cursor);
                    list.add(p);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * Lấy thông tin một sản phẩm theo ID
     * @return Product nếu tìm thấy, null nếu không tồn tại
     */
    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return extractProductFromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Cập nhật số lượng tồn kho của một sản phẩm
     * @return Số dòng bị ảnh hưởng
     */
    public int updateQuantity(int id, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_QUANTITY, quantity);

        String whereClause = COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return db.update(TABLE_PRODUCTS, values, whereClause, whereArgs);
    }

    /**
     * Xóa sản phẩm theo ID
     * @return Số dòng bị xóa
     */
    public int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_PRODUCT_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return db.delete(TABLE_PRODUCTS, whereClause, whereArgs);
    }

    /**
     * Tính tổng giá trị toàn bộ kho hàng (SUM(price * quantity))
     * @return Tổng giá trị tiền (VNĐ). Nếu kho trống trả về 0.
     */
    public long getTotalValue() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_PRODUCT_PRICE + " * " + COLUMN_PRODUCT_QUANTITY + ") FROM " + TABLE_PRODUCTS;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0L;
    }

    /**
     * Đếm số lượng sản phẩm sắp hết hàng (số lượng tồn < 5)
     */
    public int countLowStock() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCT_QUANTITY + " < 5";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Đếm tổng số sản phẩm hiện có trong kho
     */
    public int countProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Trích xuất thông tin đối tượng Product từ Cursor đang ở vị trí hợp lệ
     */
    private Product extractProductFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_QUANTITY));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY));
        boolean active = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ACTIVE)) == 1;
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION));

        return new Product(id, name, price, quantity, category, active, description);
    }
}
