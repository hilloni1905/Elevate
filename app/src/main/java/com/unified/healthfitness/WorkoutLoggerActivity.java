package com.unified.healthfitness;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutLoggerActivity extends AppCompatActivity {

    private RecyclerView logsRecyclerView;
    private WorkoutLogDatabaseAdapter adapter;
    private TextView emptyStateText, todayCountText, yesterdayCountText;
    private WorkoutRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_logger);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = new WorkoutRepository(this);

        initViews();
        setupRecyclerView();
        loadWorkoutLogs();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkoutLogs();
    }

    private void initViews() {
        logsRecyclerView = findViewById(R.id.logsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        todayCountText = findViewById(R.id.todayCountText);
        yesterdayCountText = findViewById(R.id.yesterdayCountText);
    }

    private void setupRecyclerView() {
        adapter = new WorkoutLogDatabaseAdapter(this, new ArrayList<>(), this::deleteWorkoutLog);
        logsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logsRecyclerView.setAdapter(adapter);
    }

    private void loadWorkoutLogs() {
        repository.getAllLogs(new WorkoutRepository.DataCallback<List<WorkoutLogEntity>>() {
            @Override
            public void onSuccess(List<WorkoutLogEntity> allLogs) {
                // Group logs by date
                Map<String, List<WorkoutLogEntity>> groupedLogs = new HashMap<>();
                for (WorkoutLogEntity log : allLogs) {
                    if (!groupedLogs.containsKey(log.date)) {
                        groupedLogs.put(log.date, new ArrayList<>());
                    }
                    groupedLogs.get(log.date).add(log);
                }

                // Create date groups
                List<WorkoutLogDatabaseAdapter.DateGroup> dateGroups = new ArrayList<>();
                List<String> sortedDates = new ArrayList<>(groupedLogs.keySet());
                sortedDates.sort((a, b) -> b.compareTo(a)); // Sort descending

                for (String date : sortedDates) {
                    String displayName = WorkoutRepository.formatDisplayDate(date);
                    dateGroups.add(new WorkoutLogDatabaseAdapter.DateGroup(
                            date, displayName, groupedLogs.get(date)
                    ));
                }

                adapter.updateLogs(dateGroups);
                updateEmptyState(dateGroups.isEmpty());
                updateStats();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(WorkoutLoggerActivity.this,
                        "Error loading logs: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                updateEmptyState(true);
            }
        });
    }

    private void deleteWorkoutLog(WorkoutLogEntity log) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Workout?")
                .setMessage("Are you sure you want to delete this workout log?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteLog(log, new WorkoutRepository.DataCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Toast.makeText(WorkoutLoggerActivity.this,
                                    "Workout deleted", Toast.LENGTH_SHORT).show();
                            loadWorkoutLogs();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(WorkoutLoggerActivity.this,
                                    "Error deleting: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateText.setVisibility(View.VISIBLE);
            logsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            logsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        String today = WorkoutRepository.getTodayDate();
        String yesterday = WorkoutRepository.getYesterdayDate();

        repository.getCountByDate(today, new WorkoutRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                todayCountText.setText(String.valueOf(count));
            }

            @Override
            public void onError(Exception e) {
                todayCountText.setText("0");
            }
        });

        repository.getCountByDate(yesterday, new WorkoutRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                yesterdayCountText.setText(String.valueOf(count));
            }

            @Override
            public void onError(Exception e) {
                yesterdayCountText.setText("0");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.shutdown();
    }
}