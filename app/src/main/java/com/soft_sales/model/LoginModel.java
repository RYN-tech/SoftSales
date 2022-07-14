package com.soft_sales.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.soft_sales.BR;
import com.soft_sales.R;

import java.io.Serializable;

public class LoginModel extends BaseObservable implements Serializable {
    private String userName;
    private String password;
    private boolean isValid;

    public LoginModel() {
        userName ="";
        password ="";
        isValid = false;
    }

    public void isDataValid(){
        if (!userName.trim().isEmpty()&&!password.trim().isEmpty()){
            setValid(true);
        }else {
            setValid(false);
        }
    }

    @Bindable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        notifyPropertyChanged(BR.userName);
        isDataValid();
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);
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