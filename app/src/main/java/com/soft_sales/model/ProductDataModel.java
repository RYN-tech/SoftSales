package com.soft_sales.model;

import java.io.Serializable;
import java.util.List;

public class ProductDataModel extends ResponseModel implements Serializable {
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data implements Serializable{
        private int current_page;
        private List<ProductModel> data;

        public int getCurrent_page() {
            return current_page;
        }

        public List<ProductModel> getData() {
            return data;
        }
    }
}
