package com.soft_sales.model;

import android.util.Patterns;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.soft_sales.BR;

import java.io.Serializable;

public class BaseUrlModel extends BaseObservable implements Serializable {
    private String baseUrl;
    private boolean isValid;


    public BaseUrlModel() {
        baseUrl = "";
        isValid = false;
    }


    public void isDataValid() {
        if (!baseUrl.trim().isEmpty() && Patterns.WEB_URL.matcher(baseUrl.trim()).matches()) {
           setValid(true);
        }else {
            setValid(false);

        }
    }

    public BaseUrlModel(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Bindable
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        notifyPropertyChanged(BR.baseUrl);
        isDataValid();
    }

    @Bindable
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
        notifyPropertyChanged(BR.valid);
    }
}
