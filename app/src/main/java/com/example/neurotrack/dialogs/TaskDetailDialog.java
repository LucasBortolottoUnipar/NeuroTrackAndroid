package com.example.neurotrack.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.neurotrack.R;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.utils.IconUrlBuilder;
import com.example.neurotrack.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailDialog extends Dialog {

    private TaskInstance task;
    private Theme currentTheme;
    private OnTaskCompletedListener listener;
    private ApiService apiService;
    private SessionManager sessionManager;
    private ImageView taskIcon;
    private TextView taskTitle;
    private TextView taskDescription;
    private TextView taskPoints;
    private Button btnCancel;
    private Button btnCompleteTask;

    public interface OnTaskCompletedListener {
        void onTaskCompleted(TaskInstance task);
    }

    public TaskDetailDialog(@NonNull Context context, TaskInstance task, Theme currentTheme, OnTaskCompletedListener listener) {
        super(context);
        this.task = task;
        this.currentTheme = currentTheme;
        this.listener = listener;
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task_detail);

        initViews();
        setupViews();
        setupListeners();
    }

    private void initViews() {
        taskIcon = findViewById(R.id.task_detail_icon);
        taskTitle = findViewById(R.id.task_detail_title);
        taskDescription = findViewById(R.id.task_detail_description);
        taskPoints = findViewById(R.id.task_detail_points);
        btnCancel = findViewById(R.id.btn_cancel);
        btnCompleteTask = findViewById(R.id.btn_complete_task);
    }

    private void setupViews() {

        taskTitle.setText(task.getTaskName());

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            taskDescription.setText(task.getDescription());
            taskDescription.setVisibility(View.VISIBLE);
        } else {
            taskDescription.setText("Sem descrição disponível");
            taskDescription.setVisibility(View.VISIBLE);
        }

        if (task.getPointsAwarded() != null) {
            taskPoints.setText("⭐ " + task.getPointsAwarded() + " pontos");
        } else {
            taskPoints.setText("⭐ 0 pontos");
        }

        if (currentTheme != null && task.getIconCode() != null && !task.getIconCode().isEmpty()) {
            String iconUrl = IconUrlBuilder.buildIconUrl(currentTheme, task.getIconCode());

            if (iconUrl != null) {
                Glide.with(getContext())
                        .load(iconUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(taskIcon);
            } else {
                taskIcon.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            taskIcon.setImageResource(R.drawable.ic_placeholder);
        }

        if (task.isCompleted()) {
            btnCompleteTask.setVisibility(View.GONE);
            taskTitle.append(" ✓");
        }
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());

        btnCompleteTask.setOnClickListener(v -> {
            if (task.isCompleted()) {
                Toast.makeText(getContext(), "Tarefa já está concluída!", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            completeTask();
        });
    }

    private void completeTask() {
        btnCompleteTask.setEnabled(false);
        btnCompleteTask.setText("Completando...");

        String token = sessionManager != null ? sessionManager.getToken() : null;
        if (token == null) {
            Toast.makeText(getContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            btnCompleteTask.setEnabled(true);
            btnCompleteTask.setText("Completar");
            return;
        }

        apiService.completeTask(task.getTaskInstanceId(), token).enqueue(new Callback<TaskInstance>() {
            @Override
            public void onResponse(Call<TaskInstance> call, Response<TaskInstance> response) {
                btnCompleteTask.setEnabled(true);
                btnCompleteTask.setText("Completar");

                if (response.isSuccessful() && response.body() != null) {
                    TaskInstance updatedTask = response.body();
                    Toast.makeText(getContext(),
                            "✓ Tarefa concluída! +" + updatedTask.getPointsAwarded() + " pontos",
                            Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onTaskCompleted(updatedTask);
                    }

                    dismiss();
                } else {
                    Toast.makeText(getContext(),
                            "Erro ao completar tarefa",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TaskInstance> call, Throwable t) {
                btnCompleteTask.setEnabled(true);
                btnCompleteTask.setText("Completar");

                Toast.makeText(getContext(),
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

