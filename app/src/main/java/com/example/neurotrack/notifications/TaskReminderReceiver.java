package com.example.neurotrack.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.neurotrack.MainChildActivity;
import com.example.neurotrack.R;


public class TaskReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "task_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("TASK_ID", -1L);
        long childId = intent.getLongExtra("CHILD_ID", -1L);
        String taskName = intent.getStringExtra("TASK_NAME");
        String taskTime = intent.getStringExtra("TASK_TIME");

        createNotificationChannelIfNeeded(context);

        Intent openIntent = new Intent(context, MainChildActivity.class);
        openIntent.putExtra("CHILD_ID", childId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = (int) (taskId != -1L ? taskId : System.currentTimeMillis());

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                openIntent,
                flags
        );

        String title = context.getString(R.string.app_name) + " - Tarefa";
        String content = taskName != null ? taskName : "Você tem uma tarefa agendada";
        if (taskTime != null && !taskTime.isEmpty()) {
            content += " daqui a 10 minutos (" + taskTime + ")";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, builder.build());
    }

    private void createNotificationChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel existing = manager.getNotificationChannel(CHANNEL_ID);
            if (existing != null) {
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lembretes de tarefas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações quando chega o horário das tarefas da criança.");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            manager.createNotificationChannel(channel);
        }
    }
}

