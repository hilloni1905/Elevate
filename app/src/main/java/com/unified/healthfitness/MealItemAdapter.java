package com.unified.healthfitness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.unified.healthfitness.R;


public class MealItemAdapter extends RecyclerView.Adapter<MealItemAdapter.ViewHolder> {
    private Context context;
    private List<MealItem> meals;
    private OnMealDeleteListener deleteListener;

    public interface OnMealDeleteListener {
        void onDelete(MealItem item);
    }

    public MealItemAdapter(Context context, List<MealItem> meals, OnMealDeleteListener listener) {
        this.context = context;
        this.meals = meals;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealItem item = meals.get(position);
        holder.mealName.setText(item.name);
        holder.mealCalories.setText(String.format("%.0f cal", item.calories));
        holder.mealMacros.setText(String.format("P: %.1fg | C: %.1fg | F: %.1fg",
                item.protein, item.carbs, item.fat));

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mealName, mealCalories, mealMacros;
        ImageButton deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            mealName = itemView.findViewById(R.id.mealName);
            mealCalories = itemView.findViewById(R.id.mealCalories);
            mealMacros = itemView.findViewById(R.id.mealMacros);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}