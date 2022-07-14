package com.soft_sales.model;

import java.io.Serializable;

public class SingleOnlineInvoice extends ResponseModel implements Serializable {
    private OnlineInvoiceModel data;

    public OnlineInvoiceModel getData() {
        return data;
    }
}
