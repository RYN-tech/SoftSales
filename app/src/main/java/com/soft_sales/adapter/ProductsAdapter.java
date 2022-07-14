
package com.soft_sales.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.soft_sales.R;
import com.soft_sales.databinding.CategoryRowBinding;
import com.soft_sales.databinding.ProductRowBinding;
import com.soft_sales.model.CategoryWithProducts;
import com.soft_sales.model.ProductModel;
import com.soft_sales.uis.activity_create_sales_invoice.CreateSalesInvoiceActivity;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProductModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private MyHolder oldHolder;

    public ProductsAdapter(Context context, String lang) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lang = lang;
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {


        ProductRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.product_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        ProductModel productModel = list.get(position);
        myHolder.binding.setLang(lang);
        myHolder.binding.setModel(productModel);


        myHolder.itemView.setOnClickListener(view -> {
            if (oldHolder != null) {
                ProductModel oldModel = list.get(oldHolder.getAdapterPosition());
                if (oldModel.isSelected()) {
                    oldModel.setSelected(false);
                    list.set(oldHolder.getAdapterPosition(), oldModel);
                    oldHolder.binding.setModel(oldModel);
                }


            }

            ProductModel model = list.get(myHolder.getAdapterPosition());

            if (!model.isSelected()) {
                model.setSelected(true);
                list.set(myHolder.getAdapterPosition(), model);
                myHolder.binding.setModel(model);
                oldHolder = myHolder;

            }

            CreateSalesInvoiceActivity activity = (CreateSalesInvoiceActivity) context;
            activity.setItemProduct(myHolder.getAdapterPosition(),model);

        });

    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public ProductRowBinding binding;

        public MyHolder(ProductRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public void updateList(List<ProductModel> list) {
        if (list == null) {
            if (this.list != null) {
                this.list.clear();

            }

        } else {
            this.list = list;

        }
        notifyDataSetChanged();
    }

}
