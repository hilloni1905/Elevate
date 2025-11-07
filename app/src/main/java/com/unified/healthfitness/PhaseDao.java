package com.unified.healthfitness;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PhaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Phase... phases);

    @Query("SELECT * FROM phases")
    List<Phase> getAll();

    @Query("SELECT * FROM phases WHERE :day > dayInCycle ORDER BY dayInCycle DESC LIMIT 1")
    Phase findByDay(int day);

    @Query("DELETE FROM phases")
    void deleteAll();
}
