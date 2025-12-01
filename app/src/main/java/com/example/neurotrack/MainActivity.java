package com.example.neurotrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.adapters.ChildCardAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.ChildSummary;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private CardView cardGuardian;
    private TextView textViewGuardianName;
    private RecyclerView recyclerViewChildren;
    private ImageButton btnLogout;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getInstance().getApiService();

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupListeners();
        displayGuardianName();
        loadChildren();
    }

    @Override
    protected void onResume() {
        super.onResume();



        if (sessionManager != null && sessionManager.isLoggedIn()) {
            loadChildren();
        }
    }

    private void initViews() {
        cardGuardian = findViewById(R.id.cardGuardian);
        textViewGuardianName = findViewById(R.id.textViewGuardianName);
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
    }

    private void displayGuardianName() {
        String guardianName = sessionManager.getUserName();
        if (guardianName != null && !guardianName.isEmpty()) {
            textViewGuardianName.setText(guardianName);
        } else {
            textViewGuardianName.setText("Responsável");
        }
    }

    private void setupListeners() {

        cardGuardian.setOnClickListener(v -> {
            Intent intent = new Intent(this, gerenciarFamiliaActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void loadChildren() {
        Long guardianId = sessionManager.getUserId();

        if (guardianId == null || guardianId == -1L) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        apiService.getGuardianChildren(guardianId).enqueue(new Callback<List<ChildSummary>>() {
            @Override
            public void onResponse(Call<List<ChildSummary>> call, Response<List<ChildSummary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChildSummary> children = response.body();

                    ChildCardAdapter adapter = new ChildCardAdapter(children, child -> {
                        navigateToChildHome(child.getId());
                    });
                    recyclerViewChildren.setAdapter(adapter);

                    String token = sessionManager != null ? sessionManager.getToken() : null;

                    for (int i = 0; i < children.size(); i++) {
                        ChildSummary child = children.get(i);
                        int index = i;

                        apiService.getChildPoints(child.getId()).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    child.setTotalPoints(response.body());
                                    adapter.notifyItemChanged(index);
                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {

                            }
                        });

                        if (token != null) {
                            apiService.getChildTasks(child.getId(), token).enqueue(new Callback<List<TaskInstance>>() {
                                @Override
                                public void onResponse(Call<List<TaskInstance>> call, Response<List<TaskInstance>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        List<TaskInstance> tasks = response.body();
                                        int totalTasksToday = tasks.size();
                                        int completedTasksToday = 0;
                                        for (TaskInstance t : tasks) {
                                            if ("COMPLETED".equals(t.getStatus())) {
                                                completedTasksToday++;
                                            }
                                        }

                                        child.setTotalTasksToday(totalTasksToday);
                                        child.setTasksCompletedToday(completedTasksToday);
                                        adapter.notifyItemChanged(index);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<TaskInstance>> call, Throwable t) {

                                }
                            });
                        }
                    }

                } else {
                    Toast.makeText(MainActivity.this,
                            "Erro ao carregar crianças",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChildSummary>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToChildHome(Long childId) {
        Intent intent = new Intent(this, MainChildActivity.class);
        intent.putExtra("CHILD_ID", childId);
        startActivity(intent);
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair da conta?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

