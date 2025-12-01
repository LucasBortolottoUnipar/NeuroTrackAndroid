package com.example.neurotrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.adapters.TaskAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.dialogs.TaskDetailDialog;
import com.example.neurotrack.models.ChildSummary;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.models.User;
import com.example.neurotrack.notifications.TaskNotificationScheduler;
import com.example.neurotrack.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainChildActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFICATIONS = 1001;

    private Long childId;
    private SessionManager sessionManager;
    private ApiService apiService;
    private TaskAdapter taskAdapter;
    private Theme currentTheme;

    private TextView nomeUsuario;
    private ImageView fotoPerfil;
    private TextView tvPontuacaoValor;
    private RecyclerView recyclerViewTarefas;
    private MaterialButton btnAdicionar;
    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_child);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getInstance().getApiService();

        ensureNotificationPermission();

        childId = getIntent().getLongExtra("CHILD_ID", -1L);

        if (childId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados da criança", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadChildData();
        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();



        loadChildPoints();
        loadTasks();
    }

    private void initViews() {
        nomeUsuario = findViewById(R.id.nomeUsuario);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        tvPontuacaoValor = findViewById(R.id.tvPontuacaoValor);
        recyclerViewTarefas = findViewById(R.id.recyclerViewTarefas);
        btnAdicionar = findViewById(R.id.btnAdicionar);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(this, task -> {

            TaskDetailDialog dialog = new TaskDetailDialog(this, task, currentTheme, updatedTask -> {

                loadTasks();
                loadChildPoints(); // Atualizar pontuação
            });
            dialog.show();
        });
        recyclerViewTarefas.setAdapter(taskAdapter);
    }

    private void setupListeners() {

        btnAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, recompensaActivity.class);
            intent.putExtra("CHILD_ID", childId);
            startActivity(intent);
        });

        fotoPerfil.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void loadChildData() {
        android.util.Log.d("MainChildActivity", "==> loadChildData chamado para childId: " + childId);
        apiService.getUserById(childId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                android.util.Log.d("MainChildActivity", "==> getUserById response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    User child = response.body();
                    android.util.Log.d("MainChildActivity", "==> User recebido:");
                    android.util.Log.d("MainChildActivity", "    ID: " + child.getId());
                    android.util.Log.d("MainChildActivity", "    Nome: " + child.getFullName());
                    android.util.Log.d("MainChildActivity", "    Email: " + child.getEmail());
                    android.util.Log.d("MainChildActivity", "    Role: " + child.getRole());

                    String name = child.getFullName() != null ? child.getFullName() : "Criança";
                    nomeUsuario.setText("Olá, " + name + "!");

                    loadAvatarFromFirebase(child.getId());

                    Long themeId = child.getThemeId();
                    if (themeId == null) {
                        themeId = 1L; // TODO: ajuste se quiser outro tema padrão
                    }

                    loadTheme(themeId);

                    loadChildPoints();
                } else {
                    android.util.Log.e("MainChildActivity", "==> ERRO ao carregar dados - Code: " + response.code());
                    Toast.makeText(MainChildActivity.this,
                            "Erro ao carregar dados da criança", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                android.util.Log.e("MainChildActivity", "==> ERRO de conexão ao carregar dados", t);
                Toast.makeText(MainChildActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAvatarFromFirebase(Long childId) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference avatarRef = storage.getReference()
                    .child("avatars")
                    .child("child_" + childId + ".jpg");

            avatarRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {

                        Glide.with(MainChildActivity.this)
                                .load(uri.toString())
                                .placeholder(R.drawable.avatar_child)
                                .error(R.drawable.avatar_child)
                                .circleCrop()
                                .into(fotoPerfil);
                    })
                    .addOnFailureListener(e -> {

                        Glide.with(MainChildActivity.this)
                                .load(R.drawable.avatar_child)
                                .circleCrop()
                                .into(fotoPerfil);
                    });
        } catch (Exception e) {

            Glide.with(MainChildActivity.this)
                    .load(R.drawable.avatar_child)
                    .circleCrop()
                    .into(fotoPerfil);
        }
    }

    private void loadTheme(Long themeId) {
        if (themeId == null) {
            android.util.Log.w("MainChildActivity", "ThemeId da criança é nulo, não será carregado tema.");
            return;
        }

        apiService.getTheme(themeId).enqueue(new Callback<Theme>() {
            @Override
            public void onResponse(Call<Theme> call, Response<Theme> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentTheme = response.body();
                    if (taskAdapter != null) {
                        taskAdapter.setTheme(currentTheme);
                    }
                } else {
                    android.util.Log.e("MainChildActivity", "Erro ao carregar tema. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Theme> call, Throwable t) {
                android.util.Log.e("MainChildActivity", "Erro de conexão ao carregar tema", t);
            }
        });
    }

    private void loadChildPoints() {
        android.util.Log.d("MainChildActivity", "==> loadChildPoints chamado para childId: " + childId);

        apiService.getChildPoints(childId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                android.util.Log.d("MainChildActivity", "==> getChildPoints response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("MainChildActivity", "==> Pontos recebidos: " + response.body());
                    tvPontuacaoValor.setText(String.valueOf(response.body()));
                } else {
                    android.util.Log.e("MainChildActivity", "==> ERRO ao carregar pontos - Code: " + response.code());
                    tvPontuacaoValor.setText("0");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                android.util.Log.e("MainChildActivity", "==> ERRO de conexão ao carregar pontos", t);
                tvPontuacaoValor.setText("0");
            }
        });
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS
                );
            }
        }
    }

    private void loadTasks() {

        String token = sessionManager != null ? sessionManager.getToken() : null;
        if (token == null) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getChildTasks(childId, token).enqueue(new Callback<List<TaskInstance>>() {
            @Override
            public void onResponse(Call<List<TaskInstance>> call, Response<List<TaskInstance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskInstance> tasks = response.body();
                    taskAdapter.setTasks(tasks);

                    TaskNotificationScheduler.scheduleTaskNotifications(MainChildActivity.this, childId, tasks);

                    if (tasks.isEmpty()) {
                        Toast.makeText(MainChildActivity.this,
                                "Nenhuma tarefa para hoje!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainChildActivity.this,
                            "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TaskInstance>> call, Throwable t) {
                Toast.makeText(MainChildActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja voltar para a seleção de usuário?")
                .setPositiveButton("Sim", (dialog, which) -> finish())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
