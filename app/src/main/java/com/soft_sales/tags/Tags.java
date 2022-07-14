package com.soft_sales.tags;

import android.content.Context;

import com.soft_sales.preferences.Preferences;

public class Tags {
    public static final String not_tag = "soft_not_tag";
    public static final int not_id = 3000;

    public static final String not_tag_products = "soft_not_tag_products";
    public static final int not_products_id = 3001;

    public static final String not_tag_invoices = "soft_not_tag_invoices";
    public static final int not_invoices_id = 3002;

    public static final String not_tag_product = "soft_not_tag_product";
    public static final int not_product_id = 3003;

    public static final String not_tag_invoice = "soft_not_tag_invoice";
    public static final int not_invoice_id= 3004;

    public static final String not_tag_single_invoice = "soft_not_tag_single_invoice";
    public static final int not_single_invoice_id= 3005;

    public static final String not_tag_single_return_invoice = "soft_not_tag_single_return_invoice";
    public static final int not_single_return_invoice_id= 3006;

    public static final String not_tag_soft_app = "soft_not_tag";
    public static final int not_soft_app_id= 3007;

    public static String getBaseUrl(Context context){
        Preferences preferences = Preferences.getInstance();
        return preferences.getBaseUrl(context);
    }
}
