package com.soft_sales.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;


import com.soft_sales.R;
import com.soft_sales.databinding.SpinnerRowBinding;
import com.soft_sales.model.CategoryModel;

import java.util.List;

public class SpinnerCategoriesAdapter extends BaseAdapter {
    private List<CategoryModel> list;
    private Context context;
    private LayoutInflater inflater;
    private String lang;

    public SpinnerCategoriesAdapter(Context context, String lang) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lang = lang;
    }

    @Override
    public int getCount() {
        return list!=null?list.size():0;
    }

    @Override
    public Object getItem(int i) {
        return list!=null?list.get(i):null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder") SpinnerRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_row,viewGroup,false);
        binding.setTitle(lang.equals("ar")?list.get(i).getTitle_ar():list.get(i).getTitle_en());
        return binding.getRoot();
    }

    public void updateList(List<CategoryModel> list){
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
