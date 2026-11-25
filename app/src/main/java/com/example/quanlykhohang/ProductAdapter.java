package com.example.quanlykhohang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * Adapter cho danh sách sản phẩm hiển thị trong ListView
 */
public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final int resource;
    private final List<Product> productList;

    // Pattern ViewHolder để tối ưu hiệu năng tái sử dụng view khi cuộn danh sách
    private static class ViewHolder {
        TextView tvItemName;
        TextView tvItemCategory;
        TextView tvItemPrice;
        TextView tvItemQuantity;
        TextView tvItemLowStock;
    }

    public ProductAdapter(@NonNull Context context, int resource, @NonNull List<Product> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.productList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            holder = new ViewHolder();
            holder.tvItemName = convertView.findViewById(R.id.tvItemName);
            holder.tvItemCategory = convertView.findViewById(R.id.tvItemCategory);
            holder.tvItemPrice = convertView.findViewById(R.id.tvItemPrice);
            holder.tvItemQuantity = convertView.findViewById(R.id.tvItemQuantity);
            holder.tvItemLowStock = convertView.findViewById(R.id.tvItemLowStock);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = productList.get(position);

        if (product != null) {
            holder.tvItemName.setText(product.getName());
            holder.tvItemCategory.setText(product.getCategory());

            // Định dạng giá bán có dấu phân cách hàng nghìn (ví dụ: 150.000 đ)
            String formattedPrice = Product.formatCurrency(product.getPrice());
            holder.tvItemPrice.setText(context.getString(R.string.format_item_price, formattedPrice));

            // Hiển thị số lượng
            holder.tvItemQuantity.setText(context.getString(R.string.format_item_quantity, product.getQuantity()));

            // Kiểm tra cảnh báo sắp hết hàng (số lượng < 5)
            if (product.isLowStock()) {
                // Đổi số lượng sang màu đỏ và hiện nhãn cảnh báo
                holder.tvItemQuantity.setTextColor(ContextCompat.getColor(context, R.color.danger));
                holder.tvItemLowStock.setVisibility(View.VISIBLE);
            } else {
                // BẮT BUỘC reset lại màu và ẩn nhãn để tránh lỗi hiển thị sai khi ListView tái sử dụng convertView
                holder.tvItemQuantity.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                holder.tvItemLowStock.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
