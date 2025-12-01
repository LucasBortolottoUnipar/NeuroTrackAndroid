package com.example.neurotrack;

import android.content.Intent;
import android.os.Bundle;
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

    private void initViews() {
        cardGuardian = findViewById(R.id.cardGuardian);
        textViewGuardianName = findViewById(R.id.textViewGuardianName);
        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
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
    }

    private void loadChildren() {
        Long guardianId = sessionManager.getUserId();

        if (guardianId == null) {
            Toast.makeText(this, "Erro ao obter ID do usuário", Toast.LENGTH_SHORT).show();
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
}

