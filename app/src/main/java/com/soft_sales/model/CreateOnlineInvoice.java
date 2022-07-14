package com.soft_sales.model;

import java.io.Serializable;
import java.util.List;

public class CreateOnlineInvoice implements Serializable {
    private String customer_name;
    private String code;
    private String product_total;
    private String total_after_discount;
    private String total_after_tax;
    private String total;
    private String discount;
    private String tax;
    private String tax_method;
    private String tax_per;
    private String order_date_time;
    private boolean is_back;
    private String pay_type;
    private List<Details> details;

    public CreateOnlineInvoice(String customer_name,String code, String product_total, String total_after_discount, String total_after_tax, String total, String discount, String tax, String tax_method, String tax_per, String order_date_time, boolean is_back, String pay_type, List<Details> details) {
        this.customer_name = customer_name;
        this.code = code;
        this.product_total = product_total;
        this.total_after_discount = total_after_discount;
        this.total_after_tax = total_after_tax;
        this.total = total;
        this.discount = discount;
        this.tax = tax;
        this.tax_method = tax_method;
        this.tax_per = tax_per;
        this.order_date_time = order_date_time;
        this.is_back = is_back;
        this.pay_type = pay_type;
        this.details = details;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getProduct_total() {
        return product_total;
    }

    public String getTotal_after_discount() {
        return total_after_discount;
    }

    public String getTotal_after_tax() {
        return total_after_tax;
    }

    public String getTotal() {
        return total;
    }

    public String getDiscount() {
        return discount;
    }

    public String getTax() {
        return tax;
    }

    public String getTax_method() {
        return tax_method;
    }

    public String getTax_per() {
        return tax_per;
    }

    public String getOrder_date_time() {
        return order_date_time;
    }

    public boolean getIs_back() {
        return is_back;
    }

    public String getPay_type() {
        return pay_type;
    }

    public List<Details> getDetails() {
        return details;
    }

    public String getCode() {
        return code;
    }

    public static class Details implements Serializable{

        private String product_id;
        private String price;
        private String qty;
        private String total;

        public Details(String product_id, String price, String qty, String total) {
            this.product_id = product_id;
            this.price = price;
            this.qty = qty;
            this.total = total;
        }

        public String getProduct_id() {
            return product_id;
        }

        public String getPrice() {
            return price;
        }

        public String getQty() {
            return qty;
        }

        public String getTotal() {
            return total;
        }
    }


}
