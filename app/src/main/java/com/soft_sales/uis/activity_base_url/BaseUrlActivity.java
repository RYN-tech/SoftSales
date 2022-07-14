package com.soft_sales.uis.activity_base_url;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.soft_sales.R;
import com.soft_sales.databinding.ActivityBaseUrlBinding;
import com.soft_sales.model.BaseUrlModel;
import com.soft_sales.uis.activity_base.BaseActivity;
import com.soft_sales.uis.activity_login.LoginActivity;

public class BaseUrlActivity extends BaseActivity {
    private ActivityBaseUrlBinding binding;
    private BaseUrlModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_base_url);
        initView();
    }

    private void initView() {
        model = new BaseUrlModel();
        binding.setModel(model);

        binding.btnSave.setOnClickListener(view -> {
            String url = model.getBaseUrl();
            if (!url.endsWith("/")){
                url = url+"/";
            }
            setBaseUrl(url);
            navigateToLoginActivity();
        });

    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}