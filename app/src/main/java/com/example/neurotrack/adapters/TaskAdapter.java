package com.example.neurotrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.R;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.utils.IconUrlBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<TaskInstance> tasks;
    private OnTaskClickListener listener;
    private Theme currentTheme;

    public interface OnTaskClickListener {
        void onTaskClick(TaskInstance task);
    }

    public TaskAdapter(Context context, OnTaskClickListener listener) {
        this.context = context;
        this.tasks = new ArrayList<>();
        this.listener = listener;
    }

    public void setTasks(List<TaskInstance> tasks) {
        this.tasks = tasks != null ? new ArrayList<>(tasks) : new ArrayList<>();
        sortTasks();
        notifyDataSetChanged();
    }

    
    private void sortTasks() {
        Collections.sort(this.tasks, (t1, t2) -> {
            boolean t1Done = t1.isCompleted() || t1.isMissedByNow() || "MISSED".equals(t1.getStatus());
            boolean t2Done = t2.isCompleted() || t2.isMissedByNow() || "MISSED".equals(t2.getStatus());

            if (t1Done != t2Done) {

                return t1Done ? 1 : -1;
            }

            String d1 = t1.getScheduledFor();
            String d2 = t2.getScheduledFor();
            if (d1 != null && d2 != null && !d1.equals(d2)) {
                return d1.compareTo(d2);
            }

            String p1 = t1.getPlannedTime();
            String p2 = t2.getPlannedTime();
            if (p1 != null && p2 != null) {
                return p1.compareTo(p2);
            }

            return 0;
        });
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_instance, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskInstance task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewIcon;
        private TextView textViewTaskName;
        private TextView textViewStatus;
        private TextView textViewPoints;
        private CardView cardView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewTaskName = itemView.findViewById(R.id.textViewTaskName);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewPoints = itemView.findViewById(R.id.textViewPoints);
            cardView = (CardView) itemView;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    TaskInstance t = tasks.get(position);
                    if (!t.isMissedByNow() && !t.isCompleted()) {
                        listener.onTaskClick(t);
                    }
                }
            });
        }

        public void bind(TaskInstance task) {

            textViewTaskName.setText(task.getTaskName());

            if (task.getPointsAwarded() != null) {
                textViewPoints.setText("⭐ " + task.getPointsAwarded());
            } else {
                textViewPoints.setText("");
            }

            if (currentTheme != null && task.getIconCode() != null && !task.getIconCode().isEmpty()) {
                String iconUrl = IconUrlBuilder.buildIconUrl(currentTheme, task.getIconCode());

                if (iconUrl != null) {
                    Glide.with(context)
                            .load(iconUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_error)
                            .into(imageViewIcon);
                } else {
                    imageViewIcon.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                imageViewIcon.setImageResource(R.drawable.ic_placeholder);
            }

            boolean missed = task.isMissedByNow();
            String status = task.getStatus();

            if (task.isCompleted()) {
                textViewStatus.setText("✓ Concluída");
                textViewStatus.setTextColor(Color.parseColor("#4CAF50")); // Verde
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Fundo verde claro
            } else if (missed || "MISSED".equals(status)) {
                textViewStatus.setText("❌ Perdida");
                textViewStatus.setTextColor(Color.parseColor("#F44336")); // Vermelho
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Fundo vermelho claro
            } else if ("IN_PROGRESS".equals(status)) {
                textViewStatus.setText("⏳ Em andamento");
                textViewStatus.setTextColor(Color.parseColor("#FF9800")); // Laranja
                cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Fundo laranja claro
            } else { // PENDING ou outro
                textViewStatus.setText("⏺ Pendente");
                textViewStatus.setTextColor(Color.parseColor("#9E9E9E")); // Cinza
                cardView.setCardBackgroundColor(Color.WHITE);
            }

            if (task.getPlannedTime() != null && !task.getPlannedTime().isEmpty()) {
                String time = task.getPlannedTime().substring(0, 5); // "14:30:00" -> "14:30"
                textViewStatus.setText(textViewStatus.getText() + " • " + time);
            }

            boolean clickable = !missed && !task.isCompleted();
            itemView.setEnabled(clickable);
            itemView.setAlpha(clickable ? 1.0f : 0.6f);
        }
    }
}

