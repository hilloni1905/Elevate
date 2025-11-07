package com.unified.healthfitness;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unified.healthfitness.databinding.ActivityFullscreenAlarmBinding;

public class FullScreenAlarmActivity extends AppCompatActivity {
    private ActivityFullscreenAlarmBinding binding;
    private MediaPlayer mediaPlayer;
    private long alarmId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        alarmId = getIntent().getLongExtra("alarm_id", -1);
        String label = getIntent().getStringExtra("label");
        if (label == null) label = "Alarm";

        binding.tvAlarmTitle.setText(label);
        binding.etSnoozeMinutes.setText("5");

        binding.btnSnooze.setOnClickListener(v -> {
            int minutes = 5;
            try {
                String t = binding.etSnoozeMinutes.getText().toString().trim();
                if (!t.isEmpty()) minutes = Integer.parseInt(t);
            } catch (Exception e) { minutes = 5; }
            long next = System.currentTimeMillis() + minutes * 60_000L;
            AlarmScheduler.scheduleAlarm(this, alarmId != -1 ? alarmId : (System.currentTimeMillis() % Integer.MAX_VALUE), next);
            stopAlarmSound();
            Toast.makeText(this, "Snoozed for " + minutes + " minutes", Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnDismiss.setOnClickListener(v -> {
            if (alarmId != -1) {
                com.unified.healthfitness.db.AppDatabase db = com.unified.healthfitness.db.AppDatabase.getInstance(this);
                com.unified.healthfitness.db.Alarm a = db.alarmDao().findById(alarmId);
                if (a != null) {
                    a.enabled = false;
                    db.alarmDao().update(a);
                }
                AlarmScheduler.cancelAlarm(this, alarmId);
            }
            stopAlarmSound();
            finish();
        });

        startAlarmSound();
    }

    private void startAlarmSound() {
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mediaPlayer = MediaPlayer.create(this, alarmSound);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopAlarmSound();
        super.onDestroy();
    }
}
