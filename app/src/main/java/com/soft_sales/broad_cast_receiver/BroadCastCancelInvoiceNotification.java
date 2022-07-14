package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.soft_sales.database.UploadInvoiceService;

public class BroadCastCancelInvoiceNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, UploadInvoiceService.class);
        context.stopService(intent1);


    }
}
