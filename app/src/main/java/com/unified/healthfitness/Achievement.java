package com.unified.healthfitness;

import java.util.Objects;

public class Achievement {
    private final String title;
    private final String description;
    private final int target;
    private int progress;
    private final int iconResId;
    private final int xpReward;

    public Achievement(String title, String description, int target, int iconResId, int xpReward) {
        this.title = title;
        this.description = description;
        this.target = target;
        this.iconResId = iconResId;
        this.xpReward = xpReward;
        this.progress = 0;
    }

    public Achievement(Achievement other) {
        this.title = other.title;
        this.description = other.description;
        this.target = other.target;
        this.progress = other.progress;
        this.iconResId = other.iconResId;
        this.xpReward = other.xpReward;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public int getIconResId() { return iconResId; }
    public int getXpReward() { return xpReward; }

    public void setProgress(int progress) {
        this.progress = Math.min(progress, target);
    }

    public boolean isCompleted() {
        return progress >= target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Achievement that = (Achievement) o;
        return target == that.target &&
                progress == that.progress &&
                iconResId == that.iconResId &&
                xpReward == that.xpReward &&
                title.equals(that.title) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, target, progress, iconResId, xpReward);
    }
}
