package com.soft_sales.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;

@Entity(indices = @Index(value = {"code"},unique = true))
public class InvoiceModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long invoice_local_id;
    private String invoice_online_id="0";

    private int code=0;
    private String customer_name;
    private double total=0;
    private double discount=0;
    private double total_products;
    private double total_after_discount;
    private double total_after_tax;
    private String pay_type;
    private String tax_value;
    private String tax_method;
    private String tax_per;
    private String date;
    private boolean isUpdated = false;


    private boolean is_back=false;
    private String added_by_id;
    @Ignore
    private List<ProductModel> products;

    public InvoiceModel() {
    }

    public InvoiceModel(String invoice_online_id,int code, String customer_name, double total, double discount, double total_products, double total_after_discount, double total_after_tax, String pay_type, String tax_value, String tax_method, String tax_per, String date, boolean is_back, String added_by_id) {
        this.invoice_online_id = invoice_online_id;
        this.code = code;
        this.customer_name = customer_name;
        this.total = total;
        this.discount = discount;
        this.total_products = total_products;
        this.total_after_discount = total_after_discount;
        this.total_after_tax = total_after_tax;
        this.pay_type = pay_type;
        this.tax_value = tax_value;
        this.tax_method = tax_method;
        this.tax_per = tax_per;
        this.date = date;
        this.is_back = is_back;
        this.added_by_id = added_by_id;
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

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getTax_value() {
        return tax_value;
    }

    public void setTax_value(String tax_value) {
        this.tax_value = tax_value;
    }

    public String getTax_method() {
        return tax_method;
    }

    public void setTax_method(String tax_method) {
        this.tax_method = tax_method;
    }

    public String getTax_per() {
        return tax_per;
    }

    public void setTax_per(String tax_per) {
        this.tax_per = tax_per;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isIs_back() {
        return is_back;
    }

    public void setIs_back(boolean is_back) {
        this.is_back = is_back;
    }

    public String getAdded_by_id() {
        return added_by_id;
    }

    public void setAdded_by_id(String added_by_id) {
        this.added_by_id = added_by_id;
    }

    public double getTotal_products() {
        return total_products;
    }

    public void setTotal_products(double total_products) {
        this.total_products = total_products;
    }

    public double getTotal_after_discount() {
        return total_after_discount;
    }

    public void setTotal_after_discount(double total_after_discount) {
        this.total_after_discount = total_after_discount;
    }

    public double getTotal_after_tax() {
        return total_after_tax;
    }

    public void setTotal_after_tax(double total_after_tax) {
        this.total_after_tax = total_after_tax;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Ignore
    public List<ProductModel> getProducts() {
        return products;
    }

    @Ignore
    public void setProducts(List<ProductModel> products) {
        this.products = products;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }
}
