package com.soft_sales.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class InvoiceCrossModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long invoice_cross_id;
    private long invoice_local_id;
    private String invoice_online_id;
    private long product_local_id;
    private String product_online_id;
    private double product_price;
    private double product_amount;





    public InvoiceCrossModel(long invoice_local_id, String invoice_online_id, long product_local_id, String product_online_id, double product_price, double product_amount) {
        this.invoice_local_id = invoice_local_id;
        this.invoice_online_id = invoice_online_id;
        this.product_local_id = product_local_id;
        this.product_online_id = product_online_id;
        this.product_price = product_price;
        this.product_amount = product_amount;
    }

    public long getInvoice_cross_id() {
        return invoice_cross_id;
    }

    public void setInvoice_cross_id(long invoice_cross_id) {
        this.invoice_cross_id = invoice_cross_id;
    }

    public long getInvoice_local_id() {
        return invoice_local_id;
    }

    public void setInvoice_local_id(long invoice_local_id) {
        this.invoice_local_id = invoice_local_id;
    }

    public String getInvoice_online_id() {
        return invoice_online_id;
    }

    public void setInvoice_online_id(String invoice_online_id) {
        this.invoice_online_id = invoice_online_id;
    }

    public long getProduct_local_id() {
        return product_local_id;
    }

    public void setProduct_local_id(long product_local_id) {
        this.product_local_id = product_local_id;
    }

    public String getProduct_online_id() {
        return product_online_id;
    }

    public void setProduct_online_id(String product_online_id) {
        this.product_online_id = product_online_id;
    }

    public double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(double product_price) {
        this.product_price = product_price;
    }

    public double getProduct_amount() {
        return product_amount;
    }

    public void setProduct_amount(double product_amount) {
        this.product_amount = product_amount;
    }
}
