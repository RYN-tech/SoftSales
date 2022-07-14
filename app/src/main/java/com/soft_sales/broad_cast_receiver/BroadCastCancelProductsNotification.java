package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.soft_sales.database.LoadProductsService;
import com.soft_sales.database.LoadSalesInvoiceService;
import com.soft_sales.share.NetworkUtils;

public class BroadCastCancelProductsNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtils.getConnectivityStatus(context.getApplicationContext())) {
            Intent intent2 = new Intent(context, LoadSalesInvoiceService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent2);
            } else {
                context.startService(intent2);

            }
        }


        Intent intent1 = new Intent(context, LoadProductsService.class);
        context.stopService(intent1);
    }
}
