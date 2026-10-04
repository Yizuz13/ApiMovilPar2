package com.example.apimovilpar2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador de RecyclerView para vincular la lista de productos con el diseño item_product.xml.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductActionListener listener;
    private NumberFormat currencyFormat;

    /**
     * Interfaz para gestionar las acciones de Edición y Eliminación desde cada ítem.
     */
    public interface OnProductActionListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Nombre y Categoría
        holder.tvProductName.setText(product.getNombre());
        holder.tvProductCategory.setText(product.getCategoria());

        // Precio formateado
        holder.tvProductPrice.setText(currencyFormat.format(product.getPrecio()));

        // Stock con indicador visual por color
        int stock = product.getStock();
        if (stock == 0) {
            holder.tvProductStock.setText("Agotado (0 u.)");
            holder.tvProductStock.setTextColor(ContextCompat.getColor(context, R.color.stock_out));
        } else if (stock <= 5) {
            holder.tvProductStock.setText("Poco Stock: " + stock + " u.");
            holder.tvProductStock.setTextColor(ContextCompat.getColor(context, R.color.stock_low));
        } else {
            holder.tvProductStock.setText("Stock: " + stock + " u.");
            holder.tvProductStock.setTextColor(ContextCompat.getColor(context, R.color.stock_high));
        }

        // Configuración de listeners de botones de acción
        holder.ibEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });

        holder.ibDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * Actualiza los datos de la lista y notifica al RecyclerView.
     */
    public void updateList(List<Product> newList) {
        this.productList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para retener las referencias de las vistas de cada ítem de producto.
     */
    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductName;
        TextView tvProductCategory;
        TextView tvProductPrice;
        TextView tvProductStock;
        ImageButton ibEdit;
        ImageButton ibDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            ibEdit = itemView.findViewById(R.id.ibEditProduct);
            ibDelete = itemView.findViewById(R.id.ibDeleteProduct);
        }
    }
}
