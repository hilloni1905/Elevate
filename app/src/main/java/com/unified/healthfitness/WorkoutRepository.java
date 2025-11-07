package com.unified.healthfitness;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {

    private WorkoutLogDao workoutLogDao;
    private ExecutorService executorService;
    private Handler mainHandler;

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    public WorkoutRepository(Context context) {
        WorkoutDatabase database = WorkoutDatabase.getInstance(context);
        workoutLogDao = database.workoutLogDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Insert workout log
    public void insertLog(WorkoutLogEntity log, DataCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = workoutLogDao.insert(log);
                mainHandler.post(() -> callback.onSuccess(id));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Get all logs
    public void getAllLogs(DataCallback<List<WorkoutLogEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<WorkoutLogEntity> logs = workoutLogDao.getAllLogs();
                mainHandler.post(() -> callback.onSuccess(logs));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Get logs by date
    public void getLogsByDate(String date, DataCallback<List<WorkoutLogEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<WorkoutLogEntity> logs = workoutLogDao.getLogsByDate(date);
                mainHandler.post(() -> callback.onSuccess(logs));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Get all dates
    public void getAllDates(DataCallback<List<String>> callback) {
        executorService.execute(() -> {
            try {
                List<String> dates = workoutLogDao.getAllDates();
                mainHandler.post(() -> callback.onSuccess(dates));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Delete log
    public void deleteLog(WorkoutLogEntity log, DataCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                workoutLogDao.delete(log);
                mainHandler.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Get count by date
    public void getCountByDate(String date, DataCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = workoutLogDao.getCountByDate(date);
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Helper method to get today's date
    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Helper method to get yesterday's date
    public static String getYesterdayDate() {
        long yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(yesterday));
    }

    // Helper method to format display date
    public static String formatDisplayDate(String date) {
        String today = getTodayDate();
        String yesterday = getYesterdayDate();

        if (date.equals(today)) {
            return "Today";
        } else if (date.equals(yesterday)) {
            return "Yesterday";
        } else {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date d = inputFormat.parse(date);
                return outputFormat.format(d);
            } catch (Exception e) {
                return date;
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}