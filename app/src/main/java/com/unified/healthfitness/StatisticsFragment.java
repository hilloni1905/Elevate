package com.unified.healthfitness;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {
    private MainViewModel viewModel;
    private TextView avgStepsText, weeklyTrendText, streakText, dailyGoalText, stepsTodayText, goalPercentageText, graphYAxisLabel;
    private LinearLayout graphContainer, graphLabelsContainer;
    private CircularProgressIndicator goalProgressIndicator;
    private RecyclerView leaderboardRecycler;
    private LeaderboardAdapter leaderboardAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Find all views
        avgStepsText = view.findViewById(R.id.avg_steps_text);
        weeklyTrendText = view.findViewById(R.id.weekly_trend_text);
        graphContainer = view.findViewById(R.id.graph_container);
        graphLabelsContainer = view.findViewById(R.id.graph_labels_container);
        graphYAxisLabel = view.findViewById(R.id.graph_y_axis_label);
        streakText = view.findViewById(R.id.streak_text);
        dailyGoalText = view.findViewById(R.id.daily_goal_text);
        stepsTodayText = view.findViewById(R.id.steps_today_text);
        goalProgressIndicator = view.findViewById(R.id.goal_progress_indicator);
        goalPercentageText = view.findViewById(R.id.goal_percentage_text);
        leaderboardRecycler = view.findViewById(R.id.leaderboard_recycler);

        leaderboardRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardAdapter = new LeaderboardAdapter(new ArrayList<>(), 0);
        leaderboardRecycler.setAdapter(leaderboardAdapter);

        view.findViewById(R.id.set_goal_button).setOnClickListener(v -> showSetGoalDialog());

        observeData();
    }

    private void observeData() {
        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), weeklyData -> {
            if (weeklyData == null || weeklyData.isEmpty()) return;

            int totalSteps = 0;
            for (DailyData data : weeklyData) {
                totalSteps += data.getSteps();
            }
            int avgSteps = weeklyData.isEmpty() ? 0 : totalSteps / weeklyData.size();
            avgStepsText.setText(String.format("%,d", avgSteps));
            updateGraph(weeklyData);
        });

        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> {
            stepsTodayText.setText(String.format("%,d", steps));
            updateGoalProgress();
        });

        viewModel.getDailyGoal().observe(getViewLifecycleOwner(), goal -> {
            dailyGoalText.setText(String.format("/ %,d steps", goal));
            updateGoalProgress();
            leaderboardAdapter.setDailyGoal(goal);
        });

        viewModel.getWeeklyTrend().observe(getViewLifecycleOwner(), trend -> {
            if (trend == null || getContext() == null) return;
            
            String trendText;
            int trendColor;

            if (trend > 0.1) {
                trendText = String.format(Locale.getDefault(), "Up %.1f%%", trend);
                trendColor = R.color.primary_success;
            } else if (trend < -0.1) {
                trendText = String.format(Locale.getDefault(), "Down %.1f%%", Math.abs(trend));
                trendColor = R.color.alert_down_trends;
            } else {
                trendText = "Steady";
                trendColor = R.color.text_secondary;
            }
            weeklyTrendText.setText(trendText);
            weeklyTrendText.setTextColor(ContextCompat.getColor(getContext(), trendColor));
        });

        viewModel.getStreak().observe(getViewLifecycleOwner(), s -> streakText.setText(String.format("%d Days", s)));

        viewModel.getLeaderboard().observe(getViewLifecycleOwner(), leaderboard -> {
            if (leaderboard != null) {
                leaderboardAdapter.updateLeaderboard(leaderboard);
            }
        });

        viewModel.getGoalCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (completed != null && completed && getView() != null) {
                Snackbar.make(getView(), "Daily Goal Completed! +500 XP", Snackbar.LENGTH_LONG).show();
                viewModel.onGoalCompletedShown(); // Reset the event
            }
        });
    }

    private void updateGoalProgress() {
        Integer steps = viewModel.getSteps().getValue();
        Integer goal = viewModel.getDailyGoal().getValue();
        if (steps == null || goal == null || goal == 0) return;

        int progress = (int) ((steps / (float) goal) * 100);
        goalPercentageText.setText(String.format("%d%% of Goal", progress));

        ObjectAnimator animator = ObjectAnimator.ofInt(goalProgressIndicator, "progress", progress);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Set New Daily Goal");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Set", (dialog, which) -> {
            try {
                int newGoal = Integer.parseInt(input.getText().toString());
                viewModel.setDailyGoal(newGoal);
                Snackbar.make(requireView(), "New goal set!", Snackbar.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateGraph(List<DailyData> weeklyData) {
        graphContainer.removeAllViews();
        graphLabelsContainer.removeAllViews();
        if (weeklyData == null || weeklyData.isEmpty() || getContext() == null) return;

        int maxSteps = 0;
        for (DailyData data : weeklyData) {
            if (data.getSteps() > maxSteps) maxSteps = data.getSteps();
        }
        if (maxSteps > 0) {
            graphYAxisLabel.setText(String.format(Locale.getDefault(), "%,d", maxSteps));
        } else {
            graphYAxisLabel.setText("");
        }
        if (maxSteps == 0) maxSteps = viewModel.getDailyGoal().getValue() != null ? viewModel.getDailyGoal().getValue() : 5000;

        int barMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        int graphHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (DailyData data : weeklyData) {
            View bar = new View(getContext());
            float barHeight = maxSteps > 0 ? ((float) data.getSteps() / maxSteps) * graphHeight : 0;
            if (barHeight == 0 && data.getSteps() > 0) {
                barHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) barHeight, 1.0f);
            params.setMargins(barMargin, 0, barMargin, 0);
            bar.setLayoutParams(params);

            Integer dailyGoal = viewModel.getDailyGoal().getValue();
            int barColor = (dailyGoal != null && data.getSteps() >= dailyGoal) ? R.color.primary_success : R.color.secondary_progress;
            bar.setBackgroundColor(ContextCompat.getColor(getContext(), barColor));
            graphContainer.addView(bar);

            TextView dayLabel = new TextView(getContext());
            try {
                Date date = inputFormat.parse(data.getDate());
                dayLabel.setText(outputFormat.format(date));
            } catch (ParseException e) {
                dayLabel.setText("ERR");
            }
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            dayLabel.setLayoutParams(labelParams);
            dayLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dayLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            graphLabelsContainer.addView(dayLabel);
        }
    }

    // --- Leaderboard Adapter and ViewHolder ---
    private static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardViewHolder> {
        private List<MainViewModel.LeaderboardEntry> leaderboard;
        private int dailyGoal;

        LeaderboardAdapter(List<MainViewModel.LeaderboardEntry> leaderboard, int dailyGoal) {
            this.leaderboard = leaderboard;
            this.dailyGoal = dailyGoal;
        }

        public void setDailyGoal(int dailyGoal) {
            this.dailyGoal = dailyGoal;
        }

        public void updateLeaderboard(List<MainViewModel.LeaderboardEntry> newLeaderboard) {
            this.leaderboard.clear();
            this.leaderboard.addAll(newLeaderboard);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new LeaderboardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
            holder.bind(leaderboard.get(position), position + 1, dailyGoal);
        }

        @Override
        public int getItemCount() {
            return leaderboard != null ? leaderboard.size() : 0;
        }
    }

    private static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, nameText, scoreText;
        ImageView avatarImage, goalBadge;

        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rank_text);
            nameText = itemView.findViewById(R.id.name_text);
            scoreText = itemView.findViewById(R.id.score_text);
            avatarImage = itemView.findViewById(R.id.avatar_image);
            goalBadge = itemView.findViewById(R.id.goal_badge);
        }

        void bind(MainViewModel.LeaderboardEntry entry, int rank, int dailyGoal) {
            rankText.setText(String.valueOf(rank));
            nameText.setText(entry.getName());
            scoreText.setText(String.format("%,d steps", entry.getScore()));

            if ("You".equals(entry.getName())) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_light));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            }
            
            if (entry.getScore() >= dailyGoal) { 
                goalBadge.setVisibility(View.VISIBLE);
            } else {
                goalBadge.setVisibility(View.GONE);
            }
        }
    }
}
