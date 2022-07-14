package com.soft_sales.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class CategoryModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long category_local_id;

    private String id;
    private String title_ar;
    private String title_en;
    @Ignore
    private boolean isSelected;

    public CategoryModel() {
    }

    public long getCategory_local_id() {
        return category_local_id;
    }

    public void setCategory_local_id(long category_local_id) {
        this.category_local_id = category_local_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle_ar() {
        return title_ar;
    }

    public void setTitle_ar(String title_ar) {
        this.title_ar = title_ar;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    @Ignore
    public boolean isSelected() {
        return isSelected;
    }

    @Ignore
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
