package com.soft_sales.model;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.soft_sales.broad_cast_receiver.AlarmReceiver;

import java.io.Serializable;
import java.util.Calendar;

public class AlarmModel implements Serializable {

    @SuppressLint("MissingPermission")
    public void schedule(Context context) {
        Log.e("timer","started");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,intent,0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE,2);
        calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        if (calendar.getTimeInMillis()<=System.currentTimeMillis()){
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }

        long DAILY = 1000*60*2;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),DAILY,pendingIntent);



    }

    public void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        alarmManager.cancel(alarmPendingIntent);


    }


}
