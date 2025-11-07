package com.unified.healthfitness;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatEntity> getAll();

    @Insert
    long insert(ChatEntity entity);

    @Update
    void update(ChatEntity entity);

    @Delete
    void delete(ChatEntity entity);

    @Query("DELETE FROM chat_messages")
    void deleteAll();
}
