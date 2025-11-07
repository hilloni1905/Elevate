package com.unified.healthfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class WorkoutLogDatabaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_WORKOUT_LOG = 1;

    private Context context;
    private List<Object> items = new ArrayList<>();
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(WorkoutLogEntity log);
    }

    public static class DateGroup {
        String date;
        String displayName;
        List<WorkoutLogEntity> logs;

        public DateGroup(String date, String displayName, List<WorkoutLogEntity> logs) {
            this.date = date;
            this.displayName = displayName;
            this.logs = logs;
        }
    }

    public WorkoutLogDatabaseAdapter(Context context, List<DateGroup> dateGroups, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
        updateLogs(dateGroups);
    }

    public void updateLogs(List<DateGroup> dateGroups) {
        items.clear();
        for (DateGroup group : dateGroups) {
            items.add(group);
            items.addAll(group.logs);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DateGroup ? TYPE_DATE_HEADER : TYPE_WORKOUT_LOG;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_workout_log, parent, false);
            return new LogViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            DateGroup group = (DateGroup) items.get(position);
            ((DateHeaderViewHolder) holder).bind(group);
        } else if (holder instanceof LogViewHolder) {
            WorkoutLogEntity log = (WorkoutLogEntity) items.get(position);
            ((LogViewHolder) holder).bind(log);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, countText;

        DateHeaderViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            countText = itemView.findViewById(R.id.countText);
        }

        void bind(DateGroup group) {
            dateText.setText(group.displayName);
            countText.setText(group.logs.size() + " workout" + (group.logs.size() != 1 ? "s" : ""));
        }
    }

    class LogViewHolder extends RecyclerView.ViewHolder {
        TextView workoutName, workoutMuscle, workoutStats, workoutDate, workoutNotes;
        View difficultyIndicator;
        ImageButton deleteBtn;

        LogViewHolder(View itemView) {
            super(itemView);
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutMuscle = itemView.findViewById(R.id.workoutMuscle);
            workoutStats = itemView.findViewById(R.id.workoutStats);
            workoutDate = itemView.findViewById(R.id.workoutDate);
            workoutNotes = itemView.findViewById(R.id.workoutNotes);
            difficultyIndicator = itemView.findViewById(R.id.difficultyIndicator);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }

        void bind(WorkoutLogEntity log) {
            workoutName.setText(capitalizeWords(log.workoutName));
            workoutMuscle.setText(log.bodyPart != null ? log.bodyPart.toUpperCase() : "N/A");
            workoutStats.setText(log.sets + " sets × " + log.reps + " reps");
            workoutDate.setText(log.dateTime);

            if (log.notes != null && !log.notes.isEmpty()) {
                workoutNotes.setVisibility(View.VISIBLE);
                workoutNotes.setText("Note: " + log.notes);
            } else {
                workoutNotes.setVisibility(View.GONE);
            }

            int difficultyColor;
            if (log.difficulty != null) {
                switch (log.difficulty.toLowerCase()) {
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
                        difficultyColor = android.graphics.Color.parseColor("#2196F3");
                }
            } else {
                difficultyColor = android.graphics.Color.parseColor("#2196F3");
            }
            difficultyIndicator.setBackgroundColor(difficultyColor);

            deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(log);
                }
            });
        }

        private String capitalizeWords(String text) {
            if (text == null || text.isEmpty()) return "";
            String[] words = text.trim().split("\\s+");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return result.toString().trim();
        }
    }
}