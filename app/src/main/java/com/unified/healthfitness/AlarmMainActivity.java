package com.unified.healthfitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.unified.healthfitness.databinding.ActivityAlarmMainBinding;
import com.unified.healthfitness.db.Alarm;
import com.unified.healthfitness.db.AppDatabase;
//import com.unified.healthfitness.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class AlarmMainActivity extends AppCompatActivity implements AlarmListAdapter.OnAlarmClickListener {
    private ActivityAlarmMainBinding binding;
    private AlarmListAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlarmMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);

        adapter = new AlarmListAdapter(new ArrayList<>(), this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, EditAlarmActivity.class);
            startActivity(i);
        });

        binding.swipeRefresh.setOnRefreshListener(() -> loadAlarms());
    }

    private void loadAlarms() {
        List<Alarm> alarms = db.alarmDao().getAllAlarms();
        adapter.setAlarms(alarms);
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
    }

    @Override
    public void onAlarmToggle(Alarm alarm, boolean enabled) {
        alarm.enabled = enabled;
        db.alarmDao().update(alarm);
        if (enabled) {
            AlarmScheduler.scheduleAlarm(this, alarm.id, alarm.timeMillis);
            Toast.makeText(this, "Alarm enabled", Toast.LENGTH_SHORT).show();
        } else {
            AlarmScheduler.cancelAlarm(this, alarm.id);
            Toast.makeText(this, "Alarm disabled", Toast.LENGTH_SHORT).show();
        }
        loadAlarms();
    }

    @Override
    public void onAlarmEdit(Alarm alarm) {
        Intent i = new Intent(this, EditAlarmActivity.class);
        i.putExtra("alarm_id", alarm.id);
        startActivity(i);
    }

    @Override
    public void onAlarmDelete(Alarm alarm) {
        db.alarmDao().delete(alarm);
        AlarmScheduler.cancelAlarm(this, alarm.id);
        loadAlarms();
    }
}
