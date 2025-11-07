package com.unified.healthfitness.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UserAnswersDao {

    @Insert
    long insert(UserAnswers userAnswers);

    @Query("SELECT * FROM user_answers")
    List<UserAnswers> getAll();

    @Query("SELECT * FROM user_answers WHERE id = :userId")
    UserAnswers findById(int userId);
}
