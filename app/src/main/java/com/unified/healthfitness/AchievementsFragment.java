package com.unified.healthfitness;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;

public class AchievementsFragment extends Fragment {
    private MainViewModel viewModel;
    private TextView xpText, levelText, xpPercentageText;
    private ProgressBar xpProgress;
    private AchievementAdapter achievementAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Find all views
        xpText = view.findViewById(R.id.xp_text);
        levelText = view.findViewById(R.id.level_text);
        xpProgress = view.findViewById(R.id.xp_progress);
        xpPercentageText = view.findViewById(R.id.xp_percentage_text);
        RecyclerView achievementsRecycler = view.findViewById(R.id.achievements_recycler);

        // Setup RecyclerViews
        achievementsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        achievementAdapter = new AchievementAdapter();
        achievementsRecycler.setAdapter(achievementAdapter);

        observeData();
    }

    private void observeData() {
        viewModel.getXp().observe(getViewLifecycleOwner(), xp -> updateLevelAndXp());
        viewModel.getLevel().observe(getViewLifecycleOwner(), level -> updateLevelAndXp());

        viewModel.getAchievements().observe(getViewLifecycleOwner(), achievementAdapter::submitList);

        viewModel.getNewlyCompletedAchievement().observe(getViewLifecycleOwner(), achievement -> {
            if (achievement != null) {
                Snackbar.make(requireView(), "Achievement Unlocked: " + achievement.getTitle(), Snackbar.LENGTH_LONG).show();
                viewModel.onNewAchievementShown(); // Reset the LiveData
            }
        });
    }

    private void updateLevelAndXp() {
        Integer level = viewModel.getLevel().getValue();
        Integer xp = viewModel.getXp().getValue();
        if (level == null || xp == null) return;

        levelText.setText(String.valueOf(level));
        int xpForCurrentLevel = (level - 1) * 1000;
        int xpForNextLevel = level * 1000;
        int xpSinceLastLevel = xp - xpForCurrentLevel;
        int progress = (int) ((xpSinceLastLevel / (float)(xpForNextLevel - xpForCurrentLevel)) * 100);

        xpText.setText(String.format(Locale.getDefault(), "%,d / %,d XP", xpSinceLastLevel, (xpForNextLevel-xpForCurrentLevel)));
        xpPercentageText.setText(String.format(Locale.getDefault(), "%d%% complete", progress));
        xpProgress.setProgress(progress);
    }

    private static class AchievementAdapter extends ListAdapter<Achievement, AchievementViewHolder> {
        protected AchievementAdapter() {
            super(DIFF_CALLBACK);
        }

        private static final DiffUtil.ItemCallback<Achievement> DIFF_CALLBACK = new DiffUtil.ItemCallback<Achievement>() {
            @Override
            public boolean areItemsTheSame(@NonNull Achievement oldItem, @NonNull Achievement newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Achievement oldItem, @NonNull Achievement newItem) {
                return oldItem.equals(newItem);
            }
        };

        @NonNull
        @Override
        public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achievement, parent, false);
            return new AchievementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
            Achievement achievement = getItem(position);
            holder.bind(achievement);
        }
    }

    private static class AchievementViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, progressText, xpRewardText;
        ImageView icon;
        ProgressBar progressBar;

        AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.achievement_title);
            description = itemView.findViewById(R.id.achievement_description);
            icon = itemView.findViewById(R.id.achievement_icon);
            progressBar = itemView.findViewById(R.id.achievement_progress);
            progressText = itemView.findViewById(R.id.achievement_progress_text);
            xpRewardText = itemView.findViewById(R.id.xp_reward_text);
        }

        void bind(Achievement achievement) {
            title.setText(achievement.getTitle());
            description.setText(achievement.getDescription());
            icon.setImageResource(achievement.getIconResId());
            xpRewardText.setText(String.format(Locale.getDefault(), "+%d XP", achievement.getXpReward()));

            if (achievement.isCompleted()) {
                progressBar.setVisibility(View.GONE);
                progressText.setText(R.string.completed_achievement);
                itemView.setAlpha(1f);
                ((MaterialCardView) itemView).setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_success));
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(achievement.getTarget());
                progressBar.setProgress(achievement.getProgress());
                progressText.setText(String.format(Locale.getDefault(), "%,d / %,d", achievement.getProgress(), achievement.getTarget()));
                itemView.setAlpha(0.6f);
                ((MaterialCardView) itemView).setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.card_background));
            }
        }
    }
}
