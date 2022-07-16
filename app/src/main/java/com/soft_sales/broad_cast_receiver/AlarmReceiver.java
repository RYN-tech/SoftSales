package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.soft_sales.database.AppSyncService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startAlarmService( context);
    }


    private void startAlarmService(Context context) {

        Intent intentService = new Intent(context.getApplicationContext(), AppSyncService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getApplicationContext().startForegroundService(intentService);
        } else {
            context.getApplicationContext().startService(intentService);

        }


    }

}
