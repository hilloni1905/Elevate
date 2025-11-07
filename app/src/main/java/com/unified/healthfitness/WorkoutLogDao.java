package com.unified.healthfitness;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface WorkoutLogDao {

    @Insert
    long insert(WorkoutLogEntity log);

    @Update
    void update(WorkoutLogEntity log);

    @Delete
    void delete(WorkoutLogEntity log);

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    List<WorkoutLogEntity> getAllLogs();

    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp DESC")
    List<WorkoutLogEntity> getLogsByDate(String date);

    @Query("SELECT DISTINCT date FROM workout_logs ORDER BY date DESC")
    List<String> getAllDates();

    @Query("SELECT COUNT(*) FROM workout_logs WHERE date = :date")
    int getCountByDate(String date);

    @Query("DELETE FROM workout_logs WHERE date = :date")
    void deleteByDate(String date);

    @Query("DELETE FROM workout_logs")
    void deleteAll();

    @Query("SELECT * FROM workout_logs WHERE id = :id")
    WorkoutLogEntity getLogById(int id);

    // Get logs between date range
    @Query("SELECT * FROM workout_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    List<WorkoutLogEntity> getLogsBetweenDates(String startDate, String endDate);

    // Get total workout count
    @Query("SELECT COUNT(*) FROM workout_logs")
    int getTotalCount();

    // Get logs for current month
    @Query("SELECT * FROM workout_logs WHERE date LIKE :yearMonth || '%' ORDER BY timestamp DESC")
    List<WorkoutLogEntity> getLogsForMonth(String yearMonth);
}