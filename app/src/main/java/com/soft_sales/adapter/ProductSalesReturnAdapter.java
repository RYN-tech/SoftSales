
package com.soft_sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.soft_sales.R;
import com.soft_sales.databinding.ProductRowBinding;
import com.soft_sales.databinding.ProductSalesReturnRowBinding;
import com.soft_sales.model.ProductModel;
import com.soft_sales.uis.activity_create_sales_invoice.CreateSalesInvoiceActivity;

import java.util.List;

public class ProductSalesReturnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProductModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;
    private MyHolder oldHolder;

    public ProductSalesReturnAdapter(Context context, String lang) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lang = lang;
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {


        ProductSalesReturnRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.product_sales_return_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        ProductModel productModel = list.get(position);
        myHolder.binding.setLang(lang);
        myHolder.binding.setModel(productModel);



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
        public ProductSalesReturnRowBinding binding;

        public MyHolder(ProductSalesReturnRowBinding binding) {
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
