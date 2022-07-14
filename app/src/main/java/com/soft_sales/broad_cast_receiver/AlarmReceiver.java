package com.soft_sales.broad_cast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.soft_sales.database.AppSyncService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            startRescheduleAlarmService(context);
        } else {
            startAlarmService( context);

        }
    }


    private void startAlarmService(Context context) {

        Intent intentService = new Intent(context, AppSyncService.class);

        context.startService(intentService);


    }

    private void startRescheduleAlarmService(Context context) {
        Intent intentService = new Intent(context, AppSyncService.class);
        context.startService(intentService);

    }


}
