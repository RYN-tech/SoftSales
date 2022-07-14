package com.soft_sales.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.soft_sales.BR;

import java.io.Serializable;

public class AddProductModel extends BaseObservable  implements Serializable {
    private String name_ar;
    private String name_en;
    private String category_id;
    private String price;
    private String photo_path;
    private boolean isValid;

    public AddProductModel() {
        name_ar ="";
        name_en="";
        category_id="";
        price="";
        photo_path="";
        isValid = false;
    }

    public void isDataValid(){
        if (!name_ar.trim().isEmpty()&&!category_id.isEmpty()&&!photo_path.isEmpty()){
            setValid(true);
        }else {
            setValid(false);
        }
    }

    @Bindable
    public String getName_ar() {
        return name_ar;
    }

    public void setName_ar(String name_ar) {
        this.name_ar = name_ar;
        notifyPropertyChanged(BR.name_ar);
        isDataValid();
    }

    @Bindable
    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
        notifyPropertyChanged(BR.name_en);
    }


    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
        isDataValid();
    }


    @Bindable
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
        notifyPropertyChanged(BR.price);
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhoto_path(String photo_path) {
        this.photo_path = photo_path;
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
