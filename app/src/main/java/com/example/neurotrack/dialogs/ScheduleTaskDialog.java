package com.example.neurotrack.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.neurotrack.R;
import com.example.neurotrack.api.ApiService;
import com.example.neurotrack.api.RetrofitClient;
import com.example.neurotrack.models.CreateTaskInstanceRequest;
import com.example.neurotrack.models.TaskInstance;
import com.example.neurotrack.models.TaskTemplate;
import com.example.neurotrack.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleTaskDialog extends Dialog {

    private TaskTemplate taskTemplate;
    private Long childId;
    private OnTaskScheduledListener listener;
    private ApiService apiService;
    private SessionManager sessionManager;

    private TextView taskName;
    private EditText editTime;
    private EditText editStartDate;
    private Spinner spinnerRecurrence;
    private Button btnCancel;
    private Button btnSave;

    private Calendar selectedTime;
    private Calendar selectedDate;

    public interface OnTaskScheduledListener {
        void onTaskScheduled();
    }

    public ScheduleTaskDialog(@NonNull Context context, TaskTemplate taskTemplate, Long childId, OnTaskScheduledListener listener) {
        super(context);
        this.taskTemplate = taskTemplate;
        this.childId = childId;
        this.listener = listener;
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(context);
        this.selectedTime = Calendar.getInstance();
        this.selectedDate = Calendar.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_schedule_task);

        initViews();
        setupViews();
        setupListeners();
    }

    private void initViews() {
        taskName = findViewById(R.id.task_name);
        editTime = findViewById(R.id.edit_time);
        editStartDate = findViewById(R.id.edit_start_date);
        spinnerRecurrence = findViewById(R.id.spinner_recurrence);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupViews() {
        taskName.setText(taskTemplate.getIconCode() + " " + taskTemplate.getTitle());

        String[] recurrenceOptions = {
                "Uma vez (ONCE)",
                "Diariamente (DAILY)",
                "Personalizado (CUSTOM)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                recurrenceOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecurrence.setAdapter(adapter);
    }

    private void setupListeners() {
        editTime.setOnClickListener(v -> showTimePicker());

        editStartDate.setOnClickListener(v -> showDatePicker());

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveTask());
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

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    editStartDate.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void saveTask() {
        if (editTime.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Selecione um horário", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editStartDate.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Selecione uma data", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar scheduledDateTime = Calendar.getInstance();
        scheduledDateTime.setTime(selectedDate.getTime());
        scheduledDateTime.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
        scheduledDateTime.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
        scheduledDateTime.set(Calendar.SECOND, 0);
        scheduledDateTime.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();
        if (scheduledDateTime.before(now)) {
            Toast.makeText(getContext(), "A data e o horário da tarefa não podem ser no passado.", Toast.LENGTH_SHORT).show();
            return;
        }

        int recurrencePos = spinnerRecurrence.getSelectedItemPosition();

        btnSave.setEnabled(false);
        btnSave.setText("Salvando...");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String scheduledDate = dateFormat.format(selectedDate.getTime());
        String plannedTime = timeFormat.format(selectedTime.getTime());

        String recurrenceType;
        switch (recurrencePos) {
            case 0:
                recurrenceType = "ONCE";
                break;
            case 1:
                recurrenceType = "DAILY";
                break;
            case 2:
                recurrenceType = "CUSTOM";
                break;
            default:
                recurrenceType = "ONCE";
        }

        CreateTaskInstanceRequest request = new CreateTaskInstanceRequest(
                taskTemplate.getId(),
                childId,
                scheduledDate,
                plannedTime
        );
        request.setPointsAwarded(taskTemplate.getPoints());
        request.setRecurrenceType(recurrenceType);

        createTaskWithRecurrence(request);
    }

    private void createTaskWithRecurrence(CreateTaskInstanceRequest request) {
        String token = sessionManager != null ? sessionManager.getToken() : null;
        if (token == null) {
            Toast.makeText(getContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            btnSave.setText("Salvar");
            return;
        }

        apiService.createTaskInstance(request, token).enqueue(new Callback<TaskInstance>() {
            @Override
            public void onResponse(Call<TaskInstance> call, Response<TaskInstance> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Salvar");

                if (response.isSuccessful()) {
                    String message;
                    if ("ONCE".equals(request.getRecurrenceType())) {
                        message = "Tarefa agendada com sucesso!";
                    } else if ("DAILY".equals(request.getRecurrenceType())) {
                        message = "Tarefa diária criada com sucesso!";
                    } else {
                        message = "Tarefa(s) agendada(s) com sucesso!";
                    }

                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onTaskScheduled();
                    }
                    dismiss();
                } else {
                    String errorBody = null;
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}

                    String message = "Erro ao agendar tarefa";
                    if (response.code() == 400 && errorBody != null &&
                            errorBody.contains("intervalo de 10 minutos")) {
                        message = "Já existe uma tarefa para esta criança em até 10 minutos desse horário.";
                    }

                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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

