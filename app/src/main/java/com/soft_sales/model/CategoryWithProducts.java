package com.soft_sales.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

public class CategoryWithProducts implements Serializable {
    @Embedded
    private CategoryModel categoryModel;

    @Relation(
            parentColumn = "id",
            entityColumn = "category_id"
    )
    private List<ProductModel> products;

    public CategoryModel getCategoryModel() {
        return categoryModel;
    }

    public void setCategoryModel(CategoryModel categoryModel) {
        this.categoryModel = categoryModel;
    }

    public List<ProductModel> getProducts() {
        return products;
    }

    public void setProducts(List<ProductModel> products) {
        this.products = products;
    }
}
