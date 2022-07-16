package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.soft_sales.database.AppSyncService;
import com.soft_sales.share.NetworkUtils;

public class BroadCastNetwork extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (NetworkUtils.getConnectivityStatus(context)){
            Intent intent1 = new Intent(context, AppSyncService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent1);
            } else {
                context.startService(intent1);

            }
        }
    }
}
