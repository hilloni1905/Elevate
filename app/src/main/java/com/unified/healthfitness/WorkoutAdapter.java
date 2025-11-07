
// ============================================
// FILE 4: WorkoutAdapter.java
// ============================================
package com.unified.healthfitness;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    private List<WorkoutItem> workouts;
    private Context context;

    public WorkoutAdapter(Context context, List<WorkoutItem> workouts) {
        this.context = context;
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_grid, parent, false);
        return new WorkoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutItem workout = workouts.get(position);
        holder.workoutName.setText(capitalizeWords(workout.name));
        holder.workoutMuscle.setText(workout.muscle.toUpperCase());
        holder.workoutDifficulty.setText(workout.difficulty);

        int difficultyColor;
        switch (workout.difficulty.toLowerCase()) {
            case "beginner":
                difficultyColor = android.graphics.Color.parseColor("#4CAF50");
                break;
            case "intermediate":
                difficultyColor = android.graphics.Color.parseColor("#FF9800");
                break;
            case "expert":
                difficultyColor = android.graphics.Color.parseColor("#F44336");
                break;
            default:
                difficultyColor = android.graphics.Color.GRAY;
        }
        holder.workoutDifficulty.setTextColor(difficultyColor);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WorkoutDetailActivity.class);
            intent.putExtra("WORKOUT_NAME", workout.name);
            intent.putExtra("WORKOUT_TYPE", workout.type);
            intent.putExtra("WORKOUT_MUSCLE", workout.muscle);
            intent.putExtra("WORKOUT_EQUIPMENT", workout.equipment);
            intent.putExtra("WORKOUT_DIFFICULTY", workout.difficulty);
            intent.putExtra("WORKOUT_INSTRUCTIONS", workout.instructions);
            intent.putExtra("WORKOUT_GIF_URL", workout.gifUrl);
            intent.putExtra("WORKOUT_SECONDARY_MUSCLES", workout.secondaryMuscles);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView workoutName, workoutMuscle, workoutDifficulty;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutMuscle = itemView.findViewById(R.id.workoutMuscle);
            workoutDifficulty = itemView.findViewById(R.id.workoutDifficulty);
        }
    }
}