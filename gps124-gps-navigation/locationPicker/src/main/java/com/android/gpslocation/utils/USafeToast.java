package com.android.gpslocation.utils;

import android.content.Context;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class USafeToast {

    public static final int DURATION = 4000;

    public static final Map<Object, Long> lastShown = new HashMap<Object, Long>();

    public static boolean isRecent(Object obj) {
        Long last = lastShown.get(obj);
        if (last == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (last + DURATION < now) {
            return false;
        }
        return true;
    }

    public static synchronized void show(Context context, int resId) {
        if (isRecent(resId)) {
            return;
        }
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
        lastShown.put(resId, System.currentTimeMillis());
    }

    public static synchronized void show(Context context, String msg) {
        if (isRecent(msg)) {
            return;
        }
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        lastShown.put(msg, System.currentTimeMillis());
    }
}






 /*   //get time interval to trigger alarm manager
    private long getTimeInterval(long getInterval) {
        long interval = Integer.parseInt(String.valueOf(getInterval));//convert string interval into integer
        return interval ;//convert hours into seconds


    }


    //Trigger alarm manager with entered time interval
    public void triggerAlarmManager(long alarmTriggerTime) {
// get a Calendar object with current time
        String myStringDate = "10:40 PM";
        *//*0 for am and 1 for pm*//*
        Calendar ocalendar = Calendar.getInstance();
        ocalendar = Calendar.getInstance();
        ocalendar.setTimeInMillis(System.currentTimeMillis());
        ocalendar.set(Calendar.HOUR_OF_DAY, 10);
        ocalendar.set(Calendar.MINUTE, 40);
        ocalendar.set(Calendar.AM_PM, 1);
        ocalendar.set(Calendar.SECOND, 00);
        ocalendar.set(Calendar.MILLISECOND, 00);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);//get instance of alarm manager
        manager.set(AlarmManager.RTC_WAKEUP, ocalendar.getTimeInMillis(), pendingIntent);//set alarm manager with entered timer by converting into milliseconds

       // Toast.makeText(this, " Set time for " + ocalendar.getTimeInMillis() + " seconds.", Toast.LENGTH_SHORT).show();
    }*/