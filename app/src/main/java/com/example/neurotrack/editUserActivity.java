package com.example.neurotrack;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.Theme;
import com.example.neurotrack.models.UpdateChildRequest;
import com.example.neurotrack.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class editUserActivity extends AppCompatActivity {

    private EditText editNome;
    private EditText editDataNascimento;
    private Spinner spinnerCondicao;
    private Spinner spinnerTema;
    private MaterialButton buttonSalvar;
    private MaterialButton buttonCancelar;
    private ImageView imgUser;

    private ApiService apiService;
    private Calendar calendar;
    private List<Theme> temasList;
    private Long userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        apiService = RetrofitClient.getInstance().getApiService();
        calendar = Calendar.getInstance();
        temasList = new ArrayList<>();

        userId = getIntent().getLongExtra("USER_ID", -1L);

        if (userId == -1L) {
            Toast.makeText(this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupSpinners();
        setupListeners();
        loadThemes();
        loadUserData();
    }

    private void initViews() {
        editNome = findViewById(R.id.editNome);
        editDataNascimento = findViewById(R.id.editDataNascimento);
        spinnerCondicao = findViewById(R.id.spinnerCondicao);
        spinnerTema = findViewById(R.id.spinnerTema);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonCancelar = findViewById(R.id.buttonCancelar);
        imgUser = findViewById(R.id.imgUser);
    }

    private void setupSpinners() {
        String[] condicoes = {"Selecione a condição", "TEA", "TDAH"};
        ArrayAdapter<String> condicaoAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                condicoes
        );
        condicaoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondicao.setAdapter(condicaoAdapter);
    }

    private void setupListeners() {
        editDataNascimento.setOnClickListener(v -> showDatePicker());

        buttonSalvar.setOnClickListener(v -> updateChild());

        buttonCancelar.setOnClickListener(v -> finish());

        imgUser.setOnClickListener(v -> {
            Toast.makeText(this, "Upload de avatar em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDatePicker() {
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -5);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    editDataNascimento.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void loadThemes() {
        apiService.getThemes().enqueue(new Callback<List<Theme>>() {
            @Override
            public void onResponse(Call<List<Theme>> call, Response<List<Theme>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    temasList = response.body();

                    List<String> themeNames = new ArrayList<>();
                    themeNames.add("Selecione o tema");
                    for (Theme theme : temasList) {
                        themeNames.add(theme.getName());
                    }

                    ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(
                            editUserActivity.this,
                            android.R.layout.simple_spinner_item,
                            themeNames
                    );
                    themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTema.setAdapter(themeAdapter);

                    if (currentUser != null) {
                        selectCurrentTheme();
                    }
                } else {
                    Toast.makeText(editUserActivity.this,
                            "Erro ao carregar temas",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Theme>> call, Throwable t) {
                Toast.makeText(editUserActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        apiService.getUserById(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    populateFields();
                } else {
                    Toast.makeText(editUserActivity.this,
                            "Erro ao carregar dados do usuário",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(editUserActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentUser == null) return;

        editNome.setText(currentUser.getFullName());

        String birthDate = currentUser.getBirthDate();
        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = outputFormat.format(inputFormat.parse(birthDate));
                editDataNascimento.setText(formattedDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String condition = currentUser.getCondition();
        if (condition != null) {
            if (condition.equals("TEA")) {
                spinnerCondicao.setSelection(1);
            } else if (condition.equals("TDAH")) {
                spinnerCondicao.setSelection(2);
            }
        }

        selectCurrentTheme();

        loadAvatarFromFirebase();
    }

    private void selectCurrentTheme() {
        if (currentUser == null || currentUser.getThemeId() == null || temasList == null || temasList.isEmpty()) {
            return;
        }

        Long userThemeId = currentUser.getThemeId();
        for (int i = 0; i < temasList.size(); i++) {
            Long themeId = temasList.get(i).getId();
            if (userThemeId.equals(themeId)) {
                spinnerTema.setSelection(i + 1);
                break;
            }
        }
    }

    private void loadAvatarFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference avatarRef = storage.getReference()
                .child("avatars")
                .child("child_" + userId + ".jpg");

        avatarRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(editUserActivity.this)
                            .load(uri.toString())
                            .placeholder(R.drawable.avataradd)
                            .error(R.drawable.avataradd)
                            .into(imgUser);
                })
                .addOnFailureListener(e -> {
                    imgUser.setImageResource(R.drawable.avataradd);
                });
    }

    private void updateChild() {
        String nome = editNome.getText().toString().trim();
        String dataNascimento = editDataNascimento.getText().toString().trim();
        int condicaoPos = spinnerCondicao.getSelectedItemPosition();
        int temaPos = spinnerTema.getSelectedItemPosition();

        if (nome.isEmpty()) {
            editNome.setError("Nome é obrigatório");
            editNome.requestFocus();
            return;
        }

        if (nome.length() > 60) {
            editNome.setError("Nome deve ter no máximo 60 caracteres");
            editNome.requestFocus();
            return;
        }

        if (dataNascimento.isEmpty()) {
            Toast.makeText(this, "Data de nascimento é obrigatória", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer age = calculateAge(dataNascimento);
        if (age == null) {
            Toast.makeText(this, "Data de nascimento inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (age < 5 || age > 18) {
            Toast.makeText(this, "A criança deve ter entre 5 e 18 anos de idade.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (condicaoPos == 0) {
            Toast.makeText(this, "Selecione uma condição", Toast.LENGTH_SHORT).show();
            return;
        }

        if (temaPos == 0) {
            Toast.makeText(this, "Selecione um tema", Toast.LENGTH_SHORT).show();
            return;
        }

        String condicao = spinnerCondicao.getSelectedItem().toString();
        Long themeId = temasList.get(temaPos - 1).getId();

        String birthDateFormatted = convertDateFormat(dataNascimento);

        UpdateChildRequest request = new UpdateChildRequest(
                nome,
                birthDateFormatted,
                condicao,
                null,
                themeId
        );

        showLoading(true);

        apiService.updateUser(userId, request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(editUserActivity.this,
                            "Dados atualizados com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorBody = null;
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}

                    String message = "Erro ao atualizar dados";
                    if (response.code() == 400 && errorBody != null && !errorBody.isEmpty()) {
                        message = "Dados inválidos: " + errorBody;
                    }

                    Toast.makeText(editUserActivity.this,
                            message,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showLoading(false);
                Toast.makeText(editUserActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String convertDateFormat(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    private Integer calculateAge(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            java.util.Date dob = inputFormat.parse(date);
            if (dob == null) return null;

            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(dob);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showLoading(boolean show) {
        buttonSalvar.setEnabled(!show);
        buttonSalvar.setText(show ? "Salvando..." : "Salvar");
        buttonCancelar.setEnabled(!show);
        editNome.setEnabled(!show);
        editDataNascimento.setEnabled(!show);
        spinnerCondicao.setEnabled(!show);
        spinnerTema.setEnabled(!show);
    }
}

