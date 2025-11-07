package com.unified.healthfitness.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    long insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("SELECT * FROM alarms ORDER BY timeMillis ASC")
    List<Alarm> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    Alarm findById(long id);

    @Query("SELECT * FROM alarms WHERE enabled = 1 AND timeMillis > :now ORDER BY timeMillis ASC")
    List<Alarm> getEnabledFutureAlarms(long now);
}
