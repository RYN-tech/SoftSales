package com.soft_sales.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class ProductModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long product_local_id;
    private String id="0";
    private String category_id;
    private String title_en;
    private String title_ar;
    private String photo;
    private String price;
    private String added_by_id;
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] local_image;
    @Ignore
    private boolean isSelected = false;
    @Ignore
    private double amount =0.0;
    @Ignore
    private InvoiceCrossModel invoiceCrossModel;

    public ProductModel(String id, String category_id, String title_en, String title_ar, String photo, String price, String added_by_id, byte[] local_image) {
        this.id = id;
        this.category_id = category_id;
        this.title_en = title_en;
        this.title_ar = title_ar;
        this.photo = photo;
        this.price = price;
        this.added_by_id = added_by_id;
        this.local_image = local_image;
    }

    public long getProduct_local_id() {
        return product_local_id;
    }

    public void setProduct_local_id(long product_local_id) {
        this.product_local_id = product_local_id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public void setTitle_ar(String title_ar) {
        this.title_ar = title_ar;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setAdded_by_id(String added_by_id) {
        this.added_by_id = added_by_id;
    }

    public String getId() {
        return id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public String getTitle_en() {
        return title_en;
    }

    public String getTitle_ar() {
        return title_ar;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPrice() {
        return price;
    }

    public String getAdded_by_id() {
        return added_by_id;
    }

    public byte[] getLocal_image() {
        return local_image;
    }

    public void setLocal_image(byte[] local_image) {
        this.local_image = local_image;
    }

    @Ignore
    public boolean isSelected() {
        return isSelected;
    }

    @Ignore
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Ignore
    public double getAmount() {
        return amount;
    }

    @Ignore
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public InvoiceCrossModel getInvoiceCrossModel() {
        return invoiceCrossModel;
    }

    public void setInvoiceCrossModel(InvoiceCrossModel invoiceCrossModel) {
        this.invoiceCrossModel = invoiceCrossModel;
    }
}
