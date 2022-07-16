package com.soft_sales.uis.activity_sales_invoices;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.soft_sales.R;
import com.soft_sales.adapter.InvoiceAdapter;
import com.soft_sales.databinding.ActivitySalesInvoicesBinding;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.model.SyncModel;
import com.soft_sales.mvvm.ActivitySalesInvoiceMvvm;
import com.soft_sales.uis.activity_base.BaseActivity;
import com.soft_sales.uis.activity_print_invoice.PrintInvoiceActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SalesInvoicesActivity extends BaseActivity {
    private ActivitySalesInvoiceMvvm mvvm;
    private ActivitySalesInvoicesBinding binding;
    private InvoiceAdapter adapter;
    private String action;
    private boolean isSyncStarted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sales_invoices);
        initView();
    }

    private void initView() {
        setUpToolbar(binding.toolbar, getString(R.string.sales_invoices), R.color.white, R.color.black);

        mvvm = ViewModelProviders.of(this).get(ActivitySalesInvoiceMvvm.class);

        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setHasFixedSize(true);
        adapter = new InvoiceAdapter(this);
        binding.recView.setAdapter(adapter);

        mvvm.getOnDataSuccess().observe(this,list->{
            if (adapter!=null){
                adapter.updateList(list);
            }
        });

        binding.all.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                mvvm.getLocalSalesInvoices(this);

            }
        });

        binding.sync.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                mvvm.getSyncSalesInvoices(this);

            }
        });
        binding.unSync.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                mvvm.getUnSyncSalesInvoices(this);


            }
        });
        mvvm.getOnSingleInvoiceSuccess().observe(this,invoiceModel -> {
            if (action.equals("print")){
                Intent intent = new Intent(this, PrintInvoiceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("data",invoiceModel);
                startActivity(intent);
            }else {
                mvvm.syncInvoice(this,invoiceModel);

            }

        });

        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
       // mvvm.getLocalSalesInvoices(this);
        mvvm.getUnSyncSalesInvoices(this);


    }

    public void print(InvoiceModel invoiceModel) {
        action = "print";
        mvvm.getInvoiceWithProducts(invoiceModel.getInvoice_local_id());

    }

    public void sync(InvoiceModel invoiceModel) {
        action = "sync";

        mvvm.getInvoiceWithProducts(invoiceModel.getInvoice_local_id());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncDone(EventsModel model){
      // binding.all.setChecked(true);
       binding.unSync.setChecked(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncDataDone(SyncModel model){
        isSyncStarted = true;
        Log.e("isSync",isSyncStarted+"__");

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSyncStarted){
            if (binding.all.isChecked()){
                mvvm.getLocalSalesInvoices(this);

            }else if (binding.unSync.isChecked()){
                mvvm.getUnSyncSalesInvoices(this);

            }else if (binding.sync.isChecked()){
                mvvm.getSyncSalesInvoices(this);

            }
            isSyncStarted= false;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
}