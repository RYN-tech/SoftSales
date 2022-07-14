package com.soft_sales.model;

import java.io.Serializable;
import java.util.List;

public class OnlineInvoiceModel implements Serializable {
    private String id;
    private String code;
    private String customer_name;
    private String product_total;
    private String total_after_discount;
    private String total_after_tax;
    private String total;
    private String discount;
    private String pay_type;
    private String tax;
    private String tax_method;
    private String tax_per;
    private String order_date_time;
    private boolean is_back;
    private String added_by_id;
    private String created_at;
    private String updated_at;
    private List<Detail> details;

    public String getId() {
        return id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getTotal() {
        return total;
    }

    public String getDiscount() {
        return discount;
    }

    public String getPay_type() {
        return pay_type;
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

    public boolean isIs_back() {
        return is_back;
    }

    public String getAdded_by_id() {
        return added_by_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
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

    public String getCode() {
        return code;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public static class Detail implements Serializable{
        public String id;
        public String product_id;
        public String order_id;
        public String price;
        public String qty;
        public String total;
        public String created_at;
        public String updated_at;
        public ProductModel product;

        public String getId() {
            return id;
        }

        public String getProduct_id() {
            return product_id;
        }

        public String getOrder_id() {
            return order_id;
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

        public String getCreated_at() {
            return created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public ProductModel getProduct() {
            return product;
        }
    }
}
