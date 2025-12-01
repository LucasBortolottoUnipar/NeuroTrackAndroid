package com.example.neurotrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.R;
import com.example.neurotrack.models.TaskTemplate;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.utils.IconUrlBuilder;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    
    private Context context;
    private List<TaskTemplate> tasks;
    private Theme currentTheme;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskTemplate task);
    }
    
    public TaskAdapter(Context context, List<TaskTemplate> tasks, Theme currentTheme) {
        this.context = context;
        this.tasks = tasks;
        this.currentTheme = currentTheme;
    }
    
    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }
    
    public void updateTheme(Theme newTheme) {
        this.currentTheme = newTheme;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskTemplate task = tasks.get(position);

        holder.titleTextView.setText(task.getTitle());

        holder.pointsTextView.setText(task.getPoints() + " pontos");

        String iconUrl = IconUrlBuilder.buildIconUrl(currentTheme, task.getIconCode());

        if (iconUrl != null) {
            Glide.with(context)
                .load(iconUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(holder.iconImageView);
        } else {
            holder.iconImageView.setImageResource(R.drawable.ic_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }
    
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;
        TextView pointsTextView;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.task_icon);
            titleTextView = itemView.findViewById(R.id.task_title);
            pointsTextView = itemView.findViewById(R.id.task_points);
        }
    }
}

