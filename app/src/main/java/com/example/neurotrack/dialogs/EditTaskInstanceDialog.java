package com.example.neurotrack.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.neurotrack.R;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.CreateTaskInstanceRequest;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskInstanceDialog extends Dialog {

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(TaskInstance updated);
    }

    private final TaskInstance task;
    private final OnTaskUpdatedListener listener;
    private final ApiService apiService;
    private final SessionManager sessionManager;

    private TextView tvTitle;
    private EditText editDate;
    private EditText editTime;
    private Button btnCancel;
    private Button btnSave;

    private Calendar selectedDate;
    private Calendar selectedTime;

    public EditTaskInstanceDialog(@NonNull Context context,
                                  TaskInstance task,
                                  OnTaskUpdatedListener listener) {
        super(context);
        this.task = task;
        this.listener = listener;
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(context);
        this.selectedDate = Calendar.getInstance();
        this.selectedTime = Calendar.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_task_instance);

        initViews();
        populateFields();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTaskTitle);
        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void populateFields() {
        tvTitle.setText(task.getTaskName() != null ? task.getTaskName() : "Tarefa");

        try {
            if (task.getScheduledFor() != null) {
                SimpleDateFormat apiDateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = apiDateFmt.parse(task.getScheduledFor());
                if (d != null) {
                    selectedDate.setTime(d);
                    SimpleDateFormat display = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    editDate.setText(display.format(d));
                }
            }
        } catch (ParseException ignored) {}

        try {
            if (task.getPlannedTime() != null) {
                SimpleDateFormat apiTimeFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date t = apiTimeFmt.parse(task.getPlannedTime());
                if (t != null) {
                    selectedTime.setTime(t);
                    SimpleDateFormat displayT = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    editTime.setText(displayT.format(t));
                }
            }
        } catch (ParseException ignored) {}
    }

    private void setupListeners() {
        editDate.setOnClickListener(v -> showDatePicker());
        editTime.setOnClickListener(v -> showTimePicker());

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    editDate.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    editTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void saveChanges() {
        if (editDate.getText().toString().isEmpty() || editTime.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Selecione data e horário", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar scheduledDateTime = Calendar.getInstance();
        scheduledDateTime.setTime(selectedDate.getTime());
        scheduledDateTime.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
        scheduledDateTime.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
        scheduledDateTime.set(Calendar.SECOND, 0);
        scheduledDateTime.set(Calendar.MILLISECOND, 0);

        if (scheduledDateTime.before(Calendar.getInstance())) {
            Toast.makeText(getContext(), "A data e o horário da tarefa não podem ser no passado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager != null ? sessionManager.getToken() : null;
        if (token == null) {
            Toast.makeText(getContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat apiDateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat apiTimeFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String newDate = apiDateFmt.format(selectedDate.getTime());
        String newTime = apiTimeFmt.format(selectedTime.getTime());

        CreateTaskInstanceRequest request = new CreateTaskInstanceRequest();
        request.setScheduledFor(newDate);
        request.setPlannedTime(newTime);

        btnSave.setEnabled(false);
        btnSave.setText("Salvando...");

        apiService.updateTaskInstance(task.getTaskInstanceId(), request, token)
                .enqueue(new Callback<TaskInstance>() {
                    @Override
                    public void onResponse(Call<TaskInstance> call, Response<TaskInstance> response) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Salvar");

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(getContext(), "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onTaskUpdated(response.body());
                            }
                            dismiss();
                        } else {
                            String msg = "Erro ao atualizar tarefa";
                            try {
                                if (response.errorBody() != null) {
                                    String body = response.errorBody().string();
                                    if (body.contains("intervalo de 10 minutos")) {
                                        msg = "Já existe uma tarefa para esta criança em até 10 minutos desse horário.";
                                    }
                                }
                            } catch (Exception ignored) {}

                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TaskInstance> call, Throwable t) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Salvar");
                        Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

