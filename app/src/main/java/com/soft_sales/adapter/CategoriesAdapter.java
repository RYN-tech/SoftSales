
package com.soft_sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.soft_sales.R;
import com.soft_sales.databinding.CategoryRowBinding;
import com.soft_sales.model.CategoryModel;
import com.soft_sales.model.CategoryWithProducts;
import com.soft_sales.uis.activity_create_sales_invoice.CreateSalesInvoiceActivity;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CategoryWithProducts> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;
    private String lang;
    private MyHolder oldHolder;

    public CategoriesAdapter(Context context, String lang) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lang = lang;
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {


        CategoryRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.category_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        CategoryWithProducts categoryModel = list.get(position);
        myHolder.binding.setLang(lang);
        myHolder.binding.setModel(categoryModel);
        if (categoryModel.getCategoryModel().isSelected()){
            if (oldHolder==null){
                oldHolder = myHolder;
            }
        }

        myHolder.itemView.setOnClickListener(view -> {
            if (oldHolder!=null){
                CategoryWithProducts oldModel = list.get(oldHolder.getAdapterPosition());
                if (oldModel.getCategoryModel().isSelected()){
                    oldModel.getCategoryModel().setSelected(false);
                    list.set(oldHolder.getAdapterPosition(),oldModel);
                    oldHolder.binding.setModel(oldModel);
                }

            }

            CategoryWithProducts model = list.get(myHolder.getAdapterPosition());
            if (!model.getCategoryModel().isSelected()){
                model.getCategoryModel().setSelected(true);
                list.set(myHolder.getAdapterPosition(),model);
                myHolder.binding.setModel(model);
                oldHolder = myHolder;

            }

            CreateSalesInvoiceActivity activity = (CreateSalesInvoiceActivity) context;
            activity.setSelectedCategory(model);



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
        public CategoryRowBinding binding;

        public MyHolder(CategoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public void updateList(List<CategoryWithProducts> list) {
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
