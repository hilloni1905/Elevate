package com.unified.healthfitness.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timeMillis;
    public String label;
    public boolean enabled;
    public int snoozeMinutes;

    public Alarm(long timeMillis, String label, boolean enabled, int snoozeMinutes) {
        this.timeMillis = timeMillis;
        this.label = label;
        this.enabled = enabled;
        this.snoozeMinutes = snoozeMinutes;
    }
}
