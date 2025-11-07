package com.unified.healthfitness;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.util.Random;

public class DashboardFragment extends Fragment {
    private MainViewModel viewModel;
    private CircularProgressIndicator stepsProgress;
    private TextView stepsText, stepsLabel, distanceText, caloriesText, streakText, levelText, quoteText;

    private final String[] quotes = {
            "The journey of a thousand miles begins with a single step.",
            "Don’t count the days, make the days count.",
            "The secret of getting ahead is getting started.",
            "A little progress each day adds up to big results.",
            "Your only limit is you."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Find all views
        stepsProgress = view.findViewById(R.id.steps_progress);
        stepsText = view.findViewById(R.id.steps_text);
        stepsLabel = view.findViewById(R.id.steps_label);
        distanceText = view.findViewById(R.id.distance_text);
        caloriesText = view.findViewById(R.id.calories_text);
        streakText = view.findViewById(R.id.streak_text);
        levelText = view.findViewById(R.id.level_text);
        quoteText = view.findViewById(R.id.motivational_quote_text);

        observeData();
        setRandomQuote();
    }

    private void observeData() {
        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> updateStepsDisplay());
        viewModel.getDailyGoal().observe(getViewLifecycleOwner(), goal -> updateStepsDisplay());
        viewModel.getDistance().observe(getViewLifecycleOwner(), distance -> {
            if (distance != null) {
                distanceText.setText(String.format("%.2f km", distance));
            }
        });
        viewModel.getCalories().observe(getViewLifecycleOwner(), calories -> {
            if (calories != null) {
                caloriesText.setText(String.format("%d kcal", calories));
            }
        });
        viewModel.getStreak().observe(getViewLifecycleOwner(), streak -> {
            if (streak != null) {
                streakText.setText(String.format("%d Days", streak));
            }
        });
        viewModel.getLevel().observe(getViewLifecycleOwner(), level -> {
            if (level != null) {
                levelText.setText(String.valueOf(level));
            }
        });

        viewModel.getGoalCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (completed != null && completed && getView() != null) {
                Snackbar.make(getView(), "Daily Goal Completed! +500 XP", Snackbar.LENGTH_LONG).show();
                viewModel.onGoalCompletedShown(); // Reset the event
            }
        });
    }

    private void updateStepsDisplay() {
        Integer steps = viewModel.getSteps().getValue();
        Integer goal = viewModel.getDailyGoal().getValue();
        if (steps == null || goal == null) return;

        stepsText.setText(String.format("%,d", steps));
        stepsLabel.setText(String.format("of %,d", goal));

        if (goal > 0) {
            int progress = (int) ((steps / (float) goal) * 100);
            stepsProgress.setProgress(progress, true);
        }
    }

    private void setRandomQuote() {
        int randomIndex = new Random().nextInt(quotes.length);
        quoteText.setText(String.format("\"%s\"", quotes[randomIndex]));
    }
}
