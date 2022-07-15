package com.soft_sales.uis.activity_login;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import com.soft_sales.R;
import com.soft_sales.databinding.ActivityLoginBinding;
import com.soft_sales.model.LoginModel;
import com.soft_sales.mvvm.ActivityLoginMvvm;
import com.soft_sales.uis.activity_base.BaseActivity;
import com.soft_sales.uis.activity_base_url.BaseUrlActivity;
import com.soft_sales.uis.activity_home.HomeActivity;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;
    private ActivityLoginMvvm mvvm;
    private LoginModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        initView();
    }


    private void initView() {
        model = new LoginModel();
        binding.setModel(model);
        mvvm = ViewModelProviders.of(this).get(ActivityLoginMvvm.class);

        mvvm.getOnDataSuccess().observe(this, model -> {
                    setUserModel(model);
                    navigateToHomeActivity();
                }
        );

        binding.btnLogin.setOnClickListener(view -> {
            mvvm.Login(this,model,getLang());
        });

        binding.changeUrl.setOnClickListener(view -> {
            Intent intent = new Intent(this, BaseUrlActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }


}