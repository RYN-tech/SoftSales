package com.soft_sales.model;

import java.io.Serializable;

public class SingleProductDataModel extends ResponseModel implements Serializable {
    private ProductModel data;

    public ProductModel getData() {
        return data;
    }
}
