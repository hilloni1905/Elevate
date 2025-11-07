package com.unified.healthfitness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.unified.healthfitness.db.Alarm;
import com.unified.healthfitness.db.AppDatabase;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            AppDatabase db = AppDatabase.getInstance(context);
            long now = System.currentTimeMillis();
            List<Alarm> alarms = db.alarmDao().getEnabledFutureAlarms(now);
            for (Alarm a : alarms) {
                AlarmScheduler.scheduleAlarm(context, a.id, a.timeMillis);
            }
        }
    }
}
