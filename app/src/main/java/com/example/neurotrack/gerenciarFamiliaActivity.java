package com.example.neurotrack;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.adapters.FamilyMemberAdapter;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.ChildSummary;
import com.example.neurotrack.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class gerenciarFamiliaActivity extends AppCompatActivity {

    private RecyclerView rvMembros;
    private MaterialButton btnAdicionar;
    private FamilyMemberAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_familia);

        apiService = RetrofitClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        loadFamilyMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFamilyMembers();
    }

    private void initViews() {
        rvMembros = findViewById(R.id.rvMembros);
        btnAdicionar = findViewById(R.id.btnAdicionar);

        rvMembros.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FamilyMemberAdapter(this, new FamilyMemberAdapter.OnMemberActionListener() {
            @Override
            public void onEditClick(ChildSummary member) {
                editMember(member);
            }

            @Override
            public void onDeleteClick(ChildSummary member) {
                confirmDelete(member);
            }

            @Override
            public void onAddTaskClick(ChildSummary member) {
                addTaskForChild(member);
            }

            @Override
            public void onViewTasksClick(ChildSummary member) {
                viewChildTasks(member);
            }
        });

        rvMembros.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserActivity.class);
            startActivity(intent);
        });
    }

    private void loadFamilyMembers() {
        Long guardianId = sessionManager.getUserId();
        if (guardianId == null || guardianId == -1L) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getGuardianChildren(guardianId).enqueue(new Callback<List<ChildSummary>>() {
            @Override
            public void onResponse(Call<List<ChildSummary>> call, Response<List<ChildSummary>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChildSummary> children = response.body();
                    adapter.setMembers(children);

                    if (children.isEmpty()) {
                        Toast.makeText(gerenciarFamiliaActivity.this,
                                "Nenhuma criança cadastrada ainda",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(gerenciarFamiliaActivity.this,
                            "Erro ao carregar família",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChildSummary>> call, Throwable t) {
                Toast.makeText(gerenciarFamiliaActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTaskForChild(ChildSummary member) {
        Intent intent = new Intent(this, ParentTaskSelectionActivity.class);
        intent.putExtra("CHILD_ID", member.getId());
        startActivity(intent);
    }

    private void viewChildTasks(ChildSummary member) {
        Intent intent = new Intent(this, ChildTaskManagementActivity.class);
        intent.putExtra("CHILD_ID", member.getId());
        intent.putExtra("CHILD_NAME", member.getName());
        startActivity(intent);
    }

    private void editMember(ChildSummary member) {
        Intent intent = new Intent(this, editUserActivity.class);
        intent.putExtra("USER_ID", member.getId());
        intent.putExtra("USER_NAME", member.getName());
        startActivity(intent);
    }

    private void confirmDelete(ChildSummary member) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir criança")
                .setMessage("Tem certeza que deseja excluir " + member.getName() + "? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> deleteMember(member))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteMember(ChildSummary member) {
        apiService.deleteUser(member.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(gerenciarFamiliaActivity.this,
                            member.getName() + " foi excluído(a) com sucesso",
                            Toast.LENGTH_SHORT).show();
                    loadFamilyMembers();
                } else {
                    Toast.makeText(gerenciarFamiliaActivity.this,
                            "Erro ao excluir criança",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(gerenciarFamiliaActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

