package com.soft_sales.model;

import java.io.Serializable;

public class SettingModel extends ResponseModel implements Serializable {
    private int id;
    private String vat;
    private String name_ar;
    private String name_en;
    private String address_ar;
    private String address_en;
    private String commercial_number;
    private String logo;
    private String sale_text;
    private double tax_val;
    private byte[] imageBitmap;
    private String tax_method;
    private String end_at;
    public int getId() {
        return id;
    }

    public String getVat() {
        return vat;
    }

    public String getName_ar() {
        return name_ar;
    }

    public String getName_en() {
        return name_en;
    }

    public String getAddress_ar() {
        return address_ar;
    }

    public String getAddress_en() {
        return address_en;
    }

    public String getCommercial_number() {
        return commercial_number;
    }

    public String getLogo() {
        return logo;
    }

    public String getSale_text() {
        return sale_text;
    }

    public double getTax_val() {
        return tax_val;
    }

    public byte[] getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(byte[] imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getTax_method() {
        return tax_method;
    }

    public String getEnd_at() {
        return end_at;
    }
}
