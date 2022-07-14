package com.soft_sales.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

public class InvoiceWithProductsModel implements Serializable {
    @Embedded
    private InvoiceModel invoiceModel;
    @Relation(
            parentColumn = "invoice_local_id",
            entityColumn = "product_local_id",
            associateBy = @Junction(InvoiceCrossModel.class)
    )
    List<ProductModel> products;

    public InvoiceModel getInvoiceModel() {
        return invoiceModel;
    }

    public void setInvoiceModel(InvoiceModel invoiceModel) {
        this.invoiceModel = invoiceModel;
    }

    public List<ProductModel> getProducts() {
        return products;
    }

    public void setProducts(List<ProductModel> products) {
        this.products = products;
    }
}
