package com.soft_sales.broad_cast_receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.soft_sales.database.AppSyncService;
import com.soft_sales.model.AlarmModel;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startAlarmService( context);
    }


    private void startAlarmService(Context context) {
        if (!isMyServiceRunning(context,AppSyncService.class)){
            Intent intentService = new Intent(context.getApplicationContext(), AppSyncService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getApplicationContext().startForegroundService(intentService);
            } else {
                context.getApplicationContext().startService(intentService);

            }
        }else {
            AlarmModel alarmModel = new AlarmModel();
            alarmModel.schedule(context);
        }



    }

    private boolean isMyServiceRunning(Context context,Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
