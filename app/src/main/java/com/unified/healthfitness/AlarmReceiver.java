package com.unified.healthfitness;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.unified.healthfitness.db.Alarm;
import com.unified.healthfitness.db.AppDatabase;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "alarm_channel";
    public static final int NOTIF_ID_BASE = 2000;

    public static final String ACTION_TRIGGER = "com.sachi.alarmmanager.ACTION_TRIGGER";
    public static final String ACTION_SNOOZE = "com.sachi.alarmmanager.ACTION_SNOOZE";
    public static final String ACTION_DISMISS = "com.sachi.alarmmanager.ACTION_DISMISS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        long alarmId = intent.getLongExtra("alarm_id", -1);

        AppDatabase db = AppDatabase.getInstance(context.getApplicationContext());

        if (ACTION_SNOOZE.equals(action)) {
            int minutes = intent.getIntExtra("snooze_minutes", 5);
            long next = System.currentTimeMillis() + minutes * 60_000L;
            AlarmScheduler.scheduleAlarm(context, alarmId, next);
            showSimpleNotification(context, "Snoozed for " + minutes + " minutes", alarmId);
            return;
        } else if (ACTION_DISMISS.equals(action)) {
            Alarm alarm = db.alarmDao().findById(alarmId);
            if (alarm != null) {
                alarm.enabled = false;
                db.alarmDao().update(alarm);
            }
            AlarmScheduler.cancelAlarm(context, alarmId);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel((int)(NOTIF_ID_BASE + alarmId));
            return;
        } else if (ACTION_TRIGGER.equals(action) || action == null) {
            Alarm alarm = (alarmId != -1) ? db.alarmDao().findById(alarmId) : null;
            String label = (alarm != null && alarm.label != null) ? alarm.label : "Alarm";

            Intent fullIntent = new Intent(context, FullScreenAlarmActivity.class);
            fullIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            fullIntent.putExtra("alarm_id", alarmId);
            fullIntent.putExtra("label", label);
            context.startActivity(fullIntent);

            createNotificationChannel(context);

            Intent contentIntent = new Intent(context, AlarmMainActivity.class);
            PendingIntent contentPi = PendingIntent.getActivity(context, (int) alarmId + 1, contentIntent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

            Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
            snoozeIntent.setAction(ACTION_SNOOZE);
            snoozeIntent.putExtra("alarm_id", alarmId);
            snoozeIntent.putExtra("snooze_minutes", (alarm != null ? alarm.snoozeMinutes : 5));
            PendingIntent snoozePi = PendingIntent.getBroadcast(context, (int) alarmId + 2, snoozeIntent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

            Intent dismissIntent = new Intent(context, AlarmReceiver.class);
            dismissIntent.setAction(ACTION_DISMISS);
            dismissIntent.putExtra("alarm_id", alarmId);
            PendingIntent dismissPi = PendingIntent.getBroadcast(context, (int) alarmId + 3, dismissIntent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(label)
                    .setContentText("Tap to open — or Snooze / Dismiss")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .setContentIntent(contentPi)
                    .addAction(0, "Snooze", snoozePi)
                    .addAction(0, "Dismiss", dismissPi);

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.notify((int)(NOTIF_ID_BASE + alarmId), builder.build());
        }
    }

    private void showSimpleNotification(Context context, String text, long alarmId) {
        createNotificationChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Alarm")
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int)(NOTIF_ID_BASE + alarmId), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for Alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}
