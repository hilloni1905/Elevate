package com.unified.healthfitness.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.HashMap;

@Entity(tableName = "user_answers")
@TypeConverters(Converters.class) // We'll create this converter next
public class UserAnswers {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // We can add a name to identify the user
    public String userName;

    public HashMap<Integer, Integer> answers;

    public UserAnswers(String userName, HashMap<Integer, Integer> answers) {
        this.userName = userName;
        this.answers = answers;
    }
}
