package com.soft_sales.model;

import java.io.Serializable;

public class SettingDataModel extends ResponseModel implements Serializable {
    private SettingModel data;

    public SettingModel getData() {
        return data;
    }

    public void setData(SettingModel data) {
        this.data = data;
    }
}
