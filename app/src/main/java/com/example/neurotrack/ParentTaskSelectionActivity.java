package com.example.neurotrack;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.adapters.TaskTemplateAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.dialogs.ScheduleTaskDialog;
import com.example.neurotrack.models.TaskTemplate;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentTaskSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTasks;
    private TaskTemplateAdapter adapter;
    private ApiService apiService;
    private Long childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_task_selection);

        childId = getIntent().getLongExtra("CHILD_ID", -1L);

        if (childId == -1L) {
            Toast.makeText(this, "Erro: criança não selecionada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance().getApiService();

        initViews();
        loadTaskTemplates();
    }

    private void initViews() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskTemplateAdapter(this, taskTemplate -> {
            showScheduleDialog(taskTemplate);
        });

        recyclerViewTasks.setAdapter(adapter);
    }

    private void loadTaskTemplates() {
        apiService.getTaskTemplates().enqueue(new Callback<List<TaskTemplate>>() {
            @Override
            public void onResponse(Call<List<TaskTemplate>> call, Response<List<TaskTemplate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskTemplate> tasks = response.body();
                    adapter.setTasks(tasks);

                    if (tasks.isEmpty()) {
                        Toast.makeText(ParentTaskSelectionActivity.this,
                                "Nenhuma tarefa disponível",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ParentTaskSelectionActivity.this,
                            "Erro ao carregar tarefas",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TaskTemplate>> call, Throwable t) {
                Toast.makeText(ParentTaskSelectionActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showScheduleDialog(TaskTemplate taskTemplate) {
        ScheduleTaskDialog dialog = new ScheduleTaskDialog(
                this,
                taskTemplate,
                childId,
                () -> {
                    Toast.makeText(this, "Tarefa agendada!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        );
        dialog.show();
    }
}

