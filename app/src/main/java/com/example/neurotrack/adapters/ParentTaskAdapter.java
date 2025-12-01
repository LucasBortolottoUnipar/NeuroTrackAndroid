package com.example.neurotrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.R;
import com.example.neurotrack.models.TaskInstance;

import java.util.List;


public class ParentTaskAdapter extends RecyclerView.Adapter<ParentTaskAdapter.TaskViewHolder> {

    private Context context;
    private List<TaskInstance> tasks;
    private OnDeleteClickListener deleteListener;
    private OnEditClickListener editListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(TaskInstance task);
    }

    public interface OnEditClickListener {
        void onEditClick(TaskInstance task);
    }

    public ParentTaskAdapter(Context context, List<TaskInstance> tasks,
                             OnDeleteClickListener deleteListener,
                             OnEditClickListener editListener) {
        this.context = context;
        this.tasks = tasks;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskInstance task = tasks.get(position);

        holder.tvTaskName.setText(task.getTaskName() != null ? task.getTaskName() : "Tarefa");
        holder.tvTaskDate.setText(task.getScheduledFor());
        holder.tvTaskTime.setText(task.getPlannedTime());

        String status = task.getStatus();
        holder.tvTaskStatus.setText(getStatusText(status));
        holder.tvTaskStatus.setTextColor(getStatusColor(status));

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(task);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<TaskInstance> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING": return "Pendente";
            case "COMPLETED": return "Concluída";
            case "IN_PROGRESS": return "Em progresso";
            case "MISSED": return "Perdida";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "PENDING": return context.getResources().getColor(android.R.color.holo_orange_dark);
            case "COMPLETED": return context.getResources().getColor(android.R.color.holo_green_dark);
            case "IN_PROGRESS": return context.getResources().getColor(android.R.color.holo_blue_dark);
            case "MISSED": return context.getResources().getColor(android.R.color.holo_red_dark);
            default: return context.getResources().getColor(android.R.color.black);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvTaskDate, tvTaskTime, tvTaskStatus;
        ImageButton btnDelete, btnEdit;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}

