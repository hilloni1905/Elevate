package com.unified.healthfitness;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unified.healthfitness.databinding.ActivityEditAlarmBinding;
import com.unified.healthfitness.db.Alarm;
import com.unified.healthfitness.db.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditAlarmActivity extends AppCompatActivity {
    private ActivityEditAlarmBinding binding;
    private AppDatabase db;
    private Alarm alarm;
    private Calendar selectedDateTime = Calendar.getInstance();
    private final List<Integer> snoozeOptions = Arrays.asList(5, 10, 15, 20, 30);

    // Formatters for displaying user-friendly text
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);
        setupSnoozeSpinner();

        long id = getIntent().getLongExtra("alarm_id", -1);
        if (id != -1) {
            setTitle("Edit Alarm");
            alarm = db.alarmDao().findById(id);
            if (alarm != null) {
                selectedDateTime.setTimeInMillis(alarm.timeMillis);
                binding.etLabel.setText(alarm.label);
                binding.switchEnable.setChecked(alarm.enabled);
                // Set spinner selection
                int snoozePosition = snoozeOptions.indexOf(alarm.snoozeMinutes);
                if (snoozePosition != -1) {
                    binding.spinnerSnooze.setSelection(snoozePosition);
                }
            }
        } else {
            setTitle("New Alarm");
            alarm = null;
            // Default snooze to 10 minutes
            binding.spinnerSnooze.setSelection(1); // 10 minutes
            binding.switchEnable.setChecked(true);
        }

        updateDateTimeUI();

        binding.btnSelectDate.setOnClickListener(v -> showDatePicker());
        binding.btnSelectTime.setOnClickListener(v -> showTimePicker());
        binding.btnSave.setOnClickListener(v -> saveAlarm());
    }

    private void setupSnoozeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("5 minutes", "10 minutes", "15 minutes", "20 minutes", "30 minutes"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSnooze.setAdapter(adapter);
    }


    private void updateDateTimeUI() {
        // Check if the date is today
        Calendar today = Calendar.getInstance();
        if (selectedDateTime.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDateTime.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            binding.tvSelectedDate.setText("Today");
        } else {
            binding.tvSelectedDate.setText(DATE_FORMAT.format(selectedDateTime.getTime()));
        }
        binding.tvSelectedTime.setText(TIME_FORMAT.format(selectedDateTime.getTime()));
    }


    private void showDatePicker() {
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeUI();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void showTimePicker() {
        TimePickerDialog tp = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeUI();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false); // Use 12-hour format
        tp.show();
    }

    private void saveAlarm() {
        long millis = selectedDateTime.getTimeInMillis();

        // Ensure alarm is in the future
        if (millis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a time in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        String label = binding.etLabel.getText().toString().trim();
        if (label.isEmpty()) {
            label = "Alarm";
        }

        int snooze = snoozeOptions.get(binding.spinnerSnooze.getSelectedItemPosition());
        boolean enabled = binding.switchEnable.isChecked();

        if (alarm == null) {
            // Creating a new alarm
            Alarm a = new Alarm(millis, label, enabled, snooze);
            long newId = db.alarmDao().insert(a);
            if (enabled) {
                AlarmScheduler.scheduleAlarm(this, newId, millis);
            }
            Toast.makeText(this, "Alarm Saved", Toast.LENGTH_SHORT).show();
        } else {
            // Updating an existing alarm
            alarm.timeMillis = millis;
            alarm.label = label;
            alarm.snoozeMinutes = snooze;
            alarm.enabled = enabled;
            db.alarmDao().update(alarm);
            if (enabled) {
                AlarmScheduler.scheduleAlarm(this, alarm.id, millis);
            } else {
                AlarmScheduler.cancelAlarm(this, alarm.id);
            }
            Toast.makeText(this, "Alarm Updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
