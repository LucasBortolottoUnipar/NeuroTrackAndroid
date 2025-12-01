package com.example.neurotrack;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.adapters.ChildRewardAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.AssignRewardRequest;
import com.example.neurotrack.models.ChildReward;
import com.example.neurotrack.models.RewardCatalog;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.User;
import com.example.neurotrack.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class recompensaActivity extends AppCompatActivity {

    private Long childId;
    private SessionManager sessionManager;
    private ApiService apiService;

    private TextView nomeUsuario;
    private ImageView fotoPerfil;
    private TextView tvPontuacaoValor;
    private RecyclerView recyclerViewTarefas;
    private MaterialButton btnAdicionar;

    private ChildRewardAdapter rewardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recompensa);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getInstance().getApiService();

        childId = getIntent().getLongExtra("CHILD_ID", -1L);
        if (childId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados da criança", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecycler();
        setupListeners();

        loadChildInfo();
        loadChildPoints();
        loadRewards();
        loadChildRedeemedRewards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChildPoints();
        loadRewards();
    }

    private void initViews() {
        nomeUsuario = findViewById(R.id.nomeUsuario);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        tvPontuacaoValor = findViewById(R.id.tvPontuacaoValor);
        recyclerViewTarefas = findViewById(R.id.recyclerViewTarefas);
        btnAdicionar = findViewById(R.id.btnAdicionar);
    }

    private void setupRecycler() {
        recyclerViewTarefas.setLayoutManager(new LinearLayoutManager(this));
        rewardAdapter = new ChildRewardAdapter(this, this::onRewardSelected);
        recyclerViewTarefas.setAdapter(rewardAdapter);
    }

    private void setupListeners() {

        btnAdicionar.setOnClickListener(v -> finish());
    }

    private void loadChildInfo() {
        apiService.getUserById(childId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User child = response.body();
                    String name = child.getFullName() != null ? child.getFullName() : "Criança";
                    nomeUsuario.setText("Olá, " + name + "!");

                    loadAvatarFromFirebase(child.getId());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

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
                    .addOnSuccessListener(uri ->
                            Glide.with(recompensaActivity.this)
                                    .load(uri.toString())
                                    .placeholder(R.drawable.avatar_child)
                                    .error(R.drawable.avatar_child)
                                    .circleCrop()
                                    .into(fotoPerfil)
                    )
                    .addOnFailureListener(e ->
                            Glide.with(recompensaActivity.this)
                                    .load(R.drawable.avatar_child)
                                    .circleCrop()
                                    .into(fotoPerfil)
                    );
        } catch (Exception e) {
            Glide.with(recompensaActivity.this)
                    .load(R.drawable.avatar_child)
                    .circleCrop()
                    .into(fotoPerfil);
        }
    }

    private void loadChildPoints() {
        apiService.getChildPoints(childId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvPontuacaoValor.setText(String.valueOf(response.body()));
                } else {
                    tvPontuacaoValor.setText("0");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                tvPontuacaoValor.setText("0");
            }
        });
    }

    private void loadRewards() {
        Long guardianId = sessionManager.getUserId();
        if (guardianId == null) {
            Toast.makeText(this, "Erro ao obter ID do responsável", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getGuardianRewards(guardianId).enqueue(new Callback<List<RewardCatalog>>() {
            @Override
            public void onResponse(Call<List<RewardCatalog>> call, Response<List<RewardCatalog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rewardAdapter.setRewards(response.body());
                } else {
                    Toast.makeText(recompensaActivity.this,
                            "Erro ao carregar recompensas",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RewardCatalog>> call, Throwable t) {
                Toast.makeText(recompensaActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onRewardSelected(RewardCatalog reward) {
        String message = "Resgatar '" + reward.getName() + "' por " + reward.getCostPoints() + " pontos?";
        new AlertDialog.Builder(this)
                .setTitle("Resgatar recompensa")
                .setMessage(message)
                .setPositiveButton("Resgatar", (dialog, which) -> redeemReward(reward))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void redeemReward(RewardCatalog reward) {
        AssignRewardRequest request = new AssignRewardRequest(childId, reward.getRewardId());

        apiService.redeemReward(request).enqueue(new Callback<ChildReward>() {

            @Override
            public void onResponse(Call<ChildReward> call, Response<ChildReward> response) {
                android.util.Log.d("RewardRedeem", "redeem response code=" + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(recompensaActivity.this,
                            "Recompensa resgatada com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    loadChildPoints();

                    ChildReward redeemed = response.body();
                    if (redeemed.getRewardId() != null) {
                        loadChildRedeemedRewards();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : null;
                        android.util.Log.e("RewardRedeem", "Erro ao resgatar recompensa: " + errorBody);

                        if (errorBody != null && errorBody.contains("pontos suficientes")) {
                            Toast.makeText(recompensaActivity.this,
                                    "Pontos insuficientes para resgatar esta recompensa.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(recompensaActivity.this,
                                    "Erro ao resgatar recompensa.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(recompensaActivity.this,
                                "Erro ao processar resposta do servidor.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChildReward> call, Throwable t) {
                Toast.makeText(recompensaActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
}

