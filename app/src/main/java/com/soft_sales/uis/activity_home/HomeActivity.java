package com.soft_sales.uis.activity_home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.soft_sales.broad_cast_receiver.BroadCastNetwork;
import com.soft_sales.database.AppSyncService;
import com.soft_sales.database.LoadCategoryService;
import com.soft_sales.database.LoadProductsService;
import com.soft_sales.database.LoadSalesInvoiceService;
import com.soft_sales.database.UploadInvoiceService;
import com.soft_sales.database.UploadProductService;
import com.soft_sales.database.UploadSingleInvoiceService;
import com.soft_sales.database.UploadSingleReturnInvoiceService;
import com.soft_sales.model.AlarmModel;
import com.soft_sales.model.EventsModel;
import com.soft_sales.model.UserModel;
import com.soft_sales.mvvm.ActivityHomeMvvm;
import com.soft_sales.uis.activity_add_product.AddProductActivity;
import com.soft_sales.uis.activity_base.BaseActivity;

import com.soft_sales.R;

import com.soft_sales.databinding.ActivityHomeBinding;
import com.soft_sales.language.Language;
import com.soft_sales.uis.activity_create_return_invoice.CreateReturnInvoiceActivity;
import com.soft_sales.uis.activity_create_sales_invoice.CreateSalesInvoiceActivity;
import com.soft_sales.uis.activity_login.LoginActivity;
import com.soft_sales.uis.activity_sales_invoices.SalesInvoicesActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.paperdb.Paper;

public class HomeActivity extends BaseActivity {
    private ActivityHomeBinding binding;
    private ActionBarDrawerToggle toggle;
    private ActivityHomeMvvm mvvm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();


    }


    private void initView() {
        mvvm = ViewModelProviders.of(this).get(ActivityHomeMvvm.class);
        binding.setLang(getLang());
        binding.setModel(getUserModel());
        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this,R.color.colorPrimary));
        toggle.syncState();

        mvvm.getIsSynchronization().observe(this, aBoolean -> {
            if (aBoolean) {

                binding.llSync.setVisibility(View.VISIBLE);
            } else {

                binding.llSync.setVisibility(View.GONE);


            }
        });

        mvvm.getOnLogoutSuccess().observe(this, aBoolean -> {
            if (aBoolean){
                AlarmModel alarmModel= new AlarmModel();
                alarmModel.cancelAlarm(this.getApplicationContext());
                clearUserModel();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }

            Log.e("logged_out",aBoolean+"");
        });

        binding.background.setOnClickListener(view -> {
            binding.llSync.setVisibility(View.GONE);

        });

        binding.addProduct.setOnClickListener(view -> {
            if (getUserModel().getData().getPermissions().contains("productCreate")) {
                Intent intent = new Intent(this, AddProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }else {
                Toast.makeText(this, "Not have permission to create product", Toast.LENGTH_SHORT).show();

            }

        });

        binding.createSalesInvoice.setOnClickListener(view -> {
            if (getUserModel().getData().getPermissions().contains("ordersStore")){
                Intent intent = new Intent(this, CreateSalesInvoiceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }else {
                Toast.makeText(this, "Not have permission to create sales invoice", Toast.LENGTH_SHORT).show();
            }

        });

        binding.language.setOnClickListener(view -> {
            refreshActivity();
        });


        binding.llSalesInvoice.setOnClickListener(view -> {
            if (getUserModel().getData().getPermissions().contains("ordersIndex")) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(this, SalesInvoicesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }else {
                Toast.makeText(this, "Not have permission to show sales invoice", Toast.LENGTH_SHORT).show();

            }


        });

        binding.createReturnInvoice.setOnClickListener(view -> {

            if (getUserModel().getData().getPermissions().contains("ordersBack")) {
                Intent intent = new Intent(this, CreateReturnInvoiceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }else {
                Toast.makeText(this, "Not have permission to create return invoice", Toast.LENGTH_SHORT).show();

            }


        });

        binding.syncData.setOnClickListener(view -> {
            mvvm.syncData(this);
        });
        binding.llLogout.setOnClickListener(view -> {
            if (!isMyServiceRunning(this,AppSyncService.class)&&
                    !isMyServiceRunning(this, LoadCategoryService.class)&&
                    !isMyServiceRunning(this, LoadProductsService.class)&&
                    !isMyServiceRunning(this, LoadSalesInvoiceService.class)&&
                    !isMyServiceRunning(this, UploadInvoiceService.class)&&
                    !isMyServiceRunning(this, UploadProductService.class)&&
                    !isMyServiceRunning(this, UploadSingleInvoiceService.class)&&
                    !isMyServiceRunning(this, UploadSingleReturnInvoiceService.class)
            ){
                mvvm.prepareDataToLogout(this,getUserModel());
            }else {
                Toast.makeText(this, "Please wait App Sync Data", Toast.LENGTH_SHORT).show();
            }



        });

        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }

        mvvm.getCategories(this);

        if(getUserModel().getData().getUser().getIs_login().equals("0")){
            mvvm.prepareDataToLogout(this,getUserModel());
        }

        if (!isMyServiceRunning(this, AppSyncService.class)){
            registerBroadCast();

        }

    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void registerBroadCast() {
        registerReceiver(new BroadCastNetwork(),new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    public void refreshActivity() {
        String lang = getLang();
        if (lang.equals("ar")){
            lang ="en";
        }else {
            lang ="ar";
        }
        Paper.book().write("lang", lang);
        Language.setNewLocale(this, lang);
        new Handler()
                .postDelayed(() -> {

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }, 500);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncDone(EventsModel model){
        binding.llSync.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void listenToUserUpdate(UserModel model){
        Log.e("data","user_datasss");

        binding.setModel(model);

        if(model.getData().getUser().getIs_login().equals("0")){
            mvvm.prepareDataToLogout(this,getUserModel());
        }else {
            if (!isMyServiceRunning(this, AppSyncService.class)){
                registerBroadCast();

            }
        }


    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
