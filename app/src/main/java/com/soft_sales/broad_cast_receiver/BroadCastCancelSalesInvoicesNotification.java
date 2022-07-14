package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import com.soft_sales.database.LoadSalesInvoiceService;

public class BroadCastCancelSalesInvoicesNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent(context, LoadSalesInvoiceService.class);
        context.stopService(intent1);


    }
}
