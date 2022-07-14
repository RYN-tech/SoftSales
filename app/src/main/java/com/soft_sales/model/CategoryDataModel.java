package com.soft_sales.model;

import java.io.Serializable;
import java.util.List;

public class CategoryDataModel extends ResponseModel implements Serializable {
    public Data data;

    public Data getData() {
        return data;
    }

    public static class Data implements Serializable {
        private int current_page;
        private List<CategoryModel> data;

        public int getCurrent_page() {
            return current_page;
        }

        public List<CategoryModel> getData() {
            return data;
        }
    }
}
