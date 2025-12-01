package com.example.neurotrack;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.adapters.RewardAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.dialogs.AddRewardDialog;
import com.example.neurotrack.models.RewardCatalog;
import com.example.neurotrack.models.ChildReward;
import com.example.neurotrack.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardManagementActivity extends AppCompatActivity {

    private Long childId;
    private String childName;
    private ApiService apiService;
    private SessionManager sessionManager;
    
    private ImageButton btnBack;
    private ImageView imgAvatar;
    private TextView tvChildName;
    private TextView tvTitle;
    private RecyclerView recyclerViewRewards;
    private MaterialButton btnAddReward;
    private RewardAdapter rewardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_management);

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
        loadRewards();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvChildName = findViewById(R.id.tvChildName);
        tvTitle = findViewById(R.id.tvTitle);
        recyclerViewRewards = findViewById(R.id.recyclerViewRewards);
        btnAddReward = findViewById(R.id.btnAddReward);

        tvChildName.setText(childName);

        Glide.with(this)
            .load(R.drawable.avatar_child)
            .circleCrop()
            .into(imgAvatar);

        recyclerViewRewards.setLayoutManager(new LinearLayoutManager(this));
        rewardAdapter = new RewardAdapter(this, new ArrayList<>(), this::onEditReward, this::onDeleteReward);
        recyclerViewRewards.setAdapter(rewardAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnAddReward.setOnClickListener(v -> {
            AddRewardDialog dialog = new AddRewardDialog(this, null, reward -> {
                createReward(reward);
            });
            dialog.show();
        });
    }

    private void loadRewards() {
        Long guardianId = sessionManager.getUserId();
        if (guardianId == null || guardianId == -1L) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        apiService.getGuardianRewards(guardianId).enqueue(new Callback<List<RewardCatalog>>() {
            @Override
            public void onResponse(Call<List<RewardCatalog>> call, Response<List<RewardCatalog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rewardAdapter.setRewards(response.body());

                    loadChildRedeemedRewards();
                    
                    if (response.body().isEmpty()) {
                        Toast.makeText(RewardManagementActivity.this,
                            "Nenhuma recompensa cadastrada", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RewardManagementActivity.this,
                        "Erro ao carregar recompensas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RewardCatalog>> call, Throwable t) {
                Toast.makeText(RewardManagementActivity.this,
                    "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReward(RewardCatalog reward) {
        reward.setGuardianUserId(sessionManager.getUserId());
        
        apiService.createReward(reward).enqueue(new Callback<RewardCatalog>() {
            @Override
            public void onResponse(Call<RewardCatalog> call, Response<RewardCatalog> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RewardManagementActivity.this,
                        "Recompensa criada com sucesso!", Toast.LENGTH_SHORT).show();
                    loadRewards();
                } else {
                    Toast.makeText(RewardManagementActivity.this,
                        "Erro ao criar recompensa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RewardCatalog> call, Throwable t) {
                Toast.makeText(RewardManagementActivity.this,
                    "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onEditReward(RewardCatalog reward) {
        AddRewardDialog dialog = new AddRewardDialog(this, reward, updatedReward -> {
            updateReward(reward.getRewardId(), updatedReward);
        });
        dialog.show();
    }

    private void updateReward(Long rewardId, RewardCatalog reward) {
        apiService.updateReward(rewardId, reward).enqueue(new Callback<RewardCatalog>() {
            @Override
            public void onResponse(Call<RewardCatalog> call, Response<RewardCatalog> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RewardManagementActivity.this,
                        "Recompensa atualizada!", Toast.LENGTH_SHORT).show();
                    loadRewards();
                } else {
                    Toast.makeText(RewardManagementActivity.this,
                        "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RewardCatalog> call, Throwable t) {
                Toast.makeText(RewardManagementActivity.this,
                    "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDeleteReward(RewardCatalog reward) {
        new AlertDialog.Builder(this)
            .setTitle("Deletar recompensa")
            .setMessage("Deseja realmente deletar '" + reward.getName() + "'?")
            .setPositiveButton("Sim", (dialog, which) -> deleteReward(reward.getRewardId()))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void loadChildRedeemedRewards() {
        apiService.getChildRewards(childId).enqueue(new Callback<List<ChildReward>>() {
            @Override
            public void onResponse(Call<List<ChildReward>> call, Response<List<ChildReward>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.Set<Long> redeemedIds = new java.util.HashSet<>();
                    for (ChildReward cr : response.body()) {
                        if (cr.isRedeemed()) {
                            redeemedIds.add(cr.getRewardId());
                        }
                    }
                    rewardAdapter.setRedeemedRewardIds(redeemedIds);
                }
            }

            @Override
            public void onFailure(Call<List<ChildReward>> call, Throwable t) {

            }
        });
    }

    private void deleteReward(Long rewardId) {
        apiService.deleteReward(rewardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RewardManagementActivity.this,
                        "Recompensa deletada!", Toast.LENGTH_SHORT).show();
                    loadRewards();
                } else {
                    Toast.makeText(RewardManagementActivity.this,
                        "Erro ao deletar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RewardManagementActivity.this,
                    "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

