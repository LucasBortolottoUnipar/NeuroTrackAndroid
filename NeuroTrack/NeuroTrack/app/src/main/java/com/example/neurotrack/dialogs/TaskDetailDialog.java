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

    private TaskInstance taskInstance;
    private Theme currentTheme;
    private OnTaskActionListener listener;
    private ApiService apiService;
    private SessionManager sessionManager;

    private ImageView iconImageView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView pointsTextView;
    private Button completeButton;
    private Button cancelButton;

    public interface OnTaskActionListener {
        void onTaskCompleted(TaskInstance taskInstance);
        void onTaskCancelled();
    }

    public TaskDetailDialog(@NonNull Context context, TaskInstance taskInstance, Theme currentTheme) {
        super(context);
        this.taskInstance = taskInstance;
        this.currentTheme = currentTheme;
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(context);
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task_detail);

        initViews();
        loadTaskData();
        setupListeners();
    }

    private void initViews() {
        iconImageView = findViewById(R.id.task_detail_icon);
        titleTextView = findViewById(R.id.task_detail_title);
        descriptionTextView = findViewById(R.id.task_detail_description);
        pointsTextView = findViewById(R.id.task_detail_points);
        completeButton = findViewById(R.id.btn_complete_task);
        cancelButton = findViewById(R.id.btn_cancel);
    }

    private void loadTaskData() {
        titleTextView.setText(taskInstance.getTitle());

        descriptionTextView.setText(taskInstance.getDescription());

        pointsTextView.setText(taskInstance.getPoints() + " PONTOS ⭐");

        String iconUrl = IconUrlBuilder.buildIconUrl(currentTheme, taskInstance.getIconCode());
        if (iconUrl != null) {
            Glide.with(getContext())
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(iconImageView);
        } else {
            iconImageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private void setupListeners() {
        completeButton.setOnClickListener(v -> {
            completeTask();
        });

        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskCancelled();
            }
            dismiss();
        });
    }

    private void completeTask() {
        completeButton.setEnabled(false);
        completeButton.setText("CONCLUINDO...");

        String token = sessionManager.getToken();

        apiService.completeTask(taskInstance.getTaskInstanceId(), token)
                .enqueue(new Callback<TaskInstance>() {
            @Override
            public void onResponse(Call<TaskInstance> call, Response<TaskInstance> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TaskInstance completedTask = response.body();

                    Toast.makeText(getContext(),
                            "🎉 PARABÉNS! Você ganhou " + completedTask.getPoints() + " pontos!",
                            Toast.LENGTH_LONG).show();

                    if (listener != null) {
                        listener.onTaskCompleted(completedTask);
                    }

                    dismiss();
                } else {
                    completeButton.setEnabled(true);
                    completeButton.setText("CONCLUIR TAREFA");
                    Toast.makeText(getContext(),
                            "Erro ao concluir tarefa. Tente novamente.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TaskInstance> call, Throwable t) {
                completeButton.setEnabled(true);
                completeButton.setText("CONCLUIR TAREFA");
                Toast.makeText(getContext(),
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}

