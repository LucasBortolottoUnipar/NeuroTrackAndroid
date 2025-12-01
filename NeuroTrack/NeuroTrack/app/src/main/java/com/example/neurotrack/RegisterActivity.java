package com.example.neurotrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.CreateUserRequest;
import com.example.neurotrack.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextNome;
    private EditText editTextEmail;
    private EditText editTextSenha;
    private EditText editTextData;
    private Button buttonCadastrar;
    private TextView textViewJaTenhoConta;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = RetrofitClient.getInstance().getApiService();

        initViews();

        setupListeners();
    }

    private void initViews() {
        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextSenha);
        editTextData = findViewById(R.id.editTextData);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
        textViewJaTenhoConta = findViewById(R.id.textViewJaTenhoConta);
    }

    private void setupListeners() {
        buttonCadastrar.setOnClickListener(v -> performRegister());

        textViewJaTenhoConta.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String nome = editTextNome.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();
        String dataNascimento = editTextData.getText().toString().trim();

        if (!validateInputs(nome, email, senha, dataNascimento)) {
            return;
        }

        String dataFormatada = convertDateFormat(dataNascimento);
        if (dataFormatada == null) {
            editTextData.setError("Data inválida. Use DD/MM/AAAA");
            editTextData.requestFocus();
            return;
        }

        showLoading(true);

        CreateUserRequest request = new CreateUserRequest(
                nome,
                email,
                senha,
                "GUARDIAN",
                dataFormatada
        );

        apiService.register(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            "Cadastro realizado! Faça login para continuar.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Erro ao cadastrar";
                    if (response.code() == 400) {
                        errorMsg = "Email já cadastrado ou dados inválidos";
                    } else if (response.code() == 409) {
                        errorMsg = "Este email já está em uso";
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String nome, String email, String senha, String data) {
        if (nome.isEmpty()) {
            editTextNome.setError("Nome é obrigatório");
            editTextNome.requestFocus();
            return false;
        }

        if (nome.length() < 2) {
            editTextNome.setError("Nome deve ter pelo menos 2 caracteres");
            editTextNome.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email é obrigatório");
            editTextEmail.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            editTextEmail.setError("Email inválido");
            editTextEmail.requestFocus();
            return false;
        }

        if (senha.isEmpty()) {
            editTextSenha.setError("Senha é obrigatória");
            editTextSenha.requestFocus();
            return false;
        }

        if (senha.length() < 6) {
            editTextSenha.setError("Senha deve ter pelo menos 6 caracteres");
            editTextSenha.requestFocus();
            return false;
        }

        if (data.isEmpty()) {
            editTextData.setError("Data de nascimento é obrigatória");
            editTextData.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private String convertDateFormat(String date) {
        try {
            String[] parts = date.split("/");
            if (parts.length == 3) {
                String day = parts[0];
                String month = parts[1];
                String year = parts[2];

                if (day.length() == 1) day = "0" + day;
                if (month.length() == 1) month = "0" + month;

                return year + "-" + month + "-" + day;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void showLoading(boolean show) {
        buttonCadastrar.setEnabled(!show);
        buttonCadastrar.setText(show ? "CADASTRANDO..." : "Cadastrar");
        editTextNome.setEnabled(!show);
        editTextEmail.setEnabled(!show);
        editTextSenha.setEnabled(!show);
        editTextData.setEnabled(!show);
    }
}

