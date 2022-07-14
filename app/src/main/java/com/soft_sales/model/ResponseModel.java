package com.soft_sales.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ResponseModel implements Serializable {
    @SerializedName(value = "status",alternate = {"code"})
    private int status;
    private Object message;
    public int getStatus() {
        return status;
    }

    public Object getMessage() {
        return message;
    }
}
