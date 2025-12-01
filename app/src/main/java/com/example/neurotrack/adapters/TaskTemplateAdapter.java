package com.example.neurotrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.R;
import com.example.neurotrack.models.TaskTemplate;

import java.util.ArrayList;
import java.util.List;

public class TaskTemplateAdapter extends RecyclerView.Adapter<TaskTemplateAdapter.TaskViewHolder> {

    private Context context;
    private List<TaskTemplate> tasks;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskTemplate task);
    }

    public TaskTemplateAdapter(Context context, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = new ArrayList<>();
        this.listener = listener;
    }

    public void setTasks(List<TaskTemplate> tasks) {
        this.tasks = tasks != null ? tasks : new ArrayList<>();
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
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private ImageView taskIcon;
        private TextView taskTitle;
        private TextView taskPoints;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskIcon = itemView.findViewById(R.id.task_icon);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskPoints = itemView.findViewById(R.id.task_points);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(tasks.get(position));
                }
            });
        }

        public void bind(TaskTemplate task) {

            if (task.getTitle() != null) {
                taskTitle.setText(task.getTitle());
            } else {
                taskTitle.setText("");
            }

            if (task.getPoints() != null) {
                taskPoints.setText("⭐ " + task.getPoints() + " pontos");
            } else {
                taskPoints.setText("⭐ 0 pontos");
            }
        }
    }
}

