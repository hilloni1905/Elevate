package com.unified.healthfitness;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class SleepMainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private RadioGroup rgMode;
    private RadioButton rbWake, rbBed;
    private Spinner spCycles;
    private Button btnCalculate, btnClear;
    private TextView tvResults, tvTips;

    private final int SLEEP_CYCLE_MIN = 90;
    private final int FALL_ASLEEP_MIN = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_main);

        timePicker = findViewById(R.id.timePicker);
        rgMode = findViewById(R.id.rgMode);
        rbWake = findViewById(R.id.rbWake);
        rbBed = findViewById(R.id.rbBed);
        spCycles = findViewById(R.id.spCycles);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);
        tvResults = findViewById(R.id.tvResults);
        tvTips = findViewById(R.id.tvTips);

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);
        Calendar cal = Calendar.getInstance();
        timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(cal.get(Calendar.MINUTE));

        btnCalculate.setOnClickListener(v -> calculate());
        btnClear.setOnClickListener(v -> clearAll());
    }

    private void calculate() {
        int cycles;
        switch (spCycles.getSelectedItemPosition()) {
            case 0: cycles = 3; break;
            case 1: cycles = 4; break;
            case 2: cycles = 5; break;
            default: cycles = 6; break;
        }

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        StringBuilder sb = new StringBuilder();
        if (rbWake.isChecked()) {
            sb.append("If you want to wake at ").append(formatTime(cal.getTime())).append(":\n\n");
            for (int c = cycles; c >= 1; c--) {
                Calendar bed = (Calendar) cal.clone();
                bed.add(Calendar.MINUTE, -((c * SLEEP_CYCLE_MIN) + FALL_ASLEEP_MIN));
                sb.append("• ").append(c).append(" cycles → Go to bed at ").append(formatTime(bed.getTime())).append("\n");
            }
            tvTips.setText(generateTips(cal, true));
        } else {
            sb.append("If you sleep at ").append(formatTime(cal.getTime())).append(":\n\n");
            for (int c = 1; c <= cycles; c++) {
                Calendar wake = (Calendar) cal.clone();
                wake.add(Calendar.MINUTE, (c * SLEEP_CYCLE_MIN) + FALL_ASLEEP_MIN);
                sb.append("• ").append(c).append(" cycles → Wake at ").append(formatTime(wake.getTime())).append("\n");
            }
            tvTips.setText(generateTips(cal, false));
        }

        tvResults.setText(sb.toString());
    }

    private void clearAll() {
        Calendar cal = Calendar.getInstance();
        timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(cal.get(Calendar.MINUTE));
        spCycles.setSelection(2);
        rbWake.setChecked(true);
        tvResults.setText("Your suggested bed/wake times will appear here.");
        tvTips.setText("Sleep Tips:");
    }

    private String formatTime(Date date) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }

    private String generateTips(Calendar base, boolean isWakeMode) {
        int hour = base.get(Calendar.HOUR_OF_DAY);
        StringBuilder sb = new StringBuilder("Sleep Tips:\n");
        if (isWakeMode) {
            if (hour >= 5 && hour <= 9)
                sb.append("• Get sunlight soon after waking to strengthen circadian rhythm.\n");
            else
                sb.append("• Try to get natural light in the morning.\n");
            sb.append("• Avoid caffeine 6h before bed.\n• Maintain consistent sleep times.\n");
        } else {
            if ((hour >= 22 && hour <= 23) || (hour >= 0 && hour <= 2))
                sb.append("• Dim lights and avoid screens 30 mins before bed.\n");
            else
                sb.append("• Keep a consistent sleep schedule.\n");
            sb.append("• Avoid heavy meals and keep your room cool.\n");
        }
        return sb.toString();
    }
}
