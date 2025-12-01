package com.example.neurotrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.adapters.ParentTaskAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.dialogs.EditTaskInstanceDialog;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChildTaskManagementActivity extends AppCompatActivity {

    private Long childId;
    private String childName;
    private ApiService apiService;
    private SessionManager sessionManager;

    private TextView tvChildName;
    private RecyclerView recyclerViewTasks;
    private ParentTaskAdapter taskAdapter;
    private ImageButton btnBack;
    private MaterialButton btnAdicionarTarefa;
    private MaterialButton btnGerenciarRecompensas;

    private TextView tvStatsTitle;
    private TextView tvStatsPeriod;
    private TextView tvStatsCompletionPercent;
    private TextView tvStatsCompletedCount;
    private TextView tvStatsMissedCount;
    private TextView tvStatsPointsTotal;
    private ImageButton btnToggleStatsRange;

    private boolean showMonthlyStats = true;
    private List<TaskInstance> lastLoadedTasks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_task_management);

        apiService = RetrofitClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);

        childId = getIntent().getLongExtra("CHILD_ID", -1L);
        childName = getIntent().getStringExtra("CHILD_NAME");

        if (childId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Recarregar tarefas ao retornar da tela de adicionar tarefa
    }

    private void initViews() {
        tvChildName = findViewById(R.id.tvChildName);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        btnBack = findViewById(R.id.btnBack);
        btnAdicionarTarefa = findViewById(R.id.btnAdicionarTarefa);
        btnGerenciarRecompensas = findViewById(R.id.btnGerenciarRecompensas);

        tvStatsTitle = findViewById(R.id.tvStatsTitle);
        tvStatsPeriod = findViewById(R.id.tvStatsPeriod);
        tvStatsCompletionPercent = findViewById(R.id.tvStatsCompletionPercent);
        tvStatsCompletedCount = findViewById(R.id.tvStatsCompletedCount);
        tvStatsMissedCount = findViewById(R.id.tvStatsMissedCount);
        tvStatsPointsTotal = findViewById(R.id.tvStatsPointsTotal);
        btnToggleStatsRange = findViewById(R.id.btnToggleStatsRange);

        tvChildName.setText("Tarefas de " + childName);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new ParentTaskAdapter(this, new ArrayList<>(), this::onDeleteTask, this::onEditTask);
        recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdicionarTarefa.setOnClickListener(v -> {
            Intent intent = new Intent(this, ParentTaskSelectionActivity.class);
            intent.putExtra("CHILD_ID", childId);
            startActivity(intent);
        });

        btnGerenciarRecompensas.setOnClickListener(v -> {
            Intent intent = new Intent(this, RewardManagementActivity.class);
            intent.putExtra("CHILD_ID", childId);
            intent.putExtra("CHILD_NAME", childName);
            startActivity(intent);
        });

        btnToggleStatsRange.setOnClickListener(v -> {
            showMonthlyStats = !showMonthlyStats;
            updateStats(lastLoadedTasks);
        });
    }

    private void loadTasks() {
        String token = sessionManager != null ? sessionManager.getToken() : null;
        if (token == null) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAllChildTasks(childId, token).enqueue(new Callback<List<TaskInstance>>() {
            @Override
            public void onResponse(Call<List<TaskInstance>> call, Response<List<TaskInstance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskInstance> allTasks = response.body();

                    java.util.List<TaskInstance> visibleTasks = new java.util.ArrayList<>();
                    String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(new java.util.Date());

                    for (TaskInstance t : allTasks) {
                        String dateStr = t.getScheduledFor();
                        if (dateStr == null || dateStr.isEmpty()) {
                            continue;
                        }

                        if (dateStr.compareTo(todayStr) <= 0) {
                            visibleTasks.add(t);
                        }
                    }

                    taskAdapter.setTasks(visibleTasks);

                    lastLoadedTasks = visibleTasks;
                    updateStats(lastLoadedTasks);

                    if (visibleTasks.isEmpty()) {
                        Toast.makeText(ChildTaskManagementActivity.this,
                                "Nenhuma tarefa cadastrada", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChildTaskManagementActivity.this,
                            "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TaskInstance>> call, Throwable t) {
                Toast.makeText(ChildTaskManagementActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDeleteTask(TaskInstance task) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar tarefa")
                .setMessage("Deseja realmente deletar esta tarefa?")
                .setPositiveButton("Sim", (dialog, which) -> deleteTask(task.getTaskInstanceId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void onEditTask(TaskInstance task) {

        EditTaskInstanceDialog dialog = new EditTaskInstanceDialog(this, task, updated -> loadTasks());
        dialog.show();
    }

    private void updateStats(List<TaskInstance> tasks) {
        if (showMonthlyStats) {
            tvStatsTitle.setText("Resumo deste mês");
            updateMonthlyStats(tasks);
        } else {
            tvStatsTitle.setText("Resumo desta semana");
            updateWeeklyStats(tasks);
        }
    }

    private void updateMonthlyStats(List<TaskInstance> tasks) {

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH); // 0 = janeiro

        Calendar start = Calendar.getInstance();
        start.clear();
        start.set(year, month, 1);

        Calendar end = Calendar.getInstance();
        end.clear();
        end.set(year, month, start.getActualMaximum(Calendar.DAY_OF_MONTH));

        SimpleDateFormat displayFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStatsPeriod.setText(displayFmt.format(start.getTime()) + " - " + displayFmt.format(end.getTime()));

        if (tasks == null || tasks.isEmpty()) {
            tvStatsCompletionPercent.setText("0%");
            tvStatsCompletedCount.setText("0");
            tvStatsMissedCount.setText("0");
            tvStatsPointsTotal.setText("0");
            return;
        }

        SimpleDateFormat apiDateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int totalInMonth = 0;
        int completedCount = 0;
        int missedCount = 0;
        int totalPoints = 0;

        for (TaskInstance task : tasks) {
            String dateStr = task.getScheduledFor();
            if (dateStr == null || dateStr.isEmpty()) {
                continue;
            }

            Date date;
            try {
                date = apiDateFmt.parse(dateStr);
            } catch (ParseException e) {
                continue;
            }

            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(date);

            if (taskCal.get(Calendar.YEAR) != year || taskCal.get(Calendar.MONTH) != month) {
                continue; // fora do mês atual
            }

            totalInMonth++;

            if ("COMPLETED".equals(task.getStatus())) {
                completedCount++;
                Integer pts = task.getPointsAwarded();
                if (pts != null) {
                    totalPoints += pts;
                }
            } else if ("MISSED".equals(task.getStatus())) {
                missedCount++;
            }
        }

        int percent = 0;
        if (totalInMonth > 0) {
            percent = (int) Math.round((completedCount * 100.0) / totalInMonth);
        }

        tvStatsCompletionPercent.setText(percent + "%");
        tvStatsCompletedCount.setText(String.valueOf(completedCount));
        tvStatsMissedCount.setText(String.valueOf(missedCount));
        tvStatsPointsTotal.setText(String.valueOf(totalPoints));
    }

    private void updateWeeklyStats(List<TaskInstance> tasks) {
        Calendar now = Calendar.getInstance();

        Calendar start = (Calendar) now.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat displayFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStatsPeriod.setText(displayFmt.format(start.getTime()) + " - " + displayFmt.format(end.getTime()));

        if (tasks == null || tasks.isEmpty()) {
            tvStatsCompletionPercent.setText("0%");
            tvStatsCompletedCount.setText("0");
            tvStatsMissedCount.setText("0");
            tvStatsPointsTotal.setText("0");
            return;
        }

        SimpleDateFormat apiDateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int totalInPeriod = 0;
        int completedCount = 0;
        int missedCount = 0;
        int totalPoints = 0;

        for (TaskInstance task : tasks) {
            String dateStr = task.getScheduledFor();
            if (dateStr == null || dateStr.isEmpty()) {
                continue;
            }

            Date date;
            try {
                date = apiDateFmt.parse(dateStr);
            } catch (ParseException e) {
                continue;
            }

            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(date);

            if (taskCal.before(start) || taskCal.after(end)) {
                continue; // fora da semana atual
            }

            totalInPeriod++;

            if ("COMPLETED".equals(task.getStatus())) {
                completedCount++;
                Integer pts = task.getPointsAwarded();
                if (pts != null) {
                    totalPoints += pts;
                }
            } else if ("MISSED".equals(task.getStatus())) {
                missedCount++;
            }
        }

        int percent = 0;
        if (totalInPeriod > 0) {
            percent = (int) Math.round((completedCount * 100.0) / totalInPeriod);
        }

        tvStatsCompletionPercent.setText(percent + "%");
        tvStatsCompletedCount.setText(String.valueOf(completedCount));
        tvStatsMissedCount.setText(String.valueOf(missedCount));
        tvStatsPointsTotal.setText(String.valueOf(totalPoints));
    }

    private void deleteTask(Long taskInstanceId) {
        String token = sessionManager != null ? sessionManager.getToken() : null;
        if (token == null) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteTaskInstance(taskInstanceId, token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChildTaskManagementActivity.this,
                            "Tarefa deletada com sucesso!", Toast.LENGTH_SHORT).show();
                    loadTasks(); // Recarregar lista
                } else {
                    Toast.makeText(ChildTaskManagementActivity.this,
                            "Erro ao deletar tarefa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChildTaskManagementActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

