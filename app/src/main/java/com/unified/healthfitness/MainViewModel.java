package com.unified.healthfitness;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    // Constants
    private static final String PREFS_NAME = "DistanceAppPrefs";
    private static final String LAST_RESET_KEY = "lastDate";
    private static final String STEPS_AT_DAY_START_KEY = "stepsAtDayStart";

    // LiveData
    private final MutableLiveData<Integer> steps = new MutableLiveData<>(0);
    private final MutableLiveData<Double> distance = new MutableLiveData<>(0.0);
    private final MutableLiveData<Integer> calories = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> streak = new MutableLiveData<>(3);
    private final MutableLiveData<Integer> xp = new MutableLiveData<>(2172);
    private final MutableLiveData<Integer> level = new MutableLiveData<>(3);
    private final MutableLiveData<List<Achievement>> achievements = new MutableLiveData<>(new ArrayList<>());
    private final LiveData<List<DailyData>> weeklyData;
    private final MutableLiveData<Integer> dailyGoal = new MutableLiveData<>(5000);
    private final MutableLiveData<List<LeaderboardEntry>> leaderboard = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Float> weeklyTrend = new MutableLiveData<>(0.0f);
    private final MutableLiveData<Achievement> newlyCompletedAchievement = new MutableLiveData<>();
    private final MutableLiveData<Boolean> goalCompleted = new MutableLiveData<>(false);

    private int stepsAtDayStart = 0;
    private final DailyDataDao dailyDataDao;
    private final ExecutorService executorService;

    public static class LeaderboardEntry {
        private final String name;
        private int score;
        private boolean goalMet;

        public LeaderboardEntry(String name, int score, boolean goalMet) {
            this.name = name;
            this.score = score;
            this.goalMet = goalMet;
        }

        public String getName() { return name; }
        public int getScore() { return score; }
        public boolean isGoalMet() { return goalMet; }

        public void setScore(int score) { this.score = score; }
        public void setGoalMet(boolean goalMet) { this.goalMet = goalMet; }
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        StepsAppDatabase database = StepsAppDatabase.getInstance(application);
        dailyDataDao = database.dailyDataDao();
        executorService = Executors.newSingleThreadExecutor();

        weeklyData = dailyDataDao.getLastSevenDays();

        loadInitialData();
        initializeAchievementsAndLeaderboard();
    }

    private void loadInitialData() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        stepsAtDayStart = prefs.getInt(STEPS_AT_DAY_START_KEY, 0);
        checkIfNewDay(0); // Perform an initial check
    }

    private void initializeAchievementsAndLeaderboard() {
        List<Achievement> initialAchievements = new ArrayList<>();
        initialAchievements.add(new Achievement("Daily Strider", "Walk 1,000 steps in a single day.", 1000, R.drawable.ic_star, 100));
        initialAchievements.add(new Achievement("Walker", "Walk 5,000 steps in a single day.", 5000, R.drawable.ic_star, 250));
        initialAchievements.add(new Achievement("High Stepper", "Walk 10,000 steps in a single day.", 10000, R.drawable.ic_star, 500));
        initialAchievements.add(new Achievement("Super Walker", "Walk 15,000 steps in a single day.", 15000, R.drawable.ic_star, 750));
        initialAchievements.add(new Achievement("Mountain Climber", "Walk 25,000 steps in a single day.", 25000, R.drawable.ic_star, 1000));
        initialAchievements.add(new Achievement("Insane Walker", "Walk 50,000 steps in a single day.", 50000, R.drawable.ic_star, 2000));
        initialAchievements.add(new Achievement("Streak Keeper", "Maintain a 7-day streak.", 7, R.drawable.ic_star, 1000));
        initialAchievements.add(new Achievement("Distance Master", "Walk 5km total.", 5, R.drawable.ic_distance_master, 250));
        initialAchievements.add(new Achievement("Distance Runner", "Walk 10km total.", 10, R.drawable.ic_distance_master, 500));
        initialAchievements.add(new Achievement("Marathoner", "Walk 42km total.", 42, R.drawable.ic_distance_master, 1000));
        initialAchievements.add(new Achievement("Century Walker", "Walk 100km total.", 100, R.drawable.ic_distance_master, 1500));
        initialAchievements.add(new Achievement("Explorer", "Walk 250km total.", 250, R.drawable.ic_distance_master, 2500));
        initialAchievements.add(new Achievement("Calorie Crusher", "Burn 500 calories in one day.", 500, R.drawable.ic_star, 500));
        initialAchievements.add(new Achievement("Calorie Annihilator", "Burn 1,000 calories in one day.", 1000, R.drawable.ic_star, 1000));
        initialAchievements.add(new Achievement("Level 5 Reached", "Reach level 5.", 5, R.drawable.ic_star, 500));
        initialAchievements.add(new Achievement("Level 10 Reached", "Reach level 10.", 10, R.drawable.ic_star, 1000));
        initialAchievements.add(new Achievement("Early Bird", "Complete a walk before 8 AM.", 1, R.drawable.ic_early_bird, 150));
        initialAchievements.add(new Achievement("Night Owl", "Complete a walk after 10 PM.", 1, R.drawable.ic_star, 150));
        achievements.setValue(initialAchievements);

        List<LeaderboardEntry> dummyLeaderboard = new ArrayList<>();
        dummyLeaderboard.add(new LeaderboardEntry("You", steps.getValue() != null ? steps.getValue() : 0, false));
        dummyLeaderboard.add(new LeaderboardEntry("Shubh", 4582, false));
        dummyLeaderboard.add(new LeaderboardEntry("Krish", 2103, false));
        leaderboard.setValue(dummyLeaderboard);
        updateLeaderboard();
    }

    private void checkIfNewDay(int currentSensorSteps) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = prefs.getString(LAST_RESET_KEY, "");

        if (!today.equals(lastDate)) {
            stepsAtDayStart = currentSensorSteps;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(LAST_RESET_KEY, today);
            editor.putInt(STEPS_AT_DAY_START_KEY, stepsAtDayStart);
            editor.apply();

            steps.postValue(0);
            distance.postValue(0.0);
            calories.postValue(0);
            goalCompleted.postValue(false); // Reset the goal completion flag

            executorService.execute(() -> {
                DailyData todayData = new DailyData(today);
                dailyDataDao.insert(todayData);
            });
        }
    }

    public void updateSteps(int newStepsFromSensor) {
        checkIfNewDay(newStepsFromSensor);

        int dailySteps = newStepsFromSensor - stepsAtDayStart;
        if (dailySteps < 0) { // Device reboot might reset the sensor value
            stepsAtDayStart = newStepsFromSensor;
            dailySteps = 0;
            getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putInt(STEPS_AT_DAY_START_KEY, stepsAtDayStart).apply();
        }

        Integer oldSteps = steps.getValue();
        steps.postValue(dailySteps);
        double newDistance = dailySteps * 0.0008;
        distance.postValue(newDistance);
        int newCalories = (int) (dailySteps * 0.04);
        calories.postValue(newCalories);

        if (oldSteps != null && (dailySteps / 100 > oldSteps / 100)) {
            addXp((dailySteps / 100) - (oldSteps / 100));
        }

        Integer goal = dailyGoal.getValue();
        if (goal != null && oldSteps != null && oldSteps < goal && dailySteps >= goal) {
            goalCompleted.postValue(true);
            addXp(500); // Bonus XP
        }

        // Use final variables for the lambda expression to prevent build error
        final int finalDailySteps = dailySteps;
        final double finalNewDistance = newDistance;
        final int finalNewCalories = newCalories;
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        executorService.execute(() -> {
            dailyDataDao.updateStepsForDate(todayStr, finalDailySteps, finalNewDistance, finalNewCalories);
        });

        updateAchievements();
        updateLeaderboard();
    }
    
    public void onGoalCompletedShown() {
        goalCompleted.setValue(false);
    }
    
    public void onNewAchievementShown() {
        newlyCompletedAchievement.setValue(null);
    }

    private void updateLeaderboard() {
        List<LeaderboardEntry> currentLeaderboard = leaderboard.getValue();
        Integer currentSteps = steps.getValue();
        Integer goal = dailyGoal.getValue();
        if (currentLeaderboard == null || currentSteps == null || goal == null) return;

        for (LeaderboardEntry entry : currentLeaderboard) {
            if ("You".equals(entry.getName())) {
                entry.setScore(currentSteps);
                entry.setGoalMet(currentSteps >= goal);
                break;
            }
        }

        Collections.sort(currentLeaderboard, (e1, e2) -> Integer.compare(e2.getScore(), e1.getScore()));
        leaderboard.postValue(new ArrayList<>(currentLeaderboard));
    }

    private void addXp(int points) {
        xp.postValue((xp.getValue() != null ? xp.getValue() : 0) + points);
        checkLevelUp();
    }

    private void checkLevelUp() {
        int currentXp = xp.getValue() != null ? xp.getValue() : 0;
        int currentLevel = level.getValue() != null ? level.getValue() : 1;
        int newLevel = currentLevel;

        while (currentXp >= newLevel * 1000) {
            newLevel++;
        }

        if (newLevel > currentLevel) {
            level.postValue(newLevel);
        }
    }

    public void updateAchievements() {
        List<Achievement> currentAchievements = achievements.getValue();
        if (currentAchievements == null) return;
        
        List<Achievement> updatedAchievements = new ArrayList<>();

        Integer currentSteps = steps.getValue();
        Double currentDistance = distance.getValue();
        Integer currentStreak = streak.getValue();
        Integer currentCalories = calories.getValue();
        Integer currentLevel = level.getValue();

        for (Achievement achievement : currentAchievements) {
            Achievement updatedAchievement = new Achievement(achievement);
            boolean wasCompleted = updatedAchievement.isCompleted();

            if (!wasCompleted) {
                 switch (updatedAchievement.getTitle()) {
                    case "Daily Strider":
                    case "Walker":
                    case "High Stepper":
                    case "Super Walker":
                    case "Mountain Climber":
                    case "Insane Walker":
                        if (currentSteps != null) updatedAchievement.setProgress(currentSteps);
                        break;
                    case "Distance Master":
                    case "Distance Runner":
                    case "Marathoner":
                    case "Century Walker":
                    case "Explorer":
                        if (currentDistance != null) updatedAchievement.setProgress(currentDistance.intValue());
                        break;
                    case "Calorie Crusher":
                    case "Calorie Annihilator":
                        if (currentCalories != null) updatedAchievement.setProgress(currentCalories);
                        break;
                    case "Level 5 Reached":
                    case "Level 10 Reached":
                        if (currentLevel != null) updatedAchievement.setProgress(currentLevel);
                        break;
                    case "Early Bird":
                        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 8 && currentSteps != null && currentSteps > 500) {
                            updatedAchievement.setProgress(1);
                        }
                        break;
                    case "Night Owl":
                        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 22 && currentSteps != null && currentSteps > 500) {
                            updatedAchievement.setProgress(1);
                        }
                        break;
                    case "Streak Keeper":
                        if (currentStreak != null) updatedAchievement.setProgress(currentStreak);
                        break;
                }

                if (updatedAchievement.isCompleted()) {
                    newlyCompletedAchievement.postValue(updatedAchievement);
                    addXp(updatedAchievement.getXpReward());
                }
            }
            updatedAchievements.add(updatedAchievement);
        }
        achievements.postValue(updatedAchievements);
    }

    public void setDailyGoal(int newGoal) {
        dailyGoal.postValue(newGoal);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // Getters
    public LiveData<Integer> getSteps() { return steps; }
    public LiveData<Double> getDistance() { return distance; }
    public LiveData<Integer> getCalories() { return calories; }
    public LiveData<Integer> getStreak() { return streak; }
    public LiveData<Integer> getXp() { return xp; }
    public LiveData<Integer> getLevel() { return level; }
    public LiveData<List<Achievement>> getAchievements() { return achievements; }
    public LiveData<List<DailyData>> getWeeklyData() { return weeklyData; }
    public LiveData<Integer> getDailyGoal() { return dailyGoal; }
    public LiveData<List<LeaderboardEntry>> getLeaderboard() { return leaderboard; }
    public LiveData<Float> getWeeklyTrend() { return weeklyTrend; }
    public LiveData<Achievement> getNewlyCompletedAchievement() { return newlyCompletedAchievement; }
    public LiveData<Boolean> getGoalCompleted() { return goalCompleted; }
}
