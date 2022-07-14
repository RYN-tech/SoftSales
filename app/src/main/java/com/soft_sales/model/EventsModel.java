package com.soft_sales.model;

public class EventsModel {
    boolean sync = true;

    public EventsModel(boolean sync) {
        this.sync = sync;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
