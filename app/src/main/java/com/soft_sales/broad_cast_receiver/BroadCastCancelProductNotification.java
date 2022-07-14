package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.soft_sales.database.UploadProductService;

public class BroadCastCancelProductNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, UploadProductService.class);
        context.stopService(intent1);
    }



}
