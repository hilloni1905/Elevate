

// ========================================
// FILE 2: UserDao.java
// Package: com.unified.healthfitness.database
// Data Access Object for User
// ========================================

package com.unified.healthfitness;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.unified.healthfitness.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getUserByFirebaseId(String userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    UserEntity getCurrentUser();

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    LiveData<UserEntity> getCurrentUserLive();

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("UPDATE users SET weight = :weight, lastUpdated = :timestamp WHERE id = :id")
    void updateWeight(int id, float weight, long timestamp);

    @Query("UPDATE users SET dailyCalories = :calories, proteinGoal = :protein, carbsGoal = :carbs, fatGoal = :fat, lastUpdated = :timestamp WHERE id = :id")
    void updateNutritionGoals(int id, int calories, int protein, int carbs, int fat, long timestamp);
}

