package com.example.neurotrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.LoginRequest;
import com.example.neurotrack.models.LoginResponse;
import com.example.neurotrack.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignup;
    private Button buttonLoginGoogle;
    private Button buttonLoginFacebook;
    private Button buttonLoginApple;
    private TextView textViewEsqueceuSenha;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignup = findViewById(R.id.textViewSignup);
        buttonLoginGoogle = findViewById(R.id.buttonLoginGoogle);
        buttonLoginFacebook = findViewById(R.id.buttonLoginFacebook);
        buttonLoginApple = findViewById(R.id.buttonLoginApple);
        textViewEsqueceuSenha = findViewById(R.id.textView2);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> performLogin());
        textViewSignup.setOnClickListener(v -> navigateToRegister());

        buttonLoginGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Login Google em desenvolvimento", Toast.LENGTH_SHORT).show()
        );

        buttonLoginFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Login Facebook em desenvolvimento", Toast.LENGTH_SHORT).show()
        );

        buttonLoginApple.setOnClickListener(v ->
                Toast.makeText(this, "Login Apple em desenvolvimento", Toast.LENGTH_SHORT).show()
        );

        textViewEsqueceuSenha.setOnClickListener(v ->
                Toast.makeText(this, "Recuperação de senha em desenvolvimento", Toast.LENGTH_SHORT).show()
        );
    }

    private void performLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email é obrigatório");
            editTextEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email inválido");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Senha é obrigatória");
            editTextPassword.requestFocus();
            return;
        }

        showLoading(true);

        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    sessionManager.createLoginSession(
                            loginResponse.getToken(),
                            loginResponse.getUserId(),
                            loginResponse.getFullName(),
                            loginResponse.getRole()
                    );

                    Toast.makeText(LoginActivity.this,
                            "Bem-vindo, " + loginResponse.getFullName() + "!",
                            Toast.LENGTH_SHORT).show();
                    navigateToHome();
                } else {
                    int code = response.code();
                    String message;
                    if (code == 401 || code == 403) {
                        message = "Email ou senha incorretos";
                    } else {
                        message = "Não foi possível entrar. Tente novamente.";
                    }
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        buttonLogin.setEnabled(!show);
        buttonLogin.setText(show ? "ENTRANDO..." : "ENTRAR");
        editTextEmail.setEnabled(!show);
        editTextPassword.setEnabled(!show);
        textViewSignup.setEnabled(!show);
    }
}

