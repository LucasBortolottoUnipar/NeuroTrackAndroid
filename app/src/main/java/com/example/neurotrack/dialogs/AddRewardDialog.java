package com.example.neurotrack.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.neurotrack.R;
import com.example.neurotrack.models.RewardCatalog;

public class AddRewardDialog extends Dialog {

    private RewardCatalog existingReward;
    private OnRewardSavedListener listener;

    private EditText editRewardName;
    private EditText editRewardDescription;
    private EditText editRewardPoints;
    private Button btnCancel;
    private Button btnSave;
    private TextView tvTitle;

    public interface OnRewardSavedListener {
        void onRewardSaved(RewardCatalog reward);
    }

    public AddRewardDialog(@NonNull Context context, RewardCatalog reward, OnRewardSavedListener listener) {
        super(context);
        this.existingReward = reward;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_reward);

        initViews();
        setupListeners();
        
        if (existingReward != null) {
            populateFields();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        editRewardName = findViewById(R.id.editRewardName);
        editRewardDescription = findViewById(R.id.editRewardDescription);
        editRewardPoints = findViewById(R.id.editRewardPoints);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        if (existingReward != null) {
            tvTitle.setText("Editar Recompensa");
            btnSave.setText("Atualizar");
        } else {
            tvTitle.setText("Nova Recompensa");
            btnSave.setText("Criar");
        }
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveReward());
    }

    private void populateFields() {
        editRewardName.setText(existingReward.getName());
        editRewardDescription.setText(existingReward.getDescription());
        editRewardPoints.setText(String.valueOf(existingReward.getCostPoints()));
    }

    private void saveReward() {
        String name = editRewardName.getText().toString().trim();
        String description = editRewardDescription.getText().toString().trim();
        String pointsStr = editRewardPoints.getText().toString().trim();

        if (name.isEmpty()) {
            editRewardName.setError("Nome é obrigatório");
            editRewardName.requestFocus();
            return;
        }

        if (name.length() > 40) {
            editRewardName.setError("Nome deve ter no máximo 40 caracteres");
            editRewardName.requestFocus();
            return;
        }

        if (!description.isEmpty() && description.length() > 160) {
            editRewardDescription.setError("Descrição deve ter no máximo 160 caracteres");
            editRewardDescription.requestFocus();
            return;
        }

        if (pointsStr.isEmpty()) {
            editRewardPoints.setError("Pontos são obrigatórios");
            editRewardPoints.requestFocus();
            return;
        }

        int points;
        try {
            points = Integer.parseInt(pointsStr);
            if (points <= 0) {
                editRewardPoints.setError("Pontos devem ser maior que zero");
                editRewardPoints.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editRewardPoints.setError("Valor inválido");
            editRewardPoints.requestFocus();
            return;
        }

        RewardCatalog reward = new RewardCatalog();
        reward.setName(name);
        reward.setDescription(description);
        reward.setCostPoints(points);

        if (listener != null) {
            listener.onRewardSaved(reward);
        }

        dismiss();
    }
}

