package com.unified.healthfitness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class WaterMainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView waterCountText, goalText, reminderTimeText;
    private MaterialButton addWaterBtn, setGoalBtn, setReminderBtn, doneBtn;

    private SharedPreferences prefs;
    private int waterIntake = 0;
    private int waterGoal = 2000; // default 2 liters
    private boolean goalReached = false; // track if goal was already reached

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_main);

        progressBar = findViewById(R.id.progressBar);
        waterCountText = findViewById(R.id.waterCountText);
        goalText = findViewById(R.id.goalText);
        reminderTimeText = findViewById(R.id.reminderTimeText);
        addWaterBtn = findViewById(R.id.addWaterBtn);
        setGoalBtn = findViewById(R.id.setGoalBtn);
        setReminderBtn = findViewById(R.id.setReminderBtn);
        doneBtn = findViewById(R.id.doneBtn);

        prefs = getSharedPreferences("HydrationPrefs", Context.MODE_PRIVATE);
        waterIntake = prefs.getInt("waterIntake", 0);
        waterGoal = prefs.getInt("waterGoal", 2000);

        updateUI();

        addWaterBtn.setOnClickListener(v -> {
            waterIntake += 250;
            saveData();
            updateUI();
            checkGoalReached();
        });

        setGoalBtn.setOnClickListener(v -> showGoalDialog());
        setReminderBtn.setOnClickListener(v -> showTimePicker());

        doneBtn.setOnClickListener(v -> {
            waterIntake = 0;
            goalReached = false; // reset flag
            saveData();
            updateUI();
            Toast.makeText(this, "Daily hydration marked as done!", Toast.LENGTH_SHORT).show();
        });

        createNotificationChannel();
    }

    private void updateUI() {
        progressBar.setMax(waterGoal);
        progressBar.setProgress(waterIntake);
        waterCountText.setText(getString(R.string.water_text, waterIntake));
        goalText.setText(getString(R.string.goal_text, waterGoal));

        // Reset progress bar color if below goal
        if (!goalReached) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("waterIntake", waterIntake);
        editor.putInt("waterGoal", waterGoal);
        editor.apply();
    }

    private void checkGoalReached() {
        if (!goalReached && waterIntake >= waterGoal) {
            goalReached = true; // mark as achieved
            Toast.makeText(this, "🎉 Congrats! You reached your daily goal!", Toast.LENGTH_LONG).show();

            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.gold)));

            // Custom Goal Achieved Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_goal_achieved, null);
            builder.setView(dialogView);

            MaterialButton doneBtn = dialogView.findViewById(R.id.goalDoneBtn);
            AlertDialog dialog = builder.create();
            dialog.show();

            doneBtn.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private void showGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_goal, null);
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.inputGoal);
        MaterialButton doneBtn = dialogView.findViewById(R.id.dialogDoneBtn);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.dialogCancelBtn);

        AlertDialog dialog = builder.create();
        dialog.show();

        doneBtn.setOnClickListener(v -> {
            String value = input.getText().toString();
            if (!value.isEmpty()) {
                waterGoal = Integer.parseInt(value);
                goalReached = false; // reset flag when goal changes
                saveData();
                updateUI();
                dialog.dismiss();
            } else {
                input.setError("Please enter a valid number");
            }
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> scheduleCustomReminder(hourOfDay, minute1),
                hour,
                minute,
                true
        );
        timePickerDialog.show();
    }

    private void scheduleCustomReminder(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    scheduleAlarm(alarmManager, calendar, pendingIntent);
                } else {
                    Toast.makeText(this, "Cannot schedule exact alarm on this device.", Toast.LENGTH_SHORT).show();
                }
            } else {
                scheduleAlarm(alarmManager, calendar, pendingIntent);
            }
        }

        reminderTimeText.setText(getString(R.string.reminder_text, hour, minute));
        Toast.makeText(this, getString(R.string.reminder_text, hour, minute), Toast.LENGTH_SHORT).show();
    }

    private void scheduleAlarm(AlarmManager alarmManager, Calendar calendar, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelCompat channel = new NotificationChannelCompat.Builder(
                    "hydrationChannel",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            )
                    .setName("Hydration Reminder")
                    .setDescription("Channel for water reminders")
                    .build();

            NotificationManagerCompat.from(this).createNotificationChannel(channel);
        }
    }
}
