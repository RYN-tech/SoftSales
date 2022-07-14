package com.soft_sales.uis.activity_base;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.soft_sales.databinding.ToolbarBinding;
import com.soft_sales.language.Language;
import com.soft_sales.model.UserModel;
import com.soft_sales.model.UserSettingsModel;
import com.soft_sales.preferences.Preferences;
import com.soft_sales.printer_utils.BluetoothUtil;
import com.soft_sales.printer_utils.ESCUtil;
import com.soft_sales.printer_utils.SunmiPrintHelper;

import io.paperdb.Paper;


public class BaseActivity extends AppCompatActivity {

    public static final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_PERM = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String CAM_PERM = Manifest.permission.CAMERA;
    public static final String MANAGE_STORAGE_PERM = Manifest.permission.MANAGE_EXTERNAL_STORAGE;
    public static final String FINE_LOCATION_PERM = Manifest.permission.ACCESS_FINE_LOCATION;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        initPrinterStyle();
    }

    private void initPrinterStyle() {
        BluetoothUtil.isBlueToothPrinter =false;
        if(BluetoothUtil.isBlueToothPrinter){
            BluetoothUtil.sendData(ESCUtil.init_printer());
        }else{
            SunmiPrintHelper.getInstance().initPrinter();
        }
    }

    protected String getLang() {
        Paper.init(this);
        String lang = Paper.book().read("lang", "ar");
        return lang;
    }

    protected UserModel getUserModel() {
        Preferences preferences = Preferences.getInstance();
        return preferences.getUserData(this);
    }

    protected void setUserModel(UserModel userModel) {
        Preferences preferences = Preferences.getInstance();
        preferences.createUpdateUserData(this, userModel);
    }

    protected void clearUserModel() {
        Preferences preferences = Preferences.getInstance();
        preferences.clearUserData(this);
    }


    public void setUserSettings(UserSettingsModel userSettingsModel) {
        Preferences preferences = Preferences.getInstance();
        preferences.create_update_user_settings(this, userSettingsModel);
    }

    public UserSettingsModel getUserSettings() {
        Preferences preferences = Preferences.getInstance();
        return preferences.getUserSettings(this);
    }

    public void setBaseUrl(String url) {
        Preferences preferences = Preferences.getInstance();
        preferences.createBaseUrl(this,url);
    }

    public String getBaseUrl() {
        Preferences preferences = Preferences.getInstance();
        return preferences.getBaseUrl(this);

    }


    protected void setUpToolbar(ToolbarBinding binding, String title, int background, int arrowTitleColor) {
        binding.setLang(getLang());
        binding.setTitle(title);
        binding.arrow.setColorFilter(ContextCompat.getColor(this, arrowTitleColor));
        binding.tvTitle.setTextColor(ContextCompat.getColor(this, arrowTitleColor));
        binding.toolbar.setBackgroundResource(background);
        binding.llBack.setOnClickListener(v -> finish());
    }


}