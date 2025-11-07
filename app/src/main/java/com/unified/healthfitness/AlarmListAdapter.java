package com.unified.healthfitness;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unified.healthfitness.databinding.ItemAlarmBinding;
import com.unified.healthfitness.db.Alarm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.VH> {

    public interface OnAlarmClickListener {
        void onAlarmToggle(Alarm alarm, boolean enabled);
        void onAlarmEdit(Alarm alarm);
        void onAlarmDelete(Alarm alarm);
    }

    private List<Alarm> alarms;
    private OnAlarmClickListener listener;
    private SimpleDateFormat fmt = new SimpleDateFormat("E, h:mm a", Locale.getDefault());

    public AlarmListAdapter(List<Alarm> alarms, OnAlarmClickListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlarmBinding b = ItemAlarmBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Alarm a = alarms.get(position);
        holder.binding.tvLabel.setText(a.label != null ? a.label : "Alarm");
        holder.binding.tvTime.setText(fmt.format(new Date(a.timeMillis)));

        // Set listener to null before setting checked state to prevent unwanted listener calls from recycling
        holder.binding.switchEnable.setOnCheckedChangeListener(null);
        holder.binding.switchEnable.setChecked(a.enabled);

        holder.binding.switchEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only trigger if the state has actually changed
            if (a.enabled != isChecked) {
                if (listener != null) {
                    // Post the action to run after the layout pass is complete to prevent crash
                    buttonView.post(() -> listener.onAlarmToggle(a, isChecked));
                }
            }
        });

        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                // Post the action to be safe
                v.post(() -> listener.onAlarmEdit(a));
            }
        });
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                // Post the action to be safe
                v.post(() -> listener.onAlarmDelete(a));
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarms != null ? alarms.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemAlarmBinding binding;
        VH(ItemAlarmBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}
