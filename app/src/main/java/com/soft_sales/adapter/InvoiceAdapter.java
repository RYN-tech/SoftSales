
package com.soft_sales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.soft_sales.R;
import com.soft_sales.databinding.InvoiceRowBinding;
import com.soft_sales.databinding.ProductRowBinding;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.ProductModel;
import com.soft_sales.uis.activity_create_sales_invoice.CreateSalesInvoiceActivity;
import com.soft_sales.uis.activity_sales_invoices.SalesInvoicesActivity;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<InvoiceModel> list;
    private Context context;
    private LayoutInflater inflater;

    public InvoiceAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {


        InvoiceRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.invoice_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));
        myHolder.binding.more.setOnClickListener(view -> {
            InvoiceModel invoiceModel = list.get(myHolder.getAdapterPosition());
            if (invoiceModel.getInvoice_online_id().equals("0")){
                createMenu1(invoiceModel,view);
            }else {
                createMenu2(invoiceModel,view);

            }
        });

    }

    private void createMenu1(InvoiceModel invoiceModel, View view) {
        PopupMenu menu = new PopupMenu(context,view);
        menu.getMenuInflater().inflate(R.menu.popup_menu1,menu.getMenu());
        menu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id==R.id.print){
                SalesInvoicesActivity activity = (SalesInvoicesActivity) context;
                activity.print(invoiceModel);
            }else if (id==R.id.upload){
                SalesInvoicesActivity activity = (SalesInvoicesActivity) context;
                activity.sync(invoiceModel);
            }
            menu.dismiss();
            return true;
        });
        menu.show();
    }

    private void createMenu2(InvoiceModel invoiceModel, View view) {
        PopupMenu menu = new PopupMenu(context,view);
        menu.getMenuInflater().inflate(R.menu.popup_menu2,menu.getMenu());
        menu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id==R.id.print){
                SalesInvoicesActivity activity = (SalesInvoicesActivity) context;
                activity.print(invoiceModel);
            }
            menu.dismiss();
            return true;
        });
        menu.show();
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
        public InvoiceRowBinding binding;

        public MyHolder(InvoiceRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public void updateList(List<InvoiceModel> list) {
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
