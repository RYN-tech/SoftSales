package com.soft_sales.uis.activity_create_return_invoice;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.soft_sales.R;
import com.soft_sales.adapter.ProductSalesReturnAdapter;
import com.soft_sales.databinding.ActivityCreateReturnInvoiceBinding;
import com.soft_sales.databinding.ActivityHomeBinding;
import com.soft_sales.model.InvoiceModel;
import com.soft_sales.mvvm.ActivityHomeMvvm;
import com.soft_sales.mvvm.ActivityReturnInvoiceMvvm;
import com.soft_sales.uis.activity_base.BaseActivity;

import java.util.ArrayList;

public class CreateReturnInvoiceActivity extends BaseActivity {
    private ActivityCreateReturnInvoiceBinding binding;
    private ActivityReturnInvoiceMvvm mvvm;
    private ProductSalesReturnAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_return_invoice);
        initView();


    }


    private void initView() {
        mvvm = ViewModelProviders.of(this).get(ActivityReturnInvoiceMvvm.class);
        mvvm.getOnSingleInvoiceSuccess().observe(this,invoiceModel -> {
            if (invoiceModel!=null){
                binding.setTotal(invoiceModel.getTotal());
                binding.setItems(invoiceModel.getProducts().size());
                if(adapter!=null){
                    adapter.updateList(invoiceModel.getProducts());
                }
                if (invoiceModel.getProducts().size()>0){
                    binding.setEnable(true);
                }else {
                    binding.setEnable(false);

                }
            }else {
                adapter.updateList(new ArrayList<>());
                binding.setTotal(0.0);
                binding.setItems(0);
                binding.setEnable(false);

            }



        });
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setHasFixedSize(true);
        adapter = new ProductSalesReturnAdapter(this,getLang());
        binding.recView.setAdapter(adapter);
        binding.edtInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length()==0){
                    mvvm.getOnSingleInvoiceSuccess().setValue(null);
                }
            }
        });
        binding.imageSearch.setOnClickListener(view -> {
            String code = binding.edtInvoice.getText().toString().trim();
            if (!code.isEmpty()){
                binding.setEnable(false);

                mvvm.getInvoice(code);
            }
        });
        binding.btnCancel.setOnClickListener(view -> {
            finish();
        });

        binding.btnSave.setOnClickListener(view -> {
            InvoiceModel invoiceModel = mvvm.getOnSingleInvoiceSuccess().getValue();
            if (invoiceModel!=null){

                invoiceModel.setIs_back(true);
                mvvm.createReturnInvoice(this,invoiceModel);
                finish();
            }

        });

    }
}