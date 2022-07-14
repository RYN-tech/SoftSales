
package com.soft_sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.soft_sales.R;
import com.soft_sales.databinding.SelectedItemInvoiceSalesRowBinding;
import com.soft_sales.model.ProductModel;
import com.soft_sales.uis.activity_create_sales_invoice.CreateSalesInvoiceActivity;

import java.util.List;

public class InvoiceSalesSelectedItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProductModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;

    public InvoiceSalesSelectedItemAdapter(Context context, String lang) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lang = lang;
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {


        SelectedItemInvoiceSalesRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.selected_item_invoice_sales_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        ProductModel itemModel = list.get(position);
        myHolder.binding.setModel(itemModel);
        myHolder.binding.setLang(lang);

        myHolder.binding.cardViewDelete.setOnClickListener(view -> {
            CreateSalesInvoiceActivity activity = (CreateSalesInvoiceActivity) context;
            activity.deleteItem(list.get(myHolder.getAdapterPosition()));
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
        public SelectedItemInvoiceSalesRowBinding binding;

        public MyHolder(SelectedItemInvoiceSalesRowBinding binding) {
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
